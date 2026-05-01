package com.scholarship.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.common.annotation.OperationLog;
import com.scholarship.common.support.WebUtil;
import com.scholarship.entity.SysOperationLog;
import com.scholarship.mapper.SysOperationLogMapper;
import com.scholarship.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面。
 *
 * <p>拦截所有标注了 {@link OperationLog} 的方法，自动采集操作人、IP、URL、
 * 执行耗时等信息，写入 {@code sys_operation_log} 表。</p>
 *
 * <p>日志写入为同步调用（单条 INSERT 耗时 &lt;5ms），不给业务方法增加事务
 * 边界——日志写入失败不会回滚业务操作。</p>
 *
 * @author Scholarship Development Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper sysOperationLogMapper;

    /** 序列化请求参数/响应结果时限制长度 */
    private static final int MAX_DATA_LENGTH = 2000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Pointcut("@annotation(com.scholarship.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog opLog = method.getAnnotation(OperationLog.class);

        // 执行原方法
        Object result;
        int status = 1;  // 1=成功
        String errorMsg = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            status = 0;  // 0=失败
            errorMsg = truncate(ex.getMessage());
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            try {
                if (sysOperationLogMapper != null) {
                    SysOperationLog logEntry = buildLogEntry(opLog, joinPoint, status, errorMsg, executionTime);
                    sysOperationLogMapper.insert(logEntry);
                }
            } catch (Exception e) {
                log.warn("Failed to write operation log", e);
            }
        }

        return result;
    }

    private SysOperationLog buildLogEntry(OperationLog opLog, ProceedingJoinPoint joinPoint,
                                          int status, String errorMsg, long executionTime) {
        SysOperationLog entry = new SysOperationLog();

        // 操作类型
        entry.setOperationType(opLog.type().getCode());
        entry.setModule(opLog.module());
        entry.setDescription(opLog.description());

        // 操作人（从 SecurityContext 获取）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            entry.setOperatorId(loginUser.getUserId());
            entry.setOperatorName(loginUser.getRealName());
        }

        // 方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        entry.setOperation(signature.getDeclaringTypeName() + "." + signature.getName());
        entry.setRequestMethod(signature.getName());

        // 请求参数
        if (opLog.logParams()) {
            entry.setRequestParams(truncate(toJsonString(joinPoint.getArgs())));
        }

        // 请求信息（从 HttpServletRequest 获取）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entry.setRequestUrl(request.getRequestURI());
            entry.setOperatorIp(WebUtil.getClientIp(request));
        }

        // 执行状态
        entry.setStatus(status);
        entry.setErrorMsg(errorMsg);
        entry.setExecutionTime(executionTime);
        entry.setCreateTime(LocalDateTime.now());

        return entry;
    }

    private String toJsonString(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > MAX_DATA_LENGTH ? text.substring(0, MAX_DATA_LENGTH) + "...(truncated)" : text;
    }
}
