package com.example.converter.worker.infrastructure;

import com.rabbitmq.client.Channel;
import com.example.converter.worker.api.ConversionSubmitted;
import com.example.converter.worker.application.ProcessConversionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class RabbitConversionListener {
    private static final Logger log = LoggerFactory.getLogger(RabbitConversionListener.class);
    private final ProcessConversionUseCase conversions;

    RabbitConversionListener(ProcessConversionUseCase conversions) {
        this.conversions = conversions;
    }

    @RabbitListener(queues = "${converter.rabbit.requests-queue}")
    public void receive(ConversionSubmitted event, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        @Header(name = AmqpHeaders.REDELIVERED, defaultValue = "false") boolean redelivered)
            throws IOException {
        log.info("received conversion {} (redelivered={})", event.id(), redelivered);
        try {
            conversions.process(event.id(), event.xmlPayload());
        } catch (InterruptedException | RuntimeException exception) {
            log.error("Could not process conversion {}", event.id(), exception);
            try {
                channel.basicNack(deliveryTag, false, true);
            } finally {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
            return;
        }
        channel.basicAck(deliveryTag, false);
    }
}
