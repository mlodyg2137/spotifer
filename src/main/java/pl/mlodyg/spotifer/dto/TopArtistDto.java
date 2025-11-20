package pl.mlodyg.spotifer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopArtistDto {
    private String name;
    private String artistImageUrl;
    private long rank;
}
