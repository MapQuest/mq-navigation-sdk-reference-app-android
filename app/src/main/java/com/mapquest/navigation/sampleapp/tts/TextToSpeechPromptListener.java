package com.mapquest.navigation.sampleapp.tts;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapquest.navigation.listener.PromptListener;
import com.mapquest.navigation.listener.PromptSpeechListener;
import com.mapquest.navigation.model.Prompt;

public class TextToSpeechPromptListener implements PromptListener {
    @NonNull
    private final TextToSpeechManager mTextToSpeechManager;
    @Nullable
    private PromptSpeechListener mPromptSpeechListener;

    TextToSpeechPromptListener(@NonNull TextToSpeechManager textToSpeechManager) {
        mTextToSpeechManager = textToSpeechManager;
    }

    void setPromptSpeechListener(@Nullable PromptSpeechListener promptSpeechListener) {
        mPromptSpeechListener = promptSpeechListener;
    }

    @Override
    public void onPromptReceived(@NonNull final Prompt promptToSpeak, boolean userInitiated) {
        if (mPromptSpeechListener != null) {
            mPromptSpeechListener.onPromptSpeechBegun(promptToSpeak);
        }

        mTextToSpeechManager.speakWithFocus(promptToSpeak.getSpeech(), new DefaultSpeechListener() {
            @Override
            public void onUtteranceDone() {
                if (mPromptSpeechListener != null) {
                    mPromptSpeechListener.onPromptSpeechCompleted(promptToSpeak, false);
                }
            }

            @Override
            public void onUtteranceStopped() {
                if (mPromptSpeechListener != null) {
                    mPromptSpeechListener.onPromptSpeechCompleted(promptToSpeak, true);
                }
            }
        });
    }

    @Override
    public void onShouldCancelPrompts() {}
}
