package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Value;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.ParticipationRequestDTO.Response.ParticipationRequestDto;
import ru.practicum.dto.user.UserDTO.Response.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.StateActionUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Статический контейнер для типобезопасных DTO событий, использующий паттерн
 * "Type-Safe DTO с контрактами валидации"
 *
 * <p>Enum предоставляет пространство имён для группировки всех DTO, связанных с сущностью {@code Event}
 * и не допускает инстанцирования. Вложенные интерфейсы определяют контракты валидации полей, которые
 * переиспользуются в DTO-классах через реализацию</p>
 *
 * <h4>Преимущества подхода:</h4>
 * <ul>
 *   <li><b>Отсутствие дублирования валидаций:</b> правила валидации объявлены один раз</li>
 *   <li><b>Типобезопасность:</b> все DTO - неизменяемые классы с полной поддержкой компилятора и IDE</li>
 *   <li><b>Чистая архитектура:</b> чёткое разделение входных ({@code Request}) и выходных ({@code Response}) DTO</li>
 *   <li><b>Поддержка Spring Validation:</b> аннотации из интерфейсов корректно обрабатываются при
 *   использовании {@code @Valid}</li>
 * </ul>
 *
 * <h4>Структура:</h4>
 * <ul>
 *   <li>{@link EventDTO.HasAnnotation} - контракт для annotation: не пустой, длина от 20 до 2000 символов</li>
 *   <li>{@link EventDTO.HasDescription} - контракт для description: не пустой, длина от 20 до 7000 символов</li>
 *   <li>{@link EventDTO.HasTitle} - контракт для title: не пустой, длина от 3 до 120 символов</li>
 *   <li>{@link EventDTO.HasCategory} - контракт для category сущности: не пустой, валидация
 *   на уровне {@link CategoryDto}</li>
 *   <li>{@link EventDTO.HasCategoryId} - контракт для category id: не пустой, положительное значение</li>
 *   <li>{@link EventDTO.HasEventDate} - контракт для eventDate: не пустой, дата в настоящем или будущем,
 *   дата в формате "yyyy-MM-dd HH:mm:ss"</li>
 *   <li>{@link EventDTO.HasLocation} - контракт для location: не пустой, валидация на уровне {@link LocationDto}</li>
 *   <li>{@link EventDTO.HasPaid} - контракт для paid: не пустой</li>
 *   <li>{@link EventDTO.HasStateActionUser} - определяет тип StateActionUser</li>
 *   <li>{@link EventDTO.HasStateActionAdmin} - определяет тип HasStateActionAdmin</li>
 *   <li>{@link EventDTO.HasCreatedOn} - контракт для createdOn: дата в формате "yyyy-MM-dd HH:mm:ss"</li>
 *   <li>{@link EventDTO.HasPublishedOn} - контракт для publishedOn: дата в формате "yyyy-MM-dd HH:mm:ss"</li>
 *   <li>{@link EventDTO.HasInitiator} - контракт для initiator: валидация на уровне {@link UserShortDto}</li>
 *   <li>{@link Request.NewEventDto} - DTO для создания нового события (входной запрос)</li>
 *   <li>{@link Request.UpdateEventUserRequest} - DTO для обновления события через /users (входной запрос)</li>
 *   <li>{@link Request.UpdateEventAdminRequest} - DTO для обновления события через /admin (входной запрос)</li>
 *   <li>{@link Request.EventRequestStatusUpdateRequest} - DTO для изменения статуса запроса на участие
 *   в событии (входной запрос)</li>
 *   <li>{@link Response.EventShortDto} - краткое представление события</li>
 *   <li>{@link Response.EventFullDto} - полное представление события</li>
 *   <li>{@link Response.EventRequestStatusUpdateResult} - результат подтверждения/отклонения заявок на участие
 *   в событии</li>
 * </ul>
 *
 * <h4>Пример использования:</h4>
 * <pre>
 * {@code
 *      // В контроллере
 *      @GetMapping("/events/{id}")
 *      public EventDTO.Response.EventShortDto getEvent(@PathVariable Long id) {
 *          return eventService.getEvent(id);
 *      }
 * }
 * </pre>
 *
 * <p>Подробнее в статье: <a href="https://habr.com/ru/articles/513072/">Переосмысление DTO в Java</a>
 *
 * @see jakarta.validation.constraints
 * @see jakarta.validation.Valid
 * @see lombok.Value
 */
public enum EventDTO {
    ;

    // --- Контракты валидации (миксин-интерфейсы) ---------------------------------------------------------------------

    interface HasAnnotation {
        @NotBlank(message = "Annotation cannot be empty")
        @Size(min = 20, max = 2000, message = "Annotation must be from {min} to {max} characters")
        String getAnnotation();
    }

    interface HasDescription {
        @NotBlank(message = "Description cannot be empty")
        @Size(min = 20, max = 7000, message = "Description must be from {min} to {max} characters")
        String getDescription();
    }

    interface HasTitle {
        @NotBlank(message = "Title cannot be empty")
        @Size(min = 3, max = 120, message = "Title must be from {min} to {max} characters")
        String getTitle();
    }

    interface HasCategory {
        @NotNull(message = "Category cannot be null")
        @Valid
        CategoryDto getCategory();
    }

    interface HasCategoryId {
        @NotNull(message = "Category Id cannot be null")
        @Positive(message = "Category Id must be positive")
        Long getCategory();
    }

    interface HasEventDate {
        @NotNull(message = "Event date cannot be null")
        @FutureOrPresent(message = "Event date must be in the future or present")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getEventDate();
    }

    interface HasLocation {
        @NotNull(message = "Location cannot be null")
        @Valid
        LocationDto getLocation();
    }

    interface HasPaid {
        @NotNull(message = "Paid flag cannot be null")
        Boolean getPaid();
    }

    interface HasStateActionUser {
        StateActionUser getStateAction();
    }

    interface HasStateActionAdmin {
        StateActionAdmin getStateAction();
    }

    interface HasCreatedOn {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getCreatedOn();
    }

    interface HasPublishedOn {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getPublishedOn();
    }

    interface HasInitiator {
        @Valid
        UserShortDto getInitiator();
    }

    interface HasParticipantLimit {
        Long getParticipantLimit();
    }

    interface HasRequestModeration {
        Boolean getRequestModeration();
    }


    // --- Входящие DTO: Request ---------------------------------------------------------------------------------------

    public enum Request {
        ;

        @Value
        public static class NewEventDto implements
                HasAnnotation,
                HasCategoryId,
                HasDescription,
                HasEventDate,
                HasLocation,
                HasPaid,
                HasTitle,
                HasParticipantLimit,
                HasRequestModeration {

            String annotation;
            Long category;
            String description;
            LocalDateTime eventDate;
            LocationDto location;
            Boolean paid;
            Long participantLimit;
            Boolean requestModeration;
            String title;
        }

        @Value
        public static class UpdateEventUserRequest implements
                HasAnnotation,
                HasCategoryId,
                HasDescription,
                HasEventDate,
                HasLocation,
                HasPaid,
                HasStateActionUser,
                HasParticipantLimit,
                HasRequestModeration,
                HasTitle {

            String annotation;
            Long category;
            String description;
            LocalDateTime eventDate;
            LocationDto location;
            Boolean paid;
            Long participantLimit;
            Boolean requestModeration;
            StateActionUser stateAction;
            String title;
        }

        @Value
        public static class UpdateEventAdminRequest implements
                HasAnnotation,
                HasCategoryId,
                HasDescription,
                HasEventDate,
                HasLocation,
                HasPaid,
                HasStateActionAdmin,
                HasTitle {

            String annotation;
            Long category;
            String description;
            LocalDateTime eventDate;
            LocationDto location;
            Boolean paid;
            Long participantLimit;
            Boolean requestModeration;
            StateActionAdmin stateAction;
            String title;

        }

        @Value
        public static class EventRequestStatusUpdateRequest {
            List<Long> requestIds;
            RequestStatus status;
        }
    }


    // --- Исходящие DTO: Response -------------------------------------------------------------------------------------

    public enum Response {
        ;

        @Value
        public static class EventShortDto implements
                HasAnnotation,
                HasCategory,
                HasEventDate,
                HasInitiator,
                HasPaid,
                HasTitle {

            Long id;
            String annotation;
            CategoryDto category;
            Long confirmedRequests;
            LocalDateTime eventDate;
            UserShortDto initiator;
            Boolean paid;
            String title;
            Long views;
        }

        @Value
        public static class EventFullDto implements
                HasAnnotation,
                HasCategory,
                HasCreatedOn,
                HasDescription,
                HasEventDate,
                HasInitiator,
                HasLocation,
                HasPaid,
                HasPublishedOn,
                HasTitle {

            Long id;
            String annotation;
            CategoryDto category;
            Long confirmedRequests;
            LocalDateTime createdOn;
            String description;
            LocalDateTime eventDate;
            UserShortDto initiator;
            LocationDto location;
            Boolean paid;
            Long participantLimit;
            LocalDateTime publishedOn;
            Boolean requestModeration;
            EventState state;
            String title;
            Long views;
        }

        @Value
        public static class EventRequestStatusUpdateResult {
            List<ParticipationRequestDto> confirmedRequests;
            List<ParticipationRequestDto> rejectedRequests;
        }
    }
}