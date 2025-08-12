package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import ru.practicum.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Статический контейнер для типобезопасных DTO заявок на участие, использующий паттерн
 * "Type-Safe DTO с контрактами валидации"
 *
 * <p>Enum предоставляет пространство имён для группировки всех DTO, связанных с сущностью {@code ParticipationRequest}
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
 *   <li>{@link ParticipationRequestDTO.HasCreated} - контракт для created: тип для сериализации даты - String,
 *   форматирование - yyyy-MM-dd'T'HH:mm:ss.SSS</li>
 *   <li>{@link Request} - пустой контейнер, не предусмотрено текущей спецификацией</li>
 *   <li>{@link Response.ParticipationRequestDto} - полное представление заявки</li>
 * </ul>
 *
 * <h4>Пример использования:</h4>
 * <pre>
 * {@code
 *      // В контроллере
 *      @PostMapping("/requests")
 *      public ParticipationRequestDTO.Response.ParticipationRequestDto createRequest(
 *          @Valid @RequestBody ParticipationRequestDTO.Response.ParticipationRequestDto participationRequestDto
 *      ) {
 *          log.info("Creating request {}", participationRequestDto);
 *          return requestService.createRequest(participationRequestDto);
 *      }
 * }
 * </pre>
 *
 * <p>Подробнее в статье: <a href="https://habr.com/ru/articles/513072/">Переосмысление DTO в Java</a>
 *
 * @see com.fasterxml.jackson.annotation.JsonFormat
 * @see jakarta.validation.constraints
 * @see jakarta.validation.Valid
 * @see lombok.Value
 */
public enum ParticipationRequestDTO {
    ;

    // --- Контракты валидации (миксин-интерфейсы) ---------------------------------------------------------------------

    private interface HasCreated {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime getCreated();
    }


    // --- Входящие DTO: Request ---------------------------------------------------------------------------------------

    public enum Request {
        ;
    }


    // --- Исходящие DTO: Response -------------------------------------------------------------------------------------

    public enum Response {
        ;

        @Value
        @Builder
        public static class ParticipationRequestDto implements HasCreated {
            Long id;
            LocalDateTime created;
            Long event;
            Long requester;
            RequestStatus status;
        }
    }
}