package com.example.converter.gateway.application;

import com.example.converter.gateway.domain.ConversionRepository;
import com.example.converter.gateway.domain.ConversionRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetConversionStatusUseCase {
    private final ConversionRepository repository;

    public GetConversionStatusUseCase(ConversionRepository repository) {
        this.repository = repository;
    }

    public Optional<ConversionRequest> find(UUID id) {
        return repository.findById(id);
    }
}
