package pl.mlodyg.spotifer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_spotify_tokens")
@Getter
@Setter
public class UserSpotifyTokens {

    @Id
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Column(nullable = false)
    private String accessTokenEnc;

    @Lob
    @Column(nullable = false)
    private String refreshTokenEnc;

    @Column(nullable = false)
    private Instant expiresAt;

    @ElementCollection
    @CollectionTable(name = "user_spotify_scopes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "scope")
    private Set<String> scope = new HashSet<>();

    private Instant updatedAt;
}
