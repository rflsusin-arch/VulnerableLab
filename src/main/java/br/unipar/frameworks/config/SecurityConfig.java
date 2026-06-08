package br.unipar.frameworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CorsConfigurationSource corsConfigurationSource,
                          RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .headers(h -> h.frameOptions(f -> f.disable()))

                // Ambos os filtros registrados antes do UsernamePasswordAuthenticationFilter
                // RateLimit roda primeiro (ordem de adição importa aqui)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
