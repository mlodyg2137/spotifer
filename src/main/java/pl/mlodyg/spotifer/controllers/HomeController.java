package pl.mlodyg.spotifer.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class HomeController {
    @GetMapping("/")
    @ResponseBody
    String home() { return "Public home OK"; }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(
            Authentication auth,
            @RegisteredOAuth2AuthorizedClient("spotify-auth-code") OAuth2AuthorizedClient client
    ) {
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("authenticated", auth != null && auth.isAuthenticated());
        out.put("principal", auth != null ? auth.getName() : null);
        out.put("hasAuthorizedClient", client != null);
        out.put("accessTokenExpiresAt", client != null ? client.getAccessToken().getExpiresAt() : null);
        return out;
    }
}
