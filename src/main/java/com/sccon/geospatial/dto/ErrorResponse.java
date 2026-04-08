package com.sccon.geospatial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldViolation> violations;

    private ErrorResponse() {}

    public static ErrorResponse of(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse();
        response.status = status.value();
        response.error = status.getReasonPhrase();
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static ErrorResponse ofValidation(HttpStatus status, String message, List<FieldViolation> violations) {
        ErrorResponse response = of(status, message);
        response.violations = violations;
        return response;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldViolation> getViolations() { return violations; }

    public static class FieldViolation {

        private final String field;
        private final Object rejectedValue;
        private final String message;

        public FieldViolation(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() { return field; }
        public Object getRejectedValue() { return rejectedValue; }
        public String getMessage() { return message; }
    }
}
