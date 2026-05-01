package com.scholarship;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 研究生学业奖学金评定系统 - 主启动类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>@SpringBootApplication: Spring Boot核心注解，包含以下三个注解的组合</li>
 *   <li>&nbsp;&nbsp;- @Configuration: 标识为配置类</li>
 *   <li>&nbsp;&nbsp;- @EnableAutoConfiguration: 启用自动配置</li>
 *   <li>&nbsp;&nbsp;- @ComponentScan: 扫描组件（Controller、Service等）</li>
 *   <li>@MapperScan: 扫描MyBatis Mapper接口</li>
 *   <li>@EnableTransactionManagement: 启用事务管理</li>
 *   <li>@EnableCaching: 启用缓存支持</li>
 *   <li>@EnableAsync: 启用异步任务支持</li>
 * </ul>
 *
 * <p>访问地址：</p>
 * <ul>
 *   <li>系统地址: http://localhost:8080/api</li>
 *   <li>API文档: http://localhost:8080/api/doc.html</li>
 *   <li>Druid监控: http://localhost:8080/api/druid</li>
 * </ul>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(exclude = { RedisRepositoriesAutoConfiguration.class })
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan("com.scholarship.mapper")
public class ScholarshipApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ScholarshipApplication.class, args);
        System.out.println("""

                ========================================
                   研究生学业奖学金评定系统启动成功！
                ========================================
                   系统地址: http://localhost:8080/api
                   API文档: http://localhost:8080/api/doc.html
                   Druid监控: http://localhost:8080/api/druid
                ========================================
                """);
    }
}
