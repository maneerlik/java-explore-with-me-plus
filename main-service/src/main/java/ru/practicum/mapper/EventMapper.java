package ru.practicum.mapper;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.enums.EventState;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.List;

public final class EventMapper {

    public static Event toEvent(NewEventDto dto, Category category, User user) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        event.setPaid(false);
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        } else {
            event.setParticipantLimit(0L);
        }
        event.setRequestModeration(dto.getRequestModeration());
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        if (dto.getCreatedOn() != null) {
            event.setCreatedOn(dto.getCreatedOn());
        } else {
            event.setCreatedOn(LocalDateTime.now());
        }
        event.setPublishedOn(dto.getPublishedOn());
        event.setViews(0L);

        return event;
    }

    public static EventDto toEventDto(Event entity) {
        return EventDto.builder()
                .annotation(entity.getAnnotation())
                .categoryId(entity.getCategory().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .eventDate(entity.getEventDate())
                .location(entity.getLocation())
                .paid(entity.getPaid())
                .participantLimit(entity.getParticipantLimit())
                .requestModeration(entity.getRequestModeration())
                .build();
    }

    public static FullEventDto toFullEventDto(Event event) {
        return FullEventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .description(event.getDescription())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiator())
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .category(event.getCategory())
                .views(event.getViews())
                .build();
    }

    public static List<EventDto> toEventDtoList(List<Event> events) {
        return events.stream().map(EventMapper::toEventDto).toList();
    }

}
