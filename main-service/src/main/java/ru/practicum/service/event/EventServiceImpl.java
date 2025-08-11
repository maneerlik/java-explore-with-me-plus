package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Location;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.dto.event.EventDTO.Request.NewEventDto;
import ru.practicum.dto.event.EventDTO.Request.UpdateEventAdminRequest;
import ru.practicum.dto.event.EventDTO.Request.UpdateEventUserRequest;
import ru.practicum.dto.event.EventDTO.Response.EventFullDto;
import ru.practicum.dto.event.EventDTO.Response.EventShortDto;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EntityManager entityManager;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        validateEventDate(newEventDto.getEventDate(), 2);

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
        return EventMapper.toFullEventDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " и инициатором ID=" + userId + " не найдено."));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить уже опубликованное событие.");
        }

        updateEventFields(event, updateRequest.getAnnotation(), updateRequest.getCategory(),
                updateRequest.getDescription(), updateRequest.getEventDate(),
                updateRequest.getLocation(), updateRequest.getPaid(),
                updateRequest.getParticipantLimit(), updateRequest.getRequestModeration(),
                updateRequest.getTitle());

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == StateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toFullEventDto(eventRepository.save(event));
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

        updateEventFields(event, updateRequest.getAnnotation(), updateRequest.getCategory(),
                updateRequest.getDescription(), updateRequest.getEventDate(),
                updateRequest.getLocation(), updateRequest.getPaid(),
                updateRequest.getParticipantLimit(), updateRequest.getRequestModeration(),
                updateRequest.getTitle());

        return EventMapper.toFullEventDto(eventRepository.save(event));
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
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с ID=" + eventId + " не найдено."));

        event.setViews(event.getViews() + 1L);
        eventRepository.save(event);

        return EventMapper.toFullEventDto(event);
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

        return events.stream().map(EventMapper::toFullEventDto).collect(Collectors.toList());
    }

    private void updateEventFields(Event event, String annotation, Long categoryId, String description,
                                   LocalDateTime eventDate, LocationDto locationDto, Boolean paid,
                                   Integer participantLimit, Boolean requestModeration, String title) {
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория с ID=" + categoryId + " не найдена."));
            event.setCategory(category);
        }
        if (description != null && !description.isBlank()) {
            event.setDescription(description);
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
        if (locationDto != null) {
            Location location = getLocation(locationDto);
            event.setLocation(location);
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(Long.valueOf(participantLimit));
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void validateEventDate(LocalDateTime eventDate, int hours) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new ValidationException("Дата события должна быть как минимум через " + hours + " часа от текущего момента.");
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
}