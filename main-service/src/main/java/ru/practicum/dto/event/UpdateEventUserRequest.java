package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.enums.UserEventStateAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов.")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов.")
    private String description;

    @Future(message = "Дата события должна быть в будущем.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Long participantLimit;

    private Boolean requestModeration;

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов.")
    private String title;

    private UserEventStateAction stateAction;
}