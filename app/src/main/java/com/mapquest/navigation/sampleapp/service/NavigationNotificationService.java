package com.mapquest.navigation.sampleapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mapquest.navigation.NavigationManager;
import com.mapquest.navigation.internal.util.ArgumentValidator;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.RouteStoppedReason;
import com.mapquest.navigation.model.UserLocationTrackingConsentStatus;
import com.mapquest.navigation.sampleapp.BuildConfig;
import com.mapquest.navigation.sampleapp.MQNavigationSampleApplication;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.tts.TextToSpeechPromptListenerManager;

public class NavigationNotificationService extends Service implements LifecycleRegistryOwner {

    private static final String NAVIGATION_LANGUAGE_CODE_KEY = "navigation_language_code";
    private static final String USER_TRACKING_CONSENT_KEY = "user_tracking_consent";
    private static final String NOTIFICATION_CONTENT_INTENT_KEY = "notification_content_intent";

    private static final String TAG = LogUtil.generateLoggingTag(NavigationNotificationService.class);
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "nav-channel-id";
    private static final String NOTIFICATION_CHANNEL_NAME = "Navigation";

    private IBinder mBinder = new LocalBinder();
    private NavigationManager mNavigationManager;

    private String mLanguageCode;
    private boolean mUserTrackingConsentGranted = false;

    private PendingIntent mNotificationContentIntent;

    private TextToSpeechPromptListenerManager mTextToSpeechPromptListenerManager;
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    public static Intent buildNavigationNotificationServiceIntent(@NonNull Context context,
            @Nullable String languageCode, boolean userTrackingConsentGranted, @NonNull PendingIntent notificationContentIntent) {
        ArgumentValidator.assertNotNull(context, notificationContentIntent);

        Intent navigationNotificationServiceIntent = new Intent(context, NavigationNotificationService.class);

        navigationNotificationServiceIntent.putExtra(NAVIGATION_LANGUAGE_CODE_KEY, languageCode);
        navigationNotificationServiceIntent.putExtra(USER_TRACKING_CONSENT_KEY, userTrackingConsentGranted);
        navigationNotificationServiceIntent.putExtra(NOTIFICATION_CONTENT_INTENT_KEY, notificationContentIntent);

        return navigationNotificationServiceIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if (intent.getExtras() != null) {
            mLanguageCode = intent.getExtras().getString(NAVIGATION_LANGUAGE_CODE_KEY);
            mUserTrackingConsentGranted = intent.getExtras().getBoolean(USER_TRACKING_CONSENT_KEY);
            mNotificationContentIntent = (PendingIntent) intent.getExtras().get(NOTIFICATION_CONTENT_INTENT_KEY);
        }

        // instantiate the TTS manager (and engine) here using the provided language tag
        // NOTE: the TTS engine will get appropriately destroyed via a lifecycle-listener for ON_DESTROY, as registered below
        mTextToSpeechPromptListenerManager = new TextToSpeechPromptListenerManager(this, getLifecycle(), mLanguageCode);

        //
        // Note that START_STICKY seems to be preferred for Services that continue to run in the background
        // after they have handled requests. However, if the process is killed, we DON'T want the
        // service to be recreated automatically... without a way to actually restore navigation.
        // Therefore, we return START_NOT_STICKY here. :)
        //
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");

        // note: return true only if we want service's onRebind() method later called when new clients bind to it
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);

        mNavigationManager.deinitialize();
        mNavigationManager = null;

        clearNotification();
        super.onDestroy();
    }

    public NavigationManager getNavigationManager() {
        // create and initialize singleton NavigationManager instance (w/ location-adapter) as needed
        if(mNavigationManager == null) {
            LocationProviderAdapter locationProviderAdapter = ((MQNavigationSampleApplication) getApplication()).getLocationProviderAdapter();
            mNavigationManager = createNavigationManager(locationProviderAdapter);

            mNavigationManager.initialize();

            mTextToSpeechPromptListenerManager.initialize(mNavigationManager);
        }
        return mNavigationManager;
    }

    public void updateNotification(Notification notification) {
        startForeground(NOTIFICATION_ID, notification);
    }

    public void clearNotification() {
        stopForeground(true);
    }

    public static NavigationNotificationService fromBinder(IBinder binder) {
        if(binder instanceof LocalBinder) {
            return ((LocalBinder)binder).getService();
        } else {
            throw new IllegalArgumentException("Given IBinder is not an instance of the expected implementation type.");
        }
    }

    private NavigationManager createNavigationManager(LocationProviderAdapter locationProviderAdapter) {
        Log.d(TAG, "createNavigationManager() locationProviderAdapter: " + locationProviderAdapter);

        mNavigationManager = new NavigationManager.Builder(this, BuildConfig.API_KEY, locationProviderAdapter).build();

        mNavigationManager.addNavigationStateListener(new NotificationNavigationStateListener());

        UserLocationTrackingConsentStatus userLocationTrackingConsentStatus = mUserTrackingConsentGranted ?
                UserLocationTrackingConsentStatus.GRANTED : UserLocationTrackingConsentStatus.DENIED;
        mNavigationManager.setUserLocationTrackingConsentStatus(userLocationTrackingConsentStatus);

        return mNavigationManager;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }

    private class LocalBinder extends Binder {
        public NavigationNotificationService getService() {
            return NavigationNotificationService.this;
        }
    }

    private class NotificationNavigationStateListener implements NavigationStateListener {

        @Override
        public void onNavigationStarted() {
            updateNotification(buildNotification("Navigation in progress"));
        }

        @Override
        public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
            clearNotification();
        }

        @Override
        public void onNavigationPaused() {
            updateNotification(buildNotification("Navigation paused"));
        }

        @Override
        public void onNavigationResumed() {
            updateNotification(buildNotification("Navigation in progress"));
        }
    }


    private Notification buildNotification(String description) {
        return new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("Navigating")
                .setContentText(description)
                .setSmallIcon(R.drawable.circle)
                .setContentIntent(mNotificationContentIntent)
                .build();
    }
}
