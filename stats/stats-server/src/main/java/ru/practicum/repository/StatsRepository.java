package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с {@code Hit}
 *
 * <p>Расширяет {@link JpaRepository}. Предоставляет стандартные
 * CRUD-операции для сущности запроса</p>
 */

public interface StatsRepository extends JpaRepository<Hit, Long> {
    /**
     * Поиск всех посещений в интервале между {@code start} и
     * {@code end} ресурсов из списка {@code uris} с учетом
     * уникальности IP
     *
     * @param start дата и время начала диапазона за который нужно выгрузить статистику
     * @param end дата и время конца диапазона за который нужно выгрузить статистику
     * @param uris список uri для которых нужно выгрузить статистику
     * @return статистика уникальных посещений
     */
    @Query(value = """
        SELECT new ru.practicum.StatsDto(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM Hit h
        WHERE h.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
    """)
    List<StatsDto> findUniqueStatsByUrisAndTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    /**
     * Поиск всех посещений в интервале между {@code start} и
     * {@code end} ресурсов из списка {@code uris} с учетом
     * уникальности IP
     *
     * @param start дата и время начала диапазона за который нужно выгрузить статистику
     * @param end дата и время конца диапазона за который нужно выгрузить статистику
     * @param uris список uri для которых нужно выгрузить статистику
     * @return статистика по всем посещениям
     */
    @Query(value = """
        SELECT new ru.practicum.StatsDto(h.app, h.uri, COUNT(h.ip))
        FROM Hit h
        WHERE h.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
    """)
    List<StatsDto> findStatsByUrisAndTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}
