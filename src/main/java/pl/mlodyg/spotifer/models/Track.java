package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@AllArgsConstructor
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String spotifyId;
    private String name;
    private Integer durationMs;
    private String albumName;
    private String albumImageUrl;
    private Instant updatedAt;

    @ManyToMany
    @JoinTable(
            name = "track_artist",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private List<Artist> artists;

    public Track() {
    }
}
