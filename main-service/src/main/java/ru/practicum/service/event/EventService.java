package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.in.UpdateEventRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto getEvent(Long eventId, HttpServletRequest request);

    List<EventShortDto> getEventsByUser(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            SortValue sort,
            Integer from,
            Integer size,
            HttpServletRequest request
    );

    List<EventFullDto> getEventsByAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd,
            Integer from,
            Integer size
    );

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventRequest request);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request);
}
