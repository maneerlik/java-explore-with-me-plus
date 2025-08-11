package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.ParticipationRequestDTO;
import ru.practicum.dto.request.RequestStatusUpdateDto;
import ru.practicum.dto.request.RequestStatusUpdateResult;
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
    public FullEventDto createEvent(@Valid @NotNull @RequestBody NewEventDto event, @PathVariable Long userId) {
        return eventService.createEvent(event, userId);
    }

    @PatchMapping(path = "/{eventId}")
    public FullEventDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @Valid @NotNull @RequestBody UpdateEventUserRequest event) {
        return eventService.updateEventByUser(userId, eventId, event);
    }

    @PatchMapping(path = "/{eventId}/requests")
    public RequestStatusUpdateResult updateStatusOfEvent(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody RequestStatusUpdateDto requestStatusUpdateDto) {
        return participationRequestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }

    @GetMapping(path = "/{eventId}")
    public FullEventDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @GetMapping
    public List<EventFullDto> getEvents(@PathVariable Long userId,
                                        @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
                                        @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping(path = "/{eventId}/requests")
    public List<ParticipationRequestDTO.Response.ParticipationRequestDto> getEventsWithUserParticipating(@PathVariable Long userId, @PathVariable Long eventId) {
        return participationRequestService.getRequestsByOwner(userId, eventId);
    }

}
