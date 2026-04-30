package com.scholarship.service.impl;

import com.scholarship.common.event.CacheEvictionEvent;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.LockConstants;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.ScholarshipApplicationSubmitResponse;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.CompetitionAwardMapper;
import com.scholarship.mapper.ResearchPaperMapper;
import com.scholarship.mapper.ResearchPatentMapper;
import com.scholarship.mapper.ResearchProjectMapper;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.service.ApplicationAchievementService;
import com.scholarship.service.ReviewRecordService;
import com.scholarship.service.StudentInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ScholarshipApplicationServiceImpl concurrency tests")
class ScholarshipApplicationServiceImplTest {

    @Mock
    private ScholarshipApplicationMapper applicationMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;
    @Mock
    private StudentInfoService studentInfoService;
    @Mock
    private ApplicationAchievementService applicationAchievementService;
    @Mock
    private ReviewRecordService reviewRecordService;
    @Mock
    private ResearchPaperMapper researchPaperMapper;
    @Mock
    private ResearchPatentMapper researchPatentMapper;
    @Mock
    private ResearchProjectMapper researchProjectMapper;
    @Mock
    private CompetitionAwardMapper competitionAwardMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<CacheEvictionEvent> eventCaptor;

    private ScholarshipApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ScholarshipProperties properties = new ScholarshipProperties();
        properties.getLock().setApplicationSubmitSeconds(10L);
        service = new ScholarshipApplicationServiceImpl(
                applicationMapper,
                properties,
                redisTemplate,
                redissonClient,
                studentInfoService,
                applicationAchievementService,
                reviewRecordService,
                eventPublisher,
                researchPaperMapper,
                researchPatentMapper,
                researchProjectMapper,
                competitionAwardMapper
        );
    }

    @Test
    @DisplayName("submit should fail when application lock is occupied")
    void submitShouldFailWhenLockIsOccupied() throws InterruptedException {
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(12L);
        ScholarshipApplicationSubmitRequest request = new ScholarshipApplicationSubmitRequest();
        request.setBatchId(8L);
        request.setSelfEvaluation("self");

        when(studentInfoService.getByUserId(99L)).thenReturn(studentInfo);
        when(redissonClient.getLock(eq(LockConstants.APPLICATION_SUBMIT + "12:8"))).thenReturn(rLock);
        when(rLock.tryLock(0, 10L, TimeUnit.SECONDS)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.submitApplication(request, 99L));

        assertEquals("申请正在提交，请勿重复操作", exception.getMessage());
        verify(applicationMapper, never()).insert(any(ScholarshipApplication.class));
    }

    @Test
    @DisplayName("submit should return idempotent response when application already exists")
    void submitShouldReturnIdempotentResponseWhenApplicationExists() throws InterruptedException {
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(12L);
        ScholarshipApplicationSubmitRequest request = new ScholarshipApplicationSubmitRequest();
        request.setBatchId(8L);
        request.setSelfEvaluation("self");

        ScholarshipApplication existing = new ScholarshipApplication();
        existing.setId(66L);
        existing.setApplicationNo("SCH-20260429-000001");
        existing.setStatus(1);

        when(studentInfoService.getByUserId(99L)).thenReturn(studentInfo);
        when(redissonClient.getLock(eq(LockConstants.APPLICATION_SUBMIT + "12:8"))).thenReturn(rLock);
        when(rLock.tryLock(0, 10L, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(applicationMapper.selectOne(any())).thenReturn(existing);

        ScholarshipApplicationSubmitResponse response = service.submitApplication(request, 99L);

        assertTrue(response.isIdempotent());
        assertEquals(66L, response.getApplicationId());
        verify(applicationMapper, never()).insert(any(ScholarshipApplication.class));
    }

    @Test
    @DisplayName("review should reject invalid tutor status")
    void reviewShouldRejectInvalidTutorStatus() throws InterruptedException {
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(5L);
        application.setStudentId(10L);
        application.setStatus(7);
        application.setVersion(1);

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(10L);
        studentInfo.setTutorId(101L);

        when(redissonClient.getLock(eq(LockConstants.REVIEW_APPLICATION + 5))).thenReturn(rLock);
        when(rLock.tryLock(eq(30L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(applicationMapper.selectById(5L)).thenReturn(application);
        when(studentInfoService.getById(10L)).thenReturn(studentInfo);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.reviewApplication(5L, "通过", 101L, "tutor", 2)
        );

        assertEquals("当前状态不允许审核", exception.getMessage());
        verify(applicationMapper, never()).updateById(any(ScholarshipApplication.class));
    }

    @Test
    @DisplayName("review should publish cache eviction events after success")
    void reviewShouldPublishCacheEvictionEventsAfterSuccess() throws InterruptedException {
        ScholarshipApplication application = new ScholarshipApplication();
        application.setId(6L);
        application.setStudentId(11L);
        application.setStatus(1);
        application.setVersion(1);

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(11L);
        studentInfo.setTutorId(101L);

        when(redissonClient.getLock(eq(LockConstants.REVIEW_APPLICATION + 6))).thenReturn(rLock);
        when(rLock.tryLock(eq(30L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(applicationMapper.selectById(6L)).thenReturn(application);
        when(studentInfoService.getById(11L)).thenReturn(studentInfo);
        when(applicationMapper.updateById(any(ScholarshipApplication.class))).thenReturn(1);
        when(reviewRecordService.addReviewRecord(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        boolean result = service.reviewApplication(6L, "通过", 101L, "tutor", 2);

        assertTrue(result);
        verify(eventPublisher, times(2)).publishEvent(any(CacheEvictionEvent.class));
    }
}
