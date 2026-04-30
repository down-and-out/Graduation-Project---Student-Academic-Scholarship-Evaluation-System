package com.scholarship.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 实现 MetaObjectHandler 接口，自动填充实体类中的创建时间和更新时间字段。
 * 配合 @TableField(fill = FieldFill.INSERT) 和 @TableField(fill = FieldFill.INSERT_UPDATE) 注解使用。
 * </p>
 *
 * <p>
 * 自动填充规则：
 * - insertFill: 插入操作时，自动填充 createTime 和 updateTime 为当前时间
 * - updateFill: 更新操作时，自动填充 updateTime 为当前时间
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时的自动填充策略
     * <p>
     * 在插入数据时自动填充以下字段：
     * - createTime: 创建时间
     * - updateTime: 更新时间
     * </p>
     *
     * @param metaObject 元数据对象，包含实体类的属性信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始执行插入自动填充...");

        // 填充创建时间（严格填充策略：只有字段值为 null 时才填充）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        // 填充更新时间（严格填充策略：只有字段值为 null 时才填充）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 填充乐观锁版本号（严格填充策略：只有字段值为 null 时才填充为 1）
        this.strictInsertFill(metaObject, "version", Integer.class, 1);

        log.debug("插入自动填充完成: createTime={}, updateTime={}, version={}",
                this.getFieldValByName("createTime", metaObject),
                this.getFieldValByName("updateTime", metaObject),
                this.getFieldValByName("version", metaObject));
    }

    /**
     * 更新操作时的自动填充策略
     * <p>
     * 在更新数据时自动填充以下字段：
     * - updateTime: 更新时间
     * </p>
     *
     * @param metaObject 元数据对象，包含实体类的属性信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始执行更新自动填充...");

        // 填充更新时间（严格填充策略：只有字段值为 null 时才填充）
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        log.debug("更新自动填充完成: updateTime={}",
                this.getFieldValByName("updateTime", metaObject));
    }
}
