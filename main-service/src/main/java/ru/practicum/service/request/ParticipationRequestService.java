package ru.practicum.service.request;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    @Transactional
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    @Transactional
    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    List<ParticipationRequestDto> getRequestsByOwner(Long userId, Long eventId);

    @Transactional
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}