package com.example.converter.gateway.domain;

public class InvalidXmlException extends RuntimeException {
    public InvalidXmlException(String message) {
        super(message);
    }

    public InvalidXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
