package com.scholarship.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 奖项分配服务单元测试
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@SpringBootTest
public class AwardAllocationServiceTest {

    @Autowired
    private AwardAllocationService awardAllocationService;

    @Test
    public void testGetAwardLevelName() {
        // 测试奖项等级名称获取
        assertEquals("特等奖学金", awardAllocationService.getAwardLevelName(1));
        assertEquals("一等奖学金", awardAllocationService.getAwardLevelName(2));
        assertEquals("二等奖学金", awardAllocationService.getAwardLevelName(3));
        assertEquals("三等奖学金", awardAllocationService.getAwardLevelName(4));
        assertEquals("未获奖", awardAllocationService.getAwardLevelName(5));
        assertEquals("未知", awardAllocationService.getAwardLevelName(null));
    }

    @Test
    public void testDetermineAwardLevel() {
        // 测试奖项等级确定逻辑
        Map<Integer, AwardAllocationService.AwardQuota> quotaConfig = new HashMap<>();

        // 假设有 100 名学生，按比例计算
        int total = 100;

        // 第 1 名应该是特等奖
        Integer awardLevel1 = awardAllocationService.determineAwardLevel(1, total, quotaConfig);
        assertEquals(1, awardLevel1);

        // 第 50 名应该是三等奖
        Integer awardLevel50 = awardAllocationService.determineAwardLevel(50, total, quotaConfig);
        assertEquals(4, awardLevel50);

        // 第 80 名应该是未获奖
        Integer awardLevel80 = awardAllocationService.determineAwardLevel(80, total, quotaConfig);
        assertEquals(5, awardLevel80);
    }

    @Test
    public void testCalculateAwardAmount() {
        // 测试奖学金金额计算
        Long batchId = 1L;

        BigDecimal specialAmount = awardAllocationService.calculateAwardAmount(1, batchId);
        assertNotNull(specialAmount);

        BigDecimal firstAmount = awardAllocationService.calculateAwardAmount(2, batchId);
        assertNotNull(firstAmount);

        // 验证金额大小关系
        assertTrue(specialAmount.compareTo(firstAmount) > 0);
    }

    @Test
    public void testAllocateAwards() {
        // 测试批量奖项分配
        Long batchId = 1L;
        AwardAllocationService.AwardAllocationResult result = awardAllocationService.allocateAwards(batchId);

        // 即使没有数据，也应该返回结果对象而不是 null
        assertNotNull(result);
    }
}
