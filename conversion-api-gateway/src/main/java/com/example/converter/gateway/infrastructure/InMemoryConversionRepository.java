package com.example.converter.gateway.infrastructure;

import com.example.converter.gateway.domain.ConversionRepository;
import com.example.converter.gateway.domain.ConversionRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Repository
class InMemoryConversionRepository implements ConversionRepository {
    private final ConcurrentHashMap<UUID, ConversionRequest> requests = new ConcurrentHashMap<>();

    @Override
    public void save(ConversionRequest request) {
        requests.put(request.id(), request);
    }

    @Override
    public Optional<ConversionRequest> findById(UUID id) {
        return Optional.ofNullable(requests.get(id));
    }

    @Override
    public Optional<ConversionRequest> updateIfPresent(UUID id, UnaryOperator<ConversionRequest> update) {
        return Optional.ofNullable(requests.computeIfPresent(id, (key, current) -> update.apply(current)));
    }
}
