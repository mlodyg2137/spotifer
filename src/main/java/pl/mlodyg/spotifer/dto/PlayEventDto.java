package pl.mlodyg.spotifer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mlodyg.spotifer.models.PlayEvent;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayEventDto {
    private String trackName;
    private List<String> artistNames = new ArrayList<>();
    private String albumName;
    private String albumImageUrl;
    private String playedAt;

    public static PlayEventDto from(PlayEvent playEvent) {
        PlayEventDto dto = new PlayEventDto();
        dto.setTrackName(playEvent.getTrack().getName());
        dto.setAlbumName(playEvent.getTrack().getAlbumName());
        dto.setAlbumImageUrl(playEvent.getTrack().getAlbumImageUrl());
        dto.setPlayedAt(playEvent.getPlayedAt().toString());
        List<String> artistNames = playEvent.getTrack().getArtists().stream()
                .map(artist -> artist.getName())
                .toList();
        dto.setArtistNames(artistNames);
        return dto;
    }
}
