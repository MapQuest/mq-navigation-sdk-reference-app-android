package com.mapquest.navigation.sampleapp.tts;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.NonNull;

/**
 * Addresses a shortcoming in the design of Android's TTS API. Because TextToSpeech takes an
 * initialization listener as a constructor parameter but the new instance isn't provided as a
 * parameter to {@link OnInitListener#onInit(int)}, getting that reference can be a little
 * convoluted. This class gives users a place to store the TTS instance after it's created and
 * overloads {@code onInit()} to provide that TTS as a parameter.
 * <p>
 * Because of the way that services are bound to, it shouldn't be possible for a TTS to be
 * initialized prior to setting it on the listener.
 */
public abstract class TextToSpeechInitializationListener implements OnInitListener {
    private TextToSpeech mTextToSpeech;

    /**
     * Sets the TTS instance being initialized during its construction. This value will be provided
     * to {@link #onInit(int, TextToSpeech)}.
     */
    public void setTextToSpeech(@NonNull TextToSpeech textToSpeech) {
        mTextToSpeech = textToSpeech;
    }

    @Override
    public void onInit(int status) {
        onInit(status, mTextToSpeech);
    }

    /**
     * Overridden instead of {@link #onInit(int)} in order to gain access to the TextToSpeech that
     * was initialized.
     */
    protected abstract void onInit(int status, TextToSpeech textToSpeech);
}
