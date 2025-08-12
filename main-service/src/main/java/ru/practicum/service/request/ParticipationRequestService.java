package ru.practicum.service.request;

import ru.practicum.dto.event.EventRequestStatusUpdateDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequests(
            Long userId, Long eventId, EventRequestStatusUpdateDto requestStatusUpdateDto
    );

    Collection<ParticipationRequestDto> getUserRequests(Long userId);

    List<ParticipationRequestDto> getRequestsByOwner(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long eventId);
}
