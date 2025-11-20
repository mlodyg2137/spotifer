package pl.mlodyg.spotifer.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "top_artists_users")
@Getter
@Setter
public class TopArtistsUser {
    @Id
    private UUID userId;

    @ElementCollection
    private List<Long> artistIds;

    private long artistsNumber;
    private Instant updatedAt;
    private Instant createdAt;
}
