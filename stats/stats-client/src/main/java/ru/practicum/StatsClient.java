package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class StatsClient {
    private final RestClient restClient;
    private final String url;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        log.info("url: " + serverUrl);
        restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
        url = serverUrl;
    }

    public ResponseEntity<Object> saveHit(HitDto hitDto) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/hit").build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("StatsService error: " + res.getStatusText());
                })
                .toEntity(Object.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String formattedStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formattedEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ResponseEntity<List<ViewStatsDto>> response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/stats")
                            .queryParam("start", formattedStart)
                            .queryParam("end", formattedEnd)
                            .queryParam("unique", unique);

                    if (uris != null && !uris.isEmpty()) {
                        uriBuilder.queryParam("uris", String.join(",", uris));
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<ViewStatsDto>>() {});

        return response.getBody();
    }
}