package com.example.converter.gateway.domain;

public interface ConversionQueuePublisher {
    void publish(ConversionSubmitted event);
}
