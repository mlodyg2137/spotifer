package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mlodyg.spotifer.models.Track;

import java.util.List;

@Data
@NoArgsConstructor
public class TrackDto {

    @Id
    private Integer id;
    private String spotifyId;
    private String name;
    private Integer durationMs;
    private String albumName;
    private String albumImageUrl;
    private Integer popularity;
    private List<Artist> artists;

    public TrackDto(String spotifyId, String name, List<Artist> artists) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.artists = artists;
    }

    @Data
    public static class Artist {
        @NotBlank
        private String spotifyId;

        @NotBlank
        @Size(max = 200)
        private String name;
        private String imageUrl;
        private Integer popularity;
    }

    public TrackDto(Track track) {
        this.spotifyId = track.getSpotifyId();
        this.name = track.getName();
        this.durationMs = track.getDurationMs();
        this.albumName = track.getAlbumName();
        this.albumImageUrl = track.getAlbumImageUrl();
        this.popularity = track.getPopularity();

        this.artists = track.getArtists().stream().map(artist -> {
            Artist a = new Artist();
            a.setSpotifyId(artist.getSpotifyId());
            a.setName(artist.getName());
            a.setImageUrl(artist.getImageUrl());
            a.setPopularity(artist.getPopularity());
            return a;
        }).toList();
    }

    public TrackDto(TrackRecentlyPlayedDto trackDto) {
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
