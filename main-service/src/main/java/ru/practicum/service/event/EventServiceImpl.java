package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatsClient;
import ru.practicum.StatsDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.in.UpdateEventRequest;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Location;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.StateActionUser;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.category.CategoryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsClient statsClient;
    private final CategoryService categoryService;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EntityManager entityManager;


    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        validateDate(newEventDto.getEventDate(), 2);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + newEventDto.getCategory() + " не найдена."));

        Location location = getLocation(newEventDto.getLocation());

        Event event = EventMapper.toEvent(newEventDto, category, user, location);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        Pageable page = PageRequest.of(from / size, size);
        List<Event> events = (List<Event>) eventRepository.findAllByInitiatorId(userId, page);
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " и инициатором ID=" + userId + " не найдено."));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " и инициатором ID=" + userId + " не найдено."));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить уже опубликованное событие.");
        }

        updateEvent(event, request);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        log.info("Admin is updating event with id={}", eventId);

        Event existsEvent = getEvent(eventId);

        // Проверка категории, если указана
        if (nonNull(request.category())) categoryService.getCategoryById(request.category());

        // Определение финальной даты события
        LocalDateTime eventDate = nonNull(request.eventDate()) ? request.eventDate() : existsEvent.getEventDate();

        StateActionAdmin stateAction = request.stateActionAdmin();
        if (nonNull(stateAction)) {
            if (stateAction == StateActionAdmin.REJECT_EVENT && existsEvent.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot cancel a published event");
            }
            if (stateAction == StateActionAdmin.PUBLISH_EVENT) {
                if (existsEvent.getState() != EventState.PENDING)
                    throw new ConflictException("Only pending events can be published");
                validateDate(eventDate, 1);
            }
        }

        updateEvent(existsEvent, request);
        eventRepository.save(existsEvent);

        return EventMapper.toEventFullDto(existsEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                               Integer from, Integer size, HttpServletRequest request) {
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get("state"), EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            predicates.add(builder.or(
                    builder.like(builder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
            ));
        }
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            predicates.add(builder.equal(root.get("paid"), paid));
        }

        if (start == null && end == null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        } else {
            if (start != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), end));
            }
        }

        if (onlyAvailable != null && onlyAvailable) {
            predicates.add(builder.or(
                    builder.equal(root.get("participantLimit"), 0),
                    builder.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
            ));
        }

        query.where(predicates.toArray(new Predicate[0]));

        if (sort == SortValue.VIEWS) {
            query.orderBy(builder.desc(root.get("views")));
        } else {
            query.orderBy(builder.asc(root.get("eventDate")));
        }

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);

        if (!EventState.PUBLISHED.equals(event.getState())) {
            log.warn("Published event with id={} is not found", eventId);
            throw new NotFoundException("Published event with id={}" + eventId + " not found");
        }

        sendStat(request);
        updateEventViews(event, request);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }

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
        if (start != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), start));
        }
        if (end != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        query.where(predicates.toArray(new Predicate[0]));

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    private void updateEvent(Event event, UpdateEventRequest request) {
        String annotation = request.annotation();
        if (nonNull(annotation) && !annotation.isBlank()) event.setAnnotation(request.annotation());

        if (nonNull(request.category())) {
            CategoryDto categoryDto = categoryService.getCategoryById(request.category());
            event.setCategory(CategoryMapper.toCategory(categoryDto));
        }

        String description = request.description();
        if (nonNull(description) && !description.isBlank()) event.setDescription(description);

        if (nonNull(request.eventDate())) event.setEventDate(request.eventDate());

        if (nonNull(request.location())) {
            Location location = getLocation(request.location());
            event.setLocation(location);
        }

        if (nonNull(request.paid())) event.setPaid(request.paid());

        if (nonNull(request.participantLimit())) event.setParticipantLimit(Long.valueOf(request.participantLimit()));

        if (nonNull(request.requestModeration())) event.setRequestModeration(request.requestModeration());

        if (nonNull(request.stateActionAdmin())) {
            EventState state = switch (request.stateActionAdmin()) {
                case StateActionAdmin.PUBLISH_EVENT -> EventState.PUBLISHED;
                case StateActionAdmin.REJECT_EVENT -> EventState.CANCELED;
            };
            event.setState(state);
        }

        if (nonNull(request.stateActionUser())) {
            EventState state = switch (request.stateActionUser()) {
                case StateActionUser.SEND_TO_REVIEW -> EventState.PENDING;
                case StateActionUser.CANCEL_REVIEW -> EventState.CANCELED;
            };
            event.setState(state);
        }

        String title = request.title();
        if (nonNull(title) && !title.isBlank()) event.setTitle(title);
    }

    private void validateDate(LocalDateTime eventDate, int hours) {
        if (eventDate.minusHours(hours).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot publish event that starts less than " + hours + " hour from now");
        }
    }

    private LocalDateTime parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(date, FORMATTER);
        } catch (Exception e) {
            throw new ValidationException("Неверный формат даты. Ожидается yyyy-MM-dd HH:mm:ss");
        }
    }

    private Location getLocation(LocationDto locationDto) {
        return locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(locationDto)));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with id={} was not found", eventId);
                    return new NotFoundException("Event with id={}" + eventId + " not found");
                });
    }

    private void updateEventViews(Event event, HttpServletRequest request) {
        LocalDateTime publishedOn = event.getPublishedOn();

        if (Objects.isNull(publishedOn)) publishedOn = event.getCreatedOn();

        List<StatsDto> views = statsClient.getStats(
                publishedOn,
                LocalDateTime.now(),
                List.of(request.getRequestURI()),
                true
        );

        long newViews = views.isEmpty() ? 1L : views.getFirst().getHits();

        event.setViews(newViews);
        eventRepository.save(event);
    }

    private void sendStat(HttpServletRequest request) {
        statsClient.saveHit(HitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timeStamp(LocalDateTime.now())
                .build());
    }
}
