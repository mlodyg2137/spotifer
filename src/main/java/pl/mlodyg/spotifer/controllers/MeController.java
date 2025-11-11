package pl.mlodyg.spotifer.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.mlodyg.spotifer.models.User;
import pl.mlodyg.spotifer.repositories.UserRepository;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final UserRepository users;
    public MeController(UserRepository users) { this.users = users; }


    @GetMapping
    public Map<String, Object> me(Authentication auth) {
        UUID uid = (UUID) auth.getPrincipal();
        User u = users.findById(uid).orElseThrow();
        return Map.of(
                "id", u.getId(),
                "spotifyUserId", u.getSpotifyUserId(),
                "displayName", u.getDisplayName(),
                "avatarUrl", u.getAvatarUrl(),
                "roles", u.getRoles()
        );
    }
}
