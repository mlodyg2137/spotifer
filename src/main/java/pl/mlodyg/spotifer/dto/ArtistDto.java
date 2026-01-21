package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistDto {
    @Id
    private Integer id;

    @NotBlank
    private String spotifyId;

    @NotBlank
    @Size(max=200)
    private String name;
    private String imageUrl;
    private Integer popularity;

    public ArtistDto(TrackDto.Artist artist) {
        this.spotifyId = artist.getSpotifyId();
        this.name = artist.getName();
        this.imageUrl = artist.getImageUrl();
        this.popularity = artist.getPopularity();
    }
}
