package com.example.converter.gateway.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("converter")
public record XmlValidationProperties(DataSize maxXmlSize) {
}
