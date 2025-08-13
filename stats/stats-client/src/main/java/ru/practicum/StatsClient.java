package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StatsClient {
    private final RestClient restClient;


    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        log.info("Initializing StatsClient with URL: {}", serverUrl);
        this.restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public HitDto saveHit(HitDto hitDto) {
        return restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("Error saving hit to StatsService: {} {}",
                            response.getStatusCode(),
                            response.getStatusText()
                    );
                    throw new RuntimeException("StatsService error while saving hit: " + response.getStatusText());
                })
                .body(HitDto.class);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String formattedStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formattedEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/stats")
                            .queryParam("start", formattedStart)
                            .queryParam("end", formattedEnd);

                    if (uris != null && !uris.isEmpty()) uris.forEach(uri -> uriBuilder.queryParam("uris", uri));
                    if (unique != null) uriBuilder.queryParam("unique", unique);

                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("Error fetching stats from StatsService: {} {}",
                            response.getStatusCode(),
                            response.getStatusText()
                    );
                    throw new RuntimeException("StatsService error while getting stats: " + response.getStatusText());
                })
                .body(new ParameterizedTypeReference<>() {});
    }
}
