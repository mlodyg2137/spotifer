package pl.mlodyg.spotifer.mappers;

import pl.mlodyg.spotifer.dto.TrackRecentlyPlayedDto;
import pl.mlodyg.spotifer.externals.trackrecentlyplayed.Image;
import pl.mlodyg.spotifer.externals.trackrecentlyplayed.Item;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrackRecentlyPlayedMapper {

    private TrackRecentlyPlayedMapper() {}

    public static TrackRecentlyPlayedDto toDto(Item src) {
        if (src == null) return null;

        TrackRecentlyPlayedDto dto = new TrackRecentlyPlayedDto();
        dto.setId(null);
        dto.setSpotifyId(src.getTrack().getId());
        dto.setName(src.getTrack().getName());
        dto.setDurationMs(src.getTrack().getDurationMs());

        Instant playedAt = Instant.parse(src.getPlayedAt());
        dto.setPlayedAt(playedAt);

        // album
        if (src.getTrack().getAlbum() != null) {
            dto.setAlbumName(src.getTrack().getAlbum().getName());
            dto.setAlbumImageUrl(firstImageUrl(src));
        }

        // artists
        if (src.getTrack().getArtists() != null) {
            List<TrackRecentlyPlayedDto.Artist> artists = src.getTrack().getArtists().stream()
                    .filter(Objects::nonNull)
                    .map(a -> {
                        TrackRecentlyPlayedDto.Artist aa = new TrackRecentlyPlayedDto.Artist();
                        aa.setName(a.getName());
                        aa.setSpotifyId(a.getId());
                        return aa;
                    })
                    .collect(Collectors.toList());
            dto.setArtists(artists);
        }

        return dto;
    }

    public static List<TrackRecentlyPlayedDto> toDtos(List<Item> list) {
        if (list == null) return List.of();
        return list.stream().map(TrackRecentlyPlayedMapper::toDto).collect(Collectors.toList());
    }

    private static String firstImageUrl(Item src) {
        if (src.getTrack().getAlbum() == null || src.getTrack().getAlbum().getImages() == null) return null;
        return src.getTrack().getAlbum().getImages().stream()
                .filter(Objects::nonNull)
                .map(Image::getUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
