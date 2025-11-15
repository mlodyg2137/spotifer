package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.Track;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
}
