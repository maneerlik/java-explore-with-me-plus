package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.dto.request.ParticipationRequestDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDTO.Response.ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDTO.Response.ParticipationRequestDto> rejectedRequests;
}
