package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String formattedStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formattedEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return restClient.get()
                .uri(uriBuilder -> {
                    log.info("url before /stats: " + url);
                    uriBuilder.path("/stats")
                            .queryParam("start", formattedStart)
                            .queryParam("end", formattedEnd);

                    if (uris != null && !uris.isEmpty()) {
                        uriBuilder.queryParam("uris", String.join(",", uris));
                    }

                    return uriBuilder.queryParam("unique", unique).build();
                })
                .retrieve()
                .toEntity(Object.class);
    }

}