package pl.mlodyg.spotifer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.mlodyg.spotifer.dto.ArtistDto;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.services.SpotifyService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    @Autowired
    private final SpotifyService spotifyOAuthService;

    SpotifyController(SpotifyService spotifyOAuthService) {
        this.spotifyOAuthService = spotifyOAuthService;
    }

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return spotifyOAuthService.searchTracks(q, limit);
    }

    @GetMapping("/me/top/artists")
    public List<ArtistDto> myTopArtists(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "medium_term") String time_range,
            @RequestParam(defaultValue = "0") int offset) {
        return spotifyOAuthService.myTopArtists(limit, time_range, offset);
    }

    @GetMapping("/me/top/tracks")
    public List<TrackDto> myTopTracks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "medium_term") String time_range,
            @RequestParam(defaultValue = "0") int offset) {
        return spotifyOAuthService.myTopTracks(limit, time_range, offset);
    }

    @GetMapping("me/recently-played")
    public List<TrackDto> myRecentlyPlayed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) String before) {
        return spotifyOAuthService.myRecentlyPlayed(limit, after, before);
    }
}