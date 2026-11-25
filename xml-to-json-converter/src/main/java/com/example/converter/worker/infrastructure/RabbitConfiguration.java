package com.example.converter.worker.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class RabbitConfiguration {
    @Bean
    Declarables conversionTopology(RabbitProperties properties) {
        var requests = QueueBuilder.durable(properties.requestsQueue()).build();
        var results = QueueBuilder.durable(properties.resultsQueue()).build();
        var exchange = new DirectExchange(properties.exchange());
        return new Declarables(exchange, requests, results,
                BindingBuilder.bind(requests).to(exchange).with(properties.requestedKey()),
                BindingBuilder.bind(results).to(exchange).with(properties.completedKey()));
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper, "com.example.converter.worker.domain");
    }
}
