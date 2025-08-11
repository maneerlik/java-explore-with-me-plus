package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event AS e WHERE e.id = :eventId AND e.initiator.id = :userId")
    Optional<Event> findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId")
    Page<Event> findAllByInitiatorId(@Param("userId") Long userId, Pageable pageable);

    Optional<Event> findByIdAndPublishedOnIsNotNull(Long eventId);
}
