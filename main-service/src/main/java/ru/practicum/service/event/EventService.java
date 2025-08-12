package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.util.List;

public interface EventService {

    @Transactional
    FullEventDto createEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    FullEventDto getEventByUser(Long userId, Long eventId);

    @Transactional
    FullEventDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateRequest);

    @Transactional
    FullEventDto updateEventByAdmin(Long eventId, UpdateEventAdminDto updateRequest);

    List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                        String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                        Integer from, Integer size, HttpServletRequest request);

    @Transactional
    FullEventDto getEvent(Long eventId, HttpServletRequest request);

    List<FullEventDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);
}