package pl.mlodyg.spotifer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mlodyg.spotifer.models.Track;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTrackDto {
    private String trackName;
    private List<String> artistNames = new ArrayList<>();
    private String albumName;
    private String albumImageUrl;
    private long rank;

    public TopTrackDto(Track track, long rank) {
        this.trackName = track.getName();
        this.albumName = track.getAlbumName();
        this.albumImageUrl = track.getAlbumImageUrl();
        this.rank = rank;
        this.artistNames = track.getArtists().stream()
                .map(artist -> artist.getName())
                .toList();
    }
}
