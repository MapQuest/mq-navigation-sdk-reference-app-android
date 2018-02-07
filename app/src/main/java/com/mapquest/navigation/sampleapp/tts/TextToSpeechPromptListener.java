package com.mapquest.navigation.sampleapp.tts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapquest.navigation.listener.DefaultSpeechListener;
import com.mapquest.navigation.listener.PromptListener;
import com.mapquest.navigation.listener.PromptSpeechListener;
import com.mapquest.navigation.model.Prompt;

public class TextToSpeechPromptListener implements PromptListener {
    @NonNull
    private final TextToSpeechManager mTextToSpeechManager;

    TextToSpeechPromptListener(@NonNull TextToSpeechManager textToSpeechManager) {
        mTextToSpeechManager = textToSpeechManager;
    }

    @Override
    public void onPromptReceived(final Prompt promptToSpeak, boolean userInitiated) {
        mTextToSpeechManager.speakWithFocus(promptToSpeak.getSpeech(), new DefaultSpeechListener());
    }

    @Override
    public void cancelPrompts() {
        mTextToSpeechManager.deinitializeImmediately();
    }
}
