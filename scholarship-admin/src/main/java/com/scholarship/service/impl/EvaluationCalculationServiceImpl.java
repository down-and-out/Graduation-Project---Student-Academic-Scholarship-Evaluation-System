package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.*;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评定计算服务实现类
 * <p>
 * 负责计算学生的各项评分，包括课程成绩、科研成果、竞赛获奖、综合素质等
 * 已优化：使用批量查询避免 N+1 问题
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
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

    public EvaluationCalculationServiceImpl(ScoreRuleService scoreRuleService,
                                            ResearchPaperService researchPaperService,
                                            ResearchPatentService researchPatentService,
                                            ResearchProjectService researchProjectService,
                                            CompetitionAwardService competitionAwardService,
                                            StudentInfoService studentInfoService,
                                            ScholarshipApplicationService scholarshipApplicationService,
                                            CourseScoreService courseScoreService,
                                            MoralPerformanceService moralPerformanceService) {
        this.scoreRuleService = scoreRuleService;
        this.researchPaperService = researchPaperService;
        this.researchPatentService = researchPatentService;
        this.researchProjectService = researchProjectService;
        this.competitionAwardService = competitionAwardService;
        this.studentInfoService = studentInfoService;
        this.scholarshipApplicationService = scholarshipApplicationService;
        this.courseScoreService = courseScoreService;
        this.moralPerformanceService = moralPerformanceService;
    }

    @Override
    public EvaluationResult calculateApplication(ScholarshipApplication application) {
        log.info("开始计算申请评分，applicationId={}", application.getId());

        Long studentId = application.getStudentId();
        Long batchId = application.getBatchId();

        // 计算各项分数
        BigDecimal courseScore = calculateCourseScore(studentId, batchId);
        BigDecimal researchScore = calculateResearchScore(studentId, batchId);
        BigDecimal competitionScore = calculateCompetitionScore(studentId, batchId);
        BigDecimal qualityScore = calculateQualityScore(studentId, batchId);
        BigDecimal totalScore = calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore);

        // 获取学生信息
        StudentInfo studentInfo = studentInfoService.getByUserId(studentId);

        // 构建评定结果
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

        log.info("申请评分计算完成，applicationId={}, totalScore={}", application.getId(), totalScore);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, EvaluationResult> calculateBatchApplications(Long batchId) {
        log.info("开始计算批次所有申请评分，batchId={}", batchId);

        // 1. 查询该批次下所有申请（1次查询）
        List<ScholarshipApplication> applications = scholarshipApplicationService.list(
            new LambdaQueryWrapper<ScholarshipApplication>()
                .eq(ScholarshipApplication::getBatchId, batchId)
                .eq(ScholarshipApplication::getStatus, 3) // 评审完成状态
        );

        if (applications.isEmpty()) {
            log.info("该批次无申请记录，batchId={}", batchId);
            return Map.of();
        }

        // 2. 提取学生ID列表
        List<Long> studentIds = applications.stream()
            .map(ScholarshipApplication::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        log.info("批量计算：申请数量={}，学生数量={}", applications.size(), studentIds.size());

        // 3. 预加载所有评分规则（6次查询，但不依赖学生ID，全局缓存）
        Map<Integer, List<ScoreRule>> rulesByType = preloadAllRules();

        // 4. 批量查询所有学生数据（各1次查询）
        log.debug("开始批量查询学生数据...");
        Map<Long, StudentInfo> studentInfoMap = studentInfoService.mapByUserIds(studentIds);
        Map<Long, List<ResearchPaper>> papersByStudent = researchPaperService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchPatent>> patentsByStudent = researchPatentService.mapByStudentIds(studentIds);
        Map<Long, List<ResearchProject>> projectsByStudent = researchProjectService.mapByStudentIds(studentIds);
        Map<Long, List<CompetitionAward>> awardsByStudent = competitionAwardService.mapByStudentIds(studentIds);
        Map<Long, BigDecimal> courseScoresByStudent = courseScoreService.mapWeightedAverageByStudentIds(studentIds, batchId);
        Map<Long, BigDecimal> moralScoresByStudent = moralPerformanceService.mapTotalScoreByStudentIds(studentIds, batchId);
        log.debug("批量查询学生数据完成");

        // 5. 内存计算（无数据库查询）
        // 预先获取分数上限
        List<ScoreRule> courseRules = rulesByType.getOrDefault(5, List.of());
        List<ScoreRule> qualityRules = rulesByType.getOrDefault(6, List.of());
        BigDecimal maxCourseScore = getMaxScoreFromRules(courseRules);
        BigDecimal maxQualityScore = getMaxScoreFromRules(qualityRules);

        Map<Long, EvaluationResult> results = new HashMap<>();
        for (ScholarshipApplication application : applications) {
            Long studentId = application.getStudentId();

            // 从预加载数据中计算各项分数
            BigDecimal courseScore = courseScoresByStudent.getOrDefault(studentId, BigDecimal.ZERO);
            BigDecimal researchScore = calculateResearchScoreFromMemory(
                studentId, rulesByType, papersByStudent, patentsByStudent, projectsByStudent);
            BigDecimal competitionScore = calculateCompetitionScoreFromMemory(
                studentId, rulesByType, awardsByStudent);
            BigDecimal qualityScore = moralScoresByStudent.getOrDefault(studentId, BigDecimal.ZERO);

            // 应用分数上限检查
            if (maxCourseScore != null && courseScore.compareTo(maxCourseScore) > 0) {
                courseScore = maxCourseScore;
            }
            if (maxQualityScore != null && qualityScore.compareTo(maxQualityScore) > 0) {
                qualityScore = maxQualityScore;
            }

            BigDecimal totalScore = calculateTotalScore(courseScore, researchScore, competitionScore, qualityScore);

            // 获取学生信息
            StudentInfo studentInfo = studentInfoMap.get(studentId);

            // 构建评定结果
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

        log.info("批次申请评分计算完成，batchId={}, 计算数量={}", batchId, results.size());

        // 保存评定结果到数据库
        if (!results.isEmpty()) {
            List<EvaluationResult> resultList = new ArrayList<>(results.values());
            // 先删除该批次已有的评定结果（避免重复）
            remove(new LambdaQueryWrapper<EvaluationResult>()
                    .eq(EvaluationResult::getBatchId, batchId));
            // 批量保存新的评定结果
            saveBatch(resultList);
            log.info("批次评定结果已保存到数据库，batchId={}, 数量={}", batchId, resultList.size());
        }

        return results;
    }

    /**
     * 预加载所有评分规则
     */
    private Map<Integer, List<ScoreRule>> preloadAllRules() {
        Map<Integer, List<ScoreRule>> rulesByType = new HashMap<>();
        for (int ruleType = 1; ruleType <= 6; ruleType++) {
            List<ScoreRule> rules = scoreRuleService.listAvailableByRuleType(ruleType);
            rulesByType.put(ruleType, rules);
        }
        return rulesByType;
    }

    /**
     * 从内存数据计算科研成果分数
     */
    private BigDecimal calculateResearchScoreFromMemory(
            Long studentId,
            Map<Integer, List<ScoreRule>> rulesByType,
            Map<Long, List<ResearchPaper>> papersByStudent,
            Map<Long, List<ResearchPatent>> patentsByStudent,
            Map<Long, List<ResearchProject>> projectsByStudent) {

        BigDecimal totalScore = BigDecimal.ZERO;

        List<ScoreRule> paperRules = rulesByType.getOrDefault(1, List.of());
        List<ScoreRule> patentRules = rulesByType.getOrDefault(2, List.of());
        List<ScoreRule> projectRules = rulesByType.getOrDefault(3, List.of());

        // 计算论文分数
        List<ResearchPaper> papers = papersByStudent.getOrDefault(studentId, List.of());
        for (ResearchPaper paper : papers) {
            BigDecimal paperScore = calculatePaperScore(paper, paperRules);
            totalScore = totalScore.add(paperScore);
        }

        // 计算专利分数
        List<ResearchPatent> patents = patentsByStudent.getOrDefault(studentId, List.of());
        for (ResearchPatent patent : patents) {
            BigDecimal patentScore = calculatePatentScore(patent, patentRules);
            totalScore = totalScore.add(patentScore);
        }

        // 计算项目分数
        List<ResearchProject> projects = projectsByStudent.getOrDefault(studentId, List.of());
        for (ResearchProject project : projects) {
            BigDecimal projectScore = calculateProjectScore(project, projectRules);
            totalScore = totalScore.add(projectScore);
        }

        // 检查科研分数上限（使用 min，更严格）
        BigDecimal maxResearchScore = getMaxScoreFromRules(paperRules);
        if (maxResearchScore != null && totalScore.compareTo(maxResearchScore) > 0) {
            totalScore = maxResearchScore;
        }

        return totalScore;
    }

    /**
     * 从内存数据计算竞赛获奖分数
     */
    private BigDecimal calculateCompetitionScoreFromMemory(
            Long studentId,
            Map<Integer, List<ScoreRule>> rulesByType,
            Map<Long, List<CompetitionAward>> awardsByStudent) {

        List<ScoreRule> rules = rulesByType.getOrDefault(4, List.of());
        List<CompetitionAward> awards = awardsByStudent.getOrDefault(studentId, List.of());

        BigDecimal totalScore = BigDecimal.ZERO;
        for (CompetitionAward award : awards) {
            BigDecimal awardScore = calculateCompetitionScoreInternal(award, rules);
            totalScore = totalScore.add(awardScore);
        }

        // 检查竞赛分数上限（使用 min，更严格）
        BigDecimal maxCompetitionScore = getMaxScoreFromRules(rules);
        if (maxCompetitionScore != null && totalScore.compareTo(maxCompetitionScore) > 0) {
            totalScore = maxCompetitionScore;
        }

        return totalScore;
    }

    @Override
    public BigDecimal calculateCourseScore(Long studentId, Long batchId) {
        log.debug("计算课程成绩分数，studentId={}, batchId={}", studentId, batchId);

        // 获取课程成绩相关的评分规则
        List<ScoreRule> rules = scoreRuleService.listAvailableByRuleType(5); // 5-课程成绩

        if (rules.isEmpty()) {
            log.warn("未找到课程成绩评分规则");
            // 没有规则时，直接使用加权平均分
            return courseScoreService.calculateWeightedAverage(studentId, batchId);
        }

        // 使用 CourseScoreService 计算加权平均分
        BigDecimal weightedAverage = courseScoreService.calculateWeightedAverage(studentId, batchId);

        // 检查是否有最高分限制
        BigDecimal maxScore = rules.stream()
            .filter(rule -> rule.getMaxScore() != null)
            .map(ScoreRule::getMaxScore)
            .min(BigDecimal::compareTo)
            .orElse(null);

        if (maxScore != null && weightedAverage.compareTo(maxScore) > 0) {
            weightedAverage = maxScore;
        }

        log.debug("课程成绩分数计算完成，studentId={}, score={}", studentId, weightedAverage);
        return weightedAverage;
    }

    @Override
    public BigDecimal calculateResearchScore(Long studentId, Long batchId) {
        log.debug("计算科研成果分数，studentId={}, batchId={}", studentId, batchId);

        BigDecimal totalScore = BigDecimal.ZERO;

        // 获取科研相关的评分规则
        List<ScoreRule> paperRules = scoreRuleService.listAvailableByRuleType(1); // 1-论文
        List<ScoreRule> patentRules = scoreRuleService.listAvailableByRuleType(2); // 2-专利
        List<ScoreRule> projectRules = scoreRuleService.listAvailableByRuleType(3); // 3-项目

        // 计算论文分数
        List<ResearchPaper> papers = researchPaperService.list(
            new LambdaQueryWrapper<ResearchPaper>()
                .eq(ResearchPaper::getStudentId, studentId)
                .eq(ResearchPaper::getStatus, 2) // 审核通过
        );

        for (ResearchPaper paper : papers) {
            BigDecimal paperScore = calculatePaperScore(paper, paperRules);
            totalScore = totalScore.add(paperScore);
        }

        // 计算专利分数
        List<ResearchPatent> patents = researchPatentService.list(
            new LambdaQueryWrapper<ResearchPatent>()
                .eq(ResearchPatent::getStudentId, studentId)
                .eq(ResearchPatent::getAuditStatus, 1) // 审核通过
        );

        for (ResearchPatent patent : patents) {
            BigDecimal patentScore = calculatePatentScore(patent, patentRules);
            totalScore = totalScore.add(patentScore);
        }

        // 计算项目分数
        List<ResearchProject> projects = researchProjectService.list(
            new LambdaQueryWrapper<ResearchProject>()
                .eq(ResearchProject::getStudentId, studentId)
                .eq(ResearchProject::getAuditStatus, 1) // 审核通过
        );

        for (ResearchProject project : projects) {
            BigDecimal projectScore = calculateProjectScore(project, projectRules);
            totalScore = totalScore.add(projectScore);
        }

        // 检查科研分数上限（使用 min，更严格）
        BigDecimal maxResearchScore = getMaxScoreFromRules(paperRules);
        if (maxResearchScore != null && totalScore.compareTo(maxResearchScore) > 0) {
            totalScore = maxResearchScore;
        }

        log.debug("科研成果分数计算完成，studentId={}, score={}", studentId, totalScore);
        return totalScore;
    }

    @Override
    public BigDecimal calculateCompetitionScore(Long studentId, Long batchId) {
        log.debug("计算竞赛获奖分数，studentId={}, batchId={}", studentId, batchId);

        // 获取竞赛相关的评分规则
        List<ScoreRule> rules = scoreRuleService.listAvailableByRuleType(4); // 4-竞赛

        List<CompetitionAward> awards = competitionAwardService.list(
            new LambdaQueryWrapper<CompetitionAward>()
                .eq(CompetitionAward::getStudentId, studentId)
                .eq(CompetitionAward::getAuditStatus, 1) // 审核通过
        );

        BigDecimal totalScore = BigDecimal.ZERO;
        for (CompetitionAward award : awards) {
            BigDecimal awardScore = calculateCompetitionScoreInternal(award, rules);
            totalScore = totalScore.add(awardScore);
        }

        // 检查竞赛分数上限（使用 min，更严格）
        BigDecimal maxCompetitionScore = getMaxScoreFromRules(rules);
        if (maxCompetitionScore != null && totalScore.compareTo(maxCompetitionScore) > 0) {
            totalScore = maxCompetitionScore;
        }

        log.debug("竞赛获奖分数计算完成，studentId={}, score={}", studentId, totalScore);
        return totalScore;
    }

    @Override
    public BigDecimal calculateQualityScore(Long studentId, Long batchId) {
        log.debug("计算综合素质分数，studentId={}, batchId={}", studentId, batchId);

        // 获取德育表现相关的评分规则
        List<ScoreRule> rules = scoreRuleService.listAvailableByRuleType(6); // 6-德育表现

        // 使用 MoralPerformanceService 计算德育总分
        BigDecimal totalScore = moralPerformanceService.calculateTotalScore(studentId, batchId);

        // 检查是否有最高分限制
        BigDecimal maxQualityScore = rules.stream()
            .filter(rule -> rule.getMaxScore() != null)
            .map(ScoreRule::getMaxScore)
            .min(BigDecimal::compareTo)
            .orElse(null);

        if (maxQualityScore != null && totalScore.compareTo(maxQualityScore) > 0) {
            totalScore = maxQualityScore;
        }

        log.debug("综合素质分数计算完成，studentId={}, score={}", studentId, totalScore);
        return totalScore;
    }

    @Override
    public BigDecimal calculateTotalScore(BigDecimal courseScore, BigDecimal researchScore,
                                          BigDecimal competitionScore, BigDecimal qualityScore) {
        // 默认权重：课程 40%，科研 30%，竞赛 20%，综测 10%
        // 可以根据评分规则配置动态调整权重
        BigDecimal courseWeight = new BigDecimal("0.4");
        BigDecimal researchWeight = new BigDecimal("0.3");
        BigDecimal competitionWeight = new BigDecimal("0.2");
        BigDecimal qualityWeight = new BigDecimal("0.1");

        BigDecimal total = courseScore.multiply(courseWeight)
                .add(researchScore.multiply(researchWeight))
                .add(competitionScore.multiply(competitionWeight))
                .add(qualityScore.multiply(qualityWeight));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 计算论文分数
     */
    private BigDecimal calculatePaperScore(ResearchPaper paper, List<ScoreRule> rules) {
        BigDecimal score = BigDecimal.ZERO;

        // 根据期刊级别查找对应的规则
        Integer journalLevel = paper.getJournalLevel();
        if (journalLevel == null) {
            return score;
        }

        // 查找匹配的规则
        ScoreRule matchedRule = rules.stream()
            .filter(rule -> rule.getLevel() != null &&
                    String.valueOf(journalLevel).equals(rule.getLevel()))
            .findFirst()
            .orElse(null);

        if (matchedRule != null) {
            score = matchedRule.getScore();

            // 根据作者排名调整分数（第一作者得 full 分，其他作者按比例）
            if (paper.getAuthorRank() != null) {
                BigDecimal ratio = getAuthorRankRatio(paper.getAuthorRank());
                score = score.multiply(ratio);
            }

            // 检查上限
            if (matchedRule.getMaxScore() != null) {
                score = score.min(matchedRule.getMaxScore());
            }
        }

        return score;
    }

    /**
     * 计算专利分数
     */
    private BigDecimal calculatePatentScore(ResearchPatent patent, List<ScoreRule> rules) {
        BigDecimal score = BigDecimal.ZERO;

        // 根据专利类型查找对应的规则
        Integer patentType = patent.getPatentType();
        if (patentType == null) {
            return score;
        }

        ScoreRule matchedRule = rules.stream()
            .filter(rule -> rule.getLevel() != null &&
                    String.valueOf(patentType).equals(rule.getLevel()))
            .findFirst()
            .orElse(null);

        if (matchedRule != null) {
            score = matchedRule.getScore();

            // 根据发明人排名调整分数
            if (patent.getApplicantRank() != null) {
                BigDecimal ratio = getApplicantRankRatio(patent.getApplicantRank());
                score = score.multiply(ratio);
            }

            if (matchedRule.getMaxScore() != null) {
                score = score.min(matchedRule.getMaxScore());
            }
        }

        return score;
    }

    /**
     * 计算项目分数
     */
    private BigDecimal calculateProjectScore(ResearchProject project, List<ScoreRule> rules) {
        BigDecimal score = BigDecimal.ZERO;

        Integer projectType = project.getProjectType();
        if (projectType == null) {
            return score;
        }

        ScoreRule matchedRule = rules.stream()
            .filter(rule -> rule.getLevel() != null &&
                    String.valueOf(projectType).equals(rule.getLevel()))
            .findFirst()
            .orElse(null);

        if (matchedRule != null) {
            score = matchedRule.getScore();

            // 根据成员排名调整分数
            if (project.getMemberRank() != null) {
                BigDecimal ratio = getMemberRankRatio(project.getMemberRank());
                score = score.multiply(ratio);
            }

            if (matchedRule.getMaxScore() != null) {
                score = score.min(matchedRule.getMaxScore());
            }
        }

        return score;
    }

    /**
     * 计算竞赛获奖分数
     */
    private BigDecimal calculateCompetitionScoreInternal(CompetitionAward award, List<ScoreRule> rules) {
        BigDecimal score = BigDecimal.ZERO;

        Integer awardLevel = award.getAwardLevel();
        Integer competitionLevel = award.getCompetitionLevel();
        if (awardLevel == null || competitionLevel == null) {
            return score;
        }

        // 根据竞赛级别和获奖等级查找规则
        ScoreRule matchedRule = rules.stream()
            .filter(rule -> rule.getLevel() != null &&
                    (competitionLevel + "_" + awardLevel).equals(rule.getLevel()))
            .findFirst()
            .orElse(null);

        if (matchedRule != null) {
            score = matchedRule.getScore();

            // 团队项目按排名分配分数
            if (award.getAwardType() == 2 && award.getMemberRank() != null) {
                BigDecimal ratio = getTeamMemberRatio(award.getMemberRank());
                score = score.multiply(ratio);
            }

            if (matchedRule.getMaxScore() != null) {
                score = score.min(matchedRule.getMaxScore());
            }
        }

        return score;
    }

    /**
     * 获取作者排名比例
     */
    private BigDecimal getAuthorRankRatio(Integer authorRank) {
        if (authorRank == null) return BigDecimal.ONE;
        switch (authorRank) {
            case 1: return new BigDecimal("1.0");  // 第一作者
            case 2: return new BigDecimal("0.7");  // 第二作者
            case 3: return new BigDecimal("0.5");  // 第三作者
            default: return new BigDecimal("0.3");
        }
    }

    /**
     * 获取申请人排名比例
     */
    private BigDecimal getApplicantRankRatio(Integer applicantRank) {
        if (applicantRank == null) return BigDecimal.ONE;
        switch (applicantRank) {
            case 1: return new BigDecimal("1.0");
            case 2: return new BigDecimal("0.6");
            case 3: return new BigDecimal("0.4");
            default: return new BigDecimal("0.2");
        }
    }

    /**
     * 获取成员排名比例
     */
    private BigDecimal getMemberRankRatio(Integer memberRank) {
        if (memberRank == null) return BigDecimal.ONE;
        switch (memberRank) {
            case 1: return new BigDecimal("1.0");  // 项目负责人
            case 2: return new BigDecimal("0.7");
            case 3: return new BigDecimal("0.5");
            default: return new BigDecimal("0.3");
        }
    }

    /**
     * 获取团队成员排名比例
     */
    private BigDecimal getTeamMemberRatio(Integer memberRank) {
        if (memberRank == null) return BigDecimal.ONE;
        switch (memberRank) {
            case 1: return new BigDecimal("1.0");  // 队长
            case 2: return new BigDecimal("0.8");
            case 3: return new BigDecimal("0.6");
            default: return new BigDecimal("0.4");
        }
    }

    /**
     * 从规则列表获取最小上限分数
     */
    private BigDecimal getMaxScoreFromRules(List<ScoreRule> rules) {
        return rules.stream()
            .filter(rule -> rule.getMaxScore() != null)
            .map(ScoreRule::getMaxScore)
            .min(BigDecimal::compareTo)
            .orElse(null);
    }
}
