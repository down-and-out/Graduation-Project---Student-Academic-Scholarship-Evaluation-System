package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.ApplicationStatusEnum;
import com.scholarship.common.enums.ReviewOpinionEnum;
import com.scholarship.common.enums.ReviewStageEnum;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.result.ResultCode;
import com.scholarship.common.support.CacheConstants;
import com.scholarship.common.support.CursorPageHelper;
import com.scholarship.common.support.LockConstants;
import com.scholarship.common.support.RedissonLockSupport;
import com.scholarship.common.util.DataScopeHelper;
import com.scholarship.mapper.StudentInfoMapper;
import org.redisson.api.RedissonClient;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.ScholarshipApplicationSubmitResponse;
import com.scholarship.dto.param.ApplicationAchievementSubmitItem;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ApplicationAchievement;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.entity.ResearchProject;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.CompetitionAwardMapper;
import com.scholarship.mapper.ResearchPaperMapper;
import com.scholarship.mapper.ResearchPatentMapper;
import com.scholarship.mapper.ResearchProjectMapper;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ApplicationAchievementService;
import com.scholarship.service.CacheEvictionService;
import com.scholarship.service.ReviewRecordService;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.ApplicationAchievementVO;
import com.scholarship.vo.ScholarshipApplicationDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipApplicationServiceImpl extends ServiceImpl<ScholarshipApplicationMapper, ScholarshipApplication>
        implements ScholarshipApplicationService {

    private static final String APPLICATION_REMARK_SEPARATOR = "\n\n---补充说明---\n";
    private static final Integer TYPE_PAPER = 1;
    private static final Integer TYPE_PATENT = 2;
    private static final Integer TYPE_PROJECT = 3;
    private static final Integer TYPE_COMPETITION = 4;
    private static final Integer APPROVED_STATUS = 1;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String APP_NO_COUNTER_KEY = "app:no:counter:";
    private final ScholarshipApplicationMapper applicationMapper;
    private final ScholarshipProperties scholarshipProperties;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final StudentInfoService studentInfoService;
    private final ApplicationAchievementService applicationAchievementService;
    private final ReviewRecordService reviewRecordService;
    private final CacheEvictionService cacheEvictionService;
    private final ResearchPaperMapper researchPaperMapper;
    private final ResearchPatentMapper researchPatentMapper;
    private final ResearchProjectMapper researchProjectMapper;
    private final CompetitionAwardMapper competitionAwardMapper;
    private final StudentInfoMapper studentInfoMapper;

    @Override
    @Cacheable(value = CacheConstants.APP_PAGE,
            key = "T(com.scholarship.common.support.CacheConstants).appPageKey(#current, #size, #batchId, #studentId, #status)",
            unless = "#result == null || #result.records == null || #result.records.isEmpty()")
    public IPage<ScholarshipApplication> pageApplications(Long current, Long size, Long batchId, Long studentId, Integer status) {
        CursorPageHelper.validateOffset(current, size, scholarshipProperties.getEvaluation().getMaxOffsetRows());
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
    public List<ScholarshipApplication> listApprovedBatchPage(Long batchId, Long lastId, Long size) {
        Page<ScholarshipApplication> page = new Page<>(1, size, false);
        LambdaQueryWrapper<ScholarshipApplication> wrapper = new LambdaQueryWrapper<ScholarshipApplication>()
                .eq(ScholarshipApplication::getBatchId, batchId)
                .eq(ScholarshipApplication::getStatus, ApplicationStatusEnum.APPROVED.getCode())
                .gt(lastId != null, ScholarshipApplication::getId, lastId)
                .orderByAsc(ScholarshipApplication::getId);
        return applicationMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScholarshipApplicationSubmitResponse submitApplication(ScholarshipApplicationSubmitRequest request, Long userId) {
        StudentInfo studentInfo = studentInfoService.getByUserId(userId);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        String lockKey = LockConstants.APPLICATION_SUBMIT + studentInfo.getId() + ":" + request.getBatchId();
        return RedissonLockSupport.executeWithTryLock(redissonClient, lockKey,
                0, scholarshipProperties.getLock().getApplicationSubmitSeconds(), TimeUnit.SECONDS,
                () -> {
                    ScholarshipApplication existingApplication = findActiveApplication(studentInfo.getId(), request.getBatchId());
                    if (existingApplication != null) {
                        return buildIdempotentSubmitResponse(existingApplication);
                    }

                    LocalDateTime now = LocalDateTime.now();
                    ScholarshipApplication application = new ScholarshipApplication();
                    application.setBatchId(request.getBatchId());
                    application.setStudentId(studentInfo.getId());
                    application.setRemark(serializeApplicationRemark(request.getSelfEvaluation(), request.getRemark()));
                    application.setStatus(ApplicationStatusEnum.SUBMITTED.getCode());
                    application.setApplicationTime(now);
                    application.setSubmitTime(now);
                    application.setApplicationNo(generateApplicationNo());

                    log.info("Generated applicationNo={}, studentId={}, batchId={}",
                            application.getApplicationNo(), studentInfo.getId(), request.getBatchId());

                    try {
                        boolean inserted = applicationMapper.insert(application) > 0;
                        if (!inserted) {
                            throw new BusinessException("申请提交失败，请稍后重试");
                        }
                    } catch (DataIntegrityViolationException e) {
                        ScholarshipApplication duplicateApplication = findActiveApplication(studentInfo.getId(), request.getBatchId());
                        if (duplicateApplication != null) {
                            return buildIdempotentSubmitResponse(duplicateApplication);
                        }
                        throw new BusinessException(ResultCode.APPLICATION_ALREADY_SUBMITTED, "当前批次已存在申请，请勿重复提交");
                    }

                    List<ApplicationAchievement> achievements = buildAchievementsForSubmit(
                            application.getId(),
                            studentInfo.getId(),
                            request.getAchievements()
                    );
                    boolean replaced = applicationAchievementService.replaceByApplicationId(application.getId(), achievements);
                    if (!replaced) {
                        throw new BusinessException("申请提交失败，请稍后重试");
                    }

                    cacheEvictionService.evictApplicationAchievementsForUser(userId);
                    cacheEvictionService.evictApplicationPages();
                    return buildCreatedSubmitResponse(application);
                },
                "申请正在提交，请勿重复操作");
    }

    @Override
    public ScholarshipApplicationDetailVO getDetailById(Long applicationId, LoginUser loginUser) {
        ScholarshipApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return null;
        }

        ensureCanViewApplication(application, loginUser);

        ScholarshipApplicationDetailVO detail = new ScholarshipApplicationDetailVO();
        detail.setId(application.getId());
        detail.setApplicationNo(application.getApplicationNo());
        detail.setBatchId(application.getBatchId());
        detail.setStudentId(application.getStudentId());
        ParsedRemark parsedRemark = parseApplicationRemark(application.getRemark());
        detail.setSelfEvaluation(parsedRemark.selfEvaluation());
        detail.setRemark(parsedRemark.remark());
        detail.setStatus(application.getStatus());
        detail.setTotalScore(application.getTotalScore());
        detail.setTutorOpinion(application.getTutorOpinion());
        detail.setSubmitTime(application.getSubmitTime());
        detail.setAchievements(loadAchievementDetails(applicationId));
        return detail;
    }

    @Override
    @Cacheable(value = CacheConstants.APP_ACHIEVEMENTS, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<ApplicationAchievementVO> listAvailableAchievements(Long userId) {
        StudentInfo studentInfo = studentInfoService.getByUserId(userId);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        Long studentId = studentInfo.getId();
        List<ApplicationAchievementVO> achievements = new ArrayList<>();
        achievements.addAll(loadAvailablePapers(studentId).stream().map(this::toPaperAchievementOption).toList());
        achievements.addAll(loadAvailablePatents(studentId).stream().map(this::toPatentAchievementOption).toList());
        achievements.addAll(loadAvailableProjects(studentId).stream().map(this::toProjectAchievementOption).toList());
        achievements.addAll(loadAvailableCompetitions(studentId).stream().map(this::toCompetitionAchievementOption).toList());

        achievements.sort(Comparator
                .comparing(ApplicationAchievementVO::getAchievementType)
                .thenComparing(ApplicationAchievementVO::getAchievementId, Comparator.reverseOrder()));
        return achievements;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewApplication(Long applicationId, String opinion, Long reviewerId,
                                     String reviewerName, Integer reviewerUserType) {
        ScholarshipApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        StudentInfo student = studentInfoService.getById(application.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        Integer reviewStage = resolveReviewStage(reviewerUserType);
        if (ReviewStageEnum.TUTOR.getCode().equals(reviewStage)
                && student.getTutorId() != null
                && !student.getTutorId().equals(reviewerId)) {
            log.warn("Reviewer {} has no permission to review application {}", reviewerId, applicationId);
            throw new BusinessException("无权审核该申请，只能审核自己指导学生的申请");
        }

        Integer expectedStatus = application.getStatus();
        validateReviewableStatus(expectedStatus, reviewStage);
        Boolean passed = ReviewOpinionEnum.isPassed(opinion);

        // 使用条件更新防止 TOCTOU 竞态：只有状态未变化时才执行更新
        LambdaUpdateWrapper<ScholarshipApplication> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScholarshipApplication::getId, applicationId)
                .eq(ScholarshipApplication::getStatus, expectedStatus);
        buildReviewUpdate(updateWrapper, reviewStage, passed, opinion, reviewerId);

        boolean updated = applicationMapper.update(null, updateWrapper) > 0;
        if (!updated) {
            throw new BusinessException("申请状态已变化，请刷新后重试");
        }

        cacheEvictionService.evictApplicationDetail(applicationId);
        cacheEvictionService.evictApplicationPages();

        return reviewRecordService.addReviewRecord(
                applicationId,
                reviewStage,
                reviewerId,
                reviewerName,
                resolveReviewResultCode(passed),
                application.getTotalScore(),
                opinion
        );
    }

    private ScholarshipApplication findActiveApplication(Long studentId, Long batchId) {
        return applicationMapper.selectOne(new LambdaQueryWrapper<ScholarshipApplication>()
                .eq(ScholarshipApplication::getStudentId, studentId)
                .eq(ScholarshipApplication::getBatchId, batchId)
                .last("LIMIT 1"));
    }

    private void ensureCanViewApplication(ScholarshipApplication application, LoginUser loginUser) {
        DataScopeHelper.ensureReadable(application.getStudentId(), loginUser, "申请", studentInfoMapper);
    }

    private void validateReviewableStatus(Integer currentStatus, Integer reviewStage) {
        Integer expectedStatus = ReviewStageEnum.TUTOR.getCode().equals(reviewStage)
                ? ApplicationStatusEnum.SUBMITTED.getCode()
                : ApplicationStatusEnum.TUTOR_PASSED.getCode();
        if (!expectedStatus.equals(currentStatus)) {
            throw new BusinessException("当前状态不允许审核");
        }
    }

    private String generateApplicationNo() {
        String prefix = scholarshipProperties.getApplication().getNumberPrefix();
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String counterKey = APP_NO_COUNTER_KEY + dateStr;
        String luaScript = "local seq = redis.call('INCR', KEYS[1]) "
                + "if seq == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                + "return seq";
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

    private List<ApplicationAchievement> buildAchievementsForSubmit(Long applicationId,
                                                                    Long studentId,
                                                                    List<ApplicationAchievementSubmitItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<ApplicationAchievementSubmitItem> normalizedItems = items.stream()
                .filter(item -> item.getAchievementType() != null && item.getAchievementId() != null)
                .toList();

        Map<Long, ResearchPaper> paperMap = loadPaperMap(studentId, normalizedItems);
        Map<Long, ResearchPatent> patentMap = loadPatentMap(studentId, normalizedItems);
        Map<Long, ResearchProject> projectMap = loadProjectMap(studentId, normalizedItems);
        Map<Long, CompetitionAward> competitionMap = loadCompetitionMap(studentId, normalizedItems);

        List<ApplicationAchievement> achievements = new ArrayList<>();
        for (ApplicationAchievementSubmitItem item : normalizedItems) {
            achievements.add(buildAchievementEntity(applicationId, item, paperMap, patentMap, projectMap, competitionMap));
        }
        return achievements;
    }

    private ApplicationAchievement buildAchievementEntity(Long applicationId,
                                                          ApplicationAchievementSubmitItem item,
                                                          Map<Long, ResearchPaper> paperMap,
                                                          Map<Long, ResearchPatent> patentMap,
                                                          Map<Long, ResearchProject> projectMap,
                                                          Map<Long, CompetitionAward> competitionMap) {
        ApplicationAchievement achievement = new ApplicationAchievement();
        achievement.setApplicationId(applicationId);
        achievement.setAchievementType(item.getAchievementType());
        achievement.setAchievementId(item.getAchievementId());
        achievement.setVersion(1);

        switch (item.getAchievementType()) {
            case 1 -> {
                ResearchPaper paper = paperMap.get(item.getAchievementId());
                if (paper == null) {
                    throw new BusinessException("存在不属于当前学生或未审核通过的论文成果");
                }
                achievement.setScore(defaultScore(paper.getImpactFactor()));
                achievement.setScoreComment(buildScoreComment("论文成果", paper.getImpactFactor(), "影响因子"));
            }
            case 2 -> {
                ResearchPatent patent = patentMap.get(item.getAchievementId());
                if (patent == null) {
                    throw new BusinessException("存在不属于当前学生或未审核通过的专利成果");
                }
                achievement.setScore(defaultScore(patent.getScore()));
                achievement.setScoreComment(buildScoreComment("专利成果", patent.getScore(), "成果评分"));
            }
            case 3 -> {
                ResearchProject project = projectMap.get(item.getAchievementId());
                if (project == null) {
                    throw new BusinessException("存在不属于当前学生或未审核通过的项目成果");
                }
                achievement.setScore(defaultScore(project.getScore()));
                achievement.setScoreComment(buildScoreComment("项目成果", project.getScore(), "成果评分"));
            }
            case 4 -> {
                CompetitionAward competition = competitionMap.get(item.getAchievementId());
                if (competition == null) {
                    throw new BusinessException("存在不属于当前学生或未审核通过的竞赛成果");
                }
                achievement.setScore(defaultScore(competition.getScore()));
                achievement.setScoreComment(buildScoreComment("竞赛成果", competition.getScore(), "成果评分"));
            }
            default -> throw new BusinessException("存在未知成果类型，无法提交申请");
        }
        return achievement;
    }

    private Map<Long, ResearchPaper> loadPaperMap(Long studentId, List<ApplicationAchievementSubmitItem> items) {
        List<Long> ids = collectAchievementIds(items, TYPE_PAPER);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchPaperMapper.selectList(new LambdaQueryWrapper<ResearchPaper>()
                        .eq(ResearchPaper::getStudentId, studentId)
                        .eq(ResearchPaper::getStatus, APPROVED_STATUS)
                        .in(ResearchPaper::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchPaper::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, ResearchPatent> loadPatentMap(Long studentId, List<ApplicationAchievementSubmitItem> items) {
        List<Long> ids = collectAchievementIds(items, TYPE_PATENT);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchPatentMapper.selectList(new LambdaQueryWrapper<ResearchPatent>()
                        .eq(ResearchPatent::getStudentId, studentId)
                        .eq(ResearchPatent::getAuditStatus, APPROVED_STATUS)
                        .in(ResearchPatent::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchPatent::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, ResearchProject> loadProjectMap(Long studentId, List<ApplicationAchievementSubmitItem> items) {
        List<Long> ids = collectAchievementIds(items, TYPE_PROJECT);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchProjectMapper.selectList(new LambdaQueryWrapper<ResearchProject>()
                        .eq(ResearchProject::getStudentId, studentId)
                        .eq(ResearchProject::getAuditStatus, APPROVED_STATUS)
                        .in(ResearchProject::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchProject::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, CompetitionAward> loadCompetitionMap(Long studentId, List<ApplicationAchievementSubmitItem> items) {
        List<Long> ids = collectAchievementIds(items, TYPE_COMPETITION);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return competitionAwardMapper.selectList(new LambdaQueryWrapper<CompetitionAward>()
                        .eq(CompetitionAward::getStudentId, studentId)
                        .eq(CompetitionAward::getAuditStatus, APPROVED_STATUS)
                        .in(CompetitionAward::getId, ids))
                .stream()
                .collect(Collectors.toMap(CompetitionAward::getId, Function.identity(), (a, b) -> a));
    }

    private List<Long> collectAchievementIds(List<ApplicationAchievementSubmitItem> items, Integer type) {
        return items.stream()
                .filter(item -> type.equals(item.getAchievementType()))
                .map(ApplicationAchievementSubmitItem::getAchievementId)
                .distinct()
                .toList();
    }

    private List<ResearchPaper> loadAvailablePapers(Long studentId) {
        return researchPaperMapper.selectList(new LambdaQueryWrapper<ResearchPaper>()
                .eq(ResearchPaper::getStudentId, studentId)
                .eq(ResearchPaper::getStatus, APPROVED_STATUS)
                .orderByDesc(ResearchPaper::getCreateTime));
    }

    private List<ResearchPatent> loadAvailablePatents(Long studentId) {
        return researchPatentMapper.selectList(new LambdaQueryWrapper<ResearchPatent>()
                .eq(ResearchPatent::getStudentId, studentId)
                .eq(ResearchPatent::getAuditStatus, APPROVED_STATUS)
                .orderByDesc(ResearchPatent::getCreateTime));
    }

    private List<ResearchProject> loadAvailableProjects(Long studentId) {
        return researchProjectMapper.selectList(new LambdaQueryWrapper<ResearchProject>()
                .eq(ResearchProject::getStudentId, studentId)
                .eq(ResearchProject::getAuditStatus, APPROVED_STATUS)
                .orderByDesc(ResearchProject::getCreateTime));
    }

    private List<CompetitionAward> loadAvailableCompetitions(Long studentId) {
        return competitionAwardMapper.selectList(new LambdaQueryWrapper<CompetitionAward>()
                .eq(CompetitionAward::getStudentId, studentId)
                .eq(CompetitionAward::getAuditStatus, APPROVED_STATUS)
                .orderByDesc(CompetitionAward::getCreateTime));
    }

    private List<ApplicationAchievementVO> loadAchievementDetails(Long applicationId) {
        List<ApplicationAchievement> links = applicationAchievementService.listByApplicationId(applicationId);
        if (links.isEmpty()) {
            return List.of();
        }

        Map<Long, ResearchPaper> paperMap = loadPapersByIds(collectLinkedAchievementIds(links, TYPE_PAPER));
        Map<Long, ResearchPatent> patentMap = loadPatentsByIds(collectLinkedAchievementIds(links, TYPE_PATENT));
        Map<Long, ResearchProject> projectMap = loadProjectsByIds(collectLinkedAchievementIds(links, TYPE_PROJECT));
        Map<Long, CompetitionAward> competitionMap = loadCompetitionsByIds(collectLinkedAchievementIds(links, TYPE_COMPETITION));

        return links.stream()
                .map(link -> toAchievementDetail(link, paperMap, patentMap, projectMap, competitionMap))
                .toList();
    }

    private List<Long> collectLinkedAchievementIds(List<ApplicationAchievement> links, Integer type) {
        return links.stream()
                .filter(item -> type.equals(item.getAchievementType()))
                .map(ApplicationAchievement::getAchievementId)
                .distinct()
                .toList();
    }

    private Map<Long, ResearchPaper> loadPapersByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchPaperMapper.selectList(new LambdaQueryWrapper<ResearchPaper>().in(ResearchPaper::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchPaper::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, ResearchPatent> loadPatentsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchPatentMapper.selectList(new LambdaQueryWrapper<ResearchPatent>().in(ResearchPatent::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchPatent::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, ResearchProject> loadProjectsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return researchProjectMapper.selectList(new LambdaQueryWrapper<ResearchProject>().in(ResearchProject::getId, ids))
                .stream()
                .collect(Collectors.toMap(ResearchProject::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, CompetitionAward> loadCompetitionsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return competitionAwardMapper.selectList(new LambdaQueryWrapper<CompetitionAward>().in(CompetitionAward::getId, ids))
                .stream()
                .collect(Collectors.toMap(CompetitionAward::getId, Function.identity(), (a, b) -> a));
    }

    private ApplicationAchievementVO toAchievementDetail(ApplicationAchievement link,
                                                         Map<Long, ResearchPaper> paperMap,
                                                         Map<Long, ResearchPatent> patentMap,
                                                         Map<Long, ResearchProject> projectMap,
                                                         Map<Long, CompetitionAward> competitionMap) {
        ApplicationAchievementVO vo = new ApplicationAchievementVO();
        vo.setId(link.getId());
        vo.setAchievementType(link.getAchievementType());
        vo.setAchievementId(link.getAchievementId());
        vo.setScore(link.getScore());
        vo.setScoreComment(link.getScoreComment());
        switch (link.getAchievementType()) {
            case 1 -> fillPaperInfo(vo, paperMap.get(link.getAchievementId()));
            case 2 -> fillPatentInfo(vo, patentMap.get(link.getAchievementId()));
            case 3 -> fillProjectInfo(vo, projectMap.get(link.getAchievementId()));
            case 4 -> fillCompetitionInfo(vo, competitionMap.get(link.getAchievementId()));
            default -> vo.setSubtitle("未知成果类型");
        }
        return vo;
    }

    private ApplicationAchievementVO toPaperAchievementOption(ResearchPaper paper) {
        ApplicationAchievementVO vo = new ApplicationAchievementVO();
        vo.setAchievementType(TYPE_PAPER);
        vo.setAchievementId(paper.getId());
        vo.setScore(defaultScore(paper.getImpactFactor()));
        vo.setScoreComment(buildScoreComment("论文成果", paper.getImpactFactor(), "影响因子"));
        fillPaperInfo(vo, paper);
        return vo;
    }

    private ApplicationAchievementVO toPatentAchievementOption(ResearchPatent patent) {
        ApplicationAchievementVO vo = new ApplicationAchievementVO();
        vo.setAchievementType(TYPE_PATENT);
        vo.setAchievementId(patent.getId());
        vo.setScore(defaultScore(patent.getScore()));
        vo.setScoreComment(buildScoreComment("专利成果", patent.getScore(), "成果评分"));
        fillPatentInfo(vo, patent);
        return vo;
    }

    private ApplicationAchievementVO toProjectAchievementOption(ResearchProject project) {
        ApplicationAchievementVO vo = new ApplicationAchievementVO();
        vo.setAchievementType(TYPE_PROJECT);
        vo.setAchievementId(project.getId());
        vo.setScore(defaultScore(project.getScore()));
        vo.setScoreComment(buildScoreComment("项目成果", project.getScore(), "成果评分"));
        fillProjectInfo(vo, project);
        return vo;
    }

    private ApplicationAchievementVO toCompetitionAchievementOption(CompetitionAward competition) {
        ApplicationAchievementVO vo = new ApplicationAchievementVO();
        vo.setAchievementType(TYPE_COMPETITION);
        vo.setAchievementId(competition.getId());
        vo.setScore(defaultScore(competition.getScore()));
        vo.setScoreComment(buildScoreComment("竞赛成果", competition.getScore(), "成果评分"));
        fillCompetitionInfo(vo, competition);
        return vo;
    }

    private void fillPaperInfo(ApplicationAchievementVO vo, ResearchPaper paper) {
        if (paper == null) {
            return;
        }
        vo.setTitle(defaultText(paper.getPaperTitle(), "未命名论文"));
        vo.setSubtitle(defaultText(paper.getJournalName(), "未填写期刊"));
        vo.setAuthors(defaultText(paper.getAuthors(), "未填写作者"));
    }

    private void fillPatentInfo(ApplicationAchievementVO vo, ResearchPatent patent) {
        if (patent == null) {
            return;
        }
        vo.setTitle(defaultText(patent.getPatentName(), "未命名专利"));
        vo.setSubtitle(defaultText(patent.getPatentNo(), defaultText(patent.getApplicant(), "未填写专利号")));
        vo.setAuthors(defaultText(patent.getInventors(), defaultText(patent.getApplicant(), "未填写发明人")));
    }

    private void fillProjectInfo(ApplicationAchievementVO vo, ResearchProject project) {
        if (project == null) {
            return;
        }
        vo.setTitle(defaultText(project.getProjectName(), "未命名项目"));
        vo.setSubtitle(defaultText(project.getProjectNo(), defaultText(project.getProjectSource(), "未填写项目编号")));
        vo.setAuthors(defaultText(project.getLeaderName(), defaultText(project.getParticipants(), "未填写负责人")));
    }

    private void fillCompetitionInfo(ApplicationAchievementVO vo, CompetitionAward competition) {
        if (competition == null) {
            return;
        }
        vo.setTitle(defaultText(competition.getCompetitionName(), "未命名竞赛"));
        vo.setSubtitle(defaultText(competition.getIssuingUnit(), defaultText(competition.getOrganizer(), "未填写主办单位")));
        vo.setAuthors(defaultText(competition.getInstructor(), defaultText(competition.getTeamMembers(), "未填写人员信息")));
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO : score;
    }

    private String buildScoreComment(String label, BigDecimal score, String sourceName) {
        if (score == null) {
            return "申请时关联" + label + "，" + sourceName + "为空，按0分记录";
        }
        return "申请时关联" + label + "，分值来源：" + sourceName;
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String serializeApplicationRemark(String selfEvaluation, String remark) {
        String trimmedSelfEvaluation = selfEvaluation == null ? "" : selfEvaluation.trim();
        String trimmedRemark = remark == null ? "" : remark.trim();
        if (!trimmedSelfEvaluation.isEmpty() && !trimmedRemark.isEmpty()) {
            return trimmedSelfEvaluation + APPLICATION_REMARK_SEPARATOR + trimmedRemark;
        }
        if (!trimmedSelfEvaluation.isEmpty()) {
            return trimmedSelfEvaluation;
        }
        return trimmedRemark;
    }

    private ParsedRemark parseApplicationRemark(String text) {
        if (text == null || text.isBlank()) {
            return new ParsedRemark("", "");
        }
        String[] parts = text.split(APPLICATION_REMARK_SEPARATOR, 2);
        if (parts.length == 2) {
            return new ParsedRemark(parts[0], parts[1]);
        }
        return new ParsedRemark(text, "");
    }

    private Integer resolveReviewStage(Integer reviewerUserType) {
        return switch (reviewerUserType) {
            case 2 -> ReviewStageEnum.TUTOR.getCode();
            case 3 -> ReviewStageEnum.DEPARTMENT.getCode();
            default -> throw new BusinessException("无效的审核用户类型");
        };
    }

    private void buildReviewUpdate(LambdaUpdateWrapper<ScholarshipApplication> wrapper,
                                    Integer reviewStage, Boolean passed,
                                    String opinion, Long reviewerId) {
        LocalDateTime now = LocalDateTime.now();
        if (ReviewStageEnum.TUTOR.getCode().equals(reviewStage)) {
            if (passed == null) {
                wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.TUTOR_REVIEWING.getCode());
            } else if (passed) {
                wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.TUTOR_PASSED.getCode());
            } else {
                wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.TUTOR_REJECTED.getCode());
            }
            wrapper.set(ScholarshipApplication::getTutorOpinion, opinion);
            wrapper.set(ScholarshipApplication::getTutorId, reviewerId);
            wrapper.set(ScholarshipApplication::getTutorReviewTime, now);
            return;
        }

        if (passed == null) {
            wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.ADMIN_REVIEWING.getCode());
        } else if (passed) {
            wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.APPROVED.getCode());
        } else {
            wrapper.set(ScholarshipApplication::getStatus, ApplicationStatusEnum.ADMIN_REJECTED.getCode());
        }
        wrapper.set(ScholarshipApplication::getCollegeOpinion, opinion);
        wrapper.set(ScholarshipApplication::getCollegeReviewerId, reviewerId);
        wrapper.set(ScholarshipApplication::getCollegeReviewTime, now);
    }

    private Integer resolveReviewResultCode(Boolean passed) {
        if (passed == null) {
            return 3;
        }
        return passed ? 1 : 2;
    }

    private ScholarshipApplicationSubmitResponse buildCreatedSubmitResponse(ScholarshipApplication application) {
        ScholarshipApplicationSubmitResponse response = new ScholarshipApplicationSubmitResponse();
        response.setApplicationId(application.getId());
        response.setApplicationNo(application.getApplicationNo());
        response.setStatus(application.getStatus());
        response.setCreated(true);
        response.setIdempotent(false);
        response.setMessage("提交成功");
        return response;
    }

    private ScholarshipApplicationSubmitResponse buildIdempotentSubmitResponse(ScholarshipApplication application) {
        ScholarshipApplicationSubmitResponse response = new ScholarshipApplicationSubmitResponse();
        response.setApplicationId(application.getId());
        response.setApplicationNo(application.getApplicationNo());
        response.setStatus(application.getStatus());
        response.setCreated(false);
        response.setIdempotent(true);
        response.setMessage("当前批次已存在申请，已返回已有记录");
        return response;
    }

    private record ParsedRemark(String selfEvaluation, String remark) {
    }
}
