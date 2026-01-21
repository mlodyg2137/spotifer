package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "top_tracks_users",
        uniqueConstraints = @UniqueConstraint(name="uq_top_tracks_user_range", columnNames={"user_id","time_range"})
)
@Setter
@Getter
public class TopTracksUser {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_range", nullable = false)
    private TimeRange timeRange;

    @ElementCollection
    private List<Long> topTrackIds;

    private long tracksNumber;
    private Instant updatedAt;
    private Instant createdAt;
}
