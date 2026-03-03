package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchProject;
import com.scholarship.mapper.ResearchProjectMapper;
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
 * ResearchProjectServiceImpl 批量查询方法测试
 */
@DisplayName("ResearchProjectServiceImpl 批量查询方法测试")
class ResearchProjectServiceImplTest {

    @Mock
    private ResearchProjectMapper researchProjectMapper;

    private ResearchProjectServiceImpl researchProjectService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchProjectService = new ResearchProjectServiceImpl();
        Field baseMapperField = ResearchProjectServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchProjectService, researchProjectMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapByStudentIds_EmptyList() {
        Map<Long, List<ResearchProject>> result = researchProjectService.mapByStudentIds(List.of());

        assertTrue(result.isEmpty());
        verify(researchProjectMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapByStudentIds_NullList() {
        Map<Long, List<ResearchProject>> result = researchProjectService.mapByStudentIds(null);

        assertTrue(result.isEmpty());
        verify(researchProjectMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常批量查询")
    void testMapByStudentIds_NormalCase() {
        ResearchProject project1 = new ResearchProject();
        project1.setId(1L);
        project1.setStudentId(1L);
        project1.setAuditStatus(1); // 审核通过

        ResearchProject project2 = new ResearchProject();
        project2.setId(2L);
        project2.setStudentId(2L);
        project2.setAuditStatus(1);

        when(researchProjectMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(project1, project2));

        Map<Long, List<ResearchProject>> result = researchProjectService.mapByStudentIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals(1, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
        verify(researchProjectMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试只返回审核通过的项目")
    void testMapByStudentIds_OnlyApprovedProjects() {
        ResearchProject approvedProject = new ResearchProject();
        approvedProject.setId(1L);
        approvedProject.setStudentId(1L);
        approvedProject.setAuditStatus(1); // 审核通过

        when(researchProjectMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(approvedProject));

        Map<Long, List<ResearchProject>> result = researchProjectService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
        assertEquals(1L, result.get(1L).get(0).getId());
    }
}