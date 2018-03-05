package com.mapquest.navigation.sampleapp.tts;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Map;

/**
 * TextToSpeech allows users to listen for the completion of all utterances. This class is an
 * adapter of sorts that allows listeners to be associated with individual
 * {@link TextToSpeech#speak(CharSequence, int, Bundle, String)} calls.
 */
public class UtteranceCompletionProgressListener extends UtteranceProgressListener {
    private Map<String, SpeechListener> mListenersByUtteranceId = new HashMap<>();
    private TextToSpeech mTextToSpeech;

    public void addListener(String utteranceId, SpeechListener listener) {
        mListenersByUtteranceId.put(utteranceId, listener);
    }

    public void removeListener(String utteranceId) {
        mListenersByUtteranceId.remove(utteranceId);
    }

    @Override
    public void onStart(String utteranceId) {
        SpeechListener listener = mListenersByUtteranceId.get(utteranceId);
        if(listener != null) {
            listener.onUtteranceStarted();
        }
    }

    @Override
    public void onDone(String utteranceId) {
        SpeechListener listener = mListenersByUtteranceId.remove(utteranceId);
        if(listener != null) {
            listener.onUtteranceDone();
        }
    }

    @Override
    public void onError(String utteranceId) {
        // This isn't really interesting to us and it's deprecated anyway.
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        // NOTE: this gets called when an utterance gets *interrupted* by another utterance
        // (i.e. now that we're using QUEUE_FLUSH)
        //
        SpeechListener listener = mListenersByUtteranceId.remove(utteranceId);
        if((listener != null) && interrupted){
            listener.onUtteranceStopped();
        }
    }
}
