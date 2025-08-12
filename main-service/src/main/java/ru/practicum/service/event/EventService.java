package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    @Transactional
    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    @Transactional
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    @Transactional
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, SortValue sort,
                                        Integer from, Integer size, HttpServletRequest request);

    @Transactional
    EventFullDto getEvent(Long eventId, HttpServletRequest request);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventShortDto> searchPublicEvents(String text, List<Long> categories, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Boolean onlyAvailable, SortValue sort,
                                           Integer from, Integer size, HttpServletRequest request);
}