package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.mapper.CourseScoreMapper;
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
 * CourseScoreServiceImpl 批量计算方法测试
 */
@DisplayName("CourseScoreServiceImpl 批量计算方法测试")
class CourseScoreServiceImplTest {

    @Mock
    private CourseScoreMapper courseScoreMapper;

    @Mock
    private EvaluationBatchService evaluationBatchService;

    private CourseScoreServiceImpl courseScoreService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        courseScoreService = new CourseScoreServiceImpl(evaluationBatchService);
        Field baseMapperField = CourseScoreServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(courseScoreService, courseScoreMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapWeightedAverageByStudentIds_EmptyList() {
        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(List.of(), 1L);

        assertTrue(result.isEmpty());
        verify(courseScoreMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapWeightedAverageByStudentIds_NullList() {
        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(null, 1L);

        assertTrue(result.isEmpty());
        verify(courseScoreMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常加权平均分计算")
    void testMapWeightedAverageByStudentIds_NormalCase() {
        // 准备测试数据
        CourseScore score1 = new CourseScore();
        score1.setStudentId(1L);
        score1.setScore(new BigDecimal("90"));
        score1.setCredit(new BigDecimal("3"));

        CourseScore score2 = new CourseScore();
        score2.setStudentId(1L);
        score2.setScore(new BigDecimal("80"));
        score2.setCredit(new BigDecimal("2"));

        CourseScore score3 = new CourseScore();
        score3.setStudentId(2L);
        score3.setScore(new BigDecimal("85"));
        score3.setCredit(new BigDecimal("4"));

        when(courseScoreMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(score1, score2, score3));

        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        batch.setAcademicYear("2024");
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(List.of(1L, 2L), 1L);

        assertEquals(2, result.size());
        // 学生1: (90*3 + 80*2) / (3+2) = 430/5 = 86
        assertEquals(new BigDecimal("86.00"), result.get(1L));
        // 学生2: 85*4 / 4 = 85
        assertEquals(new BigDecimal("85.00"), result.get(2L));
    }

    @Test
    @DisplayName("测试无成绩返回零分")
    void testMapWeightedAverageByStudentIds_NoScores() {
        when(courseScoreMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of());

        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(List.of(999L), 1L);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(999L));
    }

    @Test
    @DisplayName("测试零学分处理")
    void testMapWeightedAverageByStudentIds_ZeroCredits() {
        CourseScore score = new CourseScore();
        score.setStudentId(1L);
        score.setScore(new BigDecimal("90"));
        score.setCredit(BigDecimal.ZERO);

        when(courseScoreMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(score));

        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(List.of(1L), null);

        assertEquals(BigDecimal.ZERO, result.get(1L));
    }
}