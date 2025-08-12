package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}
