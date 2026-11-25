package com.example.converter.gateway.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface ConversionRepository {
    void save(ConversionRequest request);

    Optional<ConversionRequest> findById(UUID id);

    Optional<ConversionRequest> updateIfPresent(UUID id, UnaryOperator<ConversionRequest> update);
}
