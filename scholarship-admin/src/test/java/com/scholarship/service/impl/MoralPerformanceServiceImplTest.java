package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.mapper.MoralPerformanceMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.service.EvaluationBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("MoralPerformanceServiceImpl tests")
class MoralPerformanceServiceImplTest {

    @Mock
    private MoralPerformanceMapper moralPerformanceMapper;
    @Mock
    private EvaluationBatchService evaluationBatchService;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private MoralPerformanceServiceImpl moralPerformanceService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        moralPerformanceService = new MoralPerformanceServiceImpl(evaluationBatchService, studentInfoMapper);
        Field baseMapperField = MoralPerformanceServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(moralPerformanceService, moralPerformanceMapper);
    }

    @Test
    void mapTotalScoreReturnsEmptyForEmptyInput() {
        assertTrue(moralPerformanceService.mapTotalScoreByStudentIds(List.of(), 1L).isEmpty());
        verify(moralPerformanceMapper, never()).selectList(any());
    }

    @Test
    void mapTotalScoreAggregatesApprovedScores() {
        MoralPerformance perf1 = new MoralPerformance();
        perf1.setStudentId(1L);
        perf1.setScore(new BigDecimal("10"));
        MoralPerformance perf2 = new MoralPerformance();
        perf2.setStudentId(1L);
        perf2.setScore(new BigDecimal("15"));

        when(moralPerformanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(perf1, perf2));
        EvaluationBatch batch = new EvaluationBatch();
        batch.setAcademicYear("2024");
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(List.of(1L), 1L);

        assertEquals(new BigDecimal("25"), result.get(1L));
    }
}
