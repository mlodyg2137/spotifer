package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
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
    private List<Artist> artists;

    @Data
    public static class Artist {
        private String spotifyId;
        private String name;
        private String imageUrl;
    }

    public TrackDto(Track track) {
        this.spotifyId = track.getSpotifyId();
        this.name = track.getName();
        this.durationMs = track.getDurationMs();
        this.albumName = track.getAlbumName();
        this.albumImageUrl = track.getAlbumImageUrl();
        this.artists = track.getArtists().stream().map(artist -> {
            Artist a = new Artist();
            a.setSpotifyId(artist.getSpotifyId());
            a.setName(artist.getName());
            a.setImageUrl(artist.getImageUrl());
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
