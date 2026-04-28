package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.mapper.CompetitionAwardMapper;
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

@DisplayName("CompetitionAwardServiceImpl tests")
class CompetitionAwardServiceImplTest {

    @Mock
    private CompetitionAwardMapper competitionAwardMapper;
    @Mock
    private StudentInfoMapper studentInfoMapper;

    private CompetitionAwardServiceImpl competitionAwardService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        competitionAwardService = new CompetitionAwardServiceImpl(studentInfoMapper);
        Field baseMapperField = CompetitionAwardServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(competitionAwardService, competitionAwardMapper);
    }

    @Test
    void mapByStudentIdsReturnsEmptyForEmptyInput() {
        assertTrue(competitionAwardService.mapByStudentIds(List.of()).isEmpty());
        verify(competitionAwardMapper, never()).selectList(any());
    }

    @Test
    void mapByStudentIdsGroupsAwards() {
        CompetitionAward award = new CompetitionAward();
        award.setId(1L);
        award.setStudentId(1L);

        when(competitionAwardMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(award));

        Map<Long, List<CompetitionAward>> result = competitionAwardService.mapByStudentIds(List.of(1L));

        assertEquals(1, result.get(1L).size());
    }
}
