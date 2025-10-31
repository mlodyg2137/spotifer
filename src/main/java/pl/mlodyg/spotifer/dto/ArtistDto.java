package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class ArtistDto {

    @Id
    private Integer id;
    private String spotifyId;
    private String name;
    private String imageUrl;

}
