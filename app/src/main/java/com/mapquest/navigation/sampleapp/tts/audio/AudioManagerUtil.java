package com.mapquest.navigation.sampleapp.tts.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

public final class AudioManagerUtil {
    private AudioManagerUtil() {
    }

    /**
     * @return Whether the outcome of an
     * {@link AudioManager#requestAudioFocus(OnAudioFocusChangeListener, int, int)} call was
     * success.
     */
    public static boolean wasFocusGranted(int requestOutcome) {
        return requestOutcome == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public static AudioManager getAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
}
