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

/**
 * 奖学金申请服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipApplicationServiceImpl extends ServiceImpl<ScholarshipApplicationMapper, ScholarshipApplication>
        implements ScholarshipApplicationService {

    private final ScholarshipApplicationMapper applicationMapper;
    private final ScholarshipProperties scholarshipProperties;
    private final StringRedisTemplate redisTemplate;
    private final StudentInfoService studentInfoService;

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Redis 中申请编号计数器的键前缀
     */
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
    public boolean submitApplication(ScholarshipApplication application, Long studentId) {
        // 设置学生 ID
        application.setStudentId(studentId);
        // 使用枚举设置状态
        application.setStatus(ApplicationStatusEnum.SUBMITTED.getCode());
        application.setSubmitTime(LocalDateTime.now());

        // 生成申请编号（格式：SCH-YYYYMMDD- 自增序列）
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

        // 获取申请学生的导师 ID
        StudentInfo student = studentInfoService.getById(application.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 如果学生有导师，验证当前审核人是否为该导师
        if (student.getTutorId() != null) {
            if (!student.getTutorId().equals(tutorId)) {
                log.warn("导师 {} 尝试审核不属于她指导的学生的申请 {}", tutorId, applicationId);
                throw new BusinessException("无权审核该申请，只能审核自己指导学生的申请");
            }
        }

        // 使用枚举处理审核意见
        Boolean passed = ReviewOpinionEnum.isPassed(opinion);
        if (passed == null) {
            // 待审核状态
            application.setStatus(ApplicationStatusEnum.TUTOR_REVIEWING.getCode());
        } else if (passed) {
            // 审核通过
            application.setStatus(ApplicationStatusEnum.TUTOR_PASSED.getCode());
        } else {
            // 审核不通过
            application.setStatus(ApplicationStatusEnum.TUTOR_REJECTED.getCode());
        }

        application.setTutorOpinion(opinion);
        application.setTutorId(tutorId);
        application.setTutorReviewTime(LocalDateTime.now());

        return applicationMapper.updateById(application) > 0;
    }

    /**
     * 生成唯一的申请编号
     * 格式：{prefix}-{yyyyMMdd}-{6 位自增序列}
     * 使用 Lua 脚本保证原子性，避免竞态条件
     *
     * @return 申请编号
     */
    private String generateApplicationNo() {
        String prefix = scholarshipProperties.getApplication().getNumberPrefix();
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);

        // 使用 Redis INCR 生成自增序列
        String counterKey = APP_NO_COUNTER_KEY + dateStr;

        // 使用 Lua 脚本保证 increment 和 expire 的原子性
        String luaScript =
            "local seq = redis.call('INCR', KEYS[1]) " +
            "if seq == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return seq";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long sequence = redisTemplate.execute(redisScript, Collections.singletonList(counterKey), "172800");

        // 兼容处理：如果 Lua 脚本返回 null，使用默认方式
        if (sequence == null) {
            sequence = redisTemplate.opsForValue().increment(counterKey);
            if (sequence != null && sequence == 1) {
                redisTemplate.expire(counterKey, 2, TimeUnit.DAYS);
            }
        }

        // 格式化为 6 位数字
        String seqStr = String.format("%06d", sequence);

        return String.format("%s-%s-%s", prefix, dateStr, seqStr);
    }
}
