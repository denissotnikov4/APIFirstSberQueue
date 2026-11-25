package com.example.converter.gateway.api;

import com.example.converter.gateway.domain.ConversionPublicationException;
import com.example.converter.gateway.domain.InvalidXmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ConversionErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ConversionErrorHandler.class);

    @ExceptionHandler(InvalidXmlException.class)
    ProblemDetail invalidXml(InvalidXmlException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ConversionPublicationException.class)
    ProblemDetail brokerUnavailable(ConversionPublicationException exception) {
        log.error("Could not submit conversion", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Could not queue the conversion.");
    }
}
