package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    @NotNull
    private String annotation;

    @NotNull
    @Size(max = 125)
    private String title;

    @NotNull
    private String description;

    @NotNull
    private LocalDateTime eventDate;

    @NotNull
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;

    @NotNull
    @JsonProperty("category")
    private Long categoryId;
}
