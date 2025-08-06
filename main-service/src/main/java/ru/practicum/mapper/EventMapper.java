package ru.practicum.mapper;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;

public final class EventMapper {

    public static Event toEvent(EventDto dto, Category category) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
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

}
