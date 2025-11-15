package pl.mlodyg.spotifer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artists")
@Getter
@Setter
@AllArgsConstructor
public class Artist {
    private Long id;
    private String spotifyId;
    private String name;
    private String imageUrl;
    private Instant updatedAt;

    public Artist() {
    }
}
