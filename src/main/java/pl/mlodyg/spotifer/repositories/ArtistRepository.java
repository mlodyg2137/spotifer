package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.Artist;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
