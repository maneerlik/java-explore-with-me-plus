package ru.practicum.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class ApiError {
    private String errors;
    private String reason;
    private String message;
    private String status;
    private String timestamp;

    private ApiError(Builder builder) {
        this.errors = builder.errors;
        this.message = builder.message;
        this.reason = builder.reason;
        this.status = builder.status;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    public static Builder builder(HttpStatus status, String reason) {
        return new Builder(status, reason);
    }


    public static class Builder {
        private String errors;
        private String message;
        private final String reason;
        private final String status;

        public Builder(HttpStatus status, String reason) {
            this.status = status.name();
            this.reason = reason;
        }

        public Builder errors(String errors) {
            this.errors = errors;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ApiError build() {
            return new ApiError(this);
        }
    }
}
