package ru.practicum.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ApiError;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ApiError> handleException(final Exception e) {
        logError(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError error = ApiError.builder(status, "Internal Server Error.")
                .message(e.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> conflictHandler(final ConflictException e) {
        log.warn("Conflict: {}", e.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        ApiError error = ApiError.builder(status, "Integrity constraint has been violated.")
                .message(e.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError error = ApiError.builder(status, "The required object was not found.")
                .message(e.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError error, HttpStatus status) {
        return ResponseEntity.status(status).body(error);
    }

    private void logError(Exception e) {
        String template = """
                \n
                ================================================== ERROR ===============================================
                Message: {}
                Exception type: {}
                Stacktrace: {}
                ========================================================================================================
                """;

        log.error(template, e.getMessage(), e.getClass().getSimpleName(), getStackTrace(e));
    }

    private String getStackTrace(final Exception e) {
        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(element -> "\tat " + element)
                .collect(Collectors.joining("\n"));

        return e + "\n" + stackTrace;
    }
}
