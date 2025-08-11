package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Класс {@code ParticipationRequest} - сущность заявки на участие в событии
 *
 * <p>Аннотации {@code @Getter} и {@code @ToString} автоматически генерируют соответствующие методы.
 * Аннотация {@code @EqualsAndHashCode(onlyExplicitlyIncluded = true)} генерирует методы equals() и hashCode(),
 * параметр указывает, что в методах необходимо учитывать только поля, помеченные аннотацией
 * {@code @EqualsAndHashCode.Include}. Аннотация {@code @NoArgsConstructor} создает конструктор по умолчанию,
 * необходимый для JPA и сериализации/десериализации объектов JSON. Аннотация {@code @Enumerated(EnumType.STRING)}
 * указывает как сохранить поле в БД и с каким типом. Аннотация {@code @ManyToOne(fetch = FetchType.LAZY)} описывает
 * ассоциативную связь между сущностями и указывает, что множество экземпляров этой сущности могут ссылаться на один
 * экземпляр целевой сущности, параметр указывает, что помеченный объект не будет загружаться сразу при загрузке.
 * Он будет загружен по требованию (ленивая загрузка)</p>
 *
 * <p>Аннотацию {@code @Column} можно не указывать, если имя поля в сущности совпадает с именем
 * столбца в таблице (прямо или с учетом правил именования JPA, например, camelCase -> snake_case),
 * а также если не требуется дополнительная конфигурация (nullable, unique и т.д.). {@code @JoinColumn}
 * (для связей) тоже можно опускать, если имя столбца формируется по умолчанию (например, booker -> booker_id)</p>
 *
 * <p>Поля класса:</p>
 * <ul>
 *   <li>{@code id} - уникальный идентификатор заявки</li>
 *   <li>{@code created} - дата и время создания заявки</li>
 *   <li>{@code event} - идентификатор события (не может быть null)</li>
 *   <li>{@code requester} - идентификатор пользователя, отправившего заявку (не может быть null)</li>
 *   <li>{@code status} - статус заявки (не может быть null, значение по умолчанию ApplicationStatus.PENDING)</li>
 * </ul>
 */

@Entity
@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participation_requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;


    public void confirm() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Request must be PENDING to be confirmed");
        }
        this.status = ApplicationStatus.CONFIRMED;
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}
