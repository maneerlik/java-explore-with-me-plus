package ru.practicum.mapper;

import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public class ParticipationRequestMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private ParticipationRequestMapper() {

    }

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();

    }

    public static List<ParticipationRequestDto> toParticipationRequestDtoList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

}
