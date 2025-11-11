package pl.mlodyg.spotifer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pl.mlodyg.spotifer.auth.CurrentUserSpotifyTokenProvider;

@Configuration
public class SpotifyUserWebClientConfig {
    @Bean(name = "spotifyUserWebClient")
    public WebClient spotifyUserWebClient(CurrentUserSpotifyTokenProvider tokenProvider) {
        ExchangeFilterFunction authFilter = (request, next) -> {
            String token = tokenProvider.getAccessTokenForCurrentUser();
            return next.exchange(
                    ClientRequest.from(request)
                            .headers(h -> h.setBearerAuth(token))
                            .build()
            );
        };

        return WebClient.builder()
                .baseUrl("https://api.spotify.com")
                .filter(authFilter)
                .build();
    }
}
