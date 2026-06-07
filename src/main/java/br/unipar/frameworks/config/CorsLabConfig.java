package br.unipar.frameworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsLabConfig {

    // Origens permitidas
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",   // frontend React/React Native
            "http://localhost:8080",   // a própria API
            "http://localhost:5173"    // Vite/React
    };

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // era: .allowedOrigins("*") — permite qualquer origem
                        // agora: somente origens conhecidas
                        .allowedOrigins(ALLOWED_ORIGINS)

                        // somente métodos que a API realmente usa
                        .allowedMethods("GET", "POST", "PUT", "DELETE")

                        // somente headers necessários (JWT + tipo de conteúdo)
                        .allowedHeaders("Authorization", "Content-Type", "Accept")

                        // header de autorização
                        .exposedHeaders("Authorization")

                        //permite envio de credenciais (necessário para JWT)
                        .allowCredentials(true)

                        //tempo (segundos) que o browser pode cachear o preflight OPTIONS
                        .maxAge(3600);
            }
        };
    }
}
