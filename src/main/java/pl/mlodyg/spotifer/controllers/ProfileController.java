package pl.mlodyg.spotifer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.mlodyg.spotifer.dto.PlayEventDto;
import pl.mlodyg.spotifer.dto.TopArtistDto;
import pl.mlodyg.spotifer.dto.TopTrackDto;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.services.ProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    @Autowired
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) { this.profileService = profileService; }

    @GetMapping("/top/tracks")
    public List<TopTrackDto> topTracks(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "medium_term") String time_range,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        return profileService.getTopTracksForUser(userId, limit, time_range, offset, forceRefresh);
    }

    @GetMapping("/top/artists")
    public List<TopArtistDto> topArtists(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "medium_term") String time_range,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        return profileService.getTopArtistsForUser(userId, limit, time_range, offset, forceRefresh);
    }

    @GetMapping("/recently-played")
    public List<PlayEventDto> recentlyPlayed(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        return profileService.getRecentlyPlayedTracksForUser(userId, limit, after, before, forceRefresh);
    }
}
