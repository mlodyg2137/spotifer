package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spotify_id", nullable = false)
    private String spotifyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "album_type", nullable = false)
    private String albumType;

    @Column(name = "total_tracks", nullable = false)
    private Integer totalTracks;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "release_date_precision")
    private String releaseDatePrecision;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "album_artists",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Transient
    private List<ImageObject> images;
}
