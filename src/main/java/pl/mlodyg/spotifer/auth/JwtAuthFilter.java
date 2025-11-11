package pl.mlodyg.spotifer.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.mlodyg.spotifer.models.User;
import pl.mlodyg.spotifer.repositories.UserRepository;
import pl.mlodyg.spotifer.services.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//    private final JwtService jwt;
//    private final UserRepository users;
//
//    public JwtAuthFilter(JwtService jwt, UserRepository users) {
//        this.jwt = jwt; this.users = users;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
////        String bearer = req.getHeader("Authorization");
////        if (bearer != null && bearer.startsWith("Bearer ")) {
////            try {
////                var claims = jwt.parse(bearer.substring(7));
////                var userId = UUID.fromString(claims.getSubject());
////                var user = users.findById(userId).orElse(null);
////                if (user != null && user.getStatus() == User.Status.ACTIVE) {
////                    var roles = (List<String>) claims.get("roles");
////                    var auth = new UsernamePasswordAuthenticationToken(
////                            user.getId(), null,
////                            roles.stream().map(SimpleGrantedAuthority::new).toList());
////                    SecurityContextHolder.getContext().setAuthentication(auth);
////                }
////            } catch (Exception ignored) {}
////        }
////        chain.doFilter(req, res);
//        String token = null;
//
//        // 1) Najpierw spróbuj z nagłówka
//        String bearer = req.getHeader("Authorization");
//        if (bearer != null && bearer.startsWith("Bearer ")) {
//            token = bearer.substring(7);
//        }
//
//        // 2) Fallback: cookie app_access
//        if (token == null && req.getCookies() != null) {
//            for (var c : req.getCookies()) {
//                if ("app_access".equals(c.getName())) {
//                    token = c.getValue();
//                    break;
//                }
//            }
//        }
//
//        if (token != null) {
//            try {
//                var claims = jwt.parse(token);
//                var userId = UUID.fromString(claims.getSubject());
//                var user = users.findById(userId).orElse(null);
//                if (user != null && user.getStatus() == User.Status.ACTIVE) {
//                    @SuppressWarnings("unchecked")
//                    var roles = (List<String>) claims.get("roles");
//                    var auth = new UsernamePasswordAuthenticationToken(
//                            user.getId(), null, roles.stream().map(SimpleGrantedAuthority::new).toList());
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//                }
//            } catch (Exception ignored) {}
//        }
//
//        chain.doFilter(req, res);
//    }
//}
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt; this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = null;

        // 1) z nagłówka Authorization: Bearer ...
        String bearer = req.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7);
        }

        // 2) fallback: z cookie app_access (httpOnly)
        if (token == null && req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("app_access".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                var claims = jwt.parse(token);
                var userId = UUID.fromString(claims.getSubject());
                var user = users.findById(userId).orElse(null);
                if (user != null && user.getStatus() == User.Status.ACTIVE) {
                    @SuppressWarnings("unchecked")
                    var roles = (List<String>) claims.get("roles");
                    var auth = new UsernamePasswordAuthenticationToken(
                            user.getId(), null, roles.stream().map(SimpleGrantedAuthority::new).toList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {}
        }

        chain.doFilter(req, res);
    }
}