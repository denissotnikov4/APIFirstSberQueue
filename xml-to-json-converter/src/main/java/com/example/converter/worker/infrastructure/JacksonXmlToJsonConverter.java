package com.example.converter.worker.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.example.converter.worker.domain.XmlToJsonConverter;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;

@Component
class JacksonXmlToJsonConverter implements XmlToJsonConverter {
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    JacksonXmlToJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        var inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        this.xmlMapper = new XmlMapper(inputFactory);
    }

    @Override
    public String convert(String xml) {
        try {
            var value = xmlMapper.readValue(xml, Object.class);
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not convert XML to JSON", exception);
        }
    }
}
