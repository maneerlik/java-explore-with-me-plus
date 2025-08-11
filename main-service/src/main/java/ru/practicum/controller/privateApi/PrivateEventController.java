package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDTO;
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
    public EventShortDto createEvent(@Valid @NotNull @RequestBody NewEventDto event, @PathVariable Long userId) {
        return eventService.createEvent(event, userId);
    }

    @GetMapping(path = "/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(name = "from", defaultValue = "0", required = false) int from,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping(path = "/{eventId}/requests")
    public List<ParticipationRequestDto> getEventsWithUserParticipating(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return participationRequestService.getRequestsByOwner(userId, eventId);
    }

    @PatchMapping(path = "/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @NotNull @RequestBody UpdateEventUserRequest event
    ) {
        return eventService.updateEventByUser(userId, eventId, event);
    }

    @PatchMapping(path = "/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusOfEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventDTO.Request.EventRequestStatusUpdateRequest requestStatusUpdateDto) {
        return participationRequestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }
}
