package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.UpdateEventUserDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.EventException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.WrongTimeException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public EventDto createEvent(EventDto event, Long userId) {
        userService.getUser(userId);
        CategoryDto categoryDto = categoryService.getCategoryById(event.getCategoryId());

        validateTime(event.getEventDate());

        return EventMapper.toEventDto(eventRepository.save(EventMapper.toEvent(event, CategoryMapper.toCategory(categoryDto))));
    }

    public FullEventDto updateEvent(Long userId, Long eventId, UpdateEventUserDto event) {
        userService.getUser(userId);
        Event eventToUpdate = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("События с id: " + eventId + " не существует."));

        if (eventToUpdate.getState() == EventState.PUBLISHED) {
            throw new EventException("Событие уже опубликовано.");
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
            validateTime(event.getEventDate());
            eventToUpdate.setEventDate(event.getEventDate());
        }
        if (event.getCategoryId() != null) {
            eventToUpdate.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(event.getCategoryId())));
        }

        return EventMapper.toFullEventDto(eventToUpdate);
    }

    public FullEventDto updateStatusOfEvent(Long userId, Long eventId) {

    }

    @Transactional(readOnly = true)
    public EventDto getEvent(Long userId, Long eventId) {

    }

    @Transactional(readOnly = true)
    public List<EventDto> getEvents(Long userId) {

    }

    @Transactional(readOnly = true)
    public List<EventDto> getEventsWithUserParticipating(Long userId, Long eventId) {

    }

    private void validateTime(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new WrongTimeException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }
    }

}