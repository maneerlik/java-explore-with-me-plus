package ru.practicum.mapper;

import ru.practicum.dto.event.EventDTO;
import ru.practicum.dto.event.EventDTO.Request.NewEventDto;
import ru.practicum.dto.event.EventDTO.Response.EventFullDto;
import ru.practicum.dto.event.EventDTO.Response.EventShortDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.enums.EventState;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class EventMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private EventMapper() {

    }


    public static Event toEvent(NewEventDto dto, Category category, User user) {
        Event event = new Event();

        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setLocation(LocationMapper.toLocation(dto.getLocation()));
        event.setPaid(false);
        event.setParticipantLimit(Objects.nonNull(dto.getParticipantLimit()) ? dto.getParticipantLimit() : 0L);
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

    public static EventShortDto toEventShortDto(Event event) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                UserMapper.toShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }

    public static EventFullDto toFullEventDto(Event event) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
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

    public static List<EventFullDto> toEventDtoList(List<Event> events) {
        return events.stream().map(EventMapper::toEventDto).toList();
    }
}
