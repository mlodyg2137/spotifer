package pl.mlodyg.spotifer.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
@EnableWebSecurity
public class OAuth2ClientServletConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientRepository authorizedClients) {

        var manager = new DefaultOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .clientCredentials()
                        .refreshToken()
                        .build());
        return manager;
    }

    private WebClient.Builder baseSpotifyBuilder(ServletOAuth2AuthorizedClientExchangeFilterFunction filter) {
        // Bezpieczne limity rozmiaru odpowiedzi + timeouty (dzięki Reactor Netty zamiast RestTemplate)
        return WebClient.builder()
                .baseUrl("https://api.spotify.com")
                .filter(filter)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4MB
                        .build())
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                        reactor.netty.http.client.HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(8))
                                .compress(true)
                ));
    }

    @Bean(name = "spotifyClientCredWebClient")
    public WebClient spotifyClientCredWebClient(OAuth2AuthorizedClientManager manager) {
        var filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        filter.setDefaultClientRegistrationId("spotify-client-cred");
//        filter.setDefaultOAuth2AuthorizedClient(true);
        return baseSpotifyBuilder(filter).build();
    }

    @Bean(name = "spotifyAuthCodeWebClient")
    public WebClient spotifyAuthCodeWebClient(OAuth2AuthorizedClientManager manager) {
        var filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        filter.setDefaultClientRegistrationId("spotify-auth-code");
        filter.setDefaultOAuth2AuthorizedClient(true);
        return baseSpotifyBuilder(filter).build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/spotify/search/**").permitAll()
                        .anyRequest().authenticated()
                )
//                .oauth2Login(o -> {}) // jeśli część API ma korzystać z loginu przeglądarkowego
//                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .requestCache(rc -> rc.disable())
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                ));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/whoami",
                                "/api/**",
                                "/oauth2/authorization/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }
}
