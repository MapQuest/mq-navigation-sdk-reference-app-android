package com.mapquest.navigation.sampleapp.tts;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.UtteranceProgressListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utterance progress listener that delegates to any number of listeners, allowing multiple
 * listeners to be used with a {@link android.speech.tts.TextToSpeech}, which only allows users to
 * set a single listener.
 */
public class DelegatingMultiUtteranceProgressListener extends UtteranceProgressListener {
    private List<UtteranceProgressListener> mListeners = new CopyOnWriteArrayList<>();

    public void addListener(UtteranceProgressListener listener) {
        mListeners.add(listener);
    }

    public boolean removeListener(UtteranceProgressListener listener) {
        return mListeners.remove(listener);
    }

    @Override
    public void onStart(String utteranceId) {
        for (UtteranceProgressListener listener : mListeners) {
            listener.onStart(utteranceId);
        }
    }

    @Override
    public void onDone(String utteranceId) {
        for (UtteranceProgressListener listener : mListeners) {
            listener.onDone(utteranceId);
        }
    }

    @Override
    public void onError(String utteranceId) {
        for (UtteranceProgressListener listener : mListeners) {
            listener.onError(utteranceId);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        // NOTE: this gets called when an utterance gets *interrupted* by another utterance
        // (i.e. now that we're using QUEUE_FLUSH)
        //
        for (UtteranceProgressListener listener : mListeners) {
            listener.onStop(utteranceId, interrupted);
        }
    }
}
