package com.sccon.geospatial.handler;

import com.sccon.geospatial.dto.ErrorResponse;
import com.sccon.geospatial.exception.InvalidPersonDataException;
import com.sccon.geospatial.exception.InvalidOutputFormatException;
import com.sccon.geospatial.exception.PersonConflictException;
import com.sccon.geospatial.exception.PersonNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PersonNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(PersonConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(PersonConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(InvalidOutputFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOutput(InvalidOutputFormatException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(InvalidPersonDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPersonData(InvalidPersonDataException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldViolation(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofValidation(HttpStatus.BAD_REQUEST, "Validation failed", violations));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        InvalidFormatException invalidFormatException = findCause(ex, InvalidFormatException.class);

        if (invalidFormatException != null) {
            String field = invalidFormatException.getPath().isEmpty()
                    ? "requestBody"
                    : invalidFormatException.getPath().get(0).getFieldName();
            Object rejectedValue = invalidFormatException.getValue();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.ofValidation(
                            HttpStatus.BAD_REQUEST,
                            "Malformed JSON request body",
                            List.of(new ErrorResponse.FieldViolation(
                                    field,
                                    rejectedValue,
                                    "Invalid value for field '" + field + "'"
                            ))
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Malformed JSON request body"));
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }
}
