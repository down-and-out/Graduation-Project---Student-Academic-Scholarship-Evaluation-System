package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.mapper.ResearchPatentMapper;
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
 * ResearchPatentServiceImpl 批量查询方法测试
 */
@DisplayName("ResearchPatentServiceImpl 批量查询方法测试")
class ResearchPatentServiceImplTest {

    @Mock
    private ResearchPatentMapper researchPatentMapper;

    private ResearchPatentServiceImpl researchPatentService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        researchPatentService = new ResearchPatentServiceImpl();
        Field baseMapperField = ResearchPatentServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(researchPatentService, researchPatentMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapByStudentIds_EmptyList() {
        Map<Long, List<ResearchPatent>> result = researchPatentService.mapByStudentIds(List.of());

        assertTrue(result.isEmpty());
        verify(researchPatentMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapByStudentIds_NullList() {
        Map<Long, List<ResearchPatent>> result = researchPatentService.mapByStudentIds(null);

        assertTrue(result.isEmpty());
        verify(researchPatentMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常批量查询")
    void testMapByStudentIds_NormalCase() {
        ResearchPatent patent1 = new ResearchPatent();
        patent1.setId(1L);
        patent1.setStudentId(1L);
        patent1.setAuditStatus(1); // 审核通过

        ResearchPatent patent2 = new ResearchPatent();
        patent2.setId(2L);
        patent2.setStudentId(2L);
        patent2.setAuditStatus(1);

        when(researchPatentMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(patent1, patent2));

        Map<Long, List<ResearchPatent>> result = researchPatentService.mapByStudentIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals(1, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
        verify(researchPatentMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试只返回审核通过的专利")
    void testMapByStudentIds_OnlyApprovedPatents() {
        ResearchPatent approvedPatent = new ResearchPatent();
        approvedPatent.setId(1L);
        approvedPatent.setStudentId(1L);
        approvedPatent.setAuditStatus(1); // 审核通过

        when(researchPatentMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(approvedPatent));

        Map<Long, List<ResearchPatent>> result = researchPatentService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
        assertEquals(1L, result.get(1L).get(0).getId());
    }
}