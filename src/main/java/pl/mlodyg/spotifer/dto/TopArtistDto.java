package pl.mlodyg.spotifer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopArtistDto {
    @NotBlank
    @Size(max = 200)
    private String name;
    private String artistImageUrl;
    private Integer popularity;
    private long rank;
}
