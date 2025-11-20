package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.TopTracksUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopTracksUserRepository extends JpaRepository<TopTracksUser, Long> {
    Optional<TopTracksUser> findByUserId(UUID userId);
}
