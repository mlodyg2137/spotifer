package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.TopArtistsUser;

import java.util.UUID;

@Repository
public interface TopArtistsUserRepository extends JpaRepository<TopArtistsUser, UUID> {
}
