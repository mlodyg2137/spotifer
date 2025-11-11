package pl.mlodyg.spotifer.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.mlodyg.spotifer.services.JwtService;
import pl.mlodyg.spotifer.services.SpotifyAccountLinkService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
public class SpotifyLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final ClientRegistrationRepository registrations;
    private final OAuth2AuthorizedClientService clientService;
    private final SpotifyAccountLinkService linker;
    private final JwtService jwt;


    @Value("${app.auth.post-login-redirect}") String redirectUrl;
    @Value("${app.auth.cookie-domain}") String cookieDomain;


    public SpotifyLoginSuccessHandler(ClientRegistrationRepository registrations,
                                      OAuth2AuthorizedClientService clientService,
                                      SpotifyAccountLinkService linker,
                                      JwtService jwt) {
        this.registrations = registrations;
        this.clientService = clientService;
        this.linker = linker;
        this.jwt = jwt;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var oauth = (OAuth2AuthenticationToken) authentication;
        var client = clientService.loadAuthorizedClient(oauth.getAuthorizedClientRegistrationId(), oauth.getName());
        OAuth2AuthorizedClient c = new OAuth2AuthorizedClient(
                registrations.findByRegistrationId(oauth.getAuthorizedClientRegistrationId()),
                oauth.getName(),
                client.getAccessToken(),
                client.getRefreshToken()
        );


        OAuth2User oUser = (OAuth2User) authentication.getPrincipal();
        var user = linker.upsertFromOAuth(oUser, c);


        var tokens = jwt.issueAccess(user);
        Cookie cookie = new Cookie("app_access", tokens.accessJwt());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // prod: true
        cookie.setPath("/");
        cookie.setDomain("127.0.0.1"); // prod: cookieDomain
        cookie.setMaxAge((int) Duration.between(Instant.now(), tokens.exp()).toSeconds());
        cookie.setDomain(cookieDomain);
        response.addCookie(cookie);


        response.sendRedirect(redirectUrl);
    }
}
