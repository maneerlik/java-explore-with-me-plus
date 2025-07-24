package ru.practicum;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO для вывода статистики по эндпоинту.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    private String app;

    /**
     * URI эндпоинта.
     */
    private String uri;

    private Long hits;
}