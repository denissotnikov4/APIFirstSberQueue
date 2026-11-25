package com.example.converter.gateway.api;

import com.example.converter.gateway.domain.ConversionStatus;

import java.util.UUID;

public record SubmitConversionResponse(UUID id, ConversionStatus status) {
}
