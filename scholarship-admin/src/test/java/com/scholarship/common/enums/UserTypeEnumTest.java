package com.scholarship.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserTypeEnum 用户类型枚举测试
 */
@DisplayName("UserTypeEnum 用户类型枚举测试")
class UserTypeEnumTest {

    @Test
    @DisplayName("测试所有用户类型")
    void testAllUserTypes() {
        assertEquals(1, UserTypeEnum.STUDENT.getCode());
        assertEquals("研究生", UserTypeEnum.STUDENT.getDescription());

        assertEquals(2, UserTypeEnum.TUTOR.getCode());
        assertEquals("导师", UserTypeEnum.TUTOR.getDescription());

        assertEquals(3, UserTypeEnum.ADMIN.getCode());
        assertEquals("管理员", UserTypeEnum.ADMIN.getDescription());
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - 有效类型码")
    void testValueOfCodeValid() {
        assertEquals(UserTypeEnum.STUDENT, UserTypeEnum.valueOfCode(1));
        assertEquals(UserTypeEnum.TUTOR, UserTypeEnum.valueOfCode(2));
        assertEquals(UserTypeEnum.ADMIN, UserTypeEnum.valueOfCode(3));
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - null 输入")
    void testValueOfCodeNull() {
        assertNull(UserTypeEnum.valueOfCode(null));
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - 无效类型码")
    void testValueOfCodeInvalid() {
        assertNull(UserTypeEnum.valueOfCode(0));
        assertNull(UserTypeEnum.valueOfCode(4));
        assertNull(UserTypeEnum.valueOfCode(-1));
        assertNull(UserTypeEnum.valueOfCode(100));
    }

    @Test
    @DisplayName("测试枚举数量")
    void testEnumCount() {
        assertEquals(3, UserTypeEnum.values().length);
    }

    @Test
    @DisplayName("测试所有描述不为空")
    void testAllDescriptionsNotNull() {
        for (UserTypeEnum type : UserTypeEnum.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("测试类型码唯一性")
    void testCodeUniqueness() {
        for (int i = 0; i < UserTypeEnum.values().length; i++) {
            for (int j = i + 1; j < UserTypeEnum.values().length; j++) {
                assertNotEquals(UserTypeEnum.values()[i].getCode(),
                        UserTypeEnum.values()[j].getCode(),
                        "类型码重复：" + UserTypeEnum.values()[i].name() +
                                " 和 " + UserTypeEnum.values()[j].name());
            }
        }
    }

}
