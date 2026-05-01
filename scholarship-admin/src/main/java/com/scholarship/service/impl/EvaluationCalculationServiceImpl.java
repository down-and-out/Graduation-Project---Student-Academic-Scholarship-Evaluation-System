package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.dto.WeightSetting;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.entity.ResearchProject;
import com.scholarship.entity.ScoreRule;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.CompetitionAwardService;
import com.scholarship.service.CourseScoreService;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.MoralPerformanceService;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.ResearchPatentService;
import com.scholarship.service.ResearchProjectService;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.ScoreRuleService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationCalculationServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements EvaluationCalculationService {

    private static final BigDecimal PAPER_FIRST_AUTHOR_RATIO = new BigDecimal("1.0");
    private static final BigDecimal PAPER_SECOND_AUTHOR_RATIO = new BigDecimal("0.7");
    private static final BigDecimal PAPER_THIRD_AUTHOR_RATIO = new BigDecimal("0.5");
    private static final BigDecimal PAPER_OTHER_AUTHOR_RATIO = new BigDecimal("0.3");

    private static final BigDecimal PATENT_FIRST_RATIO = new BigDecimal("1.0");
    private static final BigDecimal PATENT_SECOND_RATIO = new BigDecimal("0.6");
    private static final BigDecimal PATENT_THIRD_RATIO = new BigDecimal("0.4");
    private static final BigDecimal PATENT_OTHER_RATIO = new BigDecimal("0.2");

    private static final BigDecimal PROJECT_FIRST_RATIO = new BigDecimal("1.0");
    private static final BigDecimal PROJECT_SECOND_RATIO = new BigDecimal("0.7");
    private static final BigDecimal PROJECT_THIRD_RATIO = new BigDecimal("0.5");
    private static final BigDecimal PROJECT_OTHER_RATIO = new BigDecimal("0.3");

    private static final int RULE_TYPE_COURSE = 5;
    private static final int RULE_TYPE_QUALITY = 6;

    private static final BigDecimal TEAM_FIRST_RATIO = new BigDecimal("1.0");
    private static final BigDecimal TEAM_SECOND_RATIO = new BigDecimal("0.8");
    private static final BigDecimal TEAM_THIRD_RATIO = new BigDecimal("0.6");
    private static final BigDecimal TEAM_OTHER_RATIO = new BigDecimal("0.4");

    private final ScoreRuleService scoreRuleService;
    private final ResearchPaperService researchPaperService;
    private final ResearchPatentService researchPatentService;
    private final ResearchProjectService researchProjectService;
    private final CompetitionAwardService competitionAwardService;
    private final StudentInfoService studentInfoService;
    private final ScholarshipApplicationService scholarshipApplicationService;
    private final CourseScoreService courseScoreService;
    private final MoralPerformanceService moralPerformanceService;
    private final EvaluationBatchService evaluationBatchService;
    private final SysSettingService sysSettingService;
    private final ScholarshipProperties scholarshipProperties;
    private final TransactionTemplate transactionTemplate;

    @Override
    public EvaluationResult calculateApplication(ScholarshipApplication application) {
        log.info("开始计算申请评分，applicationId={}", application.getId());

        Long studentId = application.getStudentId();
        Long batchId = application.getBatchId();

        BigDecimal courseScore = calculateCourseScore(studentId, batchId);
        BigDecimal researchScore = calculateResearchScore(studentId, batchId);
        BigDecimal competitionScore = calculateCompetitionScore(studentId, batchId);
        BigDecimal qualityScore = calculateQualityScore(studentId, batchId);
        BigDecimal totalScore = calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore);

        StudentInfo studentInfo = studentInfoService.getById(studentId);

        EvaluationResult result = new EvaluationResult();
        result.setBatchId(batchId);
        result.setApplicationId(application.getId());
        result.setStudentId(studentId);
        if (studentInfo != null) {
            result.setStudentName(studentInfo.getName());
            result.setStudentNo(studentInfo.getStudentNo());
            result.setDepartment(studentInfo.getDepartment());
            result.setMajor(studentInfo.getMajor());
        }
        result.setCourseScore(courseScore);
        result.setResearchScore(researchScore);
        result.setCompetitionScore(competitionScore);
        result.setQualityScore(qualityScore);
        result.setTotalScore(totalScore);

        return result;
    }

    @Override
    public BatchCalculationSummary calculateBatchApplications(Long batchId) {
        log.info("开始计算批次申请评分，batchId={}", batchId);

        BatchRuleSelection batchRuleSelection = loadBatchRuleSelection(batchId);
        Map<Integer, List<ScoreRule>> rulesByType = batchRuleSelection.rulesByType();
        List<ScoreRule> courseRules = rulesByType.getOrDefault(RULE_TYPE_COURSE, List.of());
        List<ScoreRule> qualityRules = rulesByType.getOrDefault(RULE_TYPE_QUALITY, List.of());
        BigDecimal maxCourseScore = getMaxScoreFromRules(courseRules);
        BigDecimal maxQualityScore = getMaxScoreFromRules(qualityRules);

        BatchCalculationSummary summary = new BatchCalculationSummary();
        summary.setBatchId(batchId);

        // 版本化写入：获取下一个计算轮次
        int calculationRound = queryNextCalculationRound(batchId);

        // 预取权重配置，避免每个学生重复查询
        WeightSetting weightSetting = getWeightSettingSafe();

        // 预取批次学年，避免每页重复查询 evaluation_batch
        EvaluationBatch batch = evaluationBatchService.getById(batchId);
        String academicYear = batch != null ? batch.getAcademicYear() : null;

        Long lastId = null;
        long readPageSize = scholarshipProperties.getEvaluation().getReadPageSize();
        int writeBatchSize = scholarshipProperties.getEvaluation().getWriteBatchSize();

        try {
            while (true) {
                List<ScholarshipApplication> applications = scholarshipApplicationService
                        .listApprovedBatchPage(batchId, lastId, readPageSize);
                if (applications.isEmpty()) {
                    break;
                }

                List<EvaluationResult> resultList = buildBatchResults(
                        batchId,
                        applications,
                        rulesByType,
                        courseRules,
                        qualityRules,
                        maxCourseScore,
                        maxQualityScore,
                        calculationRound,
                        weightSetting,
                        academicYear
                );

                saveBatchResults(resultList, writeBatchSize);

                summary.setProcessedCount(summary.getProcessedCount() + applications.size());
                summary.setWrittenCount(summary.getWrittenCount() + resultList.size());
                summary.setPageCount(summary.getPageCount() + 1);
                lastId = applications.get(applications.size() - 1).getId();
            }

            // 全部写入完成后，批量逻辑删除旧版本
            deleteOldCalculationRounds(batchId, calculationRound);

            log.info("批次计算完成，batchId={}, round={}, processed={}, pages={}",
                    batchId, calculationRound, summary.getProcessedCount(), summary.getPageCount());
        } catch (Exception e) {
            log.error("批次计算异常，batchId={}, round={}, 清理已写入的当前轮次数据", batchId, calculationRound, e);
            cleanCurrentRoundData(batchId, calculationRound);
            throw e;
        }
        return summary;
    }

    @Override
    public BigDecimal calculateCourseScore(Long studentId, Long batchId) {
        List<ScoreRule> rules = loadRulesForType(batchId, 5);
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightedAverage = courseScoreService.calculateWeightedAverage(studentId, batchId);
        BigDecimal maxScore = getMaxScoreFromRules(rules);
        if (maxScore != null && weightedAverage.compareTo(maxScore) > 0) {
            weightedAverage = maxScore;
        }
        return weightedAverage;
    }

    @Override
    public BigDecimal calculateResearchScore(Long studentId, Long batchId) {
        BigDecimal totalScore = BigDecimal.ZERO;

        List<ScoreRule> paperRules = loadRulesForType(batchId, 1);
        List<ScoreRule> patentRules = loadRulesForType(batchId, 2);
        List<ScoreRule> projectRules = loadRulesForType(batchId, 3);

        List<ResearchPaper> papers = researchPaperService.list(
                new LambdaQueryWrapper<ResearchPaper>()
                        .eq(ResearchPaper::getStudentId, studentId)
                        .eq(ResearchPaper::getStatus, 2)
        );
        for (ResearchPaper paper : papers) {
            totalScore = totalScore.add(calculatePaperScore(paper, paperRules));
        }

        List<ResearchPatent> patents = researchPatentService.list(
                new LambdaQueryWrapper<ResearchPatent>()
                        .eq(ResearchPatent::getStudentId, studentId)
                        .eq(ResearchPatent::getAuditStatus, 1)
        );
        for (ResearchPatent patent : patents) {
            totalScore = totalScore.add(calculatePatentScore(patent, patentRules));
        }

        List<ResearchProject> projects = researchProjectService.list(
                new LambdaQueryWrapper<ResearchProject>()
                        .eq(ResearchProject::getStudentId, studentId)
                        .eq(ResearchProject::getAuditStatus, 1)
        );
        for (ResearchProject project : projects) {
            totalScore = totalScore.add(calculateProjectScore(project, projectRules));
        }

        BigDecimal maxResearchScore = getMaxScoreFromRules(paperRules);
        if (maxResearchScore != null && totalScore.compareTo(maxResearchScore) > 0) {
            totalScore = maxResearchScore;
        }
        return totalScore;
    }

    @Override
    public BigDecimal calculateCompetitionScore(Long studentId, Long batchId) {
        List<ScoreRule> rules = loadRulesForType(batchId, 4);
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<CompetitionAward> awards = competitionAwardService.list(
                new LambdaQueryWrapper<CompetitionAward>()
                        .eq(CompetitionAward::getStudentId, studentId)
                        .eq(CompetitionAward::getAuditStatus, 1)
        );

        BigDecimal totalScore = BigDecimal.ZERO;
        for (CompetitionAward award : awards) {
            totalScore = totalScore.add(calculateCompetitionScoreInternal(award, rules));
        }

        BigDecimal maxCompetitionScore = getMaxScoreFromRules(rules);
        if (maxCompetitionScore != null && totalScore.compareTo(maxCompetitionScore) > 0) {
            totalScore = maxCompetitionScore;
        }
        return totalScore;
    }

    @Override
    public BigDecimal calculateQualityScore(Long studentId, Long batchId) {
        List<ScoreRule> rules = loadRulesForType(batchId, 6);
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = moralPerformanceService.calculateTotalScore(studentId, batchId);
        BigDecimal maxQualityScore = getMaxScoreFromRules(rules);
        if (maxQualityScore != null && totalScore.compareTo(maxQualityScore) > 0) {
            totalScore = maxQualityScore;
        }
        return totalScore;
    }

    @Override
    public BigDecimal calculateTotalScore(BigDecimal courseScore, BigDecimal researchScore,
                                          BigDecimal competitionScore, BigDecimal qualityScore) {
        return calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore, getWeightSettingSafe());
    }

    private BigDecimal calculateTotalScore(BigDecimal courseScore, BigDecimal researchScore,
                                           BigDecimal competitionScore, BigDecimal qualityScore,
                                           WeightSetting weightSetting) {
        BigDecimal courseWeight = new BigDecimal("0.4");
        BigDecimal researchWeight = new BigDecimal("0.3");
        BigDecimal competitionWeight = new BigDecimal("0.2");
        BigDecimal qualityWeight = new BigDecimal("0.1");

        if (weightSetting != null
                && weightSetting.getCourseWeight() != null
                && weightSetting.getResearchWeight() != null
                && weightSetting.getCompetitionWeight() != null
                && weightSetting.getComprehensiveWeight() != null) {
            courseWeight = weightToDecimal(weightSetting.getCourseWeight());
            researchWeight = weightToDecimal(weightSetting.getResearchWeight());
            competitionWeight = weightToDecimal(weightSetting.getCompetitionWeight());
            qualityWeight = weightToDecimal(weightSetting.getComprehensiveWeight());
        }

        return courseScore.multiply(courseWeight)
                .add(researchScore.multiply(researchWeight))
                .add(competitionScore.multiply(competitionWeight))
                .add(qualityScore.multiply(qualityWeight))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private WeightSetting getWeightSettingSafe() {
        try {
            return sysSettingService.getWeightSetting();
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("读取权重设置失败，使用默认权重", e);
            return null;
        } catch (Exception e) {
            log.error("读取权重设置异常", e);
            throw new BusinessException("权重配置读取失败，无法计算综合得分", e);
        }
    }

    private List<EvaluationResult> buildBatchResults(Long batchId,
                                                     List<ScholarshipApplication> applications,
                                                     Map<Integer, List<ScoreRule>> rulesByType,
                                                     List<ScoreRule> courseRules,
                                                     List<ScoreRule> qualityRules,
                                                     BigDecimal maxCourseScore,
                                                     BigDecimal maxQualityScore,
                                                     int calculationRound,
                                                     WeightSetting weightSetting,
                                                     String academicYear) {
        List<Long> studentIds = applications.stream()
                .map(ScholarshipApplication::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, StudentInfo> studentInfoMap = studentInfoService.mapByIds(studentIds);
        Map<Long, List<ResearchPaper>> papersByStudent = researchPaperService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchPatent>> patentsByStudent = researchPatentService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchProject>> projectsByStudent = researchProjectService.mapByStudentIds(studentIds);
        Map<Long, List<CompetitionAward>> awardsByStudent = competitionAwardService.mapByStudentIds(studentIds);
        Map<Long, BigDecimal> courseScoresByStudent = courseScoreService.mapWeightedAverageByStudentIds(studentIds, academicYear);
        Map<Long, BigDecimal> moralScoresByStudent = moralPerformanceService.mapTotalScoreByStudentIds(studentIds, academicYear);

        List<EvaluationResult> results = new ArrayList<>(applications.size());
        for (ScholarshipApplication application : applications) {
            Long studentId = application.getStudentId();

            BigDecimal courseScore = courseRules.isEmpty()
                    ? BigDecimal.ZERO
                    : courseScoresByStudent.getOrDefault(studentId, BigDecimal.ZERO);
            BigDecimal researchScore = calculateResearchScoreFromMemory(
                    studentId, rulesByType, papersByStudent, patentsByStudent, projectsByStudent);
            BigDecimal competitionScore = calculateCompetitionScoreFromMemory(
                    studentId, rulesByType, awardsByStudent);
            BigDecimal qualityScore = qualityRules.isEmpty()
                    ? BigDecimal.ZERO
                    : moralScoresByStudent.getOrDefault(studentId, BigDecimal.ZERO);

            if (maxCourseScore != null && courseScore.compareTo(maxCourseScore) > 0) {
                courseScore = maxCourseScore;
            }
            if (maxQualityScore != null && qualityScore.compareTo(maxQualityScore) > 0) {
                qualityScore = maxQualityScore;
            }

            BigDecimal totalScore = calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore, weightSetting);
            StudentInfo studentInfo = studentInfoMap.get(studentId);

            EvaluationResult result = new EvaluationResult();
            result.setBatchId(batchId);
            result.setApplicationId(application.getId());
            result.setStudentId(studentId);
            result.setCalculationRound(calculationRound);
            if (studentInfo != null) {
                result.setStudentName(studentInfo.getName());
                result.setStudentNo(studentInfo.getStudentNo());
                result.setDepartment(studentInfo.getDepartment());
                result.setMajor(studentInfo.getMajor());
            }
            result.setCourseScore(courseScore);
            result.setResearchScore(researchScore);
            result.setCompetitionScore(competitionScore);
            result.setQualityScore(qualityScore);
            result.setTotalScore(totalScore);
            results.add(result);
        }
        return results;
    }

    private int queryNextCalculationRound(Long batchId) {
        EvaluationResult latest = getOne(new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getCalculationRound)
                .last("LIMIT 1"));
        return (latest != null && latest.getCalculationRound() != null)
                ? latest.getCalculationRound() + 1
                : 1;
    }

    private void deleteOldCalculationRounds(Long batchId, int currentRound) {
        transactionTemplate.executeWithoutResult(status ->
                lambdaUpdate()
                        .eq(EvaluationResult::getBatchId, batchId)
                        .lt(EvaluationResult::getCalculationRound, currentRound)
                        .set(EvaluationResult::getDeleted, 1)
                        .update()
        );
    }

    private void cleanCurrentRoundData(Long batchId, int calculationRound) {
        try {
            transactionTemplate.executeWithoutResult(status ->
                    lambdaUpdate()
                            .eq(EvaluationResult::getBatchId, batchId)
                            .eq(EvaluationResult::getCalculationRound, calculationRound)
                            .set(EvaluationResult::getDeleted, 1)
                            .update()
            );
        } catch (Exception ex) {
            log.error("清理当前轮次数据失败，batchId={}, round={}", batchId, calculationRound, ex);
        }
    }

    private void saveBatchResults(List<EvaluationResult> resultList, int writeBatchSize) {
        if (resultList.isEmpty()) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> saveBatch(resultList, writeBatchSize));
    }

    private BatchRuleSelection loadBatchRuleSelection(Long batchId) {
        EvaluationBatch batch = batchId == null ? null : evaluationBatchService.getById(batchId);
        if (batch == null) {
            return new BatchRuleSelection(preloadAllRules());
        }

        String rawSelectedRuleIds = batch.getSelectedRuleIdsJson();
        List<Long> selectedRuleIds = batch.getSelectedRuleIds();

        if (rawSelectedRuleIds == null) {
            return new BatchRuleSelection(preloadAllRules());
        }

        Map<Integer, List<ScoreRule>> rulesByType = scoreRuleService.listByIds(selectedRuleIds).stream()
                .filter(rule -> rule.getIsAvailable() != null && rule.getIsAvailable() == 1)
                .sorted(Comparator.comparing(ScoreRule::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(ScoreRule::getRuleType, HashMap::new, Collectors.toList()));
        return new BatchRuleSelection(rulesByType);
    }

    private Map<Integer, List<ScoreRule>> preloadAllRules() {
        Map<Integer, List<ScoreRule>> rulesByType = new HashMap<>();
        for (int ruleType = 1; ruleType <= 6; ruleType++) {
            rulesByType.put(ruleType, scoreRuleService.listAvailableByRuleType(ruleType));
        }
        return rulesByType;
    }

    private List<ScoreRule> loadRulesForType(Long batchId, Integer ruleType) {
        return loadBatchRuleSelection(batchId).rulesByType().getOrDefault(ruleType, List.of());
    }

    private BigDecimal calculateResearchScoreFromMemory(Long studentId,
                                                        Map<Integer, List<ScoreRule>> rulesByType,
                                                        Map<Long, List<ResearchPaper>> papersByStudent,
                                                        Map<Long, List<ResearchPatent>> patentsByStudent,
                                                        Map<Long, List<ResearchProject>> projectsByStudent) {
        BigDecimal totalScore = BigDecimal.ZERO;

        List<ScoreRule> paperRules = rulesByType.getOrDefault(1, List.of());
        List<ScoreRule> patentRules = rulesByType.getOrDefault(2, List.of());
        List<ScoreRule> projectRules = rulesByType.getOrDefault(3, List.of());

        for (ResearchPaper paper : papersByStudent.getOrDefault(studentId, List.of())) {
            totalScore = totalScore.add(calculatePaperScore(paper, paperRules));
        }
        for (ResearchPatent patent : patentsByStudent.getOrDefault(studentId, List.of())) {
            totalScore = totalScore.add(calculatePatentScore(patent, patentRules));
        }
        for (ResearchProject project : projectsByStudent.getOrDefault(studentId, List.of())) {
            totalScore = totalScore.add(calculateProjectScore(project, projectRules));
        }

        BigDecimal maxResearchScore = getMaxScoreFromRules(paperRules);
        if (maxResearchScore != null && totalScore.compareTo(maxResearchScore) > 0) {
            totalScore = maxResearchScore;
        }
        return totalScore;
    }

    private BigDecimal calculateCompetitionScoreFromMemory(Long studentId,
                                                           Map<Integer, List<ScoreRule>> rulesByType,
                                                           Map<Long, List<CompetitionAward>> awardsByStudent) {
        List<ScoreRule> rules = rulesByType.getOrDefault(4, List.of());
        BigDecimal totalScore = BigDecimal.ZERO;
        for (CompetitionAward award : awardsByStudent.getOrDefault(studentId, List.of())) {
            totalScore = totalScore.add(calculateCompetitionScoreInternal(award, rules));
        }

        BigDecimal maxCompetitionScore = getMaxScoreFromRules(rules);
        if (maxCompetitionScore != null && totalScore.compareTo(maxCompetitionScore) > 0) {
            totalScore = maxCompetitionScore;
        }
        return totalScore;
    }

    private BigDecimal calculatePaperScore(ResearchPaper paper, List<ScoreRule> rules) {
        if (paper.getJournalLevel() == null || rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ScoreRule matchedRule = rules.stream()
                .filter(rule -> rule.getLevel() != null && String.valueOf(paper.getJournalLevel()).equals(rule.getLevel()))
                .findFirst()
                .orElse(null);
        if (matchedRule == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = matchedRule.getScore();
        if (paper.getAuthorRank() != null) {
            score = score.multiply(getRankRatio(paper.getAuthorRank(), PAPER_FIRST_AUTHOR_RATIO, PAPER_SECOND_AUTHOR_RATIO, PAPER_THIRD_AUTHOR_RATIO, PAPER_OTHER_AUTHOR_RATIO));
        }
        if (matchedRule.getMaxScore() != null) {
            score = score.min(matchedRule.getMaxScore());
        }
        return score;
    }

    private BigDecimal calculatePatentScore(ResearchPatent patent, List<ScoreRule> rules) {
        if (patent.getPatentType() == null || rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ScoreRule matchedRule = rules.stream()
                .filter(rule -> rule.getLevel() != null && String.valueOf(patent.getPatentType()).equals(rule.getLevel()))
                .findFirst()
                .orElse(null);
        if (matchedRule == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = matchedRule.getScore();
        if (patent.getApplicantRank() != null) {
            score = score.multiply(getRankRatio(patent.getApplicantRank(), PATENT_FIRST_RATIO, PATENT_SECOND_RATIO, PATENT_THIRD_RATIO, PATENT_OTHER_RATIO));
        }
        if (matchedRule.getMaxScore() != null) {
            score = score.min(matchedRule.getMaxScore());
        }
        return score;
    }

    private BigDecimal calculateProjectScore(ResearchProject project, List<ScoreRule> rules) {
        if (project.getProjectType() == null || rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ScoreRule matchedRule = rules.stream()
                .filter(rule -> rule.getLevel() != null && String.valueOf(project.getProjectType()).equals(rule.getLevel()))
                .findFirst()
                .orElse(null);
        if (matchedRule == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = matchedRule.getScore();
        if (project.getMemberRank() != null) {
            score = score.multiply(getRankRatio(project.getMemberRank(), PROJECT_FIRST_RATIO, PROJECT_SECOND_RATIO, PROJECT_THIRD_RATIO, PROJECT_OTHER_RATIO));
        }
        if (matchedRule.getMaxScore() != null) {
            score = score.min(matchedRule.getMaxScore());
        }
        return score;
    }

    private BigDecimal calculateCompetitionScoreInternal(CompetitionAward award, List<ScoreRule> rules) {
        if (award.getAwardLevel() == null || award.getCompetitionLevel() == null || rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ScoreRule matchedRule = rules.stream()
                .filter(rule -> rule.getLevel() != null
                        && (award.getCompetitionLevel() + "_" + award.getAwardLevel()).equals(rule.getLevel()))
                .findFirst()
                .orElse(null);
        if (matchedRule == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = matchedRule.getScore();
        if (award.getAwardType() == 2 && award.getMemberRank() != null) {
            score = score.multiply(getRankRatio(award.getMemberRank(), TEAM_FIRST_RATIO, TEAM_SECOND_RATIO, TEAM_THIRD_RATIO, TEAM_OTHER_RATIO));
        }
        if (matchedRule.getMaxScore() != null) {
            score = score.min(matchedRule.getMaxScore());
        }
        return score;
    }

    private BigDecimal getRankRatio(Integer rank, BigDecimal first, BigDecimal second,
                                     BigDecimal third, BigDecimal other) {
        if (rank == null) {
            return BigDecimal.ONE;
        }
        return switch (rank) {
            case 1 -> first;
            case 2 -> second;
            case 3 -> third;
            default -> other;
        };
    }

    private BigDecimal getMaxScoreFromRules(List<ScoreRule> rules) {
        return rules.stream()
                .filter(rule -> rule.getMaxScore() != null)
                .map(ScoreRule::getMaxScore)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private static BigDecimal weightToDecimal(Integer weightPercent) {
        return new BigDecimal(weightPercent).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    private record BatchRuleSelection(Map<Integer, List<ScoreRule>> rulesByType) {
    }
}
