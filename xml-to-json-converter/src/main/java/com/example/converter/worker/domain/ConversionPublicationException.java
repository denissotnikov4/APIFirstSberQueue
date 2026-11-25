package com.example.converter.worker.domain;

public class ConversionPublicationException extends RuntimeException {
    public ConversionPublicationException(String message) {
        super(message);
    }

    public ConversionPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
