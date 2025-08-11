package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByEvent(Event event);

    List<ParticipationRequest> findAllByRequester(User user);
}
