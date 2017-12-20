package com.mapquest.navigation.sampleapp.tts;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapquest.navigation.NavigationManager;
import com.mapquest.navigation.internal.util.ArgumentValidator;

/**
 * Manages the TTS PromptListener implementation and handles the Android lifecycle appropriately.
 */
public class TextToSpeechPromptListenerManager implements LifecycleObserver {
    private static final String GOOGLE_TTS_ENGINE_NAME = "com.google.android.tts";

    @NonNull
    private TextToSpeechManager mTextToSpeechManager;
    @NonNull
    private final Lifecycle mLifecycle;
    @NonNull
    private final TextToSpeechPromptListener mPromptListener;
    @Nullable
    private NavigationManager mNavigationManager;

    public TextToSpeechPromptListenerManager(@NonNull Context context, @NonNull Lifecycle lifecycle) {
        this(lifecycle, new TextToSpeechManager(context.getApplicationContext(), GOOGLE_TTS_ENGINE_NAME));
    }

    /*package*/ TextToSpeechPromptListenerManager(@NonNull Lifecycle lifecycle,
            @NonNull TextToSpeechManager textToSpeechManager) {
        this(lifecycle, textToSpeechManager, new TextToSpeechPromptListener(textToSpeechManager));
    }

    /*package*/ TextToSpeechPromptListenerManager(@NonNull Lifecycle lifecycle,
            @NonNull TextToSpeechManager textToSpeechManager,
            @NonNull TextToSpeechPromptListener textToSpeechPromptListener) {
        mLifecycle = lifecycle;
        mTextToSpeechManager = textToSpeechManager;
        mTextToSpeechManager.initialize();
        mPromptListener = textToSpeechPromptListener;
        mLifecycle.addObserver(this);
    }

    /**
     * Sets up the prompt listener on the navigation manager if its at the correct state in the lifecycle.
     */
    public void initialize(@NonNull NavigationManager navigationManager) {
        ArgumentValidator.assertNotNull(navigationManager);

        mNavigationManager = navigationManager;

        if (mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            mNavigationManager.addPromptListener(mPromptListener);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    /*package*/ void destroy() {
        if (mNavigationManager != null) {
            mNavigationManager.removePromptListener(mPromptListener);
            mNavigationManager = null;
        }
        mTextToSpeechManager.deinitializeImmediately();
    }
}
