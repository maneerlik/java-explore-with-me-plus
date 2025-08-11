package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventDTO.Request.NewEventDto;
import ru.practicum.dto.event.EventDTO.Response.EventFullDto;
import ru.practicum.dto.event.EventDTO.Response.EventShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortValue;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.StateActionUser;
import ru.practicum.exception.EventException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.WrongTimeException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.user.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EntityManager entityManager;


    @Override
    public EventShortDto createEvent(NewEventDto event, Long userId) {
        User user = userService.getUserEntity(userId);
        CategoryDto categoryDto = categoryService.getCategoryById(event.getCategory());

        validateTime(event.getEventDate(), 2);

        return EventMapper.toEventShortDto(eventRepository.save(EventMapper.toEvent(event, CategoryMapper.toCategory(categoryDto), user)));
    }

    @Override
    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(eventId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не найдено."));
        event.setViews(event.getViews() + 1);
        return EventMapper.toFullEventDto(event);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        userService.getUser(userId);

        return EventMapper.toFullEventDto(eventRepository.findByEventIdAndUserId(eventId, userId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не существует.")));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return EventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, page).toList());
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            if (rangeStart != null && !rangeStart.isBlank()) {
                start = LocalDateTime.parse(rangeStart, formatter);
            }
            if (rangeEnd != null && !rangeEnd.isBlank()) {
                end = LocalDateTime.parse(rangeEnd, formatter);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Ожидается yyyy-MM-dd HH:mm:ss", e);
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        Predicate criteria = builder.conjunction();

        if (categoriesId != null && !categoriesId.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").get("id").in(categoriesId));
        }

        if (users != null && !users.isEmpty()) {
            criteria = builder.and(criteria, root.get("initiator").get("id").in(users));
        }

        if (states != null) {
            criteria = builder.and(criteria, root.get("state").in(states));
        }

        if (start != null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), start));
        }

        if (end != null) {
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        query.select(root).where(criteria);

        TypedQuery<Event> typedQuery = entityManager.createQuery(query);

        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        List<Event> events = typedQuery.getResultList();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        return EventMapper.toEventFullDtoList(events);
    }

    @Override
    public List<EventShortDto> getEventsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable, SortValue sort,
                                               Integer from, Integer size) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null) {
            Predicate annotationContain = builder.like(builder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = builder.like(builder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containText = builder.or(annotationContain, descriptionContain);

            criteria = builder.and(criteria, containText);
        }

        if (categories != null && !categories.isEmpty()) {
            Predicate containStates = root.get("category").get("id").in(categories);
            criteria = builder.and(criteria, containStates);
        }

        if (paid != null) {
            Predicate isPaid;
            if (paid) {
                isPaid = builder.isTrue(root.get("paid"));
            } else {
                isPaid = builder.isFalse(root.get("paid"));
            }
            criteria = builder.and(criteria, isPaid);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria).orderBy(builder.asc(root.get("eventDate")));
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (onlyAvailable) {
            events = events.stream()
                    .filter((event -> event.getConfirmedRequests() < (long) event.getParticipantLimit()))
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            if (sort.equals(SortValue.EVENT_DATE)) {
                events = events.stream().sorted(Comparator.comparing(Event::getEventDate)).collect(Collectors.toList());
            } else {
                events = events.stream().sorted(Comparator.comparing(Event::getViews)).collect(Collectors.toList());
            }
        }

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        return EventMapper.toEventShortDtoList(events);
    }

    @Override
    public FullEventDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest event) {
        userService.getUser(userId);
        Event eventToUpdate = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не существует."));

        if (eventToUpdate.getState() == EventState.PUBLISHED) {
            throw new EventException("Событие уже опубликовано.");
        }
        if (event == null) {
            return EventMapper.toFullEventDto(eventToUpdate);
        }
        if (event.getAnnotation() != null) {
            eventToUpdate.setAnnotation(event.getAnnotation());
        }
        if (event.getTitle() != null) {
            eventToUpdate.setTitle(event.getTitle());
        }
        if (event.getDescription() != null) {
            eventToUpdate.setDescription(event.getDescription());
        }
        if (event.getLocation() != null) {
            eventToUpdate.setLocation(event.getLocation());
        }
        if (event.getPaid() != null) {
            eventToUpdate.setPaid(event.getPaid());
        }
        if (event.getParticipantLimit() != null) {
            eventToUpdate.setParticipantLimit(event.getParticipantLimit());
        }
        if (event.getRequestModeration() != null) {
            eventToUpdate.setRequestModeration(event.getRequestModeration());
        }
        if (event.getEventDate() != null) {
            validateTime(event.getEventDate(), 2);
            eventToUpdate.setEventDate(event.getEventDate());
        }
        if (event.getCategoryId() != null) {
            eventToUpdate.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(event.getCategoryId())));
        }

        if (event.getStateAction() != null) {
            if (event.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                eventToUpdate.setState(EventState.PENDING);
            } else {
                eventToUpdate.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toFullEventDto(eventRepository.save(eventToUpdate));
    }

    @Override
    public FullEventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest event) {
        Event eventToUpdate = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не существует."));

        if (event == null) {
            return EventMapper.toFullEventDto(eventToUpdate);
        }
        if (event.getAnnotation() != null) {
            eventToUpdate.setAnnotation(event.getAnnotation());
        }
        if (event.getTitle() != null) {
            eventToUpdate.setTitle(event.getTitle());
        }
        if (event.getDescription() != null) {
            eventToUpdate.setDescription(event.getDescription());
        }
        if (event.getLocation() != null) {
            eventToUpdate.setLocation(event.getLocation());
        }
        if (event.getPaid() != null) {
            eventToUpdate.setPaid(event.getPaid());
        }
        if (event.getParticipantLimit() != null) {
            eventToUpdate.setParticipantLimit(event.getParticipantLimit());
        }
        if (event.getRequestModeration() != null) {
            eventToUpdate.setRequestModeration(event.getRequestModeration());
        }
        if (event.getEventDate() != null) {
            validateTime(event.getEventDate(), 1);
            eventToUpdate.setEventDate(event.getEventDate());
        }
        if (event.getCategoryId() != null) {
            eventToUpdate.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(event.getCategoryId())));
        }

        if (event.getStateAction() != null) {
            if (event.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                if (eventToUpdate.getPublishedOn() != null) {
                    throw new EventException("Событие уже опубликовано.");
                }
                if (eventToUpdate.getState().equals(EventState.CANCELED)) {
                    throw new EventException("Событие уже закрыто.");
                }
                eventToUpdate.setState(EventState.PUBLISHED);
                eventToUpdate.setPublishedOn(LocalDateTime.now());
            } else if (event.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
                if (eventToUpdate.getPublishedOn() != null) {
                    throw new EventException("Событие уже опубликовано.");
                }
                eventToUpdate.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toFullEventDto(eventRepository.save(eventToUpdate));
    }

    private void validateTime(LocalDateTime eventDate, Integer hours) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new WrongTimeException("Дата и время на которые намечено событие не может быть раньше, чем через " + hours + " час(а) от текущего момента");
        }
    }
}
