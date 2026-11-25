package com.example.converter.worker.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("converter")
public record ProcessingProperties(long processingDelayMs) {
}
