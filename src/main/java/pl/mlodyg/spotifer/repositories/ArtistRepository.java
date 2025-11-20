package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.Artist;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findBySpotifyId(String spotifyId);

    @Query("SELECT a FROM Artist a WHERE a.spotifyId IN :ids")
    List<Artist> findArtistsBySpotifyIds(@Param("ids") List<String> ids);
}
