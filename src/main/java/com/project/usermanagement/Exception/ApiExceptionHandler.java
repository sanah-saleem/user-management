package com.project.usermanagement.Exception;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.usermanagement.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static java.util.Arrays.stream;

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
            MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS.equals(msg) ||
            MessageConstants.EMAIL_ALREADY_REGISTERED.equals(msg) ||
            MessageConstants.USER_IS_DELETED.equals(msg) ||
            MessageConstants.INVALID_OR_EXPIRED_TOKEN.equals(msg)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MessageConstants.MESSAGE, msg));
        }
        if (MessageConstants.ACCOUNT_IS_NOT_ACTIVE.equals(msg)) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(MessageConstants.MESSAGE, msg));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(MessageConstants.MESSAGE, msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
            String allowed = stream(requiredType.getEnumConstants())
                    .map(e -> ((Enum<?>)e).name())
                    .collect(Collectors.joining(", "));
            String message = "Invalid value '" + invalidValue + "' for parameter '" + ex.getName()
                                + "'. Allowed values: " + allowed;
            return ResponseEntity.badRequest().body(Map.of(MessageConstants.MESSAGE, message));
        }
        // other type mismatch (e.g., wrong number format)
        return ResponseEntity.badRequest().body(Map.of(MessageConstants.MESSAGE, "Invalid parameter: " + ex.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            Class<?> enumType = ife.getTargetType();
            String invalidValue = String.valueOf(ife.getValue());
            String allowed = Arrays.stream(enumType.getEnumConstants())
                    .map(e -> ((Enum<?>) e).name())
                    .collect(Collectors.joining(", "));
            String message = "Invalid value '" + invalidValue + "' for field. "
                    + "Allowed values: " + allowed;
            return ResponseEntity.badRequest().body(Map.of(MessageConstants.MESSAGE, message));
        }
        return ResponseEntity.badRequest().body(Map.of(MessageConstants.MESSAGE, "Malformed Json Request"));
    }
    
}
