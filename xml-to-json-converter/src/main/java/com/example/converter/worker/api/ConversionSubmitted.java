package com.example.converter.worker.api;

import java.time.Instant;
import java.util.UUID;

public record ConversionSubmitted(UUID id, String xmlPayload, Instant submittedAt) {
}
