package ru.practicum.dto.user;

import lombok.Value;
import jakarta.validation.constraints.*;

/**
 * Статический контейнер для типобезопасных DTO пользователей, использующий паттерн
 * "Type-Safe DTO с контрактами валидации"
 *
 * <p>Enum предоставляет пространство имён для группировки всех DTO, связанных с сущностью {@code User}
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
 *   <li>{@link HasId} - контракт для id: не пустой</li>
 *   <li>{@link HasEmail} - контракт для email: не пустой, формат корректен (содержит @ и корректный домен),
 *   длина от 6 до 254 символов</li>
 *   <li>{@link HasName} - контракт для name: не пустое, длина от 2 до 250 символов</li>
 *   <li>{@link Request.NewUserRequest} - DTO для создания нового пользователя (входной запрос)</li>
 *   <li>{@link Response.UserDto} - полное представление пользователя (включает email)</li>
 *   <li>{@link Response.UserShortDto} - краткое представление пользователя (без email)</li>
 * </ul>
 *
 * <h4>Пример использования:</h4>
 * <pre>
 * {@code
 *      // В контроллере
 *      @PostMapping("/users")
 *      public UserDTO.Response.UserDto createUser(@Valid @RequestBody UserDTO.Request.NewUserRequest newUserRequest) {
 *          log.info("Creating user {}", newUserRequest);
 *          return userService.createUser(newUserRequest);
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
public enum UserDTO {
    ;

    // --- Контракты валидации (миксин-интерфейсы) ---------------------------------------------------------------------

    private interface HasId {
        @NotNull(message = "Id cannot be empty")
        Long getId();
    }

    private interface HasEmail {
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must contain '@' and a valid domain name")
        @Size(min = 6, max = 254, message = "Email must be from {min} to {max} characters")
        String getEmail();
    }

    private interface HasName {
        @NotBlank(message = "Name cannot be empty")
        @Size(min = 2, max = 250, message = "Name must be from {min} to {max} characters")
        String getName();
    }


    // --- Входящие DTO: Request ---------------------------------------------------------------------------------------

    public enum Request {
        ;

        @Value
        public static class NewUserRequest implements HasEmail, HasName {
            String email;
            String name;
        }
    }


    // --- Исходящие DTO: Response -------------------------------------------------------------------------------------

    public enum Response {
        ;

        @Value
        public static class UserDto implements HasEmail, HasName {
            Long id;
            String email;
            String name;
        }

        @Value
        public static class UserShortDto implements HasId, HasName {
            Long id;
            String name;
        }
    }
}
