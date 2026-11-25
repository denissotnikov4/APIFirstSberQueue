package com.example.converter.worker.domain;

import java.time.Instant;
import java.util.UUID;

public record ConversionCompleted(UUID id, String jsonResult, Instant completedAt) {
}
