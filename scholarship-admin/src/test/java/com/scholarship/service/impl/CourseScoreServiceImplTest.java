package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.mapper.CourseScoreMapper;
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

@DisplayName("CourseScoreServiceImpl tests")
class CourseScoreServiceImplTest {

    @Mock
    private CourseScoreMapper courseScoreMapper;
    @Mock
    private EvaluationBatchService evaluationBatchService;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private CourseScoreServiceImpl courseScoreService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        courseScoreService = new CourseScoreServiceImpl(evaluationBatchService, studentInfoMapper);
        Field baseMapperField = CourseScoreServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(courseScoreService, courseScoreMapper);
    }

    @Test
    void mapWeightedAverageReturnsEmptyForEmptyInput() {
        assertTrue(courseScoreService.mapWeightedAverageByStudentIds(List.of(), 1L).isEmpty());
        verify(courseScoreMapper, never()).selectList(any());
    }

    @Test
    void mapWeightedAverageComputesWeightedScores() {
        CourseScore score1 = new CourseScore();
        score1.setStudentId(1L);
        score1.setScore(new BigDecimal("90"));
        score1.setCredit(new BigDecimal("3"));

        CourseScore score2 = new CourseScore();
        score2.setStudentId(1L);
        score2.setScore(new BigDecimal("80"));
        score2.setCredit(new BigDecimal("2"));

        when(courseScoreMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(score1, score2));
        EvaluationBatch batch = new EvaluationBatch();
        batch.setAcademicYear("2024");
        when(evaluationBatchService.getById(1L)).thenReturn(batch);

        Map<Long, BigDecimal> result = courseScoreService.mapWeightedAverageByStudentIds(List.of(1L), 1L);

        assertEquals(new BigDecimal("86.00"), result.get(1L));
    }
}
