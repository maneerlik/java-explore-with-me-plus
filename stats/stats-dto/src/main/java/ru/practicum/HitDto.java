package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HitDto {

    @NotBlank(message = "Поле 'app' не должно быть пустым.")
    private String app;

    @NotBlank(message = "Поле 'uri' не должно быть пустым.")
    private String uri;

    @NotBlank(message = "Поле 'ip' не должно быть пустым.")
    private String ip;

    @NotNull(message = "Поле 'timestamp' не должно быть null.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}