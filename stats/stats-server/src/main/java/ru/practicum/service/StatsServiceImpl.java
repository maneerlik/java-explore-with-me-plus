package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public HitDto create(HitDto hitDto) {
        log.info("Сохранение информации о просмотре: {}", hitDto);
        Hit hit = HitMapper.toHit(hitDto);
        Hit savedHit = statsRepository.save(hit);
        return HitMapper.toHitDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            log.warn("Ошибка валидации: дата начала {} позже даты окончания {}", start, end);
            throw new ValidationException("Дата начала диапазона не может быть позже даты окончания.");
        }

        log.info("Запрос статистики: unique={}, uris={}", unique, uris);

        if (unique) {
            log.debug("Вызов getStatsUnique");
            return statsRepository.getStatsUnique(start, end, uris);
        } else {
            log.debug("Вызов getStatsAll");
            return statsRepository.getStatsAll(start, end, uris);
        }
    }
}