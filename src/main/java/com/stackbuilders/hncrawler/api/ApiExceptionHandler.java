package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HackerNewsFetchException.class)
    public ResponseEntity<Map<String, Object>> handleFetchFailure(HackerNewsFetchException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_GATEWAY.value());
        body.put("error", "Bad Gateway");
        body.put("message", ex.getMessage());
        body.put("kind", ex.getKind().name());
        if (ex.getStatusCode() != null) {
            body.put("upstreamStatus", ex.getStatusCode());
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
