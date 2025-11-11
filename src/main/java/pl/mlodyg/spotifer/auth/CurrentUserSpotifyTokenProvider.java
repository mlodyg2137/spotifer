package pl.mlodyg.spotifer.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import pl.mlodyg.spotifer.repositories.UserRepository;
import pl.mlodyg.spotifer.repositories.UserSpotifyTokensRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class CurrentUserSpotifyTokenProvider {
    private final UserRepository users;
    private final UserSpotifyTokensRepository tokensRepo;
    private final TextEncryptor enc;
    private final WebClient spotifyAuthClient;

    private final String clientId;
    private final String clientSecret;

    public CurrentUserSpotifyTokenProvider(
            UserRepository users,
            UserSpotifyTokensRepository tokensRepo,
            TextEncryptor enc,

            @Value("${spring.security.oauth2.client.registration.spotify-auth-code.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.spotify-auth-code.client-secret}") String clientSecret) {

        this.users = users;
        this.tokensRepo = tokensRepo;
        this.enc = enc;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.spotifyAuthClient = WebClient.builder()
                .baseUrl("https://accounts.spotify.com")
                .build();
    }

    public String getAccessTokenForCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null)
            throw new IllegalStateException("No authenticated user");

        var userId = UUID.fromString(auth.getPrincipal().toString());
        var tokens = tokensRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("No Spotify tokens for user " + userId));

        // jeśli zbliża się wygaśnięcie – odśwież
        var now = Instant.now();
        if (tokens.getExpiresAt() != null && tokens.getExpiresAt().isBefore(now.plusSeconds(60))) {
            var refreshEnc = tokens.getRefreshTokenEnc();
            if (refreshEnc != null && !refreshEnc.isBlank()) {
                var refreshPlain = decrypt(refreshEnc); // użyj tego samego TextEncryptor co gdzie indziej
                var newAccess = refreshAccessToken(refreshPlain);
                tokens.setAccessTokenEnc(encrypt(newAccess.token()));
                tokens.setExpiresAt(now.plusSeconds(newAccess.expiresIn()));
                tokens.setUpdatedAt(now);
                tokensRepo.save(tokens);
            }
        }

        return decrypt(tokens.getAccessTokenEnc());
    }

    private record TokenResponse(String token, long expiresIn) {}

    private TokenResponse refreshAccessToken(String refreshToken) {
        String basic = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        var resp = spotifyAuthClient.post()
                .uri("/api/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Basic " + basic)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();

        if (resp == null || resp.access_token() == null)
            throw new IllegalStateException("Failed to refresh access token");

        long ttl = resp.expires_in() != null ? resp.expires_in() : 3600;
        return new TokenResponse(resp.access_token(), ttl);
    }

    private record SpotifyTokenResponse(String access_token, String token_type, Integer expires_in, String scope) {}

    private String encrypt(String s) { return enc.encrypt(s); }
    private String decrypt(String s) { return enc.decrypt(s); }

}
