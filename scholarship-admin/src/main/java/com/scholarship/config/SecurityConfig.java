package com.scholarship.config;

import com.scholarship.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 * <p>
 * 配置系统的安全策略，包括：
 * - 认证配置：登录认证、JWT Token 认证
 * - 授权配置：接口访问权限控制
 * - 密码加密：使用 BCrypt 算法加密密码
 * - 会话管理：使用 JWT 无状态认证，不创建 Session
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity              // 启用 Web 安全功能
@EnableMethodSecurity           // 启用方法级别的安全注解（@PreAuthorize 等）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 配置 CORS
     * <p>
     * 允许前端跨域访问
     * </p>
     *
     * 注意：生产环境应将 allowedOriginPatterns 配置为具体的前端域名
     * 例如：configuration.setAllowedOriginPatterns(List.of("https://yourdomain.com"))
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的源（开发环境使用 "*"，生产环境应配置为具体的前端域名）
        // 注意：CORS 规范不允许 "*" 与 "allowCredentials=true" 同时使用
        // 但 Spring 5.3+ 支持使用 allowedOriginPatterns 来解决此限制
        configuration.setAllowedOriginPatterns(List.of("*"));
        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 允许携带认证信息
        configuration.setAllowCredentials(true);
        // 暴露给客户端的响应头
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置 HTTP 安全规则
     * <p>
     * 定义哪些接口需要认证，哪些接口可以匿名访问
     * </p>
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 对象
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== 配置 CORS ==========
                // 前后端分离架构下，需要明确的 CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ========== 禁用 CSRF ==========
                // 前后端分离架构下，使用 JWT 认证，不需要 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)

                // ========== 配置授权规则 ==========
                .authorizeHttpRequests(auth -> auth
                        // ========== 公开接口（无需认证） ==========
                        // 登录接口
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        // 测试接口（仅开发/测试环境，生产环境应移除或限制）
                        .requestMatchers("/test/**").permitAll()
                        // API 文档接口（开发环境开放）
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Druid 监控页面（仅开发环境，生产环境应禁用或添加认证）
                        // 安全提示：生产环境应通过 IP 限制或添加访问认证
                        .requestMatchers("/druid/**").permitAll()
                        // 静态资源
                        .requestMatchers("/static/**", "/assets/**").permitAll()

                        // ========== 其他所有接口都需要认证 ==========
                        .anyRequest().authenticated()
                )

                // ========== 配置会话管理 ==========
                // 使用 JWT 无状态认证，不创建 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========== 配置认证提供者 ==========
                .authenticationProvider(authenticationProvider())

                // ========== 添加 JWT 认证过滤器 ==========
                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置密码编码器
     * <p>
     * 使用 BCrypt 算法对密码进行加密
     * BCrypt 是一种自适应的单向加密算法，每次加密结果都不同（使用了盐值）
     * </p>
     *
     * @return PasswordEncoder 对象
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证提供者
     * <p>
     * 认证提供者负责：
     * 1. 从数据库加载用户信息
     * 2. 验证密码是否正确
     * </p>
     *
     * @return AuthenticationProvider 对象
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置 UserDetailsService（从数据库加载用户）
        authProvider.setUserDetailsService(customUserDetailsService);
        // 设置密码编码器（验证密码时使用）
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置认证管理器
     * <p>
     * 认证管理器用于处理认证请求
     * 登录时使用 AuthenticationManager 来验证用户名和密码
     * </p>
     *
     * @param config 认证配置对象
     * @return AuthenticationManager 对象
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
