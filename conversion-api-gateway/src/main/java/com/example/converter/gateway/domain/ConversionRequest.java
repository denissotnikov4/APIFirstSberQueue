package com.example.converter.gateway.domain;

import java.time.Instant;
import java.util.UUID;

public record ConversionRequest(
        UUID id, ConversionStatus status, Instant submittedAt, Instant completedAt, String jsonResult
) {
    public static ConversionRequest pending(UUID id, Instant submittedAt) {
        return new ConversionRequest(id, ConversionStatus.PENDING, submittedAt, null, null);
    }

    public ConversionRequest complete(ConversionCompleted event) {
        return new ConversionRequest(id, ConversionStatus.COMPLETED, submittedAt,
                event.completedAt(), event.jsonResult());
    }

    public ConversionRequest fail() {
        return new ConversionRequest(id, ConversionStatus.FAILED, submittedAt, null, null);
    }
}
