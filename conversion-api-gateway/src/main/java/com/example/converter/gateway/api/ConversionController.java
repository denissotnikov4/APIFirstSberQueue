package com.example.converter.gateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.converter.gateway.application.GetConversionStatusUseCase;
import com.example.converter.gateway.application.SubmitConversionUseCase;
import com.example.converter.gateway.domain.ConversionRequest;
import com.example.converter.gateway.domain.ConversionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(ConversionController.PATH)
class ConversionController {
    static final String PATH = "/api/v1/conversions";
    private final SubmitConversionUseCase submitConversion;
    private final GetConversionStatusUseCase getConversionStatus;
    private final ObjectMapper objectMapper;

    ConversionController(SubmitConversionUseCase submitConversion, GetConversionStatusUseCase getConversionStatus,
                         ObjectMapper objectMapper) {
        this.submitConversion = submitConversion;
        this.getConversionStatus = getConversionStatus;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubmitConversionResponse> submit(@RequestBody SubmitConversionRequest input) {
        var request = submitConversion.submit(input.xml());
        return ResponseEntity.accepted()
                .location(URI.create(PATH + "/" + request.id()))
                .body(new SubmitConversionResponse(request.id(), request.status()));
    }

    @GetMapping("/{id}")
    ConversionStatusResponse status(@PathVariable UUID id) {
        var request = findConversion(id);
        return new ConversionStatusResponse(request.id(), request.status(), request.submittedAt(),
                request.completedAt(), request.jsonResult());
    }

    @GetMapping(value = "/{id}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    JsonNode result(@PathVariable UUID id) throws JsonProcessingException {
        var request = findConversion(id);
        if (request.status() != ConversionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conversion result is unavailable.");
        }
        return objectMapper.readTree(request.jsonResult());
    }

    private ConversionRequest findConversion(UUID id) {
        return getConversionStatus.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversion not found."));
    }
}
