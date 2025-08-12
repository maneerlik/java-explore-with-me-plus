package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым.")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов.")
    private String name;

    @NotBlank(message = "Email не может быть пустым.")
    @Email(message = "Email должен быть в корректном формате.")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов.")
    private String email;
}