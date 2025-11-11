package pl.mlodyg.spotifer.services;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mlodyg.spotifer.models.User;
import pl.mlodyg.spotifer.models.UserSpotifyTokens;
import pl.mlodyg.spotifer.repositories.UserRepository;
import pl.mlodyg.spotifer.repositories.UserSpotifyTokensRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SpotifyAccountLinkService {
    private final UserRepository users;
    private final UserSpotifyTokensRepository tokensRepo;
    private final TextEncryptor enc;


    public SpotifyAccountLinkService(UserRepository users, UserSpotifyTokensRepository tokensRepo, TextEncryptor enc) {
        this.users = users; this.tokensRepo = tokensRepo; this.enc = enc;
    }


//    @Transactional
//    public User upsertFromOAuth(OAuth2User oUser, OAuth2AuthorizedClient client) {
//        String spId = oUser.getAttribute("id");
//
//        var user = users.findBySpotifyUserId(spId).orElseGet(() -> {
//            User u = new User();
//            u.setSpotifyUserId(spId);
//            return users.save(u);
//        });
//
//        user.setDisplayName(oUser.<String>getAttribute("display_name"));
//        user.setAvatarUrl(extractAvatar(oUser));
//        user.setEmail(oUser.<String>getAttribute("email"));
//        user.setCountry(oUser.<String>getAttribute("country"));
//        user.setProduct(oUser.<String>getAttribute("product"));
//        users.save(user);
//
//        var stored = tokensRepo.findById(user.getId()).orElseGet(() -> {
//            var t = new UserSpotifyTokens();
//            t.setUser(user);
//            t.setUserId(user.getId());
//            return t;
//        });
//
//        stored.setAccessTokenEnc(enc.encrypt(client.getAccessToken().getTokenValue()));
//        stored.setRefreshTokenEnc(enc.encrypt(Objects.requireNonNull(client.getRefreshToken()).getTokenValue()));
//        stored.setExpiresAt(client.getAccessToken().getExpiresAt());
//        stored.setScope(new HashSet<>(client.getAccessToken().getScopes()));
//        stored.setUpdatedAt(Instant.now());
//        tokensRepo.save(stored);
//
//        return user;
//    }
    @Transactional
    public User upsertFromOAuth(OAuth2User oUser, OAuth2AuthorizedClient client) {
        String spId = oUser.getAttribute("id");

        // upsert user
        var user = users.findBySpotifyUserId(spId).orElseGet(() -> {
            var u = new User();
            u.setSpotifyUserId(spId);
            return users.save(u);
        });
        user.setDisplayName(oUser.<String>getAttribute("display_name"));
        user.setAvatarUrl(extractAvatar(oUser));
        user.setEmail(oUser.<String>getAttribute("email"));
        user.setCountry(oUser.<String>getAttribute("country"));
        user.setProduct(oUser.<String>getAttribute("product"));
        users.save(user);

        // upsert tokens (UWAGA: bez setUserId()!)
        var stored = tokensRepo.findById(user.getId()).orElse(null);
        if (stored == null) {
            stored = new UserSpotifyTokens();
            stored.setUser(user);            // @MapsId skopiuje user.id jako PK przy persist
            // NIE: stored.setUserId(user.getId());
        }

        var access = client.getAccessToken();
        stored.setAccessTokenEnc(enc.encrypt(access.getTokenValue()));
        stored.setExpiresAt(access.getExpiresAt());
        stored.setScope(new HashSet<>(access.getScopes()));
        stored.setUpdatedAt(Instant.now());

        if (client.getRefreshToken() != null) {
            stored.setRefreshTokenEnc(enc.encrypt(client.getRefreshToken().getTokenValue()));
        }

        tokensRepo.save(stored); // jeśli nowy → persist; jeśli istniejący → merge na managed — OK
        return user;
    }


    private String extractAvatar(OAuth2User o) {
        var images = (List<Map<String,Object>>) o.getAttribute("images");
        if (images != null && !images.isEmpty()) return (String) images.get(0).get("url");
        return null;
    }
}
