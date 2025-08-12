package ru.practicum.controller.privateApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.request.ParticipationRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class ParticipationRequestController {
    private final ParticipationRequestService requestService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        log.info("Creating request for user id {} with event id {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping
    public Collection<ParticipationRequestDto> getUserRequests(
            @PathVariable Long userId
    ) {
        log.info("Getting requests for user id {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Canceling request for user id {} with request id {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
