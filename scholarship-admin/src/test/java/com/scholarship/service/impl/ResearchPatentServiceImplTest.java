package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.mapper.ResearchPatentMapper;
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

@DisplayName("ResearchPatentServiceImpl tests")
class ResearchPatentServiceImplTest {

    @Mock
    private ResearchPatentMapper researchPatentMapper;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private ResearchPatentServiceImpl researchPatentService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchPatentService = new ResearchPatentServiceImpl(studentInfoMapper);
        Field baseMapperField = ResearchPatentServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchPatentService, researchPatentMapper);
    }

    @Test
    void mapByStudentIdsReturnsEmptyForEmptyInput() {
        assertTrue(researchPatentService.mapByStudentIds(List.of()).isEmpty());
        verify(researchPatentMapper, never()).selectList(any());
    }

    @Test
    void mapByStudentIdsGroupsPatents() {
        ResearchPatent patent = new ResearchPatent();
        patent.setId(1L);
        patent.setStudentId(1L);

        when(researchPatentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(patent));

        Map<Long, List<ResearchPatent>> result = researchPatentService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
    }
}
