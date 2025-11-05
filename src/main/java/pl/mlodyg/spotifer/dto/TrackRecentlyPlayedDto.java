package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import lombok.Data;

import java.util.List;

@Data
public class TrackRecentlyPlayedDto {
    @Id
    private Integer id;
    private String spotifyId;
    private String name;
    private Integer durationMs;
    private String albumName;
    private String albumImageUrl;
    private List<TrackRecentlyPlayedDto.Artist> artists;
    private String playedAt;

    @Data
    public static class Artist {
        private String spotifyId;
        private String name;
    }

}
