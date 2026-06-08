package br.unipar.frameworks.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Armazena um bucket por IP ConcurrentHashMap é thread-safe
    // Em produção real, usar Redis para compartilhar entre múltiplas instâncias
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Limite GERAL: 60 requisições por minuto por IP
    // Reabastece 60 tokens a cada 60 segundos (1 token/segundo)
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(
                60,                          // capacidade máxima: 60 tokens
                Refill.greedy(60, Duration.ofMinutes(1)) // reabastece 60/min
        );
        return Bucket.builder().addLimit(limit).build();
    }

    // Limite para /auth/**: 10 tentativas por minuto por IP
    // Protege contra brute force em login e registro
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
                10,                          // capacidade máxima: 10 tokens
                Refill.greedy(10, Duration.ofMinutes(1)) // reabastece 10/min
        );
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Obtém o IP real do cliente
        // X-Forwarded-For é preenchido por proxies e load balancers
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        String path = request.getRequestURI();

        // Chave única por IP + tipo de rota (auth ou geral)
        // Isso garante que o limite de auth não consome do limite geral
        boolean isAuthRoute = path.startsWith("/api/auth") || path.startsWith("/auth");
        String bucketKey = (isAuthRoute ? "auth:" : "general:") + ip;

        // getOrDefault cria o bucket na primeira requisição do IP
        // e reutiliza o existente nas requisições seguintes
        Bucket bucket = buckets.computeIfAbsent(bucketKey, k ->
                isAuthRoute ? createAuthBucket() : createGeneralBucket()
        );

        // Tenta consumir 1 token
        // se houver tokens disponíveis, libera a requisição
        if (bucket.tryConsume(1)) {
            // Adiciona headers informativos para o cliente saber seu limite
            long remainingTokens = bucket.getAvailableTokens();
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
            response.setHeader("X-RateLimit-Limit", isAuthRoute ? "10" : "60");

            filterChain.doFilter(request, response);
        } else {
            // Sem tokens disponíveis bloqueia a requisição
            log.warn("Rate limit excedido para IP: {} na rota: {}", ip, path);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                {
                    "status": 429,
                    "message": "Muitas requisicoes. Tente novamente em instantes.",
                    "timestamp": "%s"
                }
                """.formatted(java.time.LocalDateTime.now()));
        }
    }
}
