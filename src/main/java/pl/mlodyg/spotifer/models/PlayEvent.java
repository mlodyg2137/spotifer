package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "play_events")
@Getter
@Setter
@AllArgsConstructor
public class PlayEvent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    Track track;

    Instant playedAt;
}
