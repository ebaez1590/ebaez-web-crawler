package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HackerNewsFetchException.class)
    public ResponseEntity<Map<String, Object>> handleFetchFailure(HackerNewsFetchException ex) {
        Map<String, Object> body = errorBody(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                ex.getMessage());
        body.put("kind", ex.getKind().name());
        if (ex.getStatusCode() != null) {
            body.put("upstreamStatus", ex.getStatusCode());
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message = "Query parameter '" + ex.getParameterName() + "' is required";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(HttpStatus.BAD_REQUEST, "Bad Request", message));
    }

    private static Map<String, Object> errorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
