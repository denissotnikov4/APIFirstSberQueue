package com.example.converter.worker.domain;

public interface ConversionQueuePublisher {
    void publish(ConversionCompleted event);
}
