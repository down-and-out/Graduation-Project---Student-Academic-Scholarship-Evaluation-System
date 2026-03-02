package com.scholarship.common.exception;

import com.scholarship.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于封装业务逻辑中的异常情况，当业务校验不通过时抛出此异常
 * 全局异常处理器会捕获此异常并返回统一的响应格式
 * </p>
 *
 * 使用示例：
 * <pre>
 * // 使用错误码枚举
 * throw new BusinessException(ResultCode.USER_NOT_EXIST);
 *
 * // 使用自定义消息
 * throw new BusinessException("用户名不能为空");
 *
 * // 使用错误码枚举和自定义消息
 * throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户ID: 123 不存在");
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造函数：使用默认错误码
     */
    public BusinessException() {
        this(ResultCode.ERROR);
    }

    /**
     * 构造函数：自定义消息
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 构造函数：使用错误码枚举
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 构造函数：使用错误码枚举和自定义消息
     *
     * @param resultCode 错误码枚举
     * @param message    自定义消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    /**
     * 构造函数：自定义错误码和消息
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数：包含原始异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        this(ResultCode.ERROR.getCode(), message, cause);
    }

    /**
     * 构造函数：包含原始异常
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
