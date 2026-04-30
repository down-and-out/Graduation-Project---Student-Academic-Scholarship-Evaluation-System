package com.scholarship.service;

import com.scholarship.entity.EvaluationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 排名生成服务单元测试
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("dev")
public class EvaluationRankServiceTest {

    @Autowired
    private EvaluationRankService evaluationRankService;

    @Test
    @Disabled("会更新 evaluation_result 表的排名数据，需 scholarship_test 库才能安全运行")
    public void testGenerateBatchRanks() {
        // 测试批量生成排名
        Long batchId = 1L;
        Map<Long, EvaluationResult> results = evaluationRankService.generateBatchRanks(batchId);

        // 即使没有数据，也应该返回空 map 而不是 null
        assertNotNull(results);
    }

    @Test
    public void testCalculateDepartmentRank() {
        // 测试院系排名计算
        Long batchId = 1L;
        String department = "计算机学院";
        List<EvaluationResult> results = evaluationRankService.calculateDepartmentRank(batchId, department);

        assertNotNull(results);
    }

    @Test
    public void testCalculateMajorRank() {
        // 测试专业排名计算
        Long batchId = 1L;
        String major = "计算机科学与技术";
        List<EvaluationResult> results = evaluationRankService.calculateMajorRank(batchId, major);

        assertNotNull(results);
    }
}
