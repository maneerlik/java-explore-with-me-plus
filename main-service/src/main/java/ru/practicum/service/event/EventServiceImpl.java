package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.UserEventStateAction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    @Value("${app.name:ewm-main-service}")
    private String appName;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EntityManager entityManager;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        validateEventDate(newEventDto.getEventDate(), 1);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + newEventDto.getCategory() + " не найдена."));

        Location location = getLocation(newEventDto.getLocation());

        Event event = EventMapper.toEvent(newEventDto, category, user, location);

        return EventMapper.toFullEventDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        Pageable page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page);
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = findEventByIdAndInitiatorId(eventId, userId);

        return EventMapper.toFullEventDto(event);
    }

    private void updateEventFromAdminRequest(Event event, UpdateEventAdminRequest dto) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            validateEventDate(dto.getEventDate(), 1);
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с ID=" + dto.getCategory() + " не найдена."));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            event.setLocation(getLocation(dto.getLocation()));
        }
    }

    private void updateEventFromUserRequest(Event event, UpdateEventUserRequest dto) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            validateEventDate(dto.getEventDate(), 1);
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с ID=" + dto.getCategory() + " не найдена."));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            event.setLocation(getLocation(dto.getLocation()));
        }
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " и инициатором ID=" + userId + " не найдено."));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить уже опубликованное событие.");
        }

        updateEventFromUserRequest(event, updateRequest);

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UserEventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == UserEventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toFullEventDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено."));

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Нельзя опубликовать событие, так как оно не в состоянии ожидания. Текущий статус: " + event.getState());
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие.");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFromAdminRequest(event, updateRequest);

        return EventMapper.toFullEventDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, SortValue sort,
                                               Integer from, Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);

        Root<Event> eventRoot = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            Predicate annotationLike = cb.like(cb.lower(eventRoot.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate descriptionLike = cb.like(cb.lower(eventRoot.get("description")), "%" + text.toLowerCase() + "%");
            predicates.add(cb.or(annotationLike, descriptionLike));
        }

        if (categories != null && !categories.isEmpty()) {
            predicates.add(eventRoot.get("category").get("id").in(categories));
        }

        if (paid != null) {
            predicates.add(cb.equal(eventRoot.get("paid"), paid));
        }

        LocalDateTime start = (rangeStart != null) ? rangeStart : LocalDateTime.now();
        predicates.add(cb.greaterThan(eventRoot.get("eventDate"), start));
        if (rangeEnd != null) {
            predicates.add(cb.lessThan(eventRoot.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            Predicate limitZero = cb.equal(eventRoot.get("participantLimit"), 0);
            Predicate limitNotReached = cb.lessThan(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit"));
            predicates.add(cb.or(limitZero, limitNotReached));
        }

        predicates.add(cb.equal(eventRoot.get("state"), EventState.PUBLISHED));

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        if (sort == SortValue.VIEWS) {
            query.orderBy(cb.desc(eventRoot.get("views")));
        } else if (sort == SortValue.EVENT_DATE) {
            query.orderBy(cb.desc(eventRoot.get("eventDate")));
        }

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        sendHitAsync(request.getRequestURI(), request.getRemoteAddr());

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с ID=" + eventId + " не найдено."));

        List<ViewStatsDto> stats = statsClient.getStats(
                event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn(),
                LocalDateTime.now(),
                List.of(request.getRequestURI()),
                true
        );

        if (!stats.isEmpty()) {
            event.setViews(stats.get(0).getHits());
        } else {
            event.setViews(0L);
        }

        sendHitAsync(request.getRequestURI(), request.getRemoteAddr());

        return EventMapper.toFullEventDto(event);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> eventRoot = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (users != null && !users.isEmpty()) {
            predicates.add(eventRoot.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            predicates.add(eventRoot.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            predicates.add(eventRoot.get("category").get("id").in(categories));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(eventRoot.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(eventRoot.get("eventDate"), rangeEnd));
        }

        query.where(predicates.toArray(new Predicate[0]));

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        return events.stream()
                .map(EventMapper::toFullEventDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> searchPublicEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, SortValue sort,
                                                  Integer from, Integer size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> eventRoot = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            predicates.add(cb.or(
                    cb.like(cb.lower(eventRoot.get("annotation")), "%" + text.toLowerCase() + "%"),
                    cb.like(cb.lower(eventRoot.get("description")), "%" + text.toLowerCase() + "%")
            ));
        }

        if (categories != null && !categories.isEmpty()) {
            predicates.add(eventRoot.get("category").get("id").in(categories));
        }

        if (paid != null) {
            predicates.add(cb.equal(eventRoot.get("paid"), paid));
        }

        LocalDateTime startDateTime = (rangeStart != null) ? rangeStart : LocalDateTime.now();
        predicates.add(cb.greaterThan(eventRoot.get("eventDate"), startDateTime));
        if (rangeEnd != null) {
            predicates.add(cb.lessThan(eventRoot.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            predicates.add(cb.or(
                    cb.equal(eventRoot.get("participantLimit"), 0),
                    cb.lessThan(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit"))
            ));
        }

        predicates.add(cb.equal(eventRoot.get("state"), EventState.PUBLISHED));

        query.where(predicates.toArray(new Predicate[0]));

        if (sort == SortValue.VIEWS) {
            query.orderBy(cb.desc(eventRoot.get("views")));
        } else {
            query.orderBy(cb.desc(eventRoot.get("eventDate")));
        }

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        sendHitAsync(request.getRequestURI(), request.getRemoteAddr());

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    private void validateEventDate(LocalDateTime eventDate, int hours) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new ValidationException("Дата события должна быть как минимум через " + hours + " часа от текущего момента.");
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementEventViews(Long eventId) {
        eventRepository.incrementViews(eventId);
    }

    private List<Event> findEventsWithPredicates(List<Predicate> predicates, SortValue sort, int from, int size) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        root.fetch("initiator", JoinType.LEFT);
        root.fetch("category", JoinType.LEFT);

        query.where(predicates.toArray(new Predicate[0]));

        if (sort != null) {
            if (sort == SortValue.VIEWS) {
                query.orderBy(builder.desc(root.get("views")));
            } else {
                query.orderBy(builder.asc(root.get("eventDate")));
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    private Location getLocation(LocationDto locationDto) {
        return locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(locationDto)));
    }

    private List<Predicate> buildAdminSearchPredicates(List<Long> users, List<EventState> states, List<Long> categories,
                                                       LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        if (users != null && !users.isEmpty()) {
            predicates.add(root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("state").in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        return predicates;
    }


    @Async
    public void sendHitAsync(String uri, String ip) {
        HitDto hitDto = new HitDto(null, appName, uri, ip, LocalDateTime.now());
        statsClient.saveHit(hitDto);
    }

    private Event findEventByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с ID=%d и инициатором ID=%d не найдено.", eventId, userId)
                ));
    }

    private List<Predicate> buildPublicSearchPredicates(String text, List<Long> categories, Boolean paid,
                                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get("state"), EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            String searchText = "%" + text.toLowerCase() + "%";
            predicates.add(builder.or(
                    builder.like(builder.lower(root.get("annotation")), searchText),
                    builder.like(builder.lower(root.get("description")), searchText)
            ));
        }
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            predicates.add(builder.equal(root.get("paid"), paid));
        }

        if (rangeStart == null && rangeEnd == null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        } else {
            if (rangeStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
        }

        if (onlyAvailable != null && onlyAvailable) {
            predicates.add(builder.or(
                    builder.equal(root.get("participantLimit"), 0),
                    builder.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
            ));
        }

        return predicates;
    }
}