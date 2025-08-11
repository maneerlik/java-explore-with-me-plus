package ru.practicum.service.event;

import ru.practicum.dto.event.EventDTO.Request.NewEventDto;
import ru.practicum.dto.event.EventDTO.Request.UpdateEventAdminRequest;
import ru.practicum.dto.event.EventDTO.Request.UpdateEventUserRequest;
import ru.practicum.dto.event.EventDTO.Response.EventFullDto;
import ru.practicum.dto.event.EventDTO.Response.EventShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.util.List;

public interface EventService {
    EventShortDto createEvent(NewEventDto event, Long userId);

    EventFullDto getEvent(Long eventId);

    EventFullDto getEventByUser(Long userId, Long eventId);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    List<EventFullDto> getEventsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);

    List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                        String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                        Integer from, Integer size);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest event);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest event);
}
