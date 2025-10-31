package pl.mlodyg.spotifer.mappers;

import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.externals.track.Image;
import pl.mlodyg.spotifer.externals.track.Item;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TrackMapper {
    private TrackMapper() {}

    public static TrackDto toDto(Item src) {
        if (src == null) return null;

        TrackDto dto = new TrackDto();
        dto.setId(null);
        dto.setSpotifyId(src.getId());
        dto.setName(src.getName());
        dto.setDurationMs(src.getDurationMs());

        // album
        if (src.getAlbum() != null) {
            dto.setAlbumName(src.getAlbum().getName());
            dto.setAlbumImageUrl(firstImageUrl(src));
        }

        // artists
        if (src.getArtists() != null) {
            List<TrackDto.Artist> artists = src.getArtists().stream()
                    .filter(Objects::nonNull)
                    .map(a -> {
                        TrackDto.Artist aa = new TrackDto.Artist();
                        aa.setName(a.getName());
                        aa.setSpotifyId(a.getId());
                        return aa;
                    })
                    .collect(Collectors.toList());
            dto.setArtists(artists);
        }

        return dto;
    }

    public static List<TrackDto> toDtos(List<Item> list) {
        if (list == null) return List.of();
        return list.stream().map(TrackMapper::toDto).collect(Collectors.toList());
    }

    private static String firstImageUrl(Item src) {
        if (src.getAlbum() == null || src.getAlbum().getImages() == null) return null;
        return src.getAlbum().getImages().stream()
                .filter(Objects::nonNull)
                .map(Image::getUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
