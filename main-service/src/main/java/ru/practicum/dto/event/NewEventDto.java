package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.location.LocationDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {
    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    @Size(min = 3, max = 120)
    private String title;

    @NotNull
    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;
    private Boolean paid;

    @Positive
    private Long participantLimit;
    private Boolean requestModeration;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;

    @NotNull
    @JsonProperty("category")
    private Long category;
}