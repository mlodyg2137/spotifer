package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "play_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayEvent {

    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;

    @ManyToOne
    Track track;

    Instant playedAt;
}
