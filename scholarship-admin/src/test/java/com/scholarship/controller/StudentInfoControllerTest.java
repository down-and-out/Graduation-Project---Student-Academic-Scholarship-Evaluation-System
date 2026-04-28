package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.StudentInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("StudentInfoController tests")
class StudentInfoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentInfoService studentInfoService;
    @Mock
    private SysUserMapper sysUserMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentInfoController(studentInfoService, sysUserMapper)).build();
    }

    @Test
    void pageUsesCurrentSignature() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        when(studentInfoService.pageStudents(1L, 10L, null, List.of(), null, List.of())).thenReturn(page);

        mockMvc.perform(get("/student-info/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addDelegatesToSave() throws Exception {
        when(studentInfoService.save(any(StudentInfo.class))).thenReturn(true);

        mockMvc.perform(post("/student-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"studentNo\":\"2024001\",\"name\":\"A\",\"gender\":1,\"enrollmentYear\":2024,\"educationLevel\":1,\"trainingMode\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateDelegatesToSyncMethod() throws Exception {
        when(studentInfoService.updateStudentWithSync(any(StudentInfo.class), eq(true))).thenReturn(true);

        mockMvc.perform(put("/student-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"userId\":1,\"studentNo\":\"2024001\",\"name\":\"A\",\"gender\":1,\"enrollmentYear\":2024,\"educationLevel\":1,\"trainingMode\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteUsesCascadeResult() throws Exception {
        when(studentInfoService.deleteWithCascade(anyLong())).thenReturn(0);

        mockMvc.perform(delete("/student-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
