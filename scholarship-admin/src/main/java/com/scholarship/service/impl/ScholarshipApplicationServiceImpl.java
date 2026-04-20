package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.ApplicationStatusEnum;
import com.scholarship.common.enums.ReviewOpinionEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.StudentInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipApplicationServiceImpl extends ServiceImpl<ScholarshipApplicationMapper, ScholarshipApplication>
        implements ScholarshipApplicationService {

    private final ScholarshipApplicationMapper applicationMapper;
    private final ScholarshipProperties scholarshipProperties;
    private final StringRedisTemplate redisTemplate;
    private final StudentInfoService studentInfoService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String APP_NO_COUNTER_KEY = "app:no:counter:";

    @Override
    public IPage<ScholarshipApplication> pageApplications(Long current, Long size, Long batchId, Long studentId, Integer status) {
        Page<ScholarshipApplication> page = new Page<>(current, size);

        LambdaQueryWrapper<ScholarshipApplication> wrapper = new LambdaQueryWrapper<>();

        if (batchId != null) {
            wrapper.eq(ScholarshipApplication::getBatchId, batchId);
        }

        if (studentId != null) {
            wrapper.eq(ScholarshipApplication::getStudentId, studentId);
        }

        if (status != null) {
            wrapper.eq(ScholarshipApplication::getStatus, status);
        }

        wrapper.orderByDesc(ScholarshipApplication::getCreateTime);

        return applicationMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(ScholarshipApplication application, Long userId) {
        StudentInfo studentInfo = studentInfoService.getByUserId(userId);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        // student_id 统一保存 student_info.id
        application.setStudentId(studentInfo.getId());
        application.setStatus(ApplicationStatusEnum.SUBMITTED.getCode());
        application.setApplicationTime(now);
        application.setSubmitTime(now);

        String applicationNo = generateApplicationNo();
        application.setApplicationNo(applicationNo);

        log.info("生成申请编号：{}", applicationNo);
        return applicationMapper.insert(application) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tutorReview(Long applicationId, String opinion, Long tutorId) {
        ScholarshipApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        StudentInfo student = studentInfoService.getById(application.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        if (student.getTutorId() != null && !student.getTutorId().equals(tutorId)) {
            log.warn("导师 {} 尝试审核不属于其指导学生的申请 {}", tutorId, applicationId);
            throw new BusinessException("无权审核该申请，只能审核自己指导学生的申请");
        }

        Boolean passed = ReviewOpinionEnum.isPassed(opinion);
        if (passed == null) {
            application.setStatus(ApplicationStatusEnum.TUTOR_REVIEWING.getCode());
        } else if (passed) {
            application.setStatus(ApplicationStatusEnum.TUTOR_PASSED.getCode());
        } else {
            application.setStatus(ApplicationStatusEnum.TUTOR_REJECTED.getCode());
        }

        application.setTutorOpinion(opinion);
        application.setTutorId(tutorId);
        application.setTutorReviewTime(LocalDateTime.now());

        return applicationMapper.updateById(application) > 0;
    }

    private String generateApplicationNo() {
        String prefix = scholarshipProperties.getApplication().getNumberPrefix();
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String counterKey = APP_NO_COUNTER_KEY + dateStr;

        String luaScript =
            "local seq = redis.call('INCR', KEYS[1]) " +
            "if seq == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return seq";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long sequence = redisTemplate.execute(redisScript, Collections.singletonList(counterKey), "172800");

        if (sequence == null) {
            sequence = redisTemplate.opsForValue().increment(counterKey);
            if (sequence != null && sequence == 1) {
                redisTemplate.expire(counterKey, 2, TimeUnit.DAYS);
            }
        }

        String seqStr = String.format("%06d", sequence);
        return String.format("%s-%s-%s", prefix, dateStr, seqStr);
    }
}
