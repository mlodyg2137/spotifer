package pl.mlodyg.spotifer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mlodyg.spotifer.models.PlayEvent;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {
}
