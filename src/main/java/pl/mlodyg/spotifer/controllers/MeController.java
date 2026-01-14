package pl.mlodyg.spotifer.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.mlodyg.spotifer.dto.MeDto;
import pl.mlodyg.spotifer.models.User;
import pl.mlodyg.spotifer.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final UserRepository users;
    public MeController(UserRepository users) { this.users = users; }


//    @GetMapping
//    public Map<String, Object> me(Authentication auth) {
//        UUID uid = (UUID) auth.getPrincipal();
//        User u = users.findById(uid).orElseThrow();
//
//        Map<String, Object> out = new HashMap<>();
//
//        out.put("id", u.getId());
//        out.put("spotifyUserId", u.getSpotifyUserId());
//        out.put("displayName", u.getDisplayName());
//        out.put("avatarUrl", u.getAvatarUrl());
//        out.put("roles", u.getRoles());
//
//        return out;
//    }
    @GetMapping
    public MeDto me(Authentication auth) {
        UUID uid = (UUID) auth.getPrincipal();
        User u = users.findById(uid).orElseThrow();

        MeDto me = new MeDto();
        me.setId(u.getId().toString());
        me.setSpotifyUserId(u.getSpotifyUserId());
        me.setDisplayName(u.getDisplayName());
        me.setAvatarUrl(u.getAvatarUrl());
        me.setRoles(u.getRoles());

        return me;
    }
}
