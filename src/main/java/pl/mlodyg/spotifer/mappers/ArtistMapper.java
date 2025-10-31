package pl.mlodyg.spotifer.mappers;

import pl.mlodyg.spotifer.dto.ArtistDto;
import pl.mlodyg.spotifer.externals.artist.Image;
import pl.mlodyg.spotifer.externals.artist.Item;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArtistMapper {

    private ArtistMapper() {}

    public static ArtistDto toDto(Item src) {
        if (src == null) return null;

        ArtistDto dto = new ArtistDto();
        dto.setId(null);
        dto.setSpotifyId(src.getId());
        dto.setName(src.getName());
        dto.setImageUrl(firstImageUrl(src));

        return dto;
    }

    public static List<ArtistDto> toDtos(List<Item> list) {
        if (list == null) return List.of();
        return list.stream().map(ArtistMapper::toDto).collect(Collectors.toList());
    }

    private static String firstImageUrl(Item src) {
        if (src.getImages() == null) return null;
        return src.getImages().stream()
                .filter(Objects::nonNull)
                .map(Image::getUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
