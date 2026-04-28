package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.dto.param.EvaluationResultQueryParam;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.service.EvaluationTaskService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.AdminEvaluationResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("EvaluationResultController tests")
class EvaluationResultControllerTest {

    @Mock
    private EvaluationResultService evaluationResultService;
    @Mock
    private EvaluationCalculationService evaluationCalculationService;
    @Mock
    private EvaluationRankService evaluationRankService;
    @Mock
    private AwardAllocationService awardAllocationService;
    @Mock
    private EvaluationTaskService evaluationTaskService;
    @Mock
    private StudentInfoService studentInfoService;

    private EvaluationResultController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EvaluationResultController(
                evaluationResultService,
                evaluationCalculationService,
                evaluationRankService,
                awardAllocationService,
                evaluationTaskService,
                studentInfoService
        );
    }

    @Test
    void pageReturnsPagedResults() {
        IPage<AdminEvaluationResultVO> page = new Page<>(1, 10);
        EvaluationResultQueryParam queryParam = new EvaluationResultQueryParam();
        queryParam.setCurrent(1L);
        queryParam.setSize(10L);
        when(evaluationResultService.pageAdminResults(1L, 10L, null, null, null, null, null, null)).thenReturn(page);

        Result<IPage<AdminEvaluationResultVO>> result = controller.page(queryParam, null);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getCurrent());
    }

    @Test
    void getByIdReturnsErrorWhenMissing() {
        when(evaluationResultService.getAdminResultById(eq(999L))).thenReturn(null);

        Result<AdminEvaluationResultVO> result = controller.getById(999L, null);

        assertEquals(500, result.getCode());
        assertNull(result.getData());
    }
}
