package com.scholarship.service;

import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.ScholarshipApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class EvaluationCalculationServiceTest {

    @Autowired
    private EvaluationCalculationService evaluationCalculationService;

    @Test
    public void testCalculateTotalScore() {
        BigDecimal courseScore = new BigDecimal("85.00");
        BigDecimal researchScore = new BigDecimal("90.00");
        BigDecimal competitionScore = new BigDecimal("80.00");
        BigDecimal qualityScore = new BigDecimal("88.00");

        BigDecimal totalScore = evaluationCalculationService.calculateTotalScore(
                courseScore, researchScore, competitionScore, qualityScore
        );

        assertNotNull(totalScore);
        assertEquals(new BigDecimal("85.80"), totalScore);
    }

    @Test
    public void testCalculateApplication() {
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(1L);
        application.setBatchId(1L);
        application.setStudentId(1L);
        application.setStatus(3);

        EvaluationResult result = evaluationCalculationService.calculateApplication(application);

        assertNotNull(result);
        assertEquals(1L, result.getBatchId());
        assertEquals(1L, result.getStudentId());
        assertNotNull(result.getTotalScore());
    }

    @Test
    public void testCalculateBatchApplications() {
        Long batchId = 1L;
        BatchCalculationSummary summary = evaluationCalculationService.calculateBatchApplications(batchId);

        assertNotNull(summary);
        assertEquals(batchId, summary.getBatchId());
    }
}
