package com.example.converter.gateway.application;

import com.example.converter.gateway.domain.ConversionCompleted;
import com.example.converter.gateway.domain.ConversionRepository;
import com.example.converter.gateway.domain.ConversionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompleteConversionUseCase {
    private static final Logger log = LoggerFactory.getLogger(CompleteConversionUseCase.class);
    private final ConversionRepository repository;

    public CompleteConversionUseCase(ConversionRepository repository) {
        this.repository = repository;
    }

    public void complete(ConversionCompleted event) {
        var updated = repository.updateIfPresent(event.id(), current -> {
            if (current.status() == ConversionStatus.COMPLETED) {
                log.warn("Ignoring duplicate completion for conversion {}", event.id());
                return current;
            }
            return current.complete(event);
        });
        if (updated.isEmpty()) {
            log.warn("Ignoring completion for unknown conversion {}", event.id());
        }
    }
}
