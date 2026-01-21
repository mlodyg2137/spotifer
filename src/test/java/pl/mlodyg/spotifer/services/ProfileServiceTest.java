package pl.mlodyg.spotifer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.mlodyg.spotifer.dto.TrackRecentlyPlayedDto;
import pl.mlodyg.spotifer.models.*;
import pl.mlodyg.spotifer.repositories.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private TopTracksUserRepository topTracksUserRepository;
    private TopArtistsUserRepository topArtistsUserRepository;
    private TrackRepository trackRepository;
    private ArtistRepository artistRepository;
    private PlayEventRepository playEventRepository;
    private SpotifyService spotifyService;

    private ProfileService profileService;

    @BeforeEach
    void setup() {
        topTracksUserRepository = mock(TopTracksUserRepository.class);
        topArtistsUserRepository = mock(TopArtistsUserRepository.class);
        trackRepository = mock(TrackRepository.class);
        artistRepository = mock(ArtistRepository.class);
        playEventRepository = mock(PlayEventRepository.class);
        spotifyService = mock(SpotifyService.class);

        profileService = new ProfileService(
                topTracksUserRepository,
                topArtistsUserRepository,
                trackRepository,
                artistRepository,
                playEventRepository,
                spotifyService
        );

        ReflectionTestUtils.setField(profileService, "topFreshnessHours", 24L);
        ReflectionTestUtils.setField(profileService, "recentlyPlayedFreshnessMinutes", 20L);
    }

    Artist getSampleArtist(String name) {
        Artist artist = new Artist();
        artist.setName(name);
        artist.setSpotifyId("spotifyId" + name);
        return artist;
    }

    @Test
    void getTopTracksForUser_whenCacheFresh_shouldNotCallSpotify() {
        UUID userId = UUID.randomUUID();

        TopTracksUser cached = new TopTracksUser();
        cached.setUserId(userId);
        cached.setTimeRange(TimeRange.MEDIUM_TERM);
        cached.setUpdatedAt(Instant.now()); // fresh
        cached.setTopTrackIds(List.of(1L, 2L));
        cached.setTracksNumber(2);

        when(topTracksUserRepository.findByUserIdAndTimeRange(eq(userId), eq(TimeRange.MEDIUM_TERM)))
                .thenReturn(Optional.of(cached));

        Track t1 = new Track(); t1.setId(1L); t1.setName("T1"); t1.setArtists(List.of(getSampleArtist("A1")));
        Track t2 = new Track(); t2.setId(2L); t2.setName("T2"); t2.setArtists(List.of(getSampleArtist("A2")));
        when(trackRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(t1, t2));

        var result = profileService.getTopTracksForUser(userId, 20, "medium_term", 0, false);

        assertEquals(2, result.size());
        assertEquals("T1", result.get(0).getTrackName());
        verify(spotifyService, never()).myTopTracks(anyInt(), anyString(), anyInt());
        verify(topTracksUserRepository, never()).save(any());
    }

    @Test
    void getTopArtistsForUser_whenCacheFresh_shouldNotCallSpotify() {
        UUID userId = UUID.randomUUID();

        TopArtistsUser cached = new TopArtistsUser();
        cached.setUserId(userId);
        cached.setTimeRange(TimeRange.MEDIUM_TERM);
        cached.setUpdatedAt(Instant.now()); // fresh
        cached.setArtistIds(List.of(1L, 2L));
        cached.setArtistsNumber(2);

        when(topArtistsUserRepository.findByUserIdAndTimeRange(eq(userId), eq(TimeRange.MEDIUM_TERM)))
                .thenReturn(Optional.of(cached));

        Artist a1 = new Artist(); a1.setId(1L); a1.setName("A1");
        Artist a2 = new Artist(); a2.setId(1L); a2.setName("A2");
        when(artistRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(a1, a2));

        var result = profileService.getTopArtistsForUser(userId, 20, "medium_term", 0, false);

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).getName());
        verify(spotifyService, never()).myTopArtists(anyInt(), anyString(), anyInt());
        verify(topArtistsUserRepository, never()).save(any());
    }

    @Test
    void syncRecentlyPlayedForUser_shouldDeduplicate() {
        UUID userId = UUID.randomUUID();

        TrackRecentlyPlayedDto rp = mock(TrackRecentlyPlayedDto.class);
        when(rp.getSpotifyId()).thenReturn("spT1");
        when(rp.getPlayedAt()).thenReturn(Instant.parse("2026-01-01T10:00:00Z"));

        when(spotifyService.myRecentlyPlayed(20, null, null))
                .thenReturn(List.of(rp));

        // upsertTrackFromSpotify(TrackRecentlyPlayedDto) -> trackRepository.findBySpotifyId...
        Track track = new Track(); track.setId(999L);
        when(trackRepository.findBySpotifyId("spT1")).thenReturn(Optional.of(track));

        when(playEventRepository.existsByUserIdAndTrackIdAndPlayedAt(
                eq(userId), eq(999L), eq(Instant.parse("2026-01-01T10:00:00Z"))
        )).thenReturn(true);

        profileService.syncRecentlyPlayedForUser(userId, 20, null, null);

        verify(playEventRepository, never()).save(any(PlayEvent.class));
    }
}
