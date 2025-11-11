package pl.mlodyg.spotifer.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.mlodyg.spotifer.models.User;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.issuer}")
    private String issuer;
    @Value("${app.jwt.access-ttl-min}")
    private long accessTtlMin;


    public record Tokens(String accessJwt, String jti, Instant exp) {}


    public Tokens issueAccess(User u) {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(accessTtlMin));
        String jti = UUID.randomUUID().toString();


        String jwt = Jwts.builder()
                .setId(jti)
                .setIssuer(issuer)
                .setSubject(u.getId().toString())
                .claim("roles", Set.copyOf(u.getRoles()))
                .claim("name", u.getDisplayName())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
        return new Tokens(jwt, jti, exp);
    }


    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build().parseClaimsJws(token).getBody();
    }
}
