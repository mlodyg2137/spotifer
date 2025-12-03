package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistDto {
    @Id
    private Integer id;
    private String spotifyId;
    private String name;
    private String imageUrl;

    public ArtistDto(TrackDto.Artist artist) {
        this.spotifyId = artist.getSpotifyId();
        this.name = artist.getName();
        this.imageUrl = artist.getImageUrl();
    }
}
