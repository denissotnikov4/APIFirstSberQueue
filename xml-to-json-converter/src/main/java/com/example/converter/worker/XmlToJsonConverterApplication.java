package com.example.converter.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class XmlToJsonConverterApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmlToJsonConverterApplication.class, args);
    }
}
