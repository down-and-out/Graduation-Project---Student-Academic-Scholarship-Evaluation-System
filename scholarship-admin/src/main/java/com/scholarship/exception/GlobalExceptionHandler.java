package com.scholarship.exception;

import com.scholarship.common.result.Result;
import com.scholarship.common.result.ResultCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用@RestControllerAdvice 注解，统一处理 Controller 层抛出的各种异常
 * 返回格式统一的响应结果，提高系统的健壮性和用户体验
 * </p>
 *
 * 处理的异常类型：
 * <ul>
 *   <li>BusinessException: 业务异常</li>
 *   <li>MethodArgumentNotValidException: 参数校验异常（@Valid 注解）</li>
 *   <li>BindException: 参数绑定异常</li>
 *   <li>ConstraintViolationException: 约束违反异常</li>
 *   <li>AccessDeniedException: 访问拒绝异常</li>
 *   <li>NoHandlerFoundException: 404 异常</li>
 *   <li>HttpRequestMethodNotSupportedException: 请求方法不支持异常</li>
 *   <li>ExpiredJwtException: JWT Token 过期异常</li>
 *   <li>MalformedJwtException: JWT Token 格式错误异常</li>
 *   <li>SignatureException: JWT Token 签名错误异常</li>
 *   <li>UnsupportedJwtException: 不支持的 JWT Token 异常</li>
 *   <li>Exception: 其他未知异常</li>
 * </ul>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常
     * 当业务逻辑校验不通过时抛出 BusinessException，在此处统一处理
     *
     * @param e       业务异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常处理 ====================

    /**
     * 处理参数校验异常（@Valid 注解触发）
     * 用于处理 RequestBody 参数的校验异常
     *
     * @param e       参数校验异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常：URI={}, Errors={}", request.getRequestURI(), errorMsg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    /**
     * 处理参数绑定异常
     * 用于处理表单参数绑定的异常
     *
     * @param e       参数绑定异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常：URI={}, Errors={}", request.getRequestURI(), errorMsg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    /**
     * 处理约束违反异常
     * 用于处理@RequestParam 等参数的校验异常
     *
     * @param e       约束违反异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束违反异常：URI={}, Errors={}", request.getRequestURI(), errorMsg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    // ==================== 安全相关异常处理 ====================

    /**
     * 处理认证失败异常（用户名或密码错误）
     * 当登录时用户名或密码不正确时抛出
     *
     * @param e       认证失败异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("认证失败：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
    }

    /**
     * 处理用户名不存在异常
     * 当登录时用户名不存在时抛出
     *
     * @param e       用户名不存在异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e, HttpServletRequest request) {
        log.warn("用户不存在：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
    }

    /**
     * 处理访问拒绝异常
     * 当用户访问无权限的资源时抛出
     *
     * @param e       访问拒绝异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问拒绝：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    // ==================== JWT Token 异常处理 ====================

    /**
     * 处理 JWT Token 过期异常
     * 当 Token 已过期时抛出
     *
     * @param e       JWT 过期异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleExpiredJwtException(ExpiredJwtException e, HttpServletRequest request) {
        log.warn("Token 已过期：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.TOKEN_EXPIRED);
    }

    /**
     * 处理 JWT Token 格式错误异常
     * 当 Token 格式不正确时抛出
     *
     * @param e       JWT 格式错误异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleMalformedJwtException(MalformedJwtException e, HttpServletRequest request) {
        log.warn("Token 格式错误：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.TOKEN_INVALID);
    }

    /**
     * 处理 JWT Token 签名错误异常
     * 当 Token 签名验证失败时抛出
     *
     * @param e       JWT 签名错误异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleSignatureException(SignatureException e, HttpServletRequest request) {
        log.warn("Token 签名验证失败：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.TOKEN_INVALID);
    }

    /**
     * 处理不支持的 JWT Token 异常
     * 当 Token 不被支持时抛出
     *
     * @param e       不支持的 JWT 异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnsupportedJwtException(UnsupportedJwtException e, HttpServletRequest request) {
        log.warn("Token 不被支持：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.TOKEN_INVALID);
    }

    /**
     * 处理 JWT 通用异常（兜底）
     *
     * @param e       JWT 异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleJwtException(JwtException e, HttpServletRequest request) {
        log.warn("JWT 异常：URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCode.TOKEN_INVALID);
    }

    // ==================== HTTP 相关异常处理 ====================

    /**
     * 处理 404 异常
     * 当访问的资源不存在时抛出
     *
     * @param e       404 异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("资源不存在：URI={}, Method={}", request.getRequestURI(), e.getHttpMethod());
        return Result.error(ResultCode.NOT_FOUND);
    }

    /**
     * 处理请求方法不支持异常
     * 当使用不正确的 HTTP 方法访问接口时抛出
     *
     * @param e       请求方法不支持异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持：URI={}, Method={}", request.getRequestURI(), e.getMethod());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED);
    }

    // ==================== 其他异常处理 ====================

    /**
     * 处理所有未捕获的异常
     * 这是异常处理的最后一道防线，捕获所有未被上述方法处理的异常
     *
     * @param e       异常对象
     * @param request HTTP 请求对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常：URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
