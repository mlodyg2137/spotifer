package pl.mlodyg.spotifer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mlodyg.spotifer.models.Artist;
import pl.mlodyg.spotifer.models.Track;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTrackDto {
    @NotBlank
    @Size(max = 200)
    private String trackName;
    private List<String> artistNames = new ArrayList<>();
    private String albumName;
    private String albumImageUrl;
    private Integer popularity;
    private long rank;

    public TopTrackDto(Track track, long rank) {
        this.trackName = track.getName();
        this.albumName = track.getAlbumName();
        this.albumImageUrl = track.getAlbumImageUrl();
        this.rank = rank;
        this.popularity = track.getPopularity();
        this.artistNames = track.getArtists().stream()
                .map(Artist::getName)
                .toList();
    }
}
