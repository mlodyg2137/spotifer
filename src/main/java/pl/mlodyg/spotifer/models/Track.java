package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spotify_id", nullable = false)
    private String spotifyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs;
}
