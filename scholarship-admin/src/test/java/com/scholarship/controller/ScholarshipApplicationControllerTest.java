package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.vo.ScholarshipApplicationDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@DisplayName("ScholarshipApplicationController tests")
class ScholarshipApplicationControllerTest {

    @Mock
    private ScholarshipApplicationService applicationService;

    private ScholarshipApplicationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ScholarshipApplicationController(applicationService);
    }

    @Test
    void pageReturnsSuccess() {
        IPage<?> page = new Page<>(1, 10);
        when(applicationService.pageApplications(eq(1L), eq(10L), isNull(), isNull(), isNull(), isNull())).thenReturn((IPage) page);

        Result<IPage<com.scholarship.entity.ScholarshipApplication>> result = controller.page(1L, 10L, null, null, null, null);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getCurrent());
    }

    @Test
    void getByIdReturnsErrorWhenMissing() {
        when(applicationService.getDetailById(eq(999L), isNull())).thenReturn(null);

        Result<ScholarshipApplicationDetailVO> result = controller.getById(999L, null);

        assertEquals(500, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdReturnsDetailWhenFound() {
        ScholarshipApplicationDetailVO detail = new ScholarshipApplicationDetailVO();
        detail.setId(1L);
        when(applicationService.getDetailById(eq(1L), isNull())).thenReturn(detail);

        Result<ScholarshipApplicationDetailVO> result = controller.getById(1L, null);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
    }
}
