package com.scholarship.config;

import com.scholarship.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 该过滤器在每个请求到达 Controller 之前执行，负责：
 * 1. 从请求头中提取 JWT Token
 * 2. 验证 Token 的有效性
 * 3. 如果 Token 有效，将用户信息设置到 SecurityContext 中
 * 4. 如果 Token 无效或不存在，继续执行后续过滤器链
 * </p>
 *
 * 注意：生产环境应避免记录完整 Token 到日志，防止泄露
 *
 * 继承 OncePerRequestFilter 确保每个请求只执行一次过滤
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * JWT 请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 过滤器核心方法
     * <p>
     * 处理流程：
     * 1. 从请求头中提取 Token
     * 2. 检查 Token 是否在黑名单中
     * 3. 解析 Token 获取用户名
     * 4. 从数据库加载用户详细信息
     * 5. 验证 Token 有效性
     * 6. 将认证信息设置到 SecurityContext
     * </p>
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 从请求头中提取 JWT Token
            String token = extractTokenFromRequest(request);

            // 如果 Token 存在且当前没有认证信息
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 检查 Token 是否在黑名单中（用户已登出）
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Token 已在黑名单中，拒绝访问");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 从 Token 中提取用户名
                String username = jwtUtil.extractUsername(token);

                // 如果用户名不为空
                if (username != null) {
                    // 从数据库加载用户详细信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证 Token 是否有效
                    if (jwtUtil.validateToken(token, username)) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // 设置认证详情（包含 IP 地址等信息）
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 将认证信息设置到 SecurityContext 中
                        // 后续的过滤器或 Controller 可以通过 SecurityContext 获取当前用户信息
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户 [{}] 认证成功", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证失败：认证过程中发生异常");
            // 清空 SecurityContext，确保未认证用户不能访问受限资源
            SecurityContextHolder.clearContext();
            // 注意：不记录完整异常消息，避免泄露敏感信息
            log.debug("JWT 认证异常详情：", e);
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     * <p>
     * Token 格式：Authorization: Bearer {token}
     * </p>
     *
     * @param request HTTP 请求对象
     * @return JWT Token，如果不存在返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 从请求头中获取 Authorization
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // 检查是否存在且以"Bearer "开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 去掉"Bearer "前缀，返回纯净的 Token
            return bearerToken.substring(7);
        }

        return null;
    }
}
