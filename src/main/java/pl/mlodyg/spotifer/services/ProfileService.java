package pl.mlodyg.spotifer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.repositories.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    public List<TrackDto> getTopTracksForUser(UUID userId, int limit, boolean forceRefresh) {
        var now = Instant.now(clock);
        var ttl = Duration.ofHours(topFreshnessHours);

        var top = topTracksUserRepository.findById(userId).orElse(null);
        boolean isFresh = top != null && !forceRefresh &&
                top.getUpdatedAt() != null &&
                top.getUpdatedAt().plus(ttl).isAfter(now);

        if (!isFresh) {
            // 1) pobierz top z Spotify
            var spotifyTop = spotifyService.fetchTopTracks(userId, 50);
            // załóżmy, że zwraca listę czegoś w stylu SpotifyTrackDto (z id, name, artists, image itd.)

            // 2) upsert Track + Artist
            List<Long> trackIds = new ArrayList<>();
            for (SpotifyTrackDto st : spotifyTop) {
                Track track = upsertTrackFromSpotify(st);
                trackIds.add(track.getId());
            }

            // 3) zapisz cache
            if (top == null) {
                top = new TopTracksUser();
                top.setUserId(userId);
            }
            top.setTrackIds(trackIds);
            top.setTracksNumber(trackIds.size());
            top.setUpdatedAt(now);
            topTracksRepo.save(top);
        }

        // 4) dociągnij Track po ID w kolejności z listy
        var ids = top.getTrackIds();
        if (limit > 0 && limit < ids.size()) {
            ids = ids.subList(0, limit);
        }

        var tracks = trackRepo.findAllById(ids);

        // mapujemy do mapy id -> Track dla zachowania kolejności
        var byId = tracks.stream().collect(Collectors.toMap(Track::getId, t -> t));

        List<TopTrackDto> result = new ArrayList<>();
        int rank = 1;
        for (Long id : ids) {
            Track t = byId.get(id);
            if (t != null) {
                result.add(TopTrackDto.from(track = t, rank++));
            }
        }
        return result;

    }
}
