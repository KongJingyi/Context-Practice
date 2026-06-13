package com.team13.context.trtc;

public class TrtcRecordingException extends RuntimeException {

    public TrtcRecordingException(String message) {
        super(message);
    }

    public TrtcRecordingException(String message, Throwable cause) {
        super(message, cause);
    }
}
