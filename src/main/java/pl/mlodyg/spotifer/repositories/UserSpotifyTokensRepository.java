package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mlodyg.spotifer.models.UserSpotifyTokens;

import java.util.UUID;

public interface UserSpotifyTokensRepository extends JpaRepository<UserSpotifyTokens, UUID> {

}
