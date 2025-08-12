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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.mapper.HitMapper.toHitDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public HitDto create(HitDto hitDto) {
        log.info("Creating hit: {}", hitDto);
        Hit hit = HitMapper.toHit(hitDto);
        Hit savedHit = statsRepository.save(hit);
        log.info("Created hit: {}", savedHit);
        return toHitDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            log.warn("Ошибка валидации: дата начала {} позже даты окончания {}", start, end);
            throw new ValidationException("Дата начала диапазона не может быть позже даты окончания.");
        }

        boolean isUriFilterActive = uris != null && !uris.isEmpty();

        log.info("Запрос статистики: unique={}, uris active={}, uris={}", unique, isUriFilterActive, uris);

        if (unique) {
            if (isUriFilterActive) {
                log.debug("Вызов getStatsUniqueIpForUris");
                return statsRepository.getStatsUniqueIpForUris(start, end, uris);
            } else {
                log.debug("Вызов getStatsUniqueIp");
                return statsRepository.getStatsUniqueIp(start, end);
            }
        } else {
            if (isUriFilterActive) {
                log.debug("Вызов getStatsAllForUris");
                return statsRepository.getStatsAllForUris(start, end, uris);
            } else {
                log.debug("Вызов getStatsAll");
                return statsRepository.getStatsAll(start, end);
            }
        }
    }

    private LocalDateTime parseDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }
}