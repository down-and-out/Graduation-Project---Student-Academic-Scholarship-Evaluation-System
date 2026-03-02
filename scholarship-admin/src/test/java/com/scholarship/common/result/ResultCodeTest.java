package com.scholarship.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResultCode 响应状态码枚举测试
 */
@DisplayName("ResultCode 响应状态码枚举测试")
class ResultCodeTest {

    @Test
    @DisplayName("测试通用响应码")
    void testGeneralCodes() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("操作成功", ResultCode.SUCCESS.getMessage());

        assertEquals(500, ResultCode.ERROR.getCode());
        assertEquals("操作失败", ResultCode.ERROR.getMessage());
    }

    @Test
    @DisplayName("测试客户端错误码")
    void testClientErrorCodes() {
        assertEquals(400, ResultCode.BAD_REQUEST.getCode());
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        assertEquals(403, ResultCode.FORBIDDEN.getCode());
        assertEquals(404, ResultCode.NOT_FOUND.getCode());
        assertEquals(405, ResultCode.METHOD_NOT_ALLOWED.getCode());
        assertEquals(408, ResultCode.REQUEST_TIMEOUT.getCode());
        assertEquals(413, ResultCode.PAYLOAD_TOO_LARGE.getCode());
        assertEquals(429, ResultCode.TOO_MANY_REQUESTS.getCode());
    }

    @Test
    @DisplayName("测试服务端错误码")
    void testServerErrorCodes() {
        assertEquals(500, ResultCode.INTERNAL_SERVER_ERROR.getCode());
        assertEquals(503, ResultCode.SERVICE_UNAVAILABLE.getCode());
    }

    @Test
    @DisplayName("测试登录认证错误码")
    void testLoginErrorCodes() {
        assertEquals(6001, ResultCode.LOGIN_ERROR.getCode());
        assertEquals(6002, ResultCode.USER_DISABLED.getCode());
        assertEquals(6003, ResultCode.USER_NOT_EXIST.getCode());
        assertEquals(6004, ResultCode.USER_ALREADY_EXIST.getCode());
        assertEquals(6005, ResultCode.PASSWORD_ERROR.getCode());
        assertEquals(6006, ResultCode.OLD_PASSWORD_ERROR.getCode());
        assertEquals(6007, ResultCode.TOKEN_EXPIRED.getCode());
        assertEquals(6008, ResultCode.TOKEN_INVALID.getCode());
    }

    @Test
    @DisplayName("测试业务模块错误码")
    void testBusinessErrorCodes() {
        assertEquals(6011, ResultCode.STUDENT_NOT_EXIST.getCode());
        assertEquals(6012, ResultCode.ACHIEVEMENT_NOT_EXIST.getCode());
        assertEquals(6013, ResultCode.ACHIEVEMENT_ALREADY_REVIEWED.getCode());
        assertEquals(6014, ResultCode.SCORE_RULE_NOT_EXIST.getCode());
        assertEquals(6015, ResultCode.APPLICATION_NOT_EXIST.getCode());
        assertEquals(6016, ResultCode.APPLICATION_ALREADY_SUBMITTED.getCode());
        assertEquals(6017, ResultCode.APPLICATION_NOT_SUBMITTED.getCode());
        assertEquals(6018, ResultCode.BATCH_NOT_EXIST.getCode());
        assertEquals(6019, ResultCode.BATCH_ALREADY_ENDED.getCode());
        assertEquals(6020, ResultCode.BATCH_NOT_STARTED.getCode());
        assertEquals(6021, ResultCode.NO_PERMISSION.getCode());
    }

    @Test
    @DisplayName("测试错误消息不为空")
    void testMessagesNotNull() {
        for (ResultCode code : ResultCode.values()) {
            assertNotNull(code.getMessage(), "错误码 " + code.name() + " 的消息不能为空");
            assertTrue(code.getMessage().length() > 0, "错误码 " + code.name() + " 的消息不能为空字符串");
        }
    }

    @Test
    @DisplayName("测试错误码唯一性")
    void testCodeUniqueness() {
        // 使用 Map 按错误码分组，更高效地检测重复
        Map<Integer, List<ResultCode>> codeMap = Arrays.stream(ResultCode.values())
                .collect(Collectors.groupingBy(ResultCode::getCode));

        for (Map.Entry<Integer, List<ResultCode>> entry : codeMap.entrySet()) {
            if (entry.getKey() == 500) {
                // 允许 ERROR 和 INTERNAL_SERVER_ERROR 有相同的 500 码
                continue;
            }
            assertEquals(1, entry.getValue().size(),
                    "错误码 " + entry.getKey() + " 重复：" + entry.getValue().stream()
                            .map(ResultCode::name)
                            .collect(Collectors.joining(", ")));
        }
    }

    @Test
    @DisplayName("测试枚举包含所有预期错误码")
    void testExpectedCodesExist() {
        // 验证关键错误码存在，而非硬编码数量
        // 通用响应码
        assertNotNull(ResultCode.valueOf("SUCCESS"));
        assertNotNull(ResultCode.valueOf("ERROR"));

        // 客户端错误码
        assertNotNull(ResultCode.valueOf("BAD_REQUEST"));
        assertNotNull(ResultCode.valueOf("UNAUTHORIZED"));
        assertNotNull(ResultCode.valueOf("FORBIDDEN"));
        assertNotNull(ResultCode.valueOf("NOT_FOUND"));
        assertNotNull(ResultCode.valueOf("TOO_MANY_REQUESTS"));

        // 服务端错误码
        assertNotNull(ResultCode.valueOf("INTERNAL_SERVER_ERROR"));
        assertNotNull(ResultCode.valueOf("SERVICE_UNAVAILABLE"));

        // 登录认证错误码
        assertNotNull(ResultCode.valueOf("LOGIN_ERROR"));
        assertNotNull(ResultCode.valueOf("USER_NOT_EXIST"));
        assertNotNull(ResultCode.valueOf("TOKEN_EXPIRED"));

        // 业务模块错误码
        assertNotNull(ResultCode.valueOf("STUDENT_NOT_EXIST"));
        assertNotNull(ResultCode.valueOf("APPLICATION_NOT_EXIST"));
        assertNotNull(ResultCode.valueOf("NO_PERMISSION"));
    }
}
