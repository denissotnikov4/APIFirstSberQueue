package com.example.converter.gateway.api;

import com.example.converter.gateway.domain.ConversionStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversionStatusResponse(
        UUID id, ConversionStatus status, Instant submittedAt, Instant completedAt, String jsonResult
) {
}
