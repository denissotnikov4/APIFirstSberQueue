package com.example.converter.worker.infrastructure;

import com.example.converter.worker.domain.ConversionCompleted;
import com.example.converter.worker.domain.ConversionQueuePublisher;
import com.example.converter.worker.domain.ConversionPublicationException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
class RabbitConversionPublisher implements ConversionQueuePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties properties;

    RabbitConversionPublisher(RabbitTemplate rabbitTemplate, RabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(ConversionCompleted event) {
        var correlation = new CorrelationData();
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.completedKey(), event,
                    message -> {
                        message.getMessageProperties().getHeaders().remove(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME);
                        return message;
                    }, correlation);
            var confirmation = correlation.getFuture()
                    .get(properties.publishConfirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (correlation.getReturned() != null || !confirmation.isAck()) {
                throw new ConversionPublicationException("RabbitMQ did not accept conversion " + event.id());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConversionPublicationException("Publication interrupted for conversion " + event.id(), exception);
        } catch (AmqpException | ExecutionException | TimeoutException exception) {
            throw new ConversionPublicationException("Publication could not be confirmed for conversion " + event.id(), exception);
        }
    }
}
