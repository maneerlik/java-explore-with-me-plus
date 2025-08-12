package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    @NotNull(message = "Id cannot be empty")
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 250, message = "Name must be from {min} to {max} characters")
    private String name;
}
