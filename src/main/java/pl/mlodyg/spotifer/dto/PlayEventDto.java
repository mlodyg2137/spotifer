package pl.mlodyg.spotifer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
