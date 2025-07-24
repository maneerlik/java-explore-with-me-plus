package ru.practicum;

import lombok.experimental.UtilityClass;
import ru.practicum.model.EndpointHit;

@UtilityClass
public class HitMapper {

    public static EndpointHit toEndpointHit(HitDto hitDto) {
        if (hitDto == null) {
            return null;
        }
        return EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }

    public static HitDto toHitDto(EndpointHit endpointHit) {
        if (endpointHit == null) {
            return null;
        }
        return HitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }
}