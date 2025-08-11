package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.util.List;

public interface EventService {
    FullEventDto createEvent(NewEventDto event, Long userId);

    FullEventDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto event);

    FullEventDto updateEventByAdmin(Long eventId, UpdateEventAdminDto event);

    FullEventDto getEvent(Long eventId);

    FullEventDto getEventByUser(Long userId, Long eventId);

    List<EventDto> getEvents(Long userId, Integer from, Integer size);

    List<EventDto> getEventsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                              String rangeStart, String rangeEnd, Integer from, Integer size);

    List<EventDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                             String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                             Integer from, Integer size);

}
