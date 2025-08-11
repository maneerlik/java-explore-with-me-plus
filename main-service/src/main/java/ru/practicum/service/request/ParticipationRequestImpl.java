package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatusUpdateDto;
import ru.practicum.dto.request.RequestStatusUpdateResult;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.RequestStatusToUpdate;
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

        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            log.warn("User with id={} not found when creating requests", userId);
            throw new NotFoundException(String.format("User with id: %s not found", userId));
        }
        User user = userRepository.findById(userId).get();

        boolean eventExists = eventRepository.existsById(eventId);
        if (!eventExists) {
            log.warn("Event with id={} not found when creating requests", eventId);
            throw new NotFoundException(String.format("Event with id: %s not found", eventId));
        }
        Event event = eventRepository.findById(eventId).get();

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
    public RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не существует."));

        RequestStatusUpdateResult result = new RequestStatusUpdateResult();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndRequesterId(eventId, userId);
        List<ParticipationRequest> requestsToUpdate = requests.stream().filter(request -> requestStatusUpdateDto.getRequestIds().contains(request.getId())).toList();

        if (requestsToUpdate.stream().anyMatch(request -> !RequestStatus.PENDING.equals(request.getStatus()))) {
            throw new ParticipationException("Найден запрос не со статусом ожидания.");
        }

        if (event.getConfirmedRequests() + requestsToUpdate.size() > event.getParticipantLimit() && requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            throw new ParticipationException("exceeding the limit of participants");
        }

        for (ParticipationRequest request : requestsToUpdate) {
            request.setStatus(RequestStatus.valueOf(requestStatusUpdateDto.getStatus().toString()));
        }

        requestRepository.saveAll(requestsToUpdate);

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToUpdate.size());
        }

        eventRepository.save(event);

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            result.setConfirmedRequests(ParticipationRequestMapper.toParticipationRequestDtoList(requestsToUpdate));
        }

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED)) {
            result.setRejectedRequests(ParticipationRequestMapper.toParticipationRequestDtoList(requestsToUpdate));
        }

        return result;
    }

    @Override
    public Collection<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting participation requests for user with id={}", userId);

        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            log.warn("User with id={} not found when getting requests", userId);
            throw new NotFoundException(String.format("User with id: %s not found", userId));
        }
        User user = userRepository.findById(userId).get();

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

        boolean requestExists = requestRepository.existsById(requestId);
        if (!requestExists) throw new NotFoundException(String.format("Request with id: %s not found", requestId));
        ParticipationRequest request = requestRepository.findById(requestId).get();

        boolean userIsRequester = request.getRequester().getId().equals(userId);
        if (!userIsRequester) {
            log.warn("User with id={} tried to cancel request id={}, but is not the requester", userId, requestId);
            throw new IllegalStateException("User is not requester");
        }

        log.debug("Cancelling participation request id={} for user id={}", requestId, userId);
        request.reject();

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }
}
