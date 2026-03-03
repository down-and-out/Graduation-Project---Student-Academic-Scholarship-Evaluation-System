package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
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
 * StudentInfoServiceImpl 批量查询方法测试
 */
@DisplayName("StudentInfoServiceImpl 批量查询方法测试")
class StudentInfoServiceImplTest {

    @Mock
    private StudentInfoMapper studentInfoMapper;

    private StudentInfoServiceImpl studentInfoService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        studentInfoService = new StudentInfoServiceImpl(studentInfoMapper);
        Field baseMapperField = StudentInfoServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(studentInfoService, studentInfoMapper);
    }

    @Test
    @DisplayName("测试空列表返回空 Map")
    void testMapByUserIds_EmptyList() {
        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of());

        assertTrue(result.isEmpty());
        verify(studentInfoMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试 null 列表返回空 Map")
    void testMapByUserIds_NullList() {
        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(null);

        assertTrue(result.isEmpty());
        verify(studentInfoMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试正常批量查询")
    void testMapByUserIds_NormalCase() {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setUserId(101L);
        student1.setName("张三");
        student1.setStudentNo("2024001");

        StudentInfo student2 = new StudentInfo();
        student2.setId(2L);
        student2.setUserId(102L);
        student2.setName("李四");
        student2.setStudentNo("2024002");

        when(studentInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(student1, student2));

        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of(101L, 102L));

        assertEquals(2, result.size());
        assertEquals("张三", result.get(101L).getName());
        assertEquals("李四", result.get(102L).getName());
        verify(studentInfoMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试无匹配数据返回空 Map")
    void testMapByUserIds_NoMatch() {
        when(studentInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of());

        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of(999L));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试按 userId 分组")
    void testMapByUserIds_GroupedByUserId() {
        StudentInfo student = new StudentInfo();
        student.setId(1L);
        student.setUserId(101L);
        student.setName("张三");

        when(studentInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(student));

        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of(101L));

        // 验证 Map 的 key 是 userId 而不是 id
        assertTrue(result.containsKey(101L));
        assertEquals(101L, result.get(101L).getUserId());
    }

    @Test
    @DisplayName("测试重复 userId 处理")
    void testMapByUserIds_DuplicateUserIds() {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setUserId(101L);
        student1.setName("张三");

        StudentInfo student2 = new StudentInfo();
        student2.setId(2L);
        student2.setUserId(101L); // 相同的 userId
        student2.setName("李四");

        // 模拟数据库返回两条相同 userId 的记录（正常情况不应发生）
        when(studentInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(student1, student2));

        Map<Long, StudentInfo> result = studentInfoService.mapByUserIds(List.of(101L));

        // 应该只保留一条记录（第一条）
        assertEquals(1, result.size());
        assertNotNull(result.get(101L));
    }
}