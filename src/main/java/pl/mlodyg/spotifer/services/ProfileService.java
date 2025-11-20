package pl.mlodyg.spotifer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mlodyg.spotifer.dto.*;
import pl.mlodyg.spotifer.models.*;
import pl.mlodyg.spotifer.repositories.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final TopTracksUserRepository topTracksUserRepository;
    private final TopArtistsUserRepository topArtistsUserRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final PlayEventRepository playEventRepository;
    private final SpotifyService spotifyService;
    private final Clock clock = Clock.systemUTC();

    @Value("${app.top.freshness-hours:24}") private long topFreshnessHours;
    @Value("${app.recently-played.freshness-minutes:20}") private long recentlyPlayedFreshnessMinutes;

    public ProfileService(
            TopTracksUserRepository topTracksUserRepository,
            TopArtistsUserRepository topArtistsUserRepository,
            TrackRepository trackRepository,
            ArtistRepository artistRepository,
            PlayEventRepository playEventRepository,
            SpotifyService spotifyService
    ) {
        this.topTracksUserRepository = topTracksUserRepository;
        this.topArtistsUserRepository = topArtistsUserRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.playEventRepository = playEventRepository;
        this.spotifyService = spotifyService;
    }

    /* ================= TOP TRACKS ================= */

    public List<TopTrackDto> getTopTracksForUser(UUID userId, int limit, String time_range, int offset, boolean forceRefresh) {
        var now = Instant.now(clock);
        var ttl = Duration.ofHours(topFreshnessHours);

        // get TopTracksUser model or null (it contains userId and list of Track IDs)
        var top = topTracksUserRepository.findByUserId(userId).orElse(null);
        boolean isFresh = top != null && !forceRefresh &&
                top.getUpdatedAt() != null &&
                top.getUpdatedAt().plus(ttl).isAfter(now);

        // if not fresh, fetch from SpotifyService and update cache (fresh is when updatedAt + ttl > now)
        if (!isFresh) {
            var spotifyTop = spotifyService.myTopTracks(limit, time_range, offset);

            // upsert Track + Artist
            List<Long> trackIds = new ArrayList<>();
            for (TrackDto st : spotifyTop) {
                Track track = upsertTrackFromSpotify(st);
                trackIds.add(track.getId());
            }

            // cache
            if (top == null) {
                top = new TopTracksUser();
                top.setUserId(userId);
            }
            top.setTopTrackIds(trackIds);
            top.setTracksNumber(trackIds.size());
            top.setUpdatedAt(now);
            topTracksUserRepository.save(top);
        }

        // 4) dociągnij Track po ID w kolejności z listy
        var ids = top.getTopTrackIds();
        if (limit > 0 && limit < ids.size()) {
            ids = ids.subList(0, limit);
        }

        var tracks = trackRepository.findAllById(ids);

//        var byId = tracks.stream().collect(Collectors.toMap(Track::getId, t -> t));
//
//        List<TopTrackDto> result = new ArrayList<>();
//        int rank = 1;
//        for (Long id : ids) {
//            Track t = byId.get(id);
//            if (t != null) {
//                result.add(TopTrackDto.from(track = t, rank++));
//            }
//        }
        List<TopTrackDto> result = new ArrayList<>();
        int rank = 1;
        for (Track track : tracks) {
            result.add(new TopTrackDto(track, rank));
            rank++;
        }
        return result;

    }

    /* ================= TOP ARTISTS ================= */

    @Transactional
    public List<TopArtistDto> getTopArtistsForUser(UUID userId, int limit, String time_range, int offset, boolean forceRefresh) {
        var now = Instant.now(clock);
        var ttl = Duration.ofHours(topFreshnessHours);

        var top = topArtistsUserRepository.findByUserId(userId).orElse(null);
        boolean isFresh = top != null && !forceRefresh &&
                top.getUpdatedAt() != null &&
                top.getUpdatedAt().plus(ttl).isAfter(now);

        if (!isFresh) {
            var spotifyTop = spotifyService.myTopArtists(limit, time_range, offset);
            List<Long> artistIds = new ArrayList<>();
            for (ArtistDto sa : spotifyTop) {
                Artist artist = upsertArtistFromSpotify(sa);
                artistIds.add(artist.getId());
            }
            if (top == null) {
                top = new TopArtistsUser();
                top.setUserId(userId);
            }
            top.setArtistIds(artistIds);
            top.setArtistsNumber(artistIds.size());
            top.setUpdatedAt(now);
            topArtistsUserRepository.save(top);
        }

        var ids = top.getArtistIds();
        if (limit > 0 && limit < ids.size()) {
            ids = ids.subList(0, limit);
        }

        var artists = artistRepository.findAllById(ids);

        List<TopArtistDto> result = new ArrayList<>();
        int rank = 1;
        for (Artist artist : artists) {
            result.add(new TopArtistDto(artist.getName(), artist.getImageUrl(), rank));
            rank++;
        }

        return result;
    }

    /* ================= RECENTLY PLAYED / LICZNIK ================= */

    @Transactional
    public void syncRecentlyPlayedForUser(UUID userId, int limit, String after, String before) {
        // 1) pobierz recently-played z Spotify
        // 2) upsert Track / Artist
        // 3) zapisz PlayEvent z playedAt + track
        // 4) deduplikacja (np. po (userId, trackId, playedAt))

        List<TrackRecentlyPlayedDto> items = spotifyService.myRecentlyPlayed(limit, after, before);

        for (TrackRecentlyPlayedDto item : items) {
            // 2) upsert Track / Artist
            Track track = upsertTrackFromSpotify(item.getTrack());
            Instant playedAt = item.getPlayedAt();

            // 3) deduplikacja po (userId, trackId, playedAt)
            boolean exists = playEventRepository.existsByUserIdAndTrackIdAndPlayedAt(
                    userId,
                    track.getId(),
                    playedAt
            );
            if (exists) {
                continue; // ten event już mamy w bazie
            }

            // 4) zapis PlayEvent
            PlayEvent event = new PlayEvent();
            event.setUserId(userId);
            event.setTrack(track);
            event.setPlayedAt(playedAt);

            playEventRepository.save(event);
        }

    }

    @Transactional(readOnly = true)
    public List<PlayEventDto> getRecentHistory(UUID userId, int limit) {
        // np. ostatnie N PlayEvent dla usera, posortowane po playedAt DESC
        return playEventRepository.findRecentByUserId(userId, PageRequest.of(0, limit))
                .stream()
                .map(PlayEventDto::from)
                .toList();
    }

    /* ================= HELPER METHODS ================= */

    private Track upsertTrackFromSpotify(TrackDto track) {
        return trackRepository.findBySpotifyId(track.getSpotifyId())
                .map(existing -> updateTrackIfNeeded(existing, track))
                .orElseGet(() -> createTrack(track));
    }

    private Track createTrack(TrackDto trackDto) {
        Track track = new Track();
        track.setSpotifyId(trackDto.getSpotifyId());
        track.setName(trackDto.getName());
        track.setAlbumImageUrl(trackDto.getAlbumImageUrl());
        track.setDurationMs(trackDto.getDurationMs());
        track.setUpdatedAt(Instant.now(clock));

        List<String> artistSpotifyIds = trackDto.getArtists().stream()
                .map(TrackDto.Artist::getSpotifyId)
                .toList();
        List<Artist> artists = artistRepository.findArtistsBySpotifyIds(artistSpotifyIds);
        track.setArtists(artists);

        return trackRepository.save(track);
    }

    private Track updateTrackIfNeeded(Track existing, TrackDto st) {
        existing.setName(st.getName());
        existing.setAlbumImageUrl(st.getAlbumImageUrl());
        existing.setDurationMs(st.getDurationMs());
        existing.setUpdatedAt(Instant.now(clock));
        return trackRepository.save(existing);
    }

    private Artist upsertArtistFromSpotify(ArtistDto artist) {
        return artistRepository.findBySpotifyId(artist.getSpotifyId())
                .map(existing -> updateArtistIfNeeded(existing, artist))
                .orElseGet(() -> createArtist(artist));
    }

    private Artist createArtist(ArtistDto sa) {
        Artist a = new Artist();
        a.setSpotifyId(sa.getSpotifyId());
        a.setName(sa.getName());
        a.setImageUrl(sa.getImageUrl());
        a.setUpdatedAt(Instant.now(clock));
        return artistRepository.save(a);
    }

    private Artist updateArtistIfNeeded(Artist existing, ArtistDto artist) {
        existing.setName(artist.getName());
        existing.setImageUrl(artist.getImageUrl());
        existing.setUpdatedAt(Instant.now(clock));
        return artistRepository.save(existing);
    }
}
