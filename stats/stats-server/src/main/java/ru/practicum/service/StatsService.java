package ru.practicum.service;

import java.util.Collection;
import java.util.List;

public interface StatsService {
    HitDto create(HitDto hitDto);

    Collection<StatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
