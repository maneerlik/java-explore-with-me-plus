package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "Request IDs cannot be null")
    private List<Long> requestIds;

    @NotNull(message = "Status cannot be null")
    private RequestStatus status;
}
