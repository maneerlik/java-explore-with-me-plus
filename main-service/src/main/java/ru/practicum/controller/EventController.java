package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.UpdateEventUserDto;
import ru.practicum.model.Event;
import ru.practicum.service.event.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class EventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@Valid @NotNull @RequestBody EventDto event, @PathVariable Long userId) {
        return eventService.createEvent(event, userId);
    }

    @PatchMapping(path = "/{eventId}")
    public FullEventDto updateEvent(Long userId, Long eventId, @NotNull @RequestBody UpdateEventUserDto event) {
        return eventService.updateEvent(userId, eventId, event);
    }

    @PatchMapping(path = "/{eventId}/requests")
    public FullEventDto updateStatusOfEvent(Long userId, Long eventId) {
        return eventService.updateStatusOfEvent(userId, eventId);
    }

    @GetMapping(path = "/{eventId}")
    public EventDto getEvent(Long userId, Long eventId) {
        return eventService.getEvent(userId, eventId);
    }

    @GetMapping
    public List<EventDto> getEvents(Long userId) {
        return eventService.getEvents(userId);
    }

    @GetMapping(path = "/{eventId}/requests")
    public List<EventDto> getEventsWithUserParticipating(Long userId, Long eventId) {
        return eventService.getEventsWithUserParticipating(userId, eventId);
    }

}
