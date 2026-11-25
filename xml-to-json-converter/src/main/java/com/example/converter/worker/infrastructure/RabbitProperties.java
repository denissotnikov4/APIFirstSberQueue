package com.example.converter.worker.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("converter.rabbit")
public record RabbitProperties(
        String exchange,
        String requestsQueue,
        String resultsQueue,
        String requestedKey,
        String completedKey,
        Duration publishConfirmTimeout
) {
}
