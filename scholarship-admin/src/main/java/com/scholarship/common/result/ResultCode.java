package com.scholarship.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举类
 * <p>
 * 定义系统中所有的响应状态码和对应的提示消息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 通用响应码 ==========

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 操作失败
     */
    ERROR(500, "操作失败"),

    // ========== 客户端错误 (4xx) ==========

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未授权，需要登录
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 请求实体过大
     */
    PAYLOAD_TOO_LARGE(413, "请求实体过大"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ========== 服务端错误 (5xx) ==========

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // ========== 业务错误码 (6xx) ==========

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(6001, "用户名或密码错误"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(6002, "用户已被禁用"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(6003, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXIST(6004, "用户已存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(6005, "密码错误"),

    /**
     * 原密码错误
     */
    OLD_PASSWORD_ERROR(6006, "原密码错误"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(6007, "Token已过期"),

    /**
     * Token无效
     */
    TOKEN_INVALID(6008, "Token无效"),

    // ========== 业务模块错误码 (601xx-609xx) ==========

    /**
     * 研究生信息不存在
     */
    STUDENT_NOT_EXIST(6011, "研究生信息不存在"),

    /**
     * 科研成果不存在
     */
    ACHIEVEMENT_NOT_EXIST(6012, "科研成果不存在"),

    /**
     * 科研成果已审核
     */
    ACHIEVEMENT_ALREADY_REVIEWED(6013, "科研成果已审核，不能重复操作"),

    /**
     * 评分规则不存在
     */
    SCORE_RULE_NOT_EXIST(6014, "评分规则不存在"),

    /**
     * 申请不存在
     */
    APPLICATION_NOT_EXIST(6015, "申请不存在"),

    /**
     * 申请已提交
     */
    APPLICATION_ALREADY_SUBMITTED(6016, "申请已提交，不能修改"),

    /**
     * 申请未提交
     */
    APPLICATION_NOT_SUBMITTED(6017, "申请未提交，不能审核"),

    /**
     * 评定批次不存在
     */
    BATCH_NOT_EXIST(6018, "评定批次不存在"),

    /**
     * 评定批次已结束
     */
    BATCH_ALREADY_ENDED(6019, "评定批次已结束"),

    /**
     * 评定批次未开始
     */
    BATCH_NOT_STARTED(6020, "评定批次未开始"),

    /**
     * 无权限操作
     */
    NO_PERMISSION(6021, "无权限操作");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}
