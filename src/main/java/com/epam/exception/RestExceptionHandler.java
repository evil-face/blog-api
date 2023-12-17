package com.epam.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<Map<String, List<String>>> handleValidationError(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();

        LOGGER.warn("Payload validation errors: {}", errors);
        return ResponseEntity.unprocessableEntity().body(Map.of("errors", errors));
    }

    @ExceptionHandler(PostNotFoundException.class)
    private ResponseEntity<String> handlePostNotFoundException(PostNotFoundException e) {
        LOGGER.warn("Post with id '{}' wasn't found", e.getMessage());
        return ResponseEntity.notFound().build();
    }
}
