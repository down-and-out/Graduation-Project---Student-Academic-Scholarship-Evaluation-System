package com.scholarship.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointRateLimitFilter extends OncePerRequestFilter {

    private static final String KEY_PREFIX = "rate-limit:";

    private static final DefaultRedisScript<Long> INCR_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return count",
            Long.class
    );

    private final ScholarshipProperties scholarshipProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ScholarshipProperties.RateLimitConfig config = scholarshipProperties.getRateLimit();
        if (!config.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        ScholarshipProperties.EndpointLimit endpointLimit = resolveLimit(request.getMethod(), uri, config);
        if (endpointLimit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        String actorId = resolveActorId();
        if (isExceeded(uri, "ip", clientIp, endpointLimit.getIpLimit(), endpointLimit.getIpWindowSeconds())
                || (actorId != null && isExceeded(uri, "actor", actorId, endpointLimit.getActorLimit(), endpointLimit.getActorWindowSeconds()))) {
            log.warn("Endpoint rate limit triggered, uri={}, clientIp={}, actorId={}", uri, clientIp, actorId);
            writeTooManyRequests(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private ScholarshipProperties.EndpointLimit resolveLimit(String method, String uri,
                                                             ScholarshipProperties.RateLimitConfig config) {
        if ("POST".equalsIgnoreCase(method) && uri.endsWith("/auth/login")) {
            return config.getLogin();
        }
        if ("POST".equalsIgnoreCase(method) && uri.endsWith("/auth/register")) {
            return config.getRegister();
        }
        if ("POST".equalsIgnoreCase(method) && uri.endsWith("/application/submit")) {
            return config.getApplicationSubmit();
        }
        if ("POST".equalsIgnoreCase(method) && uri.contains("/evaluation-result/evaluate/")) {
            return config.getEvaluationTrigger();
        }
        if ("GET".equalsIgnoreCase(method) && uri.endsWith("/evaluation-result/export")) {
            return config.getExport();
        }
        return null;
    }

    private boolean isExceeded(String uri, String scope, String actor, int limit, int windowSeconds) {
        if (actor == null || actor.isBlank() || limit <= 0 || windowSeconds <= 0) {
            return false;
        }
        String key = KEY_PREFIX + uri + ":" + scope + ":" + actor;
        try {
            Long count = redisTemplate.execute(INCR_WITH_EXPIRE_SCRIPT,
                    Collections.singletonList(key), String.valueOf(windowSeconds));
            if (count == null) {
                count = 1L;
            }
            return count > limit;
        } catch (Exception e) {
            log.error("Rate limit Redis operation failed, key={}, degrading to pass-through", key, e);
            return false;
        }
    }

    private String resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return authentication.getName();
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(429, "请求过于频繁，请稍后再试")));
    }
}
