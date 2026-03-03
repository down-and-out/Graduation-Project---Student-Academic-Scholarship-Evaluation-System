package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.mapper.ResearchPaperMapper;
import com.scholarship.service.StudentInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ResearchPaperServiceImpl 批量查询方法测试
 */
@DisplayName("ResearchPaperServiceImpl 批量查询方法测试")
class ResearchPaperServiceImplTest {

    @Mock
    private ResearchPaperMapper researchPaperMapper;

    @Mock
    private StudentInfoService studentInfoService;

    private ResearchPaperServiceImpl researchPaperService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchPaperService = new ResearchPaperServiceImpl(researchPaperMapper, studentInfoService);
        Field baseMapperField = ResearchPaperServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchPaperService, researchPaperMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapByStudentIds_EmptyList() {
        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(List.of());

        assertTrue(result.isEmpty());
        verify(researchPaperMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapByStudentIds_NullList() {
        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(null);

        assertTrue(result.isEmpty());
        verify(researchPaperMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常批量查询")
    void testMapByStudentIds_NormalCase() {
        // 准备测试数据
        ResearchPaper paper1 = new ResearchPaper();
        paper1.setId(1L);
        paper1.setStudentId(1L);
        paper1.setStatus(2); // 审核通过

        ResearchPaper paper2 = new ResearchPaper();
        paper2.setId(2L);
        paper2.setStudentId(1L);
        paper2.setStatus(2);

        ResearchPaper paper3 = new ResearchPaper();
        paper3.setId(3L);
        paper3.setStudentId(2L);
        paper3.setStatus(2);

        when(researchPaperMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(paper1, paper2, paper3));

        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals(2, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
        verify(researchPaperMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试无匹配数据返回空 Map")
    void testMapByStudentIds_NoMatch() {
        when(researchPaperMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of());

        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(List.of(999L));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试只返回审核通过的论文")
    void testMapByStudentIds_OnlyApprovedPapers() {
        ResearchPaper approvedPaper = new ResearchPaper();
        approvedPaper.setId(1L);
        approvedPaper.setStudentId(1L);
        approvedPaper.setStatus(2); // 审核通过

        ResearchPaper pendingPaper = new ResearchPaper();
        pendingPaper.setId(2L);
        pendingPaper.setStudentId(1L);
        pendingPaper.setStatus(0); // 待审核，不应返回

        when(researchPaperMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(approvedPaper));

        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
        assertEquals(1L, result.get(1L).get(0).getId());
    }
}