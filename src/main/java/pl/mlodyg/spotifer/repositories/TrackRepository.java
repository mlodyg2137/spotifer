package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.models.Track;

import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findBySpotifyId(String spotifyId);
}
