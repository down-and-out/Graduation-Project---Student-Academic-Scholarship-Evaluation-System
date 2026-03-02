package com.scholarship.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewOpinionEnum 审核意见枚举测试
 */
@DisplayName("ReviewOpinionEnum 审核意见枚举测试")
class ReviewOpinionEnumTest {

    @Test
    @DisplayName("测试所有审核意见")
    void testAllOpinions() {
        assertEquals("通过", ReviewOpinionEnum.PASS.getText());
        assertTrue(ReviewOpinionEnum.PASS.getPassed());

        assertEquals("不通过", ReviewOpinionEnum.REJECT.getText());
        assertFalse(ReviewOpinionEnum.REJECT.getPassed());

        assertEquals("拒绝", ReviewOpinionEnum.REFUSE.getText());
        assertFalse(ReviewOpinionEnum.REFUSE.getPassed());

        assertEquals("同意", ReviewOpinionEnum.AGREE.getText());
        assertTrue(ReviewOpinionEnum.AGREE.getPassed());

        assertEquals("不同意", ReviewOpinionEnum.DISAGREE.getText());
        assertFalse(ReviewOpinionEnum.DISAGREE.getPassed());

        assertEquals("待审核", ReviewOpinionEnum.PENDING.getText());
        assertNull(ReviewOpinionEnum.PENDING.getPassed());
    }

    @Test
    @DisplayName("测试 valueOfText 方法 - 有效意见")
    void testValueOfTextValid() {
        assertEquals(ReviewOpinionEnum.PASS, ReviewOpinionEnum.valueOfText("通过"));
        assertEquals(ReviewOpinionEnum.REJECT, ReviewOpinionEnum.valueOfText("不通过"));
        assertEquals(ReviewOpinionEnum.AGREE, ReviewOpinionEnum.valueOfText("同意"));
        assertEquals(ReviewOpinionEnum.DISAGREE, ReviewOpinionEnum.valueOfText("不同意"));
    }

    @Test
    @DisplayName("测试 valueOfText 方法 - null 输入")
    void testValueOfTextNull() {
        assertEquals(ReviewOpinionEnum.PENDING, ReviewOpinionEnum.valueOfText(null));
    }

    @Test
    @DisplayName("测试 valueOfText 方法 - 无效意见")
    void testValueOfTextInvalid() {
        assertEquals(ReviewOpinionEnum.PENDING, ReviewOpinionEnum.valueOfText("未知意见"));
        assertEquals(ReviewOpinionEnum.PENDING, ReviewOpinionEnum.valueOfText(""));
        assertEquals(ReviewOpinionEnum.PENDING, ReviewOpinionEnum.valueOfText("unknown"));
    }

    @Test
    @DisplayName("测试 isPassed 方法 - 通过意见")
    void testIsPassedTrue() {
        assertTrue(ReviewOpinionEnum.isPassed("通过"));
        assertTrue(ReviewOpinionEnum.isPassed("同意"));
    }

    @Test
    @DisplayName("测试 isPassed 方法 - 不通过意见")
    void testIsPassedFalse() {
        assertFalse(ReviewOpinionEnum.isPassed("不通过"));
        assertFalse(ReviewOpinionEnum.isPassed("拒绝"));
        assertFalse(ReviewOpinionEnum.isPassed("不同意"));
    }

    @Test
    @DisplayName("测试 isPassed 方法 - 待审核")
    void testIsPassedPending() {
        assertNull(ReviewOpinionEnum.isPassed(null));
        assertNull(ReviewOpinionEnum.isPassed("待审核"));
    }

    @Test
    @DisplayName("测试枚举数量")
    void testEnumCount() {
        assertEquals(6, ReviewOpinionEnum.values().length);
    }

    @Test
    @DisplayName("测试所有文本不为空")
    void testAllTextsNotNull() {
        for (ReviewOpinionEnum opinion : ReviewOpinionEnum.values()) {
            assertNotNull(opinion.getText());
            assertFalse(opinion.getText().isEmpty());
        }
    }

    @Test
    @DisplayName("测试通过状态分类")
    void testPassedClassification() {
        // 验证所有枚举的通过状态分类正确
        for (ReviewOpinionEnum opinion : ReviewOpinionEnum.values()) {
            if (opinion == ReviewOpinionEnum.PENDING) {
                assertNull(opinion.getPassed());
            } else if (opinion == ReviewOpinionEnum.PASS || opinion == ReviewOpinionEnum.AGREE) {
                assertTrue(opinion.getPassed());
            } else {
                assertFalse(opinion.getPassed());
            }
        }
    }
}
