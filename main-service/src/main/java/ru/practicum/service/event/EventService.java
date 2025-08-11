package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.util.List;

public interface EventService {
    FullEventDto createEvent(NewEventDto event, Long userId);

    FullEventDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest event);

    FullEventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest event);

    FullEventDto getEvent(Long eventId);

    FullEventDto getEventByUser(Long userId, Long eventId);

    List<EventFullDto> getEvents(Long userId, Integer from, Integer size);

    List<EventFullDto> getEventsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);

    List<EventFullDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                       String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                       Integer from, Integer size);

}
