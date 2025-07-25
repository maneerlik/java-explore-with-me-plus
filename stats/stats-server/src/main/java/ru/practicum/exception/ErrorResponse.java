package ru.practicum.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private String timestamp = LocalDateTime.now().toString();
    private int status;
    private String error;
    private String message;
    private String stackTrace;
    private Map<String, String> validationErrors;

    private ErrorResponse(Builder builder) {
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.stackTrace = builder.stackTrace;
        this.validationErrors = builder.validationErrors;
    }


    public static Builder builder(int status, String error) {
        return new Builder(status, error);
    }


    public static class Builder {
        private final int status;
        private final String error;
        private String message;
        private String stackTrace;
        private Map<String, String> validationErrors;

        public Builder(int status, String error) {
            this.status = status;
            this.error = error;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder validationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
