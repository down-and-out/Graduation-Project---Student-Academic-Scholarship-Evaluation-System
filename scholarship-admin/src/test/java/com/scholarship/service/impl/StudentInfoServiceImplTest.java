package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.ResearchPatentService;
import com.scholarship.service.ResearchProjectService;
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

@DisplayName("StudentInfoServiceImpl tests")
class StudentInfoServiceImplTest {

    @Mock
    private StudentInfoMapper studentInfoMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private ResearchPaperService researchPaperService;
    @Mock
    private ResearchPatentService researchPatentService;
    @Mock
    private ResearchProjectService researchProjectService;

    private StudentInfoServiceImpl studentInfoService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        studentInfoService = new StudentInfoServiceImpl(
                studentInfoMapper,
                sysUserMapper,
                researchPaperService,
                researchPatentService,
                researchProjectService
        );
        Field baseMapperField = StudentInfoServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(studentInfoService, studentInfoMapper);
    }

    @Test
    void mapByUserIdsReturnsEmptyForEmptyInput() {
        assertTrue(studentInfoService.mapByUserIds(List.of()).isEmpty());
        verify(studentInfoMapper, never()).selectList(any());
    }

    @Test
    void mapByUserIdsGroupsStudentsByUserId() {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setUserId(101L);
        student1.setName("A");

        StudentInfo student2 = new StudentInfo();
        student2.setId(2L);
        student2.setUserId(102L);
        student2.setName("B");

        when(studentInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(student1, student2));

        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of(101L, 102L));

        assertEquals(2, result.size());
        assertEquals("A", result.get(101L).getName());
        assertEquals("B", result.get(102L).getName());
    }
}
