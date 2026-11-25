package com.example.converter.gateway.application;

import com.example.converter.gateway.domain.ConversionPublicationException;
import com.example.converter.gateway.domain.ConversionQueuePublisher;
import com.example.converter.gateway.domain.ConversionRepository;
import com.example.converter.gateway.domain.ConversionRequest;
import com.example.converter.gateway.domain.ConversionStatus;
import com.example.converter.gateway.domain.ConversionSubmitted;
import com.example.converter.gateway.domain.XmlValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SubmitConversionUseCase {
    private final ConversionRepository repository;
    private final ConversionQueuePublisher publisher;
    private final XmlValidator validator;

    public SubmitConversionUseCase(ConversionRepository repository, ConversionQueuePublisher publisher,
                                   XmlValidator validator) {
        this.repository = repository;
        this.publisher = publisher;
        this.validator = validator;
    }

    public ConversionRequest submit(String xml) {
        validator.validate(xml);
        var request = ConversionRequest.pending(UUID.randomUUID(), Instant.now());
        repository.save(request);
        try {
            publisher.publish(new ConversionSubmitted(request.id(), xml, request.submittedAt()));
        } catch (ConversionPublicationException exception) {
            repository.updateIfPresent(request.id(), current ->
                    current.status() == ConversionStatus.PENDING ? current.fail() : current);
            throw exception;
        }
        return request;
    }
}
