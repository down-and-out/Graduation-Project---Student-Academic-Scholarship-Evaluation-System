package com.scholarship.common.annotation;

import com.scholarship.common.enums.OperationLogTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标注在需要记录操作日志的 Controller 方法上。
 *
 * <p>由 {@link com.scholarship.common.aspect.OperationLogAspect} 切面拦截并异步写入
 * {@code sys_operation_log} 表，不阻塞业务线程。</p>
 *
 * @author Scholarship Development Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作模块，如 "用户管理"、"评定管理" */
    String module() default "";

    /** 操作类型 */
    OperationLogTypeEnum type();

    /** 操作描述，如 "提交奖学金申请" */
    String description() default "";

    /** 是否记录请求参数，默认 true */
    boolean logParams() default true;

    /** 是否记录响应结果，默认 false（避免大对象序列化） */
    boolean logResult() default false;
}
