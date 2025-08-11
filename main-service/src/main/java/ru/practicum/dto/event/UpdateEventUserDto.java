package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.enums.StateActionForUser;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserDto {
    @Size(min = 20, max = 2000)
    private String annotation;

    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;

    @Positive
    private Long participantLimit;
    private Boolean requestModeration;
    private StateActionForUser stateAction;

    @JsonProperty("category")
    private Long categoryId;
}
