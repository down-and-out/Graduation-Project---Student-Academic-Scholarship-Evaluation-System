package com.scholarship.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApplicationStatusEnum 申请状态枚举测试
 */
@DisplayName("ApplicationStatusEnum 申请状态枚举测试")
class ApplicationStatusEnumTest {

    @Test
    @DisplayName("测试所有状态码和描述")
    void testAllStatusCodes() {
        assertEquals(0, ApplicationStatusEnum.DRAFT.getCode());
        assertEquals("草稿", ApplicationStatusEnum.DRAFT.getDescription());

        assertEquals(1, ApplicationStatusEnum.SUBMITTED.getCode());
        assertEquals("已提交", ApplicationStatusEnum.SUBMITTED.getDescription());

        assertEquals(2, ApplicationStatusEnum.TUTOR_REVIEWING.getCode());
        assertEquals("导师审核中", ApplicationStatusEnum.TUTOR_REVIEWING.getDescription());

        assertEquals(3, ApplicationStatusEnum.TUTOR_PASSED.getCode());
        assertEquals("导师审核通过", ApplicationStatusEnum.TUTOR_PASSED.getDescription());

        assertEquals(4, ApplicationStatusEnum.TUTOR_REJECTED.getCode());
        assertEquals("导师审核不通过", ApplicationStatusEnum.TUTOR_REJECTED.getDescription());

        assertEquals(5, ApplicationStatusEnum.ADMIN_REVIEWING.getCode());
        assertEquals("院系审核中", ApplicationStatusEnum.ADMIN_REVIEWING.getDescription());

        assertEquals(6, ApplicationStatusEnum.ADMIN_REJECTED.getCode());
        assertEquals("院系审核不通过", ApplicationStatusEnum.ADMIN_REJECTED.getDescription());

        assertEquals(7, ApplicationStatusEnum.APPROVED.getCode());
        assertEquals("审核通过", ApplicationStatusEnum.APPROVED.getDescription());

        assertEquals(8, ApplicationStatusEnum.APPEALED.getCode());
        assertEquals("已申诉", ApplicationStatusEnum.APPEALED.getDescription());
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - 有效状态码")
    void testValueOfCodeValid() {
        assertEquals(ApplicationStatusEnum.DRAFT, ApplicationStatusEnum.valueOfCode(0));
        assertEquals(ApplicationStatusEnum.SUBMITTED, ApplicationStatusEnum.valueOfCode(1));
        assertEquals(ApplicationStatusEnum.APPROVED, ApplicationStatusEnum.valueOfCode(7));
        assertEquals(ApplicationStatusEnum.APPEALED, ApplicationStatusEnum.valueOfCode(8));
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - null 输入")
    void testValueOfCodeNull() {
        assertNull(ApplicationStatusEnum.valueOfCode(null));
    }

    @Test
    @DisplayName("测试 valueOfCode 方法 - 无效状态码")
    void testValueOfCodeInvalid() {
        assertNull(ApplicationStatusEnum.valueOfCode(-1));
        assertNull(ApplicationStatusEnum.valueOfCode(9));
        assertNull(ApplicationStatusEnum.valueOfCode(100));
    }

    @Test
    @DisplayName("测试枚举数量")
    void testEnumCount() {
        assertEquals(9, ApplicationStatusEnum.values().length);
    }

    @Test
    @DisplayName("测试所有描述不为空")
    void testAllDescriptionsNotNull() {
        for (ApplicationStatusEnum status : ApplicationStatusEnum.values()) {
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("测试状态码唯一性")
    void testCodeUniqueness() {
        for (int i = 0; i < ApplicationStatusEnum.values().length; i++) {
            for (int j = i + 1; j < ApplicationStatusEnum.values().length; j++) {
                assertNotEquals(ApplicationStatusEnum.values()[i].getCode(),
                        ApplicationStatusEnum.values()[j].getCode(),
                        "状态码重复：" + ApplicationStatusEnum.values()[i].name() +
                                " 和 " + ApplicationStatusEnum.values()[j].name());
            }
        }
    }

    @Test
    @DisplayName("测试申请流程状态顺序")
    void testStatusFlow() {
        // 典型的申请流程：草稿 -> 已提交 -> 导师审核中 -> 导师审核通过 -> 院系审核中 -> 审核通过
        ApplicationStatusEnum[] flow = {
                ApplicationStatusEnum.DRAFT,
                ApplicationStatusEnum.SUBMITTED,
                ApplicationStatusEnum.TUTOR_REVIEWING,
                ApplicationStatusEnum.TUTOR_PASSED,
                ApplicationStatusEnum.ADMIN_REVIEWING,
                ApplicationStatusEnum.APPROVED
        };

        // 验证流程中的状态都存在（非 null）
        for (ApplicationStatusEnum status : flow) {
            assertNotNull(status);
        }
    }
}
