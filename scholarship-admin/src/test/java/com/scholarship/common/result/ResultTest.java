package com.scholarship.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 统一响应结果类测试
 */
@DisplayName("Result 统一响应结果测试")
class ResultTest {

    @Test
    @DisplayName("测试 success() 无参数方法")
    void testSuccess() {
        Result<Void> result = Result.success();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试 success 带列表数据")
    void testSuccessWithList() {
        List<String> list = List.of("item1", "item2", "item3");
        Result<List<String>> result = Result.success(list);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试 success 带 Map 数据")
    void testSuccessWithMap() {
        Map<String, Object> map = Map.of("key1", "value1", "key2", 123);
        Result<Map<String, Object>> result = Result.success(map);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试 success(String message) 自定义消息")
    void testSuccessWithMessage() {
        Result<Void> result = Result.success("自定义成功消息");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("自定义成功消息", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试 success(String message, T data) 自定义消息和数据")
    void testSuccessWithMessageAndData() {
        Result<String> result = Result.success("操作成功", "返回数据");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("返回数据", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试 error() 无参数方法")
    void testError() {
        Result<Void> result = Result.error();

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("测试 error(String message) 自定义错误消息")
    void testErrorWithMessage() {
        Result<Void> result = Result.error("自定义错误消息");

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("自定义错误消息", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("测试 error(ResultCode) 使用错误码枚举")
    void testErrorWithResultCode() {
        Result<Void> result = Result.error(ResultCode.USER_NOT_EXIST);

        assertNotNull(result);
        assertEquals(6003, result.getCode());
        assertEquals("用户不存在", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("测试 error(ResultCode, String) 使用错误码和自定义消息")
    void testErrorWithResultCodeAndMessage() {
        Result<Void> result = Result.error(ResultCode.TOKEN_EXPIRED, "Token 已过期，请重新登录");

        assertNotNull(result);
        assertEquals(6007, result.getCode());
        assertEquals("Token 已过期，请重新登录", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("测试 error(Integer code, String message) 自定义码和消息")
    void testErrorWithCodeAndMessage() {
        Result<Void> result = Result.error(4004, "资源未找到");

        assertNotNull(result);
        assertEquals(4004, result.getCode());
        assertEquals("资源未找到", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("测试各种错误码枚举")
    void testAllErrorCodes() {
        // 通用错误
        assertErrorCode(ResultCode.SUCCESS, 200, "操作成功");
        assertErrorCode(ResultCode.ERROR, 500, "操作失败");

        // 客户端错误
        assertErrorCode(ResultCode.BAD_REQUEST, 400, "请求参数错误");
        assertErrorCode(ResultCode.UNAUTHORIZED, 401, "未授权，请先登录");
        assertErrorCode(ResultCode.FORBIDDEN, 403, "禁止访问");
        assertErrorCode(ResultCode.NOT_FOUND, 404, "资源不存在");
        assertErrorCode(ResultCode.TOO_MANY_REQUESTS, 429, "请求过于频繁，请稍后再试");

        // 业务错误
        assertErrorCode(ResultCode.LOGIN_ERROR, 6001, "用户名或密码错误");
        assertErrorCode(ResultCode.USER_DISABLED, 6002, "用户已被禁用");
        assertErrorCode(ResultCode.PASSWORD_ERROR, 6005, "密码错误");

        // 业务模块错误
        assertErrorCode(ResultCode.STUDENT_NOT_EXIST, 6011, "研究生信息不存在");
        assertErrorCode(ResultCode.APPLICATION_NOT_EXIST, 6015, "申请不存在");
        assertErrorCode(ResultCode.BATCH_NOT_EXIST, 6018, "评定批次不存在");
        assertErrorCode(ResultCode.NO_PERMISSION, 6021, "无权限操作");
    }

    private void assertErrorCode(ResultCode code, int expectedCode, String expectedMessage) {
        Result<Void> result = Result.error(code);
        assertEquals(expectedCode, result.getCode());
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    @DisplayName("测试 null 值序列化排除")
    void testNullExclusion() {
        Result<Void> result = Result.success();
        // 由于@JsonInclude(JsonInclude.Include.NON_NULL)，data 字段为 null 时应该被排除
        assertNull(result.getData());
    }
}
