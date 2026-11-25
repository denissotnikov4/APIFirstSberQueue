package com.example.converter.gateway.domain;

import java.time.Instant;
import java.util.UUID;

public record ConversionSubmitted(UUID id, String xmlPayload, Instant submittedAt) {
}
