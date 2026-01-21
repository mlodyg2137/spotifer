package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.TimeRange;
import pl.mlodyg.spotifer.models.TopArtistsUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopArtistsUserRepository extends JpaRepository<TopArtistsUser, Long> {
//    Optional<TopArtistsUser> findByUserId(UUID userId);
    Optional<TopArtistsUser> findByUserIdAndTimeRange(UUID userId, TimeRange timeRange);
}
