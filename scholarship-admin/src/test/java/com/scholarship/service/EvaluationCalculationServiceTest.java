package com.scholarship.service;

import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.ScholarshipApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评定计算服务单元测试
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@SpringBootTest
public class EvaluationCalculationServiceTest {

    @Autowired
    private EvaluationCalculationService evaluationCalculationService;

    @Test
    public void testCalculateTotalScore() {
        // 测试总分计算
        BigDecimal courseScore = new BigDecimal("85.00");
        BigDecimal researchScore = new BigDecimal("90.00");
        BigDecimal competitionScore = new BigDecimal("80.00");
        BigDecimal qualityScore = new BigDecimal("88.00");

        BigDecimal totalScore = evaluationCalculationService.calculateTotalScore(
            courseScore, researchScore, competitionScore, qualityScore
        );

        assertNotNull(totalScore);
        // 验证权重计算：85*0.4 + 90*0.3 + 80*0.2 + 88*0.1 = 34 + 27 + 16 + 8.8 = 85.8
        assertEquals(new BigDecimal("85.80"), totalScore);
    }

    @Test
    public void testCalculateApplication() {
        // 创建测试申请
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(1L);
        application.setBatchId(1L);
        application.setStudentId(1L);
        application.setStatus(3); // 评审完成

        EvaluationResult result = evaluationCalculationService.calculateApplication(application);

        assertNotNull(result);
        assertEquals(1L, result.getBatchId());
        assertEquals(1L, result.getStudentId());
        assertNotNull(result.getTotalScore());
    }

    @Test
    public void testCalculateBatchApplications() {
        // 测试批量计算
        Long batchId = 1L;
        Map<Long, EvaluationResult> results = evaluationCalculationService.calculateBatchApplications(batchId);

        // 即使没有数据，也应该返回空 map 而不是 null
        assertNotNull(results);
    }
}
