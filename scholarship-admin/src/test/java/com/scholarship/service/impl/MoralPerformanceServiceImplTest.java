package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.mapper.MoralPerformanceMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MoralPerformanceServiceImpl 批量计算方法测试
 */
@DisplayName("MoralPerformanceServiceImpl 批量计算方法测试")
class MoralPerformanceServiceImplTest {

    @Mock
    private MoralPerformanceMapper moralPerformanceMapper;

    @Mock
    private EvaluationBatchService evaluationBatchService;

    private MoralPerformanceServiceImpl moralPerformanceService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        moralPerformanceService = new MoralPerformanceServiceImpl(evaluationBatchService);
        Field baseMapperField = MoralPerformanceServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(moralPerformanceService, moralPerformanceMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapTotalScoreByStudentIds_EmptyList() {
        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(List.of(), 1L);

        assertTrue(result.isEmpty());
        verify(moralPerformanceMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapTotalScoreByStudentIds_NullList() {
        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(null, 1L);

        assertTrue(result.isEmpty());
        verify(moralPerformanceMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常德育总分计算")
    void testMapTotalScoreByStudentIds_NormalCase() {
        MoralPerformance perf1 = new MoralPerformance();
        perf1.setStudentId(1L);
        perf1.setScore(new BigDecimal("10"));
        perf1.setAuditStatus(1);

        MoralPerformance perf2 = new MoralPerformance();
        perf2.setStudentId(1L);
        perf2.setScore(new BigDecimal("15"));
        perf2.setAuditStatus(1);

        MoralPerformance perf3 = new MoralPerformance();
        perf3.setStudentId(2L);
        perf3.setScore(new BigDecimal("20"));
        perf3.setAuditStatus(1);

        when(moralPerformanceMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(perf1, perf2, perf3));

        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        batch.setAcademicYear("2024");
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(List.of(1L, 2L), 1L);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("25"), result.get(1L)); // 10 + 15
        assertEquals(new BigDecimal("20"), result.get(2L));
    }

    @Test
    @DisplayName("测试无德育记录返回零分")
    void testMapTotalScoreByStudentIds_NoRecords() {
        when(moralPerformanceMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of());

        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(List.of(999L), 1L);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(999L));
    }

    @Test
    @DisplayName("测试只计算审核通过的记录")
    void testMapTotalScoreByStudentIds_OnlyApprovedRecords() {
        MoralPerformance approved = new MoralPerformance();
        approved.setStudentId(1L);
        approved.setScore(new BigDecimal("10"));
        approved.setAuditStatus(1);

        // 查询时已过滤，所以返回的只有审核通过的
        when(moralPerformanceMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(approved));

        Map<Long, BigDecimal> result = moralPerformanceService.mapTotalScoreByStudentIds(List.of(1L), null);

        assertEquals(new BigDecimal("10"), result.get(1L));
    }
}