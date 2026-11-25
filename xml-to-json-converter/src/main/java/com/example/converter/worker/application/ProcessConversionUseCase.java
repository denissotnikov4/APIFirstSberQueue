package com.example.converter.worker.application;

import com.example.converter.worker.domain.ConversionCompleted;
import com.example.converter.worker.domain.ConversionQueuePublisher;
import com.example.converter.worker.domain.XmlToJsonConverter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessConversionUseCase {
    private final XmlToJsonConverter converter;
    private final ConversionQueuePublisher publisher;
    private final ProcessingProperties properties;

    public ProcessConversionUseCase(XmlToJsonConverter converter, ConversionQueuePublisher publisher,
                                    ProcessingProperties properties) {
        this.converter = converter;
        this.publisher = publisher;
        this.properties = properties;
    }

    public void process(UUID id, String xml) throws InterruptedException {
        Thread.sleep(properties.processingDelayMs());
        var json = converter.convert(xml);
        publisher.publish(new ConversionCompleted(id, json, Instant.now()));
    }
}
