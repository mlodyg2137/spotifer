package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackRecentlyPlayedDto {
    @Id
    private Integer id;
    private String spotifyId;
    private String name;
    private Integer durationMs;
    private String albumName;
    private String albumImageUrl;
    private List<TrackRecentlyPlayedDto.Artist> artists;
    private Instant playedAt;

    @Data
    public static class Artist {
        private String spotifyId;
        private String name;
        private String imageUrl;
    }

    public TrackRecentlyPlayedDto(TrackDto trackDto) {
        this.spotifyId = trackDto.getSpotifyId();
        this.name = trackDto.getName();
        this.durationMs = trackDto.getDurationMs();
        this.albumName = trackDto.getAlbumName();
        this.albumImageUrl = trackDto.getAlbumImageUrl();

        this.artists = trackDto.getArtists().stream().map(artist -> {
            Artist a = new Artist();
            a.setSpotifyId(artist.getSpotifyId());
            a.setName(artist.getName());
            a.setImageUrl(artist.getImageUrl());
            return a;
        }).toList();
    }
}
