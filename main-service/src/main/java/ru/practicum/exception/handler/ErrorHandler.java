package ru.practicum.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public ResponseEntity<ApiError> handleValidationException(final ValidationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError error = ApiError.builder(status, "validation error")
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        response.put("errors", errors);
        return response;
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariable(final MissingPathVariableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError error = ApiError.builder(status, "Missing path variable.")
                .message(ex.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(final MissingServletRequestParameterException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError error = ApiError.builder(status, "Missing params of method.")
                .message(ex.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    @ExceptionHandler(WrongTimeException.class)
    public ResponseEntity<ApiError> handleWrongTime(final WrongTimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError error = ApiError.builder(status, "Wrong time of event.")
                .message(ex.getMessage())
                .build();
        return buildResponseEntity(error, status);
    }

    @ExceptionHandler(EventException.class)
    public ResponseEntity<ApiError> handleEventException(final EventException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ApiError error = ApiError.builder(status, "Event exception.")
                .message(ex.getMessage())
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
