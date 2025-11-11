package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mlodyg.spotifer.models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findBySpotifyUserId(String spotifyUserId);
}
