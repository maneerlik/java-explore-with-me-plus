package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HitDto {
    private Long id;

    @NotBlank(message = "App name cannot be empty")
    private String app;

    @NotBlank(message = "URI cannot be empty")
    private String uri;
    private String ip;
    private LocalDateTime timeStamp;
}
