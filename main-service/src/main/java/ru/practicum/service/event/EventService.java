package ru.practicum.service.event;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.UpdateEventUserDto;

import java.util.List;

public interface EventService {
    EventDto createEvent(EventDto event, Long userId);

    FullEventDto updateEvent(Long userId, Long eventId, UpdateEventUserDto event);

    FullEventDto updateStatusOfEvent(Long userId, Long eventId);

    EventDto getEvent(Long userId, Long eventId);

    List<EventDto> getEvents(Long userId);

    List<EventDto> getEventsWithUserParticipating(Long userId, Long eventId);
}
