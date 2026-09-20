package com.example.TriageIQ.Exception;

public class InvalidAiResponseException extends RuntimeException {
    public InvalidAiResponseException(String message) {
        super(message);
    }

    public InvalidAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
