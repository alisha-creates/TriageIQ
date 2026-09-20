package com.example.TriageIQ.Exception;

import com.example.TriageIQ.DTO.ResponseDTO.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(EntityExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest req) {
        String message = req.getRequestURI().contains("verify-email")
                ? "This verification link has expired. Request a new one."
                : "Your session has expired. Please log in again.";
        return build(HttpStatus.UNAUTHORIZED, message, req, null);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleMalformedJwt(JwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid or tampered token", req, null);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleGenericJwt(JwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid token", req, null);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2AuthenticationError(OAuth2AuthenticationProcessingException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceError(AiServiceException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "AI service is temporarily unavailable. Please try again.", req, null);
    }

    @ExceptionHandler(InvalidAiResponseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAiResponse(InvalidAiResponseException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "AI returned an unexpected response. Please try again.", req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> ErrorResponse.FieldError.builder()
                        .field(f.getField())
                        .message(f.getDefaultMessage())
                        .build())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", req, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest req, List<ErrorResponse.FieldError> fieldErrors) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
