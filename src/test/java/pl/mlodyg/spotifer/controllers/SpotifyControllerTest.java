package pl.mlodyg.spotifer.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.mlodyg.spotifer.dto.ArtistDto;
import pl.mlodyg.spotifer.dto.TrackDto;
import pl.mlodyg.spotifer.services.SpotifyService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpotifyControllerTest {

    private MockMvc mockMvc;
    private SpotifyService spotifyService;

    @BeforeEach
    void setup() {
        spotifyService = Mockito.mock(SpotifyService.class);

        SpotifyController controller = new SpotifyController(spotifyService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    TrackDto.Artist getSampleArtist(String name) {
        TrackDto.Artist artist = new TrackDto.Artist();

        artist.setSpotifyId("spotifyId" + name);
        artist.setName(name);
        artist.setImageUrl("imageUrl");
        artist.setPopularity(0);

        return artist;
    }

    @Test
    void shouldReturnTopTracks() throws Exception {
        List<TrackDto> tracks = List.of(
                new TrackDto("id1", "Track 1", List.of(getSampleArtist("Artist 1"))),
                new TrackDto("id2", "Track 2", List.of(getSampleArtist("Artist 2"))
        ));

        when(spotifyService.myTopTracks(10, "medium_term", 0))
                .thenReturn(tracks);

        mockMvc.perform(get("/api/v1/spotify/me/top/tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Track 1"));

        verify(spotifyService).myTopTracks(10, "medium_term", 0);
    }

    @Test
    void shouldReturnTopArtists() throws Exception {
        List<ArtistDto> artists = List.of(
                new ArtistDto(getSampleArtist("Artist 1")),
                new ArtistDto(getSampleArtist("Artist 2"))
        );

        when(spotifyService.myTopArtists(10, "medium_term", 0))
                .thenReturn(artists);

        mockMvc.perform(get("/api/v1/spotify/me/top/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Artist 1"));

        verify(spotifyService).myTopArtists(10, "medium_term", 0);
    }

    @Test
    void shouldReturnRecentlyPlayed() throws Exception {
        when(spotifyService.myRecentlyPlayed(10, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/spotify/me/recently-played"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

}