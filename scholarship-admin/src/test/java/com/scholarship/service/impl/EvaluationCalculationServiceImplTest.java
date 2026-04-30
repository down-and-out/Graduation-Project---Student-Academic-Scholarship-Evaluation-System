package com.scholarship.service.impl;

import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.CompetitionAwardService;
import com.scholarship.service.CourseScoreService;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.MoralPerformanceService;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.ResearchPatentService;
import com.scholarship.service.ResearchProjectService;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.ScoreRuleService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.service.SysSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("EvaluationCalculationServiceImpl batching tests")
class EvaluationCalculationServiceImplTest {

    @Mock
    private EvaluationResultMapper evaluationResultMapper;
    @Mock
    private ScoreRuleService scoreRuleService;
    @Mock
    private ResearchPaperService researchPaperService;
    @Mock
    private ResearchPatentService researchPatentService;
    @Mock
    private ResearchProjectService researchProjectService;
    @Mock
    private CompetitionAwardService competitionAwardService;
    @Mock
    private StudentInfoService studentInfoService;
    @Mock
    private ScholarshipApplicationService scholarshipApplicationService;
    @Mock
    private CourseScoreService courseScoreService;
    @Mock
    private MoralPerformanceService moralPerformanceService;
    @Mock
    private EvaluationBatchService evaluationBatchService;
    @Mock
    private SysSettingService sysSettingService;
    @Mock
    private TransactionTemplate transactionTemplate;

    private EvaluationCalculationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ScholarshipProperties properties = new ScholarshipProperties();
        properties.getEvaluation().setReadPageSize(2L);
        properties.getEvaluation().setWriteBatchSize(2);

        service = Mockito.spy(new EvaluationCalculationServiceImpl(
                scoreRuleService,
                researchPaperService,
                researchPatentService,
                researchProjectService,
                competitionAwardService,
                studentInfoService,
                scholarshipApplicationService,
                courseScoreService,
                moralPerformanceService,
                evaluationBatchService,
                sysSettingService,
                properties,
                transactionTemplate
        ));
        ReflectionTestUtils.setField(service, "baseMapper", evaluationResultMapper);

        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, java.util.function.Consumer.class).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        doReturn(true).when(service).remove(any());
        doReturn(true).when(service).saveBatch(anyList(), anyInt());
        when(studentInfoService.mapByIds(anyList())).thenReturn(Map.of());
        when(researchPaperService.mapByStudentIds(anyList())).thenReturn(Map.of());
        when(researchPatentService.mapByStudentIds(anyList())).thenReturn(Map.of());
        when(researchProjectService.mapByStudentIds(anyList())).thenReturn(Map.of());
        when(competitionAwardService.mapByStudentIds(anyList())).thenReturn(Map.of());
        when(courseScoreService.mapWeightedAverageByStudentIds(anyList(), eq(1L))).thenReturn(Map.of());
        when(moralPerformanceService.mapTotalScoreByStudentIds(anyList(), eq(1L))).thenReturn(Map.of());
    }

    @Test
    @DisplayName("calculate should iterate through multiple pages and accumulate summary")
    void calculateShouldIterateThroughMultiplePagesAndAccumulateSummary() {
        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        batch.setSelectedRuleIdsJson(null);

        ScholarshipApplication app1 = application(1L, 101L);
        ScholarshipApplication app2 = application(2L, 102L);
        ScholarshipApplication app3 = application(3L, 103L);

        when(evaluationBatchService.getById(1L)).thenReturn(batch);
        for (int ruleType = 1; ruleType <= 6; ruleType++) {
            when(scoreRuleService.listAvailableByRuleType(ruleType)).thenReturn(List.of());
        }
        when(scholarshipApplicationService.listApprovedBatchPage(1L, null, 2L)).thenReturn(List.of(app1, app2));
        when(scholarshipApplicationService.listApprovedBatchPage(1L, 2L, 2L)).thenReturn(List.of(app3));
        when(scholarshipApplicationService.listApprovedBatchPage(1L, 3L, 2L)).thenReturn(List.of());

        BatchCalculationSummary summary = service.calculateBatchApplications(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getBatchId());
        assertEquals(3, summary.getProcessedCount());
        assertEquals(3, summary.getWrittenCount());
        assertEquals(2, summary.getPageCount());
        verify(service, times(2)).saveBatch(anyList(), eq(2));
    }

    @Test
    @DisplayName("calculate should return empty summary when no page data")
    void calculateShouldReturnEmptySummaryWhenNoPageData() {
        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(1L);
        batch.setSelectedRuleIdsJson(null);

        when(evaluationBatchService.getById(1L)).thenReturn(batch);
        for (int ruleType = 1; ruleType <= 6; ruleType++) {
            when(scoreRuleService.listAvailableByRuleType(ruleType)).thenReturn(List.of());
        }
        when(scholarshipApplicationService.listApprovedBatchPage(1L, null, 2L)).thenReturn(List.of());

        BatchCalculationSummary summary = service.calculateBatchApplications(1L);

        assertEquals(0, summary.getProcessedCount());
        assertEquals(0, summary.getWrittenCount());
        assertEquals(0, summary.getPageCount());
        verify(service, times(0)).saveBatch(anyList(), anyInt());
    }

    private ScholarshipApplication application(Long id, Long studentId) {
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(id);
        application.setBatchId(1L);
        application.setStudentId(studentId);
        return application;
    }
}
