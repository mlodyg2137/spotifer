package pl.mlodyg.spotifer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.mlodyg.spotifer.dto.ArtistDto;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.externals.artist.ArtistExternal;
import pl.mlodyg.spotifer.externals.track.TrackExternal;
import pl.mlodyg.spotifer.mappers.ArtistMapper;
import pl.mlodyg.spotifer.mappers.TrackMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService {

    @Autowired
    @Qualifier("spotifyClientCredWebClient")
    private final WebClient spotifyClientCredWebClient;

    @Autowired
    @Qualifier("spotifyAuthCodeWebClient")
    private final WebClient spotifyAuthCodeWebClient;

    SpotifyService(WebClient spotifyClientCredWebClient,
                   WebClient spotifyAuthCodeWebClient) {
        this.spotifyClientCredWebClient = spotifyClientCredWebClient;
        this.spotifyAuthCodeWebClient = spotifyAuthCodeWebClient;
    }

    public Map<String, Object> searchTracks(String q, int limit) {
        try {
            return spotifyClientCredWebClient.get()
                    .uri(uri -> uri.path("/v1/search")
                            .queryParam("q", q)
                            .queryParam("type", "track")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, resp -> resp.createException().flatMap(e -> {
                        // Możesz odczytać Retry-After i zadecydować co dalej
                        return reactor.core.publisher.Mono.error(e);
                    }))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}) // TODO: Utwórz DTO zamiast ParameterizedTypeReference (czyli tu bedzie List<TrackDto>)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (WebClientResponseException e) {
            throw e;
        }
    }

    public List<ArtistDto> myTopArtists(int limit, String time_range, int offset) {
        ArtistExternal responseJson = spotifyAuthCodeWebClient.get()
                .uri(uri -> uri.path("v1/me/top/artists")
                        .queryParam("limit", limit)
                        .queryParam("time_range", time_range)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(ArtistExternal.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        return ArtistMapper.toDtos(responseJson.getItems());
    }

    public List<TrackDto> myTopTracks(int limit, String time_range, int offset) {
        TrackExternal responseJson = spotifyAuthCodeWebClient.get()
                .uri(uri -> uri.path("v1/me/top/tracks")
                        .queryParam("limit", limit)
                        .queryParam("time_range", time_range)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(TrackExternal.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        return TrackMapper.toDtos(responseJson.getItems());
    }

    public List<TrackDto> myRecentlyPlayed(int limit, String after, String before) {

        if (after != null && before != null) {
            throw new IllegalArgumentException("Cannot specify both 'after' and 'before' parameters.");
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));

        TrackExternal responseJson = spotifyAuthCodeWebClient.get()
                .uri(uri -> {
                    var b = uri.path("v1/me/player/recently-played")
                            .queryParam("limit", safeLimit);
                    if (after != null && !after.isBlank()) {
                        b.queryParam("after", after);
                    }
                    if (before != null && !before.isBlank()) {
                        b.queryParam("before", before);
                    }
                    return b.build();
                })
                .retrieve()
                .bodyToMono(TrackExternal.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        return TrackMapper.toDtos(responseJson.getItems());
    }
}
