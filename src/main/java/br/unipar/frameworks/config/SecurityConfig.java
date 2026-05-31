// config/SecurityConfig.java
package br.unipar.frameworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
// Importa a implementação BCrypt do Spring Security.
// Importa a interface genérica de encoder de senhas do Spring Security.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration indica ao Spring que esta classe declara Beans de configuração.
// É equivalente a um arquivo XML de configuração
@Configuration
public class SecurityConfig {

    // @Bean registra o retorno deste método como um componente gerenciado pelo Spring.
    // O Spring Boot auto-detecta o SecurityFilterChain e aplica as regras de segurança
    // automaticamente em todas as requisições HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF (Cross-Site Request Forgery) é uma proteção que exige um token
                // especial em formulários para evitar ataques externos.
                // Aqui está DESABILITADA porque a API usa JWT (stateless)
                // não tem sessão/cookie para proteger.
                // Em aplicações com formulários HTML tradicional, deixaria habilitado.
                .csrf(csrf -> csrf.disable())

                // Define as regras de autorização por rota:
                .authorizeHttpRequests(auth -> auth

                        // Rotas de autenticação (/api/auth/login, /api/auth/register) são
                        // públicas — qualquer pessoa pode acessar sem estar logada.
                        .requestMatchers("/api/auth/**").permitAll()

                        // Rotas administrativas exigem que o usuário esteja autenticado
                        // E tenha a role "ADMIN". O Spring Security adiciona "ROLE_" automaticamente,
                        // então no banco o valor deve ser "ADMIN" (sem o prefixo).
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // O console do H2 é liberado para acesso sem autenticação.
                        // Necessário para fins de laboratório em produção jamais faria isso.
                        .requestMatchers("/h2-console/**").permitAll()

                        // Qualquer outra rota não mapeada acima exige que o usuário
                        // esteja autenticado (token JWT válido).
                        .anyRequest().authenticated()
                )

                // O console H2 é renderizado em um <iframe> dentro do navegador.
                // Por padrão, o Spring Security bloqueia iframes via header
                // "X-Frame-Options: DENY". Esta linha desabilita essa proteção
                // para que o console H2 consiga carregar normalmente.
                // Em produção, NUNCA desabilite isso abre brecha para Clickjacking.
                .headers(h -> h.frameOptions(f -> f.disable()));

        // Retorna a cadeia de filtros configurada.
        // O Spring Boot a registra automaticamente no pipeline de requisições HTTP.
        return http.build();
    }
    // @Bean registra este PasswordEncoder no contexto do Spring.
    // Assim ele pode ser injetado em qualquer outro componente via @Autowired
    // ou injeção por construtor (como no AuthController).
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Retorna uma instância do BCryptPasswordEncoder com strength padrão (10).
        // Isso significa que o algoritmo faz 2^10 = 1024 iterações de hash,
        // tornando ataques de força bruta extremamente lentos.
        //
        // Exemplo do que ele faz:
        //   encode("admin123")  → "$2a$10$eImiTXuWVxfM37uY4JANjQ..."  (hash diferente a cada chamada)
        //   matches("admin123", hash) → true  (compara sem precisar descriptografar)
        //
        // BCrypt é ONE-WAY (sentido único) — não é possível "descriptografar".
        // Para verificar, sempre use .matches(senhaPlana, hashSalvo).
        return new BCryptPasswordEncoder();
    }
}
