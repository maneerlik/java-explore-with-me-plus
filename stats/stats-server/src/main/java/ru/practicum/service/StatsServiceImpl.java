package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
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
    public Collection<StatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Getting statistics from: {}, to: {} (URIs: {}, unique {})", start, end, uris, unique);

        LocalDateTime startTime = parseDate(start);
        LocalDateTime endTime = parseDate(end);
        if (startTime.isAfter(endTime)) throw new ValidationException("Start date should be before end");

        return unique
                ? statsRepository.findUniqueStatsByUrisAndTimestampBetween(startTime, endTime, uris)
                : statsRepository.findStatsByUrisAndTimestampBetween(startTime, endTime, uris);
    }


    private LocalDateTime parseDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }
}
