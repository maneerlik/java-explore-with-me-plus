package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private Set<Long> events = new HashSet<>();

    private Boolean pinned = false;

    @NotBlank(message = "Заголовок подборки не может быть пустым.")
    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов.")
    private String title;
}