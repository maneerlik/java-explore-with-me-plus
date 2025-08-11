package ru.practicum.mapper;

import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

public class ParticipationRequestMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private ParticipationRequestMapper() {

    }


    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequest.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();

    }
}
