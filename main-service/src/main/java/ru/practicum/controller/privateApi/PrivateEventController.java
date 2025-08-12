package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDTO.Request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventDTO.Request.NewEventDto;
import ru.practicum.dto.event.EventDTO.Request.UpdateEventUserRequest;
import ru.practicum.dto.event.EventDTO.Response.EventFullDto;
import ru.practicum.dto.event.EventDTO.Response.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.EventDTO.Response.EventShortDto;
import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.service.event.EventService;
import ru.practicum.service.request.ParticipationRequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class PrivateEventController {
    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto event, @PathVariable Long userId) {
        log.info("PRIVATE-API: Пользователь ID={} создает событие: {}", userId, event);
        return eventService.createEvent(event, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("PRIVATE-API: Пользователь ID={} запрашивает свое событие ID={}", userId, eventId);
        return eventService.getEventByUser(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("PRIVATE-API: Пользователь ID={} запрашивает список своих событий (from={}, size={})", userId, from, size);
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("PRIVATE-API: Пользователь ID={} запрашивает заявки на участие для своего события ID={}", userId, eventId);
        return participationRequestService.getRequestsByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest event
    ) {
        log.info("PRIVATE-API: Пользователь ID={} обновляет свое событие ID={}: {}", userId, eventId, event);
        return eventService.updateEventByUser(userId, eventId, event);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateParticipationRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest requestStatusUpdateDto
    ) {
        log.info("PRIVATE-API: Пользователь ID={} обновляет статусы заявок для события ID={}: {}", userId, eventId, requestStatusUpdateDto);
        return participationRequestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }
}