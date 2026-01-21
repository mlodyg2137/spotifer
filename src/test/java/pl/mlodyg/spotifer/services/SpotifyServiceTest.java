package pl.mlodyg.spotifer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import pl.mlodyg.spotifer.dto.ArtistDto;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.dto.TrackRecentlyPlayedDto;
import pl.mlodyg.spotifer.externals.artist.ArtistExternal;
import pl.mlodyg.spotifer.externals.track.TrackExternal;
import pl.mlodyg.spotifer.externals.trackrecentlyplayed.TrackRecentlyPlayedExternal;
import pl.mlodyg.spotifer.mappers.ArtistMapper;
import pl.mlodyg.spotifer.mappers.TrackMapper;
import pl.mlodyg.spotifer.mappers.TrackRecentlyPlayedMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock
    private WebClient spotifyClientCredWebClient;
    @Mock
    private WebClient spotifyAuthCodeWebClient;
    @Mock
    private WebClient spotifyUserWebClient;

    private SpotifyService spotifyService;

    @BeforeEach
    void setup() {
        spotifyService = new SpotifyService(
                spotifyClientCredWebClient,
                spotifyAuthCodeWebClient,
                spotifyUserWebClient
        );
    }

    // Helper: buduje URI z tego samego typu UriBuilder, którego używa WebClient
    private URI buildUri(Function<UriBuilder, URI> fn) {
        DefaultUriBuilderFactory f = new DefaultUriBuilderFactory("http://example.com/");
        return fn.apply(f.builder());
    }

    // Helper: mock chain GET -> uri(fn) -> retrieve() -> bodyToMono(clazz/ref) -> timeout -> block
    private <T> void mockGetChain(WebClient client, Class<T> clazz, T responseObj) {
//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(client.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(eq(clazz))).thenReturn(Mono.just(responseObj));
    }

    private void mockGetChainParameterized(WebClient client, Map<String, Object> responseMap) {
//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(client.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);

        // searchTracks() używa ParameterizedTypeReference<Map<String,Object>>
        when(resp.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseMap));

        // onStatus(...) jest w chainie; Mockito i tak to przełknie, bo jest na typie ResponseSpec.
        when(resp.onStatus(any(), any())).thenReturn(resp);
    }

    // ========================= myRecentlyPlayed =========================

    @Test
    void myRecentlyPlayed_whenAfterAndBeforeProvided_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> spotifyService.myRecentlyPlayed(10, "123", "456"));
    }

    @Test
    void myRecentlyPlayed_shouldClampLimitToMin1_andNotIncludeAfterBeforeWhenNull() {
        // given
        TrackRecentlyPlayedExternal response = mock(TrackRecentlyPlayedExternal.class);
        when(response.getItems()).thenReturn(List.of());

//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(spotifyUserWebClient.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(TrackRecentlyPlayedExternal.class)).thenReturn(Mono.just(response));

        try (MockedStatic<TrackRecentlyPlayedMapper> mapper = mockStatic(TrackRecentlyPlayedMapper.class)) {
            mapper.when(() -> TrackRecentlyPlayedMapper.toDtos(anyList()))
                    .thenReturn(List.of());

            // when (limit=0 -> safeLimit=1)
            var result = spotifyService.myRecentlyPlayed(0, null, null);

            // then
            assertNotNull(result);
            assertEquals(0, result.size());

            // sprawdzamy URI (limit=1)
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
            verify(req).uri(captor.capture());

            URI uri = buildUri(captor.getValue());
            String s = uri.toString();
            assertTrue(s.contains("/v1/me/player/recently-played"));
            assertTrue(s.contains("limit=1"));
            assertFalse(s.contains("after="));
            assertFalse(s.contains("before="));
        }
    }

    @Test
    void myRecentlyPlayed_shouldClampLimitToMax50_andIncludeAfterParam_whenProvided() {
        TrackRecentlyPlayedExternal response = mock(TrackRecentlyPlayedExternal.class);
        when(response.getItems()).thenReturn(List.of());

//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(spotifyUserWebClient.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(TrackRecentlyPlayedExternal.class)).thenReturn(Mono.just(response));

        try (MockedStatic<TrackRecentlyPlayedMapper> mapper = mockStatic(TrackRecentlyPlayedMapper.class)) {
            mapper.when(() -> TrackRecentlyPlayedMapper.toDtos(anyList()))
                    .thenReturn(List.of(new TrackRecentlyPlayedDto()));

            var result = spotifyService.myRecentlyPlayed(999, "1700000000000", null);

            assertEquals(1, result.size());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
            verify(req).uri(captor.capture());

            URI uri = buildUri(captor.getValue());
            String s = uri.toString();
            assertTrue(s.contains("/v1/me/player/recently-played"));
            assertTrue(s.contains("limit=50"));
            assertTrue(s.contains("after=1700000000000"));
            assertFalse(s.contains("before="));
        }
    }

    @Test
    void myRecentlyPlayed_shouldIncludeBeforeParam_whenProvided_andIgnoreBlankAfterBefore() {
        TrackRecentlyPlayedExternal response = mock(TrackRecentlyPlayedExternal.class);
        when(response.getItems()).thenReturn(List.of());

//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(spotifyUserWebClient.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(TrackRecentlyPlayedExternal.class)).thenReturn(Mono.just(response));

        try (MockedStatic<TrackRecentlyPlayedMapper> mapper = mockStatic(TrackRecentlyPlayedMapper.class)) {
            mapper.when(() -> TrackRecentlyPlayedMapper.toDtos(anyList()))
                    .thenReturn(List.of());

            // after = " " (blank) -> powinno NIE wejść do query
            spotifyService.myRecentlyPlayed(10, null, "1700000000001");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
            verify(req).uri(captor.capture());

            URI uri = buildUri(captor.getValue());
            String s = uri.toString();
            assertTrue(s.contains("limit=10"));
            assertTrue(s.contains("before=1700000000001"));
            assertFalse(s.contains("after="));
        }
    }

    // ========================= myTopTracks =========================

    @Test
    void myTopTracks_shouldCallSpotifyUserClient_andMapUsingTrackMapper() {
        TrackExternal response = mock(TrackExternal.class);
        when(response.getItems()).thenReturn(List.of());

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);

        when(spotifyUserWebClient.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(TrackExternal.class)).thenReturn(Mono.just(response));

        List<TrackDto> mapped = List.of(new TrackDto());
        try (MockedStatic<TrackMapper> mapper = mockStatic(TrackMapper.class)) {
            mapper.when(() -> TrackMapper.toDtos(anyList())).thenReturn(mapped);

            var result = spotifyService.myTopTracks(10, "medium_term", 0);

            assertSame(mapped, result);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
            verify(req).uri(captor.capture());

            URI uri = buildUri(captor.getValue());
            String s = uri.toString();
            assertTrue(s.contains("/v1/me/top/tracks"));
            assertTrue(s.contains("limit=10"));
            assertTrue(s.contains("time_range=medium_term"));
            assertTrue(s.contains("offset=0"));
        }
    }


    // ========================= myTopArtists =========================

    @Test
    void myTopArtists_shouldCallSpotifyUserClient_andMapUsingArtistMapper() {
        ArtistExternal response = mock(ArtistExternal.class);
        when(response.getItems()).thenReturn(List.of());

//        WebClient.RequestHeadersUriSpec<?> req = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> spec = mock(WebClient.RequestHeadersSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec req = mock(WebClient.RequestHeadersUriSpec.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec spec = mock(WebClient.RequestHeadersSpec.class);

        WebClient.ResponseSpec resp = mock(WebClient.ResponseSpec.class);
        when(spotifyUserWebClient.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(spec);
        when(spec.retrieve()).thenReturn(resp);
        when(resp.bodyToMono(ArtistExternal.class)).thenReturn(Mono.just(response));

        List<ArtistDto> mapped = List.of(new ArtistDto());
        try (MockedStatic<ArtistMapper> mapper = mockStatic(ArtistMapper.class)) {
            mapper.when(() -> ArtistMapper.toDtos(anyList())).thenReturn(mapped);

            var result = spotifyService.myTopArtists(20, "short_term", 5);

            assertSame(mapped, result);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
            verify(req).uri(captor.capture());

            URI uri = buildUri(captor.getValue());
            String s = uri.toString();
            assertTrue(s.contains("/v1/me/top/artists"));
            assertTrue(s.contains("limit=20"));
            assertTrue(s.contains("time_range=short_term"));
            assertTrue(s.contains("offset=5"));
        }
    }
}