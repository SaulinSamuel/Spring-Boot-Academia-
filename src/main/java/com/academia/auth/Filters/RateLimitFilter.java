package com.academia.auth.Filters;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("!test")
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> CAMINHOS_EXCLUIDOS = List.of(
        "/actuator/**",
        "/error"
    );

    private static final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(50_000)
        .build();

    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return CAMINHOS_EXCLUIDOS.stream()
            .anyMatch(padrao -> PATH_MATCHER.match(padrao, path));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException
    {

        String ip = resolverIpCliente(request);

        Bucket bucket = buckets.get(ip, chave -> criarBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        long segundos = (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000);

        response.setStatus(429);
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.getWriter().write(
            "{\"erro\":\"Muitas requisições. Tente novamente mais tarde.\"}"
        );
    }

    private Bucket criarBucket() {
 
        Bandwidth limit = Bandwidth.builder()
            .capacity(10)
            .refillGreedy(1, Duration.ofSeconds(6))
            .build();
 
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private String resolverIpCliente(HttpServletRequest request) {
 
        String forwarded = request.getHeader("X-Forwarded-For");
 
        if (forwarded != null && !forwarded.isBlank()) {
            
            return forwarded.split(",")[0].trim();
        }
 
        return request.getRemoteAddr();
    }

}
