package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ScholarshipApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ScholarshipApplicationController 奖学金申请控制器测试
 */
@DisplayName("ScholarshipApplicationController 奖学金申请控制器测试")
class ScholarshipApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScholarshipApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ScholarshipApplicationController controller = new ScholarshipApplicationController(applicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试分页查询申请记录")
    void testPage() throws Exception {
        IPage<ScholarshipApplication> page = new Page<>(1, 10);
        when(applicationService.pageApplications(anyLong(), anyLong(), anyLong(), anyLong(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/application/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试分页查询 - 带筛选条件")
    void testPageWithFilters() throws Exception {
        IPage<ScholarshipApplication> page = new Page<>(1, 10);
        when(applicationService.pageApplications(1L, 10L, 1L, 100L, 2))
                .thenReturn(page);

        mockMvc.perform(get("/application/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("batchId", "1")
                        .param("studentId", "100")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取申请详情 - 存在")
    void testGetByIdExists() throws Exception {
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(1L);
        application.setStudentId(100L);
        application.setBatchId(1L);
        application.setStatus(1);

        when(applicationService.getById(1L)).thenReturn(application);

        mockMvc.perform(get("/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("测试获取申请详情 - 不存在")
    void testGetByIdNotExists() throws Exception {
        when(applicationService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/application/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("申请不存在"));
    }

    @Test
    @DisplayName("测试默认分页参数")
    void testDefaultPageParams() throws Exception {
        IPage<ScholarshipApplication> page = new Page<>(1, 10);
        when(applicationService.pageApplications(anyLong(), anyLong(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/application/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
