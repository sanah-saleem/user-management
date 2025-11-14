package com.project.usermanagement.Exception;

import java.util.Map;
import java.util.stream.Collectors;

import com.project.usermanagement.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        fe -> fe.getField(), 
                        fe -> fe.getDefaultMessage(),
                        (a,b) -> a
                        ));

        return ResponseEntity.badRequest().body(Map.of(
                MessageConstants.MESSAGE, MessageConstants.VALIDATION_FAILED,
            MessageConstants.ERRORS, errors
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException ex) {
        var msg = ex.getMessage();
        if (MessageConstants.INVALID_CREDENTIALS.equals(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MessageConstants.MESSAGE, msg));
        }
        if (MessageConstants.CURRENT_PASSWORD_IS_INCORRECT.equals(msg) ||
            MessageConstants.NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD.equals(msg) ||
            MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS.equals(msg)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MessageConstants.MESSAGE, msg));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(MessageConstants.MESSAGE, msg));
    }
    
}
