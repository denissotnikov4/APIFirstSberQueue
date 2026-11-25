package com.example.converter.gateway.infrastructure;

import com.example.converter.gateway.application.CompleteConversionUseCase;
import com.example.converter.gateway.domain.ConversionCompleted;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class RabbitConversionListener {
    private final CompleteConversionUseCase conversions;

    RabbitConversionListener(CompleteConversionUseCase conversions) {
        this.conversions = conversions;
    }

    @RabbitListener(queues = "${converter.rabbit.results-queue}")
    public void receive(ConversionCompleted event) {
        conversions.complete(event);
    }
}
