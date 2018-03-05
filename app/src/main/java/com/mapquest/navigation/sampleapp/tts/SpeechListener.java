package com.mapquest.navigation.sampleapp.tts;

public interface SpeechListener {
    void onUtteranceStarted();
    void onUtteranceDone();
    void onUtteranceStopped();
}
