package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;

    @NotBlank(message = "Annotation cannot be empty")
    @Size(min = 20, max = 2000, message = "Annotation must be from {min} to {max} characters")
    private String annotation;

    @Valid
    private CategoryDto category;
    private Long confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 20, max = 7000, message = "Description must be from {min} to {max} characters")
    private String description;

    @NotNull(message = "Event date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    private UserShortDto initiator;

    @Valid
    private LocationDto location;

    @NotNull(message = "Paid flag cannot be null")
    private Boolean paid;
    private Long participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 3, max = 120, message = "Title must be from {min} to {max} characters")
    private String title;

    private Long views;
}
