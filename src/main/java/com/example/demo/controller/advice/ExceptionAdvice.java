package com.example.demo.controller.advice;

import com.example.demo.tool.exception.NotEnoughFundsInAccount;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

import feign.RetryableException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, реализующий функционал обработки исключений, возникающих в работе сервиса
 */
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    @ExceptionHandler(NotEnoughFundsInAccount.class)
    public ResponseEntity<Map<String, String>> handle(NotEnoughFundsInAccount e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Error message", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new HashMap<>(errors));
    }

    @ExceptionHandler(NotUniqueUsernameException.class)
    public ResponseEntity<Map<String, String>> handle(NotUniqueUsernameException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Error message", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    @ExceptionHandler(NotUniqueEmailException.class)
    public ResponseEntity<Map<String, String>> handle(NotUniqueEmailException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Error message", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<Map<String, String>> handle(RetryableException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Error message", "An error occurred while getting the exchange rate");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }


}
