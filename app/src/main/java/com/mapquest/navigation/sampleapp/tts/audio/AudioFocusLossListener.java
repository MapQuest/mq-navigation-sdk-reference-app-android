package com.mapquest.navigation.sampleapp.tts.audio;

import android.media.AudioManager.OnAudioFocusChangeListener;

import java.util.Set;

import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static com.mapquest.navigation.internal.collection.CollectionsUtil.asSet;

/**
 * Audio focus change listener with callbacks specifically for when focus is lost.
 */
public abstract class AudioFocusLossListener implements OnAudioFocusChangeListener {
    private static final Set<Integer> FOCUS_LOSS_CHANGES = asSet(
            AUDIOFOCUS_LOSS,
            AUDIOFOCUS_LOSS_TRANSIENT,
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (FOCUS_LOSS_CHANGES.contains(focusChange)) {
            onAudioFocusLost(focusChange);
        }
    }

    protected abstract void onAudioFocusLost(int focusChange);
}
