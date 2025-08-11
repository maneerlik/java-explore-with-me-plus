package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventDTO.Request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventDTO.Response.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ParticipationException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("User with id={} is creating request for event with id={}", userId, eventId);

        User user = getUser(userId);
        Event event = getEvent(eventId);

        boolean requestExists = requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent();
        if (requestExists) throw new ConflictException("Request already exists");

        boolean userIsInitiator = event.getInitiator().getId().equals(userId);
        if (userIsInitiator) throw new ConflictException("Initiator cannot apply for his event");

        boolean eventIsPublished = event.getState().equals(EventState.PUBLISHED);
        if (!eventIsPublished) throw new ConflictException("Cannot apply for an unpublished event");

        List<ParticipationRequest> requests = requestRepository.findAllByEvent(event);
        boolean limitIsExceeded = requests.size() >= event.getParticipantLimit();
        boolean isRequestModeration = event.getRequestModeration();
        if (!isRequestModeration && limitIsExceeded) throw new ConflictException("Member limit exceeded");

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        if (!isRequestModeration) {
            log.debug("Event id={} has disabled request moderation, auto-confirming request", event.getId());
            request.confirm();
        }
        ParticipationRequest requestSaved = requestRepository.save(request);

        return ParticipationRequestMapper.toParticipationRequestDto(requestSaved);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequests(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest requestStatusUpdateDto
    ) {
        log.info("User with id={} is modifying requests for event with id={}", userId, eventId);

        Event event = getEvent(eventId);
        getUser(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("User with id={} is not the initiator of event with id={}", userId, eventId);
            throw new ParticipationException("Only the event initiator can update participation requests");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.debug("Request moderation disabled or part limit is 0. No action needed for event id={}", eventId);
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(requestStatusUpdateDto.getRequestIds());
        if (requests.isEmpty()) {
            log.debug("No requests found for IDs: {}", requestStatusUpdateDto.getRequestIds());
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        if (requests.stream().anyMatch(req -> !req.getEvent().getId().equals(eventId))) {
            log.warn("Some requests do not belong to event with id={}", eventId);
            throw new ParticipationException("Some requests do not belong to the specified event");
        }

        if (requests.stream().anyMatch(req -> req.getStatus() != RequestStatus.PENDING)) {
            log.warn("Some requests are not in PENDING status: {}", requests);
            throw new ParticipationException("Cannot update requests not in PENDING status");
        }

        long confirmedRequestsCount = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L;
        long participantLimit = event.getParticipantLimit();

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        if (requestStatusUpdateDto.getStatus() == RequestStatus.CONFIRMED) {
            long availableSlots = participantLimit - confirmedRequestsCount;
            if (availableSlots <= 0) {
                log.warn("Participant limit already reached for event id={}", eventId);
                throw new ParticipationException("The participant limit has been reached");
            }

            long toConfirm = Math.min(requests.size(), availableSlots);
            confirmedRequests.addAll(requests.subList(0, (int) toConfirm));
            rejectedRequests.addAll(requests.subList((int) toConfirm, requests.size()));

            rejectedRequests.forEach(req -> req.setStatus(RequestStatus.REJECTED));

        } else if (requestStatusUpdateDto.getStatus() == RequestStatus.REJECTED) {
            rejectedRequests.addAll(requests);
        } else {
            throw new IllegalArgumentException("Unsupported status: " + requestStatusUpdateDto.getStatus());
        }

        confirmedRequests.forEach(req -> req.setStatus(RequestStatus.CONFIRMED));

        requestRepository.saveAll(confirmedRequests);
        requestRepository.saveAll(rejectedRequests);

        if (!confirmedRequests.isEmpty()) {
            event.setConfirmedRequests(confirmedRequestsCount + confirmedRequests.size());
            eventRepository.save(event);
        }

        List<ParticipationRequestDto> confirmedDtos = confirmedRequests.isEmpty() ?
                List.of() :
                ParticipationRequestMapper.toParticipationRequestDtoList(confirmedRequests);

        List<ParticipationRequestDto> rejectedDtos = rejectedRequests.isEmpty() ?
                List.of() :
                ParticipationRequestMapper.toParticipationRequestDtoList(rejectedRequests);

        log.info("Successfully updated {} requests for event id={}: {} confirmed, {} rejected",
                requests.size(), eventId, confirmedRequests.size(), rejectedRequests.size());

        return new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);
    }

    @Override
    public Collection<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting participation requests for user with id={}", userId);

        User user = getUser(userId);
        Collection<ParticipationRequest> requests = requestRepository.findAllByRequester(user);
        log.debug("Found {} requests for user id={}", requests.size(), userId);

        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByOwner(Long userId, Long eventId) {
        return ParticipationRequestMapper.toParticipationRequestDtoList(requestRepository.findAllByEventIdAndRequesterId(eventId, userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("User with id={} is cancelling request with id={}", userId, requestId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Request with id={} not found", requestId);
                    return new NotFoundException("Request with id=" + requestId + " not found");
                });

        boolean userIsRequester = request.getRequester().getId().equals(userId);
        if (!userIsRequester) {
            log.warn("User with id={} tried to cancel request id={}, but is not the requester", userId, requestId);
            throw new IllegalStateException("User is not requester");
        }

        log.debug("Cancelling participation request id={} for user id={}", requestId, userId);
        request.reject();

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with id={} not found", eventId);
                    return new NotFoundException("Event with id=" + eventId + " not found");
                });
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", userId);
                    return new NotFoundException("User with id=" + userId + " not found");
                });
    }
}
