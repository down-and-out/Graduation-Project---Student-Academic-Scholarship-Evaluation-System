package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchProject;
import com.scholarship.mapper.ResearchProjectMapper;
import com.scholarship.mapper.StudentInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ResearchProjectServiceImpl tests")
class ResearchProjectServiceImplTest {

    @Mock
    private ResearchProjectMapper researchProjectMapper;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private ResearchProjectServiceImpl researchProjectService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchProjectService = new ResearchProjectServiceImpl(studentInfoMapper);
        Field baseMapperField = ResearchProjectServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchProjectService, researchProjectMapper);
    }

    @Test
    void mapByStudentIdsReturnsEmptyForEmptyInput() {
        assertTrue(researchProjectService.mapByStudentIds(List.of()).isEmpty());
        verify(researchProjectMapper, never()).selectList(any());
    }

    @Test
    void mapByStudentIdsGroupsProjects() {
        ResearchProject project = new ResearchProject();
        project.setId(1L);
        project.setStudentId(1L);

        when(researchProjectMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(project));

        Map<Long, List<ResearchProject>> result = researchProjectService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
    }
}
