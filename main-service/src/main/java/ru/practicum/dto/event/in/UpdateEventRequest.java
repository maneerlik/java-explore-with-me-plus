package ru.practicum.dto.event.in;

import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.StateActionUser;

import java.time.LocalDateTime;

public record UpdateEventRequest(
        String annotation,
        Long category,
        String description,
        LocalDateTime eventDate,
        LocationDto location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        StateActionAdmin stateActionAdmin,
        StateActionUser stateActionUser,
        String title
) {
    public static UpdateEventRequest fromAdminRequest(UpdateEventAdminRequest request) {
        return new UpdateEventRequest(
                request.getAnnotation(),
                request.getCategory(),
                request.getDescription(),
                request.getEventDate(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getStateAction(),
                null,
                request.getTitle()
        );
    }

    public static UpdateEventRequest fromUserRequest(UpdateEventUserRequest request) {
        return new UpdateEventRequest(
                request.getAnnotation(),
                request.getCategory(),
                request.getDescription(),
                request.getEventDate(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                null,
                request.getStateAction(),
                request.getTitle()
        );
    }
}
