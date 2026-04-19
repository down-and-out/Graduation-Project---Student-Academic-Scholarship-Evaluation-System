package com.scholarship.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Knife4j (OpenAPI 3) 配置类
 * <p>
 * 配置 API 文档生成的相关信息，包括：
 * - API 基本信息（标题、描述、版本等）
 * - 分组管理
 * - JWT 认证配置
 * - 服务器信息
 * </p>
 *
 * 访问地址：http://localhost:8080/api/doc.html
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Configuration
public class Knife4jConfig {

    /**
     * API 基本信息常量
     */
    private static final String API_TITLE = "研究生学业奖学金评定系统 API";
    private static final String API_DESCRIPTION = "提供研究生学业奖学金评定系统的所有接口文档";
    private static final String API_VERSION = "1.0.0";
    private static final String API_LICENSE = "MIT";
    private static final String API_LICENSE_URL = "https://opensource.org/licenses/MIT";

    /**
     * JWT 认证相关的常量
     */
    private static final String SECURITY_SCHEME_NAME = "JWT 认证";
    private static final String SECURITY_SCHEME_DESCRIPTION = "请输入 JWT Token（格式：Bearer {token}）";

    /**
     * 配置 OpenAPI 基本信息
     * <p>
     * 这里配置文档的标题、描述、版本、许可协议等信息
     * </p>
     *
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title(API_TITLE)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION)
                        // 联系方式
                        .contact(new Contact()
                                .name("Scholarship Development Team")
                                .email("support@scholarship.com"))
                        // 许可协议
                        .license(new License()
                                .name(API_LICENSE)
                                .url(API_LICENSE_URL)))
                // JWT 认证配置
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(SECURITY_SCHEME_DESCRIPTION)))
                // 注意：不在全局设置安全要求，因为部分接口（登录、注册、API 文档等）是公开的
                // 需要在具体接口上使用 @SecurityRequirement 注解来标识需要 JWT 认证的接口
                // 服务器信息
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("本地开发环境"),
                        new Server().url("/api").description("当前服务器")
                ));
    }

    /**
     * 配置 API 分组 - 认证管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-认证管理")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * 配置 API 分组 - 研究生信息管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi studentInfoApi() {
        return GroupedOpenApi.builder()
                .group("02-研究生信息管理")
                .pathsToMatch("/student-info/**")
                .build();
    }

    /**
     * 配置 API 分组 - 科研论文管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi researchPaperApi() {
        return GroupedOpenApi.builder()
                .group("03-科研论文管理")
                .pathsToMatch("/paper/**")
                .build();
    }

    /**
     * 配置 API 分组 - 科研专利管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi researchPatentApi() {
        return GroupedOpenApi.builder()
                .group("04-科研专利管理")
                .pathsToMatch("/research-patent/**")
                .build();
    }

    /**
     * 配置 API 分组 - 科研项目管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi researchProjectApi() {
        return GroupedOpenApi.builder()
                .group("05-科研项目管理")
                .pathsToMatch("/research-project/**")
                .build();
    }

    /**
     * 配置 API 分组 - 学科竞赛获奖管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi competitionAwardApi() {
        return GroupedOpenApi.builder()
                .group("06-学科竞赛获奖管理")
                .pathsToMatch("/competition-award/**")
                .build();
    }

    /**
     * 配置 API 分组 - 评定批次管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi evaluationBatchApi() {
        return GroupedOpenApi.builder()
                .group("07-评定批次管理")
                .pathsToMatch("/evaluation-batch/**")
                .build();
    }

    /**
     * 配置 API 分组 - 评分规则管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi scoreRuleApi() {
        return GroupedOpenApi.builder()
                .group("08-评分规则管理")
                .pathsToMatch("/score-rule/**", "/rule-category/**")
                .build();
    }

    /**
     * 配置 API 分组 - 结果异议管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi resultAppealApi() {
        return GroupedOpenApi.builder()
                .group("09-结果异议管理")
                .pathsToMatch("/result-appeal/**")
                .build();
    }

    /**
     * 配置 API 分组 - 奖学金申请管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi scholarshipApplicationApi() {
        return GroupedOpenApi.builder()
                .group("10-奖学金申请管理")
                .pathsToMatch("/application/**")
                .build();
    }

    /**
     * 配置 API 分组 - 评定结果管理模块
     *
     * @return GroupedOpenApi 对象
     */
    @Bean
    public GroupedOpenApi evaluationResultApi() {
        return GroupedOpenApi.builder()
                .group("11-评定结果管理")
                .pathsToMatch("/evaluation-result/**")
                .build();
    }
}
