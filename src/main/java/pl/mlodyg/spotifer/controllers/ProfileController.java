package pl.mlodyg.spotifer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public List<TrackDto> topTracks(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        return profileService.getTopTracksForUser(userId, limit, forceRefresh);
    }
}
