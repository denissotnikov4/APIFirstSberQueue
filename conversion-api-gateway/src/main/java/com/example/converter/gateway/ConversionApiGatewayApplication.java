package com.example.converter.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConversionApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConversionApiGatewayApplication.class, args);
    }
}
