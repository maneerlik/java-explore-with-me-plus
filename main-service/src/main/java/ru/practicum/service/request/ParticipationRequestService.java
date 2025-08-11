package ru.practicum.service.request;

import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;

import java.util.Collection;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    Collection<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long eventId);
}
