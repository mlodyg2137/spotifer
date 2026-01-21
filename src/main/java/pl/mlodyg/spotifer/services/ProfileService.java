package pl.mlodyg.spotifer.services;

import com.fasterxml.jackson.databind.util.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mlodyg.spotifer.dto.*;
import pl.mlodyg.spotifer.models.*;
import pl.mlodyg.spotifer.repositories.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class ProfileService {

    private final TopTracksUserRepository topTracksUserRepository;
    private final TopArtistsUserRepository topArtistsUserRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final PlayEventRepository playEventRepository;
    private final SpotifyService spotifyService;
    private final Clock clock = Clock.systemUTC();

    // DEBUG LOGGING
//    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

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

    @Transactional
    public List<TopTrackDto> getTopTracksForUser(UUID userId, int limit, String time_range, int offset, boolean forceRefresh) {
        var now = Instant.now(clock);
        var ttl = Duration.ofHours(topFreshnessHours);

        // get TopTracksUser model or null (it contains userId and list of Track IDs)
        var top = topTracksUserRepository.findByUserIdAndTimeRange(userId, convert(time_range)).orElse(null);
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
            top.setTimeRange(convert(time_range));
            topTracksUserRepository.save(top);
        }

        var ids = top.getTopTrackIds();
        if (limit > 0 && limit < ids.size()) {
            ids = ids.subList(0, limit);
        }

        var tracks = trackRepository.findAllById(ids);

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

        var top = topArtistsUserRepository.findByUserIdAndTimeRange(userId, convert(time_range)).orElse(null);
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
            top.setTimeRange(convert(time_range));
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
            result.add(new TopArtistDto(artist.getName(), artist.getImageUrl(), artist.getPopularity(), rank));
            rank++;
        }

        return result;
    }

    /* ================= RECENTLY PLAYED / LICZNIK ================= */

    @Transactional
    public void syncRecentlyPlayedForUser(UUID userId, int limit, String after, String before) {
        List<TrackRecentlyPlayedDto> items = spotifyService.myRecentlyPlayed(limit, after, before);

        for (TrackRecentlyPlayedDto item : items) {
            Track track = upsertTrackFromSpotify(item);
            Instant playedAt = item.getPlayedAt();

            // deduplication
            boolean exists = playEventRepository.existsByUserIdAndTrackIdAndPlayedAt(
                    userId,
                    track.getId(),
                    playedAt
            );

            if (exists) {
                continue;
            }

            PlayEvent event = new PlayEvent();
            event.setUserId(userId);
            event.setTrack(track);
            event.setPlayedAt(playedAt);

            playEventRepository.save(event);
        }

    }

    @Transactional
    public List<PlayEventDto> getRecentlyPlayedTracksForUser(UUID userId, int limit, String after, String before, boolean forceRefresh) {

        var now = Instant.now(clock);
        var ttl = Duration.ofMinutes(recentlyPlayedFreshnessMinutes);

        var top = playEventRepository.findRecentByUserId(userId, PageRequest.of(0, limit));

        // we take first item. if first item is not fresh, we update
        PlayEvent first = top.getContent().stream().findFirst().orElse(null);
        boolean isFresh = first != null && !forceRefresh &&
                first.getPlayedAt() != null &&
                first.getPlayedAt().plus(ttl).isAfter(now);


        if (!isFresh) {
            syncRecentlyPlayedForUser(userId, limit, after, before);
        }

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

    private Track upsertTrackFromSpotify(TrackRecentlyPlayedDto trackRecentlyPlayedDto) {
        return trackRepository.findBySpotifyId(trackRecentlyPlayedDto.getSpotifyId())
                .orElseGet(() -> createTrack(trackRecentlyPlayedDto));
    }

    private Track createTrack(TrackDto trackDto) {
        Track track = new Track();
        track.setSpotifyId(trackDto.getSpotifyId());
        track.setName(trackDto.getName());
        track.setAlbumName(trackDto.getAlbumName());
        track.setAlbumImageUrl(trackDto.getAlbumImageUrl());
        track.setDurationMs(trackDto.getDurationMs());
        track.setUpdatedAt(Instant.now(clock));
        track.setPopularity(trackDto.getPopularity());

        // Artists
        List<Artist> artists = new ArrayList<>();
        for (TrackDto.Artist artistDto : trackDto.getArtists()) {
            Artist artist = upsertArtistFromSpotify(artistDto);
            artists.add(artist);
        }
        track.setArtists(artists);

        return trackRepository.save(track);
    }

    private Track createTrack(TrackRecentlyPlayedDto trackRecentlyPlayedDto) {
        return createTrack(new TrackDto(trackRecentlyPlayedDto));
    }

    private Track updateTrackIfNeeded(Track existing, TrackDto st) {
        existing.setName(st.getName());
        existing.setAlbumName(st.getAlbumName());
        existing.setAlbumImageUrl(st.getAlbumImageUrl());
        existing.setDurationMs(st.getDurationMs());
        existing.setUpdatedAt(Instant.now(clock));
        existing.setPopularity(st.getPopularity());

        // Artists
        List<Artist> artists = new ArrayList<>();
        for (TrackDto.Artist artistDto : st.getArtists()) {
            Artist artist = upsertArtistFromSpotify(artistDto);
            artists.add(artist);
        }

        return trackRepository.save(existing);
    }

    private Artist upsertArtistFromSpotify(ArtistDto artist) {
        return artistRepository.findBySpotifyId(artist.getSpotifyId())
                .map(existing -> updateArtistIfNeeded(existing, artist))
                .orElseGet(() -> createArtist(artist));
    }

    private Artist upsertArtistFromSpotify(TrackDto.Artist artist) {
        return upsertArtistFromSpotify(new ArtistDto(artist));
    }

    private Artist createArtist(ArtistDto sa) {
        Artist a = new Artist();
        a.setSpotifyId(sa.getSpotifyId());
        a.setName(sa.getName());
        a.setImageUrl(sa.getImageUrl());
        a.setUpdatedAt(Instant.now(clock));
        a.setPopularity(sa.getPopularity());
        return artistRepository.save(a);
    }

    private Artist updateArtistIfNeeded(Artist existing, ArtistDto artist) {
        existing.setName(artist.getName());
        existing.setImageUrl(artist.getImageUrl());
        existing.setUpdatedAt(Instant.now(clock));
        existing.setPopularity(artist.getPopularity());
        return artistRepository.save(existing);
    }

    public TimeRange convert(String source) {
        switch (source.toLowerCase()) {
            case "short_term":
                return TimeRange.SHORT_TERM;
            case "medium_term":
                return TimeRange.MEDIUM_TERM;
            case "long_term":
                return TimeRange.LONG_TERM;
            default:
                throw new IllegalArgumentException("Invalid time range: " + source);
        }
    }
}
