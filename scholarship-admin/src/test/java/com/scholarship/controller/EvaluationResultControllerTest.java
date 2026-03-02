package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.service.StudentInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EvaluationResultController 评定结果控制器测试
 */
@DisplayName("EvaluationResultController 评定结果控制器测试")
class EvaluationResultControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EvaluationResultService evaluationResultService;

    @Mock
    private StudentInfoService studentInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        EvaluationResultController controller = new EvaluationResultController(
                evaluationResultService,
                studentInfoService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试分页查询评定结果")
    void testPage() throws Exception {
        Page<EvaluationResult> page = new Page<>(1, 10);
        when(evaluationResultService.page(any(Page.class))).thenReturn(page);

        mockMvc.perform(get("/evaluation-result/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("测试分页查询 - 带筛选条件")
    void testPageWithFilters() throws Exception {
        Page<EvaluationResult> page = new Page<>(1, 10);
        when(evaluationResultService.page(any(Page.class))).thenReturn(page);

        mockMvc.perform(get("/evaluation-result/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("batchId", "1")
                        .param("studentId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取评定结果详情 - 存在")
    void testGetByIdExists() throws Exception {
        EvaluationResult result = new EvaluationResult();
        result.setId(1L);
        result.setStudentId(100L);
        result.setBatchId(1L);

        when(evaluationResultService.getById(1L)).thenReturn(result);

        mockMvc.perform(get("/evaluation-result/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("测试获取评定结果详情 - 不存在")
    void testGetByIdNotExists() throws Exception {
        when(evaluationResultService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/evaluation-result/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("结果不存在"));
    }

    @Test
    @DisplayName("测试默认分页参数")
    void testDefaultPageParams() throws Exception {
        Page<EvaluationResult> page = new Page<>(1, 10);
        when(evaluationResultService.page(any(Page.class))).thenReturn(page);

        mockMvc.perform(get("/evaluation-result/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }
}
