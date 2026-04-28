package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.mapper.ResearchPaperMapper;
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

@DisplayName("ResearchPaperServiceImpl tests")
class ResearchPaperServiceImplTest {

    @Mock
    private ResearchPaperMapper researchPaperMapper;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private ResearchPaperServiceImpl researchPaperService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchPaperService = new ResearchPaperServiceImpl(researchPaperMapper, studentInfoMapper);
        Field baseMapperField = ResearchPaperServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchPaperService, researchPaperMapper);
    }

    @Test
    void mapByStudentIdsReturnsEmptyForEmptyInput() {
        assertTrue(researchPaperService.mapByStudentIds(List.of()).isEmpty());
        verify(researchPaperMapper, never()).selectList(any());
    }

    @Test
    void mapByStudentIdsGroupsApprovedPapers() {
        ResearchPaper paper1 = new ResearchPaper();
        paper1.setId(1L);
        paper1.setStudentId(1L);
        ResearchPaper paper2 = new ResearchPaper();
        paper2.setId(2L);
        paper2.setStudentId(1L);

        when(researchPaperMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(paper1, paper2));

        Map<Long, List<ResearchPaper>> result = researchPaperService.mapByStudentIds(List.of(1L));

        assertEquals(2, result.get(1L).size());
    }
}
