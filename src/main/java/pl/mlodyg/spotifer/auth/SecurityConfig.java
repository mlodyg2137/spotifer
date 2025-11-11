package pl.mlodyg.spotifer.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final SpotifyLoginSuccessHandler successHandler;


    public SecurityConfig(JwtAuthFilter jwtAuthFilter, SpotifyLoginSuccessHandler successHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.successHandler = successHandler;
    }


    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception { return buildApi(http); }


    @Bean
    @Order(2)
    SecurityFilterChain web(HttpSecurity http) throws Exception { return buildWeb(http); }


    private SecurityFilterChain buildApi(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/api/spotify/search").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED)));
        return http.build();
    }


    private SecurityFilterChain buildWeb(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/assets/**", "/swagger/**", "/api-docs/**", "/login/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(o -> o.successHandler(successHandler))
                .logout(l -> l.logoutSuccessUrl("/"));
        return http.build();
    }
}
