package com.authapp.athentication.exception;

import com.authapp.athentication.dtos.Errorresponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandle {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Errorresponse> handleResourceNotFoundException(ResourceNotFoundException exception, HttpServletRequest request) {

            return buildResponse(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage(),
                    request
            );
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Errorresponse> handleIllegalArgumentException(IllegalArgumentException exception,HttpServletRequest request) {
            return buildResponse(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage(),
                    request
            );
        }

    // 401 - Invalid token
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Errorresponse > handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request
        );
    }

    // 401 - Invalid credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Errorresponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                request
        );
    }

    // 400 - Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Errorresponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    // 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Errorresponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        // Log the real exception for debugging
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    //methid
    private ResponseEntity<Errorresponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest  request
    ) {

        Errorresponse response = new Errorresponse(
             message,status, request.getRequestURI(),Instant.now()
        );

        return ResponseEntity.status(status).body(response);

    }
    }
