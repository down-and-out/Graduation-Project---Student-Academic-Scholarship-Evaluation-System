package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.mapper.CompetitionAwardMapper;
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
 * CompetitionAwardServiceImpl 批量查询方法测试
 */
@DisplayName("CompetitionAwardServiceImpl 批量查询方法测试")
class CompetitionAwardServiceImplTest {

    @Mock
    private CompetitionAwardMapper competitionAwardMapper;

    private CompetitionAwardServiceImpl competitionAwardService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        competitionAwardService = new CompetitionAwardServiceImpl();
        Field baseMapperField = CompetitionAwardServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(competitionAwardService, competitionAwardMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapByStudentIds_EmptyList() {
        Map<Long, List<CompetitionAward>> result = competitionAwardService.mapByStudentIds(List.of());

        assertTrue(result.isEmpty());
        verify(competitionAwardMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapByStudentIds_NullList() {
        Map<Long, List<CompetitionAward>> result = competitionAwardService.mapByStudentIds(null);

        assertTrue(result.isEmpty());
        verify(competitionAwardMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常批量查询")
    void testMapByStudentIds_NormalCase() {
        CompetitionAward award1 = new CompetitionAward();
        award1.setId(1L);
        award1.setStudentId(1L);
        award1.setAuditStatus(1); // 审核通过

        CompetitionAward award2 = new CompetitionAward();
        award2.setId(2L);
        award2.setStudentId(2L);
        award2.setAuditStatus(1);

        when(competitionAwardMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(award1, award2));

        Map<Long, List<CompetitionAward>> result = competitionAwardService.mapByStudentIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals(1, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
        verify(competitionAwardMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试只返回审核通过的获奖")
    void testMapByStudentIds_OnlyApprovedAwards() {
        CompetitionAward approvedAward = new CompetitionAward();
        approvedAward.setId(1L);
        approvedAward.setStudentId(1L);
        approvedAward.setAuditStatus(1); // 审核通过

        when(competitionAwardMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(approvedAward));

        Map<Long, List<CompetitionAward>> result = competitionAwardService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
        assertEquals(1L, result.get(1L).get(0).getId());
    }
}