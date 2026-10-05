package com.horizonbank.horizon_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.horizonbank.horizon_api.service.RequestInProgressException;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestInProgressException.class)
    public ResponseEntity<Map<String, Object>> handleRequestInProgress(
            RequestInProgressException ex,
            HttpServletRequest request) {

        Map<String, Object> problem = new LinkedHashMap<>();

        problem.put("type", "about:blank");
        problem.put("title", "Request In Progress");
        problem.put("status", 409);
        problem.put("detail", ex.getMessage());
        problem.put("instance", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        Map<String, Object> problem = new LinkedHashMap<>();

        problem.put("type", "about:blank");
        problem.put("title", "Bad Request");
        problem.put("status", 400);
        problem.put("detail", ex.getMessage());
        problem.put("instance", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestHeader(
        MissingRequestHeaderException ex,
        HttpServletRequest request) {

    Map<String, Object> problem = new LinkedHashMap<>();

    problem.put("type", "about:blank");
    problem.put("title", "Bad Request");
    problem.put("status", 400);
    problem.put("detail", "Idempotency-Key is required");
    problem.put("instance", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }
}