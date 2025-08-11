package ru.practicum.dto.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class LocationDto {
    @JsonIgnore
    private Long id;

    @NotNull
    @DecimalMin(value = "-90", inclusive = false, message = "Latitude must be at least -90.0")
    @DecimalMax(value = "90", inclusive = false, message = "Latitude must be at most 90.0")
    Float lat;

    @NotNull
    @DecimalMin(value = "-180", inclusive = false, message = "Longitude must be at least -180.0")
    @DecimalMax(value = "180", inclusive = false, message = "Longitude must be at most 180.0")
    Float lon;
}
