package com.mapquest.navigation.sampleapp.tts;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapquest.navigation.NavigationManager;
import com.mapquest.navigation.internal.util.ArgumentValidator;

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

    public TextToSpeechPromptListenerManager(@NonNull Context context,
                                             @NonNull Lifecycle lifecycle,
                                             @Nullable String languageTag ) {
        this(lifecycle, new TextToSpeechManager(context.getApplicationContext(), GOOGLE_TTS_ENGINE_NAME), languageTag);
    }

    /*package*/ TextToSpeechPromptListenerManager(
            @NonNull Lifecycle lifecycle,
            @NonNull TextToSpeechManager textToSpeechManager,
            @Nullable String languageTag) {
        this(lifecycle, textToSpeechManager, new TextToSpeechPromptListener(textToSpeechManager), languageTag);
    }

    /*package*/ TextToSpeechPromptListenerManager(@NonNull Lifecycle lifecycle,
                                              @NonNull TextToSpeechManager textToSpeechManager,
                                              @NonNull TextToSpeechPromptListener textToSpeechPromptListener,
                                              @Nullable String languageTag) {
        mLifecycle = lifecycle;
        mTextToSpeechManager = textToSpeechManager;
        mTextToSpeechManager.initialize(languageTag);
        mPromptListener = textToSpeechPromptListener;
        mLifecycle.addObserver(this);
    }

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
        mPromptListener.setPromptSpeechListener(null);
    }
}
