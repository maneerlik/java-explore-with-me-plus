package ru.practicum.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RequestStatusUpdateResult {
    private List<ParticipationRequestDTO.Response.ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDTO.Response.ParticipationRequestDto> rejectedRequests;
}
