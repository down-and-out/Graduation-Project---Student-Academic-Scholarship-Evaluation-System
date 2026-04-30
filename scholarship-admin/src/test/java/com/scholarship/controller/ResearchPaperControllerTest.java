package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.StudentInfoService;
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
 * ResearchPaperController 科研论文控制器测试
 */
@DisplayName("ResearchPaperController 科研论文控制器测试")
class ResearchPaperControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResearchPaperService researchPaperService;
    @Mock
    private StudentInfoService studentInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ResearchPaperController controller = new ResearchPaperController(researchPaperService, studentInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试分页查询论文")
    void testPage() throws Exception {
        IPage<ResearchPaper> page = new Page<>(1, 10);
        when(researchPaperService.pagePapers(anyLong(), anyLong(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/paper/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试分页查询 - 带筛选条件")
    void testPageWithFilters() throws Exception {
        IPage<ResearchPaper> page = new Page<>(1, 10);
        when(researchPaperService.pagePapers(1L, 10L, 100L, 1))
                .thenReturn(page);

        mockMvc.perform(get("/paper/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("studentId", "100")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取论文详情 - 存在")
    void testGetByIdExists() throws Exception {
        ResearchPaper paper = new ResearchPaper();
        paper.setId(1L);
        paper.setPaperTitle("测试论文标题");
        paper.setStudentId(100L);

        when(researchPaperService.getById(1L)).thenReturn(paper);

        mockMvc.perform(get("/paper/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.paperTitle").value("测试论文标题"));
    }

    @Test
    @DisplayName("测试获取论文详情 - 不存在")
    void testGetByIdNotExists() throws Exception {
        when(researchPaperService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/paper/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("论文不存在"));
    }

    @Test
    @DisplayName("测试默认分页参数")
    void testDefaultPageParams() throws Exception {
        IPage<ResearchPaper> page = new Page<>(1, 10);
        when(researchPaperService.pagePapers(anyLong(), anyLong(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/paper/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
