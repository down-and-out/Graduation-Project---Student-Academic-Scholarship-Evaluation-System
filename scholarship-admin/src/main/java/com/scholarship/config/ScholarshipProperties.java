package com.scholarship.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 奖学金业务配置类
 * <p>
 * 读取 application.yml 中 scholarship.* 配置项
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "scholarship")
public class ScholarshipProperties {

    /**
     * 系统配置
     */
    private final SystemConfig system = new SystemConfig();

    /**
     * 评分配置
     */
    private final ScoreConfig score = new ScoreConfig();

    /**
     * 申请编号配置
     */
    private final ApplicationConfig application = new ApplicationConfig();

    /**
     * 系统配置类
     */
    @Data
    public static class SystemConfig {
        /**
         * 默认密码
         */
        private String defaultPassword;

        /**
         * 系统名称
         */
        private String name;

        /**
         * 系统版本
         */
        private String version;
    }

    /**
     * 评分配置类
     */
    @Data
    public static class ScoreConfig {
        /**
         * 总分
         */
        private Integer totalScore;

        /**
         * 及格分
         */
        private Integer passScore;
    }

    /**
     * 申请编号配置类
     */
    @Data
    public static class ApplicationConfig {
        /**
         * 申请编号前缀
         */
        private String numberPrefix = "SCH";
    }
}
