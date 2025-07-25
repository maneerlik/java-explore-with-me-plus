package ru.practicum.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ErrorResponse;
import ru.practicum.exception.ValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ErrorResponse handleException(final Exception e) {
        logError(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ErrorResponse.builder(status.value(), status.getReasonPhrase())
                        .message(e.getMessage())
                        .stackTrace(getStackTrace(e))
                        .build();
    }

    @ExceptionHandler
    public ErrorResponse validationHandler(final ValidationException e) {
        logError(e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ErrorResponse.builder(status.value(), status.getReasonPhrase())
                .message(e.getMessage())
                .stackTrace(getStackTrace(e))
                .build();
    }

    private void logError(Exception e) {
        String template = """
            \n================================================= ERROR ==================================================
            Message: {}
            Exception type: {}
            """;

        log.error(template, e.getMessage(), e.getClass().getName(), e);
    }

    private String getStackTrace(final Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
