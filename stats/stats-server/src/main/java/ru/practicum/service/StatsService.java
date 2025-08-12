package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsService {
    HitDto create(HitDto hitDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}