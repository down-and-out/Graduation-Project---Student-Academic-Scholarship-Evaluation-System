package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.entity.StudentInfo;
import com.scholarship.service.StudentInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudentInfoController 研究生信息控制器测试
 */
@DisplayName("StudentInfoController 研究生信息控制器测试")
class StudentInfoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentInfoService studentInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        StudentInfoController controller = new StudentInfoController(studentInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试分页查询研究生信息")
    void testPage() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        when(studentInfoService.pageStudents(anyLong(), anyLong(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/student-info/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试分页查询 - 带筛选条件")
    void testPageWithFilters() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        when(studentInfoService.pageStudents(1L, 10L, "张三", "计算机学院", 1))
                .thenReturn(page);

        mockMvc.perform(get("/student-info/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("keyword", "张三")
                        .param("department", "计算机学院")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试根据 ID 获取学生信息 - 存在")
    void testGetByIdExists() throws Exception {
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(1L);
        studentInfo.setName("张三");
        studentInfo.setStudentNo("2024001");

        when(studentInfoService.getById(1L)).thenReturn(studentInfo);

        mockMvc.perform(get("/student-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    @DisplayName("测试根据 ID 获取学生信息 - 不存在")
    void testGetByIdNotExists() throws Exception {
        when(studentInfoService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/student-info/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("学生信息不存在"));
    }

    @Test
    @DisplayName("测试新增学生信息 - 成功")
    void testAddSuccess() throws Exception {
        when(studentInfoService.save(any(StudentInfo.class))).thenReturn(true);

        mockMvc.perform(post("/student-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"studentNo\":\"2024001\",\"name\":\"张三\",\"gender\":1,\"enrollmentYear\":2024,\"educationLevel\":1,\"trainingMode\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"));
    }

    @Test
    @DisplayName("测试新增学生信息 - 失败")
    void testAddFailure() throws Exception {
        when(studentInfoService.save(any(StudentInfo.class))).thenReturn(false);

        mockMvc.perform(post("/student-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"studentNo\":\"2024001\",\"name\":\"张三\",\"gender\":1,\"enrollmentYear\":2024,\"educationLevel\":1,\"trainingMode\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("新增失败"));
    }

    @Test
    @DisplayName("测试更新学生信息 - 成功")
    void testUpdateSuccess() throws Exception {
        when(studentInfoService.updateById(any(StudentInfo.class))).thenReturn(true);

        mockMvc.perform(put("/student-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"userId\":1,\"studentNo\":\"2024001\",\"name\":\"张三\",\"gender\":1,\"enrollmentYear\":2024,\"educationLevel\":1,\"trainingMode\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));
    }

    @Test
    @DisplayName("测试删除学生信息 - 成功")
    void testDeleteSuccess() throws Exception {
        when(studentInfoService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/student-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("测试删除学生信息 - 失败")
    void testDeleteFailure() throws Exception {
        when(studentInfoService.removeById(1L)).thenReturn(false);

        mockMvc.perform(delete("/student-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除失败"));
    }

    @Test
    @DisplayName("测试默认分页参数")
    void testDefaultPageParams() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        when(studentInfoService.pageStudents(anyLong(), anyLong(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/student-info/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }
}
