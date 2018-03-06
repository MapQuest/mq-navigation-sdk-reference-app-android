package com.mapquest.navigation.sampleapp.tts;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapquest.navigation.internal.util.ArgumentValidator;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.internal.util.SystemVersionUtil;
import com.mapquest.navigation.sampleapp.tts.audio.AudioFocusLossListener;

import java.util.HashMap;
import java.util.Locale;

import static com.mapquest.navigation.internal.collection.CollectionsUtil.asStringMap;
import static com.mapquest.navigation.sampleapp.tts.audio.AudioManagerUtil.getAudioManager;
import static com.mapquest.navigation.sampleapp.tts.audio.AudioManagerUtil.wasFocusGranted;

/**
 * Used to speak text. Proxies calls to Android's underlying TTS implementation, handling additional
 * concerns including initialization, audio volume and ducking.
 */
public class TextToSpeechManager {

    /* Stream used to speak messages. */
    private static final int AUDIO_MANAGER_STREAM = AudioManager.STREAM_MUSIC;

    private final String mPreferredTtsEngineName;

    private float mVolume = 0.85f;
    private boolean mMuted;

    private Context mContext;
    private TextToSpeech mTextToSpeech;

    private int mTextQueueLength;
    private boolean mHasFocus;
    private static long sUtteranceCount; // Used for generating unique utterance IDs

    private Bundle mSpeechParameters;
    private DelegatingMultiUtteranceProgressListener mUtteranceListenerManager = new DelegatingMultiUtteranceProgressListener();
    private UtteranceCompletionProgressListener mUtteranceCompletionProgressListener = new UtteranceCompletionProgressListener();

    private FocusLossListener mFocusLossListener = new FocusLossListener();

    private static final String TAG = LogUtil.generateLoggingTag(TextToSpeechManager.class);

    public TextToSpeechManager(Context context, String preferredTtsEngineName) {
        mContext = context.getApplicationContext();

        mSpeechParameters = new Bundle();
        mSpeechParameters.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AUDIO_MANAGER_STREAM);
        mSpeechParameters.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mVolume);

        mPreferredTtsEngineName = preferredTtsEngineName;
    }

    public synchronized void initialize(@Nullable final String languageTag) {
        if(mTextToSpeech != null) {
            Log.w(TAG, "Ignoring request to initialize when already initialized.");
            return;
        }

        // Since TextToSpeech doesn't have methods to check whether an instance is ready to use,
        // only set mTextToSpeech to the TTS created below after it has been initialized. If
        // mTextToSpeech is set, the instance it's set to is initialized.
        TextToSpeechInitializationListener initializationListener = new TextToSpeechInitializationListener() {
            @Override
            protected void onInit(int status, TextToSpeech textToSpeech) {
                if (status == TextToSpeech.SUCCESS) {
                    mTextToSpeech = textToSpeech;
                    if (getLocaleFromLanguageTag(languageTag) != null) {
                        Log.w(TAG, "TTS initialization, set language to " + languageTag);
                        mTextToSpeech.setLanguage(getLocaleFromLanguageTag(languageTag));
                    } else {
                        Log.w(TAG, "TTS initialization");
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            }
        };

        TextToSpeech textToSpeech = mPreferredTtsEngineName == null ?
                new TextToSpeech(mContext, initializationListener) :
                new TextToSpeech(mContext, initializationListener, mPreferredTtsEngineName);
        textToSpeech.setOnUtteranceProgressListener(mUtteranceListenerManager);
        mUtteranceListenerManager.addListener(new FocusReleasingUtteranceProgressListener());
        mUtteranceListenerManager.addListener(mUtteranceCompletionProgressListener);

        initializationListener.setTextToSpeech(textToSpeech);
    }

    void deinitializeImmediately() {
        if(mTextToSpeech == null) {
            Log.w(TAG, "Ignoring request to deinitialize when uninitialized.");
            return;
        }

        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
        mTextToSpeech = null;
    }

    synchronized void speakWithFocus(String text, SpeechListener speechListener) {
        if(mTextToSpeech == null) {
            Log.i(TAG, "Ignoring request to speak while uninitialized.");
            return;
        }

        if(!mHasFocus) {
            requestAudioFocus();
        }
        speak(text, speechListener);
    }

    private void speak(String text, SpeechListener speechListener) {
        text = text.trim();
        mTextQueueLength++;

        String utteranceId = generateUtteranceId();
        mUtteranceCompletionProgressListener.addListener(utteranceId, speechListener);

        if(SystemVersionUtil.hasMinimumSystemApiLevel(21)) {
            speakTTS_API21(mTextToSpeech, text, mSpeechParameters, utteranceId);
        } else {
            speakTTS_APIBelow21(mTextToSpeech, text, mSpeechParameters, utteranceId);
        }
    }

    @SuppressWarnings("deprecation")
    private void speakTTS_APIBelow21(TextToSpeech tts, String text, Bundle speechParams, String utteranceId) {
        HashMap<String, String> parameterMap = asStringMap(speechParams);
        parameterMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, parameterMap);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakTTS_API21(TextToSpeech tts, String text, Bundle speechParams, String utteranceId) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, speechParams, utteranceId);
    }

    private void requestAudioFocus() {
        int requestOutcome = getAudioManager(mContext).requestAudioFocus(
                mFocusLossListener,
                AUDIO_MANAGER_STREAM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        mHasFocus = wasFocusGranted(requestOutcome);
    }

    private void releaseAudioFocus() {
        getAudioManager(mContext).abandonAudioFocus(mFocusLossListener);
        mHasFocus = false;
    }

    public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
        ArgumentValidator.assertInRange("Volume must be between 0 (silent) and 1 (full volume.)", volume, 0.0, 1.0);

        mVolume = volume;
        mSpeechParameters.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,
                mMuted ? 0 : volume);
    }

    public float getVolume() {
        return mVolume;
    }

    private void mute() {
        mMuted = true;
        mSpeechParameters.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0);

        stopTts();
    }

    private void unmute() {
        mMuted = false;
        mSpeechParameters.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mVolume);
    }

    public void setMuted(boolean muted) {
        if(muted) {
            mute();
        } else {
            unmute();
        }
    }

    public boolean isMuted() {
        return mMuted;
    }

    private void stopTts() {
        if(mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
        mTextQueueLength = 0;
    }

    private String generateUtteranceId() {
        return mContext.getPackageName() + "/utterance/" + sUtteranceCount++;
    }

    /** Audio focus listener that stops speaking if we lose focus. */
    private class FocusLossListener extends AudioFocusLossListener {
        @Override
        protected void onAudioFocusLost(int focusChange) {
            mHasFocus = false;

            if (mTextToSpeech != null && mTextToSpeech.isSpeaking()) {
                stopTts();
            }
        }
    }

    private Locale getLocaleFromLanguageTag(String languageTag) {
        if (languageTag != null && !languageTag.isEmpty()) {
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale : locales) {
                if (languageTag.equals(locale.toString())) {
                    return locale;
                }
            }
        }
        return null;
    }

    private class FocusReleasingUtteranceProgressListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) { }

        @Override
        public void onDone(String utteranceId) {
            handleUtteranceDequeue();
        }

        @Override
        public void onError(String utteranceId) {
            handleUtteranceDequeue();
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            handleUtteranceDequeue();
        }

        private void handleUtteranceDequeue() {
            mTextQueueLength--;

            if(mTextQueueLength < 1) {
                releaseAudioFocus();
            }
        }
    }
}
