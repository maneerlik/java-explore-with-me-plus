package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Пользователь id={} создает запрос на участие в событии id={}", userId, eventId);

        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос от пользователя " + userId + " на событие " + eventId + " уже существует.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может подавать заявку на собственное событие.");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя подать заявку на неопубликованное событие. Текущий статус: " + event.getState());
        }

        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Лимит участников для события " + eventId + " был достигнут.");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        boolean needsModeration = event.getRequestModeration() && event.getParticipantLimit() != 0;
        request.setStatus(needsModeration ? RequestStatus.PENDING : RequestStatus.CONFIRMED);

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Успешно создан запрос id={} со статусом {}", savedRequest.getId(), savedRequest.getStatus());

        return ParticipationRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Пользователь id={} обновляет статусы заявок {} для события id={}", userId, statusUpdateRequest.getRequestIds(), eventId);

        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только инициатор события может обновлять статусы заявок.");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.warn("Событие id={} не требует модерации заявок. Обновление не требуется.", eventId);
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        List<ParticipationRequest> requestsToUpdate = requestRepository.findAllByIdIn(statusUpdateRequest.getRequestIds());
        if (requestsToUpdate.stream().anyMatch(req -> req.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("Можно изменять только заявки в статусе PENDING.");
        }

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();
        RequestStatus newStatus = statusUpdateRequest.getStatus();

        if (newStatus == RequestStatus.REJECTED) {
            requestsToUpdate.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            rejectedRequests.addAll(requestsToUpdate);
        } else if (newStatus == RequestStatus.CONFIRMED) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            long limit = event.getParticipantLimit();

            if (confirmedCount >= limit) {
                throw new ConflictException("Лимит участников уже достигнут. Невозможно подтвердить новые заявки.");
            }

            for (ParticipationRequest request : requestsToUpdate) {
                if (confirmedCount < limit) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(request);
                    confirmedCount++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(request);
                }
            }

            if (confirmedCount >= limit) {
                List<ParticipationRequest> otherPendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
                otherPendingRequests.forEach(req -> req.setStatus(RequestStatus.REJECTED));
                rejectedRequests.addAll(otherPendingRequests);
                log.info("Достигнут лимит участников для события {}. Автоматически отклонено {} других заявок.", eventId, otherPendingRequests.size());
            }
        }

        requestRepository.saveAll(confirmedRequests);
        requestRepository.saveAll(rejectedRequests);

        return new EventRequestStatusUpdateResult(
                toDtoList(confirmedRequests),
                toDtoList(rejectedRequests)
        );
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение всех заявок на участие для пользователя id={}", userId);
        getUser(userId);
        return toDtoList(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByOwner(Long userId, Long eventId) {
        log.info("Владелец id={} получает заявки для своего события id={}", userId, eventId);
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь " + userId + " не является инициатором события " + eventId);
        }
        return toDtoList(requestRepository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Пользователь id={} отменяет свой запрос id={}", userId, requestId);
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " от пользователя " + userId + " не найден."));

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConflictException("Невозможно отменить уже подтвержденную заявку.");
        }

        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}