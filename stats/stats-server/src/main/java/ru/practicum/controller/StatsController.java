package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.service.StatsService;

import java.util.Collection;
import java.util.List;

/**
 * Контроллер для обработки HTTP-запросов, связанных с обработкой статистики
 * посещения сервиса. Обеспечивает взаимодействие между клиентом и сервисным слоем
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;


    /**
     * Создать запрос
     *
     * @param hitDto объект, содержащий данные о новом запросе
     *
     * @return HitDto объект созданного запроса
     */
    @PostMapping("/hit")
    public HitDto createHit(@RequestBody @Valid HitDto hitDto) {
        log.info("Creating hit {} in the service", hitDto);
        return statsService.create(hitDto);
    }

    /**
     * Получить статистику посещений
     *
     * @param start дата и время начала диапазона за который нужно выгрузить статистику
     * @param end дата и время конца диапазона за который нужно выгрузить статистику
     * @param uris список uri для которых нужно выгрузить статистику
     * @param unique учитывать только уникальные посещения (только с уникальным ip)
     *
     * @return Collection<StatsDto> список с результатом выборки
     */
    @GetMapping("/stats")
    public Collection<StatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @NotNull String start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @NotNull String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique
    ) {
        log.info("Getting stats from parameters: start={}; end={}; uris={}; unique={}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}
