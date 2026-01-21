package pl.mlodyg.spotifer.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.mlodyg.spotifer.dto.TopArtistDto;
import pl.mlodyg.spotifer.dto.TopTrackDto;
import pl.mlodyg.spotifer.services.ProfileService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileControllerTest {

    private MockMvc mockMvc;
    private ProfileService profileService;

    @BeforeEach
    void setup() {
        profileService = Mockito.mock(ProfileService.class);

        ProfileController controller = new ProfileController(profileService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private TopArtistDto getSampleArtist(String name) {
        TopArtistDto artist = new TopArtistDto();
        artist.setName(name);
        return artist;
    }

    private TopTrackDto getSampleTopTrack(String name) {
        TopTrackDto track = new TopTrackDto();
        track.setTrackName(name);
        return track;
    }

    @Test
    void shouldReturnTopTracksForUser() throws Exception {
        UUID userId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        List<TopTrackDto> tracks = List.of(
                getSampleTopTrack("Track 1"),
                getSampleTopTrack("Track 2")
        );

        when(profileService.getTopTracksForUser(userId, 20, "medium_term", 0, false))
                .thenReturn(tracks);

        mockMvc.perform(get("/api/v1/profile/top/tracks").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trackName").value("Track 1"));

        verify(profileService).getTopTracksForUser(userId, 20, "medium_term", 0, false);
    }

    @Test
    void shouldReturnTopArtistsForUser() throws Exception {
        UUID userId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        List<TopArtistDto> artists = List.of(
                getSampleArtist("Artist 1"),
                getSampleArtist("Artist 2")
        );

        when(profileService.getTopArtistsForUser(userId, 20, "medium_term", 0, false))
                .thenReturn(artists);

        mockMvc.perform(get("/api/v1/profile/top/artists").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Artist 1"));

        verify(profileService).getTopArtistsForUser(userId, 20, "medium_term", 0, false);
    }

    @Test
    void shouldReturnRecentlyPlayedForUser() throws Exception {
        UUID userId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        when(profileService.getRecentlyPlayedTracksForUser(userId, 20, null, null, false))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/profile/recently-played").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(profileService).getRecentlyPlayedTracksForUser(userId, 20, null, null, false);
    }
}