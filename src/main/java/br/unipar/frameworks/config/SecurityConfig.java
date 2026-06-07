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

    //Injeta o CorsConfigurationSource definido no CorsLabConfig.java
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //CORS habilitado e apontando para o CorsLabConfig
                // Sem isso Spring Security bloqueia ANTES do filtro CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF desabilitado (API stateless com JWT)
                .csrf(csrf -> csrf.disable())

                // Sessão stateless cada requisição precisa do token JWT
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Rotas de auth com e sem prefixo /api/ (cobre os dois casos)
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()

                        // Swagger liberado para acesso sem autenticação
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Console H2 liberado
                        .requestMatchers("/h2-console/**").permitAll()

                        // Rotas admin exigem role ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Tudo o resto exige autenticação
                        .anyRequest().authenticated()
                )

                // Desabilita proteção de iframe para o console H2 funcionar
                .headers(h -> h.frameOptions(f -> f.disable()))

                // JWT roda antes do filtro padrão de usuário/senha
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}