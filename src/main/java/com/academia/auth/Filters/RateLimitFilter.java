package com.academia.auth.Filters;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("!test")
public class RateLimitFilter implements Filter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException
    {

        String ip = request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(ip, chave -> {
            
            Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(1, Duration.ofSeconds(6))
                .build();

            return Bucket.builder()
                .addLimit(limit)
                .build();
        });

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            chain.doFilter(request, httpServletResponse);
        } else {

            long nanos = probe.getNanosToWaitForRefill();

            long segundos = (long) Math.ceil(nanos / 1_000_000_000.0);

            httpServletResponse.setStatus(429);
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setHeader("Retry-After", String.valueOf(segundos));

            httpServletResponse.getWriter().write(
                "{\"erro\":\"Muitas requisições. Tente novamente mais tarde.\"}"
            );
            return;
        }
    }

}
