package ru.practicum;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatusCode;

@Service
@AllArgsConstructor
public class StatsClient {
    private final RestClient restClient;
    private final String url;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        restClient = RestClient.create();
        url = serverUrl;
    }

    public ResponseEntity<Object> saveHit(HitDto hitDto) {
        return restClient.post()
                .uri(url + "/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("StatsService error: " + res.getStatusText());
                })
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> getStats() {
        return restClient.get()
                .uri(url + "/stats")
                .retrieve()
                .toEntity(Object.class);
    }

}
