package ru.practicum.mapper;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.enums.EventState;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class EventMapper {
    private EventMapper() {

    }

    public static Event toEvent(NewEventDto dto, Category category, User user, Location location) {
        if (dto == null) {
            return null;
        }
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(location)
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .initiator(user)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .publishedOn(null)
                .views(0L)
                .confirmedRequests(0L)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        if (event == null) {
            return null;
        }
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getEventDate(),
                UserMapper.toShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                event.getViews(),
                event.getConfirmedRequests(),
                event.getParticipantLimit()
        );
    }

    public static Set<EventShortDto> toEventShortDtoSet(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptySet();
        }
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toSet());
    }

    public static List<EventShortDto> toEventShortDtoList(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public static EventFullDto toFullEventDto(Event event, long confirmedRequestsCount) {
        if (event == null) {
            return null;
        }
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequestsCount,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                UserMapper.toShortDto(event.getInitiator()),
                LocationMapper.toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                event.getViews()
        );
    }

    public static EventFullDto toFullEventDto(Event event) {
        if (event == null) {
            return null;
        }

        return toFullEventDto(event, 0L);
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream()
                .map(EventMapper::toFullEventDto)
                .collect(Collectors.toList());
    }
}