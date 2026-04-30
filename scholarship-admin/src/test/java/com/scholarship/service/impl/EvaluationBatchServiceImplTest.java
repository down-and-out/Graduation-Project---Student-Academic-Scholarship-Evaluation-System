package com.scholarship.service.impl;

import com.scholarship.common.exception.BusinessException;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.mapper.EvaluationTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@DisplayName("EvaluationBatchServiceImpl tests")
class EvaluationBatchServiceImplTest {

    @Mock
    private EvaluationTaskMapper evaluationTaskMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EvaluationBatchServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = Mockito.spy(new EvaluationBatchServiceImpl(evaluationTaskMapper, eventPublisher));
    }

    @Test
    @DisplayName("startPublicity should reject when active evaluation task exists")
    void startPublicityShouldRejectWhenActiveTaskExists() {
        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(10L);
        batch.setBatchStatus(3);

        doReturn(batch).when(service).getById(10L);
        when(evaluationTaskMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.startPublicity(10L));

        assertEquals("当前批次仍有评定任务执行中，暂不能开始公示", exception.getMessage());
    }
}
