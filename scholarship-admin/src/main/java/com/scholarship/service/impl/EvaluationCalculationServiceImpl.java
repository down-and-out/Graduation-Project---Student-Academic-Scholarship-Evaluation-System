package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluationCalculationServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements EvaluationCalculationService {

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

    public EvaluationCalculationServiceImpl(ScoreRuleService scoreRuleService,
                                            ResearchPaperService researchPaperService,
                                            ResearchPatentService researchPatentService,
                                            ResearchProjectService researchProjectService,
                                            CompetitionAwardService competitionAwardService,
                                            StudentInfoService studentInfoService,
                                            ScholarshipApplicationService scholarshipApplicationService,
                                            CourseScoreService courseScoreService,
                                            MoralPerformanceService moralPerformanceService,
                                            EvaluationBatchService evaluationBatchService) {
        this.scoreRuleService = scoreRuleService;
        this.researchPaperService = researchPaperService;
        this.researchPatentService = researchPatentService;
        this.researchProjectService = researchProjectService;
        this.competitionAwardService = competitionAwardService;
        this.studentInfoService = studentInfoService;
        this.scholarshipApplicationService = scholarshipApplicationService;
        this.courseScoreService = courseScoreService;
        this.moralPerformanceService = moralPerformanceService;
        this.evaluationBatchService = evaluationBatchService;
    }

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
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, EvaluationResult> calculateBatchApplications(Long batchId) {
        log.info("开始计算批次申请评分，batchId={}", batchId);

        List<ScholarshipApplication> applications = scholarshipApplicationService.list(
                new LambdaQueryWrapper<ScholarshipApplication>()
                        .eq(ScholarshipApplication::getBatchId, batchId)
                        .eq(ScholarshipApplication::getStatus, 3)
        );

        if (applications.isEmpty()) {
            return Map.of();
        }

        List<Long> studentIds = applications.stream()
                .map(ScholarshipApplication::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        BatchRuleSelection batchRuleSelection = loadBatchRuleSelection(batchId);
        Map<Integer, List<ScoreRule>> rulesByType = batchRuleSelection.rulesByType();

        Map<Long, StudentInfo> studentInfoMap = studentInfoService.mapByIds(studentIds);
        Map<Long, List<ResearchPaper>> papersByStudent = researchPaperService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchPatent>> patentsByStudent = researchPatentService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchProject>> projectsByStudent = researchProjectService.mapByStudentIds(studentIds);
        Map<Long, List<CompetitionAward>> awardsByStudent = competitionAwardService.mapByStudentIds(studentIds);
        Map<Long, BigDecimal> courseScoresByStudent = courseScoreService.mapWeightedAverageByStudentIds(studentIds, batchId);
        Map<Long, BigDecimal> moralScoresByStudent = moralPerformanceService.mapTotalScoreByStudentIds(studentIds, batchId);

        List<ScoreRule> courseRules = rulesByType.getOrDefault(5, List.of());
        List<ScoreRule> qualityRules = rulesByType.getOrDefault(6, List.of());
        BigDecimal maxCourseScore = getMaxScoreFromRules(courseRules);
        BigDecimal maxQualityScore = getMaxScoreFromRules(qualityRules);

        Map<Long, EvaluationResult> results = new HashMap<>();
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

            BigDecimal totalScore = calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore);
            StudentInfo studentInfo = studentInfoMap.get(studentId);

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

            results.put(application.getId(), result);
        }

        if (!results.isEmpty()) {
            List<EvaluationResult> resultList = new ArrayList<>(results.values());
            remove(new LambdaQueryWrapper<EvaluationResult>()
                    .eq(EvaluationResult::getBatchId, batchId));
            saveBatch(resultList);
        }

        return results;
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
        BigDecimal courseWeight = new BigDecimal("0.4");
        BigDecimal researchWeight = new BigDecimal("0.3");
        BigDecimal competitionWeight = new BigDecimal("0.2");
        BigDecimal qualityWeight = new BigDecimal("0.1");

        return courseScore.multiply(courseWeight)
                .add(researchScore.multiply(researchWeight))
                .add(competitionScore.multiply(competitionWeight))
                .add(qualityScore.multiply(qualityWeight))
                .setScale(2, RoundingMode.HALF_UP);
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
            score = score.multiply(getAuthorRankRatio(paper.getAuthorRank()));
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
            score = score.multiply(getApplicantRankRatio(patent.getApplicantRank()));
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
            score = score.multiply(getMemberRankRatio(project.getMemberRank()));
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
            score = score.multiply(getTeamMemberRatio(award.getMemberRank()));
        }
        if (matchedRule.getMaxScore() != null) {
            score = score.min(matchedRule.getMaxScore());
        }
        return score;
    }

    private BigDecimal getAuthorRankRatio(Integer authorRank) {
        if (authorRank == null) {
            return BigDecimal.ONE;
        }
        return switch (authorRank) {
            case 1 -> new BigDecimal("1.0");
            case 2 -> new BigDecimal("0.7");
            case 3 -> new BigDecimal("0.5");
            default -> new BigDecimal("0.3");
        };
    }

    private BigDecimal getApplicantRankRatio(Integer applicantRank) {
        if (applicantRank == null) {
            return BigDecimal.ONE;
        }
        return switch (applicantRank) {
            case 1 -> new BigDecimal("1.0");
            case 2 -> new BigDecimal("0.6");
            case 3 -> new BigDecimal("0.4");
            default -> new BigDecimal("0.2");
        };
    }

    private BigDecimal getMemberRankRatio(Integer memberRank) {
        if (memberRank == null) {
            return BigDecimal.ONE;
        }
        return switch (memberRank) {
            case 1 -> new BigDecimal("1.0");
            case 2 -> new BigDecimal("0.7");
            case 3 -> new BigDecimal("0.5");
            default -> new BigDecimal("0.3");
        };
    }

    private BigDecimal getTeamMemberRatio(Integer memberRank) {
        if (memberRank == null) {
            return BigDecimal.ONE;
        }
        return switch (memberRank) {
            case 1 -> new BigDecimal("1.0");
            case 2 -> new BigDecimal("0.8");
            case 3 -> new BigDecimal("0.6");
            default -> new BigDecimal("0.4");
        };
    }

    private BigDecimal getMaxScoreFromRules(List<ScoreRule> rules) {
        return rules.stream()
                .filter(rule -> rule.getMaxScore() != null)
                .map(ScoreRule::getMaxScore)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private record BatchRuleSelection(Map<Integer, List<ScoreRule>> rulesByType) {
    }
}
