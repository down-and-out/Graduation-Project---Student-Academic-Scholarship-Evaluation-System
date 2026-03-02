package com.scholarship.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置 MyBatis-Plus 的核心功能，包括：
 * - 分页插件：支持各种数据库的分页查询
 * - 乐观锁插件：支持乐观锁，防止并发更新数据丢失
 * - 防止全表更新删除插件：防止误操作导致全表更新或删除
 * </p>
 *
 * 使用分页插件示例：
 * <pre>
 * Page&lt;User&gt; page = new Page&lt;&gt;(1, 10);  // 第 1 页，每页 10 条
 * userMapper.selectPage(page, null);
 * List&lt;User&gt; records = page.getRecords();   // 获取记录
 * long total = page.getTotal();                // 获取总数
 * </pre>
 *
 * 使用乐观锁示例：
 * <pre>
 * // 1. 实体类中添加 version 字段并标注@Version 注解
 * // 2. 更新时 MyBatis-Plus 会自动检查并更新版本号
 * userMapper.updateById(user);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 单页最大分页条数（防止恶意请求一次性拉取大量数据）
     */
    private static final long MAX_PAGE_LIMIT = 100L;

    /**
     * 配置 MyBatis-Plus 拦截器
     * <p>
     * 添加多个插件拦截器，实现分页、乐观锁、防全表更新删除等功能
     * </p>
     *
     * @return MybatisPlusInterceptor 对象
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ========== 1. 分页插件 ==========
        // 支持多种数据库的分页查询
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页分页条数限制（防止恶意请求一次性拉取大量数据）
        paginationInterceptor.setMaxLimit(MAX_PAGE_LIMIT);
        // 当请求的页码超过最大页数时，返回空数据（避免返回旧数据造成混淆）
        paginationInterceptor.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // ========== 2. 乐观锁插件 ==========
        // 支持乐观锁功能，防止并发更新导致数据丢失
        // 需要在实体类的 version 字段上添加@Version 注解
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // ========== 3. 防止全表更新删除插件 ==========
        // 防止误操作导致全表更新或删除
        // 一旦检测到全表更新删除操作，将抛出异常
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
