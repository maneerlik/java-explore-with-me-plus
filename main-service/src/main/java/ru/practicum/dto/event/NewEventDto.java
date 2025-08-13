package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.dto.location.LocationDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Annotation cannot be empty")
    @Size(min = 20, max = 2000, message = "Annotation must be from {min} to {max} characters")
    private String annotation;

    @NotNull(message = "Category Id cannot be null")
    @Positive(message = "Category Id must be positive")
    private Long category;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 20, max = 7000, message = "Description must be from {min} to {max} characters")
    private String description;

    @NotNull(message = "Event date cannot be null")
    @FutureOrPresent(message = "Event date must be in the future or present")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Location cannot be null")
    private LocationDto location;

    @NotNull(message = "Paid flag cannot be null")
    private Boolean paid;

    @PositiveOrZero(message = "ParticipantLimit must be positive")
    private Long participantLimit;
    private Boolean requestModeration;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 3, max = 120, message = "Title must be from {min} to {max} characters")
    private String title;
}
