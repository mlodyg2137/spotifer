package pl.mlodyg.spotifer.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.PlayEvent;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {
    @Query("""
        SELECT p FROM PlayEvent p
        WHERE p.userId = :userId
        ORDER BY p.playedAt DESC
    """)
    Page<PlayEvent> findRecentByUserId(UUID userId, Pageable pageable);
    boolean existsByUserIdAndTrackIdAndPlayedAt(UUID userId, Long trackId, Instant playedAt);
}
