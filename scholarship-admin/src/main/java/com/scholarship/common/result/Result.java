package com.scholarship.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 * <p>
 * 所有Controller接口的返回值都使用此类进行封装，确保响应格式的一致性
 * </p>
 *
 * @param <T> 响应数据的泛型类型
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)  // null值不序列化
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     * 200: 成功
     * 400: 请求参数错误
     * 401: 未授权
     * 403: 禁止访问
     * 404: 资源不存在
     * 500: 服务器内部错误
     */
    @Schema(description = "响应码")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private Long timestamp;

    /**
     * 私有构造函数，防止外部直接实例化
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 私有构造函数，设置响应码和消息
     */
    private Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    /**
     * 私有构造函数，设置完整的响应信息
     */
    private Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // ==================== 成功响应 ====================

    /**
     * 返回成功响应（无数据）
     *
     * @param <T> 泛型类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 返回成功响应（自定义消息）
     *
     * @param message 自定义消息
     * @param <T>     泛型类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message);
    }

    /**
     * 返回成功响应（自定义消息和数据）
     *
     * @param message 自定义消息
     * @param data    响应数据
     * @param <T>     泛型类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败响应 ====================

    /**
     * 返回失败响应（使用默认错误码）
     *
     * @param <T> 泛型类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 返回失败响应（自定义消息）
     *
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 返回失败响应（使用错误码枚举）
     *
     * @param resultCode 错误码枚举
     * @param <T>        泛型类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 返回失败响应（使用错误码枚举和自定义消息）
     *
     * @param resultCode 错误码枚举
     * @param message    自定义消息
     * @param <T>        泛型类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message);
    }

    /**
     * 返回失败响应（自定义码和消息）
     *
     * @param code    响应码
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    // ==================== 判断方法 ====================

    /**
     * 判断响应是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
