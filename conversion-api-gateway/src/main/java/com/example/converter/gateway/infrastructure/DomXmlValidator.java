package com.example.converter.gateway.infrastructure;

import com.example.converter.gateway.domain.InvalidXmlException;
import com.example.converter.gateway.domain.XmlValidator;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Component
class DomXmlValidator implements XmlValidator {
    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private final XmlValidationProperties properties;

    DomXmlValidator(XmlValidationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void validate(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new InvalidXmlException("XML must not be empty.");
        }
        if (xml.getBytes(StandardCharsets.UTF_8).length > properties.maxXmlSize().toBytes()) {
            throw new InvalidXmlException("XML exceeds the allowed size of " + properties.maxXmlSize() + ".");
        }

        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(DISALLOW_DOCTYPE, true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXException | IOException exception) {
            throw new InvalidXmlException("XML must be well-formed and must not contain a DOCTYPE.", exception);
        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException("Could not configure secure XML validation", exception);
        }
    }
}
