package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.CourseScoreImportResult;
import com.scholarship.dto.query.CourseScoreQuery;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.CourseScoreMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.service.CourseScoreService;
import com.scholarship.service.EvaluationBatchService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程成绩服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseScoreServiceImpl extends ServiceImpl<CourseScoreMapper, CourseScore>
        implements CourseScoreService {

    private static final List<String> TERM_HEADERS = List.of("学期", "开课学期", "学年学期", "课程学期");
    private static final List<String> COURSE_NAME_HEADERS = List.of("课程名称");
    private static final List<String> COURSE_CODE_HEADERS = List.of("课程代码", "课程编号");
    private static final List<String> CREDIT_HEADERS = List.of("学分");
    private static final List<String> EFFECTIVE_SCORE_HEADERS = List.of("有效成绩", "成绩", "总评成绩", "课程成绩", "最终成绩");
    private static final List<String> COURSE_TYPE_HEADERS = List.of("课程性质", "课程类别");
    private static final List<String> EXAM_TYPE_HEADERS = List.of("考试类型", "考核类型");
    private static final List<String> EXAM_STATUS_HEADERS = List.of("考试情况", "考核情况");
    private static final List<String> INVALID_SCORE_HEADERS = List.of("无效成绩");
    private static final List<String> GPA_HEADERS = List.of("绩点");
    private static final List<String> EARNED_CREDIT_HEADERS = List.of("已获取学分", "获得学分");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private static final Pattern TERM_PATTERN = Pattern.compile("(20\\d{2})\\s*([秋春夏冬上下])");

    private final EvaluationBatchService evaluationBatchService;
    private final StudentInfoMapper studentInfoMapper;

    @Lazy
    @Autowired
    private CourseScoreServiceImpl self;

    @Override
    public boolean save(CourseScore entity) {
        fillStudentSnapshot(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(CourseScore entity) {
        fillStudentSnapshot(entity);
        return super.updateById(entity);
    }

    private boolean updateImportedScore(CourseScore entity, boolean hasGpaColumn) {
        return updateImportedScore(entity, hasGpaColumn, null);
    }

    private boolean updateImportedScore(CourseScore entity, boolean hasGpaColumn, Map<Long, StudentInfo> studentCache) {
        fillStudentSnapshot(entity, studentCache);
        var update = lambdaUpdate()
                .eq(CourseScore::getId, entity.getId())
                .set(CourseScore::getStudentNo, entity.getStudentNo())
                .set(CourseScore::getStudentName, entity.getStudentName())
                .set(CourseScore::getCourseName, entity.getCourseName())
                .set(CourseScore::getCourseCode, entity.getCourseCode())
                .set(CourseScore::getAcademicYear, entity.getAcademicYear())
                .set(CourseScore::getSemester, entity.getSemester())
                .set(CourseScore::getCredit, entity.getCredit())
                .set(CourseScore::getCourseType, entity.getCourseType())
                .set(CourseScore::getScore, entity.getScore())
                .set(CourseScore::getScoreText, entity.getScoreText())
                .set(CourseScore::getRemark, entity.getRemark());
        if (hasGpaColumn) {
            update.set(CourseScore::getGpa, entity.getGpa());
        }
        return update.update();
    }

    @Override
    public List<CourseScore> listByStudentId(Long studentId, Long batchId) {
        log.debug("查询学生成绩，studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseScore::getStudentId, studentId);
        wrapper.eq(CourseScore::getDeleted, 0);

        // 如果指定了批次 ID，根据批次的学年筛选成绩
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null && batch.getAcademicYear() != null) {
                wrapper.eq(CourseScore::getAcademicYear, batch.getAcademicYear());
            }
        }

        applyDefaultSort(wrapper);
        return list(wrapper);
    }

    @Override
    public BigDecimal calculateWeightedAverage(Long studentId, Long batchId) {
        log.debug("计算加权平均分，studentId={}, batchId={}", studentId, batchId);

        List<CourseScore> scores = listByStudentId(studentId, batchId);

        if (scores.isEmpty()) {
            log.warn("未找到学生成绩，studentId={}", studentId);
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (CourseScore score : scores) {
            BigDecimal credit = score.getCredit();
            BigDecimal scoreValue = score.getScore();

            if (credit != null && scoreValue != null) {
                totalWeightedScore = totalWeightedScore.add(scoreValue.multiply(credit));
                totalCredits = totalCredits.add(credit);
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightedAverage = totalWeightedScore.divide(totalCredits, 2, RoundingMode.HALF_UP);
        log.debug("加权平均分计算完成，studentId={}, weightedAverage={}", studentId, weightedAverage);

        return weightedAverage;
    }

    @Override
    public BigDecimal calculateTotalCredits(Long studentId) {
        log.debug("计算总学分，studentId={}", studentId);

        List<CourseScore> scores = list(
            new LambdaQueryWrapper<CourseScore>()
                .eq(CourseScore::getStudentId, studentId)
                .eq(CourseScore::getDeleted, 0)
        );

        BigDecimal totalCredits = BigDecimal.ZERO;
        for (CourseScore score : scores) {
            if (score.getCredit() != null && score.getScore() != null && score.getScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                totalCredits = totalCredits.add(score.getCredit());
            }
        }

        log.debug("总学分计算完成，studentId={}, totalCredits={}", studentId, totalCredits);
        return totalCredits;
    }

    @Override
    public BigDecimal calculateAverageGPA(Long studentId) {
        log.debug("计算平均绩点，studentId={}", studentId);

        List<CourseScore> scores = list(
            new LambdaQueryWrapper<CourseScore>()
                .eq(CourseScore::getStudentId, studentId)
                .eq(CourseScore::getDeleted, 0)
        );

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGPA = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (CourseScore score : scores) {
            BigDecimal credit = score.getCredit();
            BigDecimal gpa = score.getGpa();

            if (credit != null && gpa != null) {
                totalGPA = totalGPA.add(gpa.multiply(credit));
                totalCredits = totalCredits.add(credit);
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageGPA = totalGPA.divide(totalCredits, 2, RoundingMode.HALF_UP);
        log.debug("平均绩点计算完成，studentId={}, averageGPA={}", studentId, averageGPA);

        return averageGPA;
    }

    @Override
    public Map<Long, BigDecimal> mapWeightedAverageByStudentIds(List<Long> studentIds, Long batchId) {
        String academicYear = null;
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null) {
                academicYear = batch.getAcademicYear();
            }
        }
        return mapWeightedAverageByStudentIds(studentIds, academicYear);
    }

    @Override
    public Map<Long, BigDecimal> mapWeightedAverageByStudentIds(List<Long> studentIds, String academicYear) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        log.debug("批量计算加权平均分，studentCount={}, academicYear={}", studentIds.size(), academicYear);

        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<CourseScore>()
            .in(CourseScore::getStudentId, studentIds);
        if (academicYear != null) {
            wrapper.eq(CourseScore::getAcademicYear, academicYear);
        }
        List<CourseScore> allScores = list(wrapper);

        Map<Long, List<CourseScore>> scoresByStudent = allScores.stream()
            .collect(java.util.stream.Collectors.groupingBy(CourseScore::getStudentId));

        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long studentId : studentIds) {
            List<CourseScore> scores = scoresByStudent.getOrDefault(studentId, List.of());

            if (scores.isEmpty()) {
                result.put(studentId, BigDecimal.ZERO);
                continue;
            }

            BigDecimal totalWeightedScore = BigDecimal.ZERO;
            BigDecimal totalCredits = BigDecimal.ZERO;

            for (CourseScore score : scores) {
                BigDecimal credit = score.getCredit();
                BigDecimal scoreValue = score.getScore();

                if (credit != null && scoreValue != null) {
                    totalWeightedScore = totalWeightedScore.add(scoreValue.multiply(credit));
                    totalCredits = totalCredits.add(credit);
                }
            }

            if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
                result.put(studentId, BigDecimal.ZERO);
            } else {
                result.put(studentId, totalWeightedScore.divide(totalCredits, 2, RoundingMode.HALF_UP));
            }
        }

        log.debug("批量计算加权平均分完成，resultCount={}", result.size());
        return result;
    }

    @Override
    public IPage<CourseScore> queryPage(CourseScoreQuery query) {
        log.debug("分页查询课程成绩，query={}", query);

        Page<CourseScore> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<CourseScore> wrapper = buildQueryWrapper(query);
        applyDefaultSort(wrapper);

        return page(page, wrapper);
    }

    @Override
    public List<String> listAcademicYearsByStudentId(Long studentId) {
        if (studentId == null) {
            return List.of();
        }

        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<CourseScore>()
                .select(CourseScore::getAcademicYear)
                .eq(CourseScore::getStudentId, studentId)
                .isNotNull(CourseScore::getAcademicYear)
                .groupBy(CourseScore::getAcademicYear)
                .orderByDesc(CourseScore::getAcademicYear)
                .last("LIMIT 100");

        return list(wrapper).stream()
                .map(CourseScore::getAcademicYear)
                .filter(year -> year != null && !year.isBlank())
                .toList();
    }

    @Override
    public List<CourseScore> queryForExport(CourseScoreQuery query, int maxRows) {
        log.debug("查询成绩列表用于导出，query={}, maxRows={}", query, maxRows);

        LambdaQueryWrapper<CourseScore> wrapper = buildQueryWrapper(query);
        applyDefaultSort(wrapper);
        if (maxRows > 0) {
            wrapper.last("LIMIT " + maxRows);
        }

        return list(wrapper);
    }

    @Override
    public CourseScoreImportResult importScores(InputStream inputStream, String originalFilename, StudentInfo studentInfo) throws IOException {
        log.info("导入课程成绩开始, studentId={}, studentNo={}, fileName={}",
                studentInfo.getId(), studentInfo.getStudentNo(), originalFilename);

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            int headerRowIndex = findHeaderRowIndex(sheet, formatter);
            if (headerRowIndex < 0) {
                throw new IllegalArgumentException("未识别到成绩表头，请确认文件为主修成绩导出模板");
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerIndexes = buildHeaderIndexes(headerRow, formatter);
            Integer courseNameIndex = findHeaderIndex(headerIndexes, COURSE_NAME_HEADERS);
            Integer courseCodeIndex = findHeaderIndex(headerIndexes, COURSE_CODE_HEADERS);
            Integer creditIndex = findHeaderIndex(headerIndexes, CREDIT_HEADERS);
            Integer effectiveScoreIndex = findHeaderIndex(headerIndexes, EFFECTIVE_SCORE_HEADERS);
            if (courseNameIndex == null || creditIndex == null || effectiveScoreIndex == null) {
                throw new IllegalArgumentException("成绩表缺少必要列：课程名称、学分或有效成绩");
            }

            Integer termIndex = findHeaderIndex(headerIndexes, TERM_HEADERS);
            Integer courseTypeIndex = findHeaderIndex(headerIndexes, COURSE_TYPE_HEADERS);
            Integer examTypeIndex = findHeaderIndex(headerIndexes, EXAM_TYPE_HEADERS);
            Integer examStatusIndex = findHeaderIndex(headerIndexes, EXAM_STATUS_HEADERS);
            Integer invalidScoreIndex = findHeaderIndex(headerIndexes, INVALID_SCORE_HEADERS);
            Integer gpaIndex = findHeaderIndex(headerIndexes, GPA_HEADERS);
            boolean hasGpaColumn = gpaIndex != null;
            Integer earnedCreditIndex = findHeaderIndex(headerIndexes, EARNED_CREDIT_HEADERS);
            if (termIndex == null) {
                throw new IllegalArgumentException("成绩表缺少必要列：学期");
            }

            String currentTerm = null;
            int importedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;
            Map<Long, StudentInfo> studentSnapshotCache = new HashMap<>();
            studentSnapshotCache.put(studentInfo.getId(), studentInfo);
            Map<String, TermScoreCache> termScoreCache = new HashMap<>();
            List<CourseScore> newScores = new ArrayList<>();
            List<CourseScore> updateScores = new ArrayList<>();
            List<Long> duplicateIdsToRemove = new ArrayList<>();
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }

                String courseName = getCellText(row, courseNameIndex, formatter);
                if (isSummaryRow(courseName)) {
                    continue;
                }
                if (courseName == null || courseName.isBlank()) {
                    skippedCount++;
                    continue;
                }

                String termText = getCellText(row, termIndex, formatter);
                if (termText != null && !termText.isBlank()) {
                    currentTerm = termText.trim();
                }
                AcademicTerm academicTerm = parseAcademicTerm(currentTerm);
                if (academicTerm == null) {
                    skippedCount++;
                    continue;
                }

                String courseCode = getCellText(row, courseCodeIndex, formatter);
                String effectiveScoreText = getCellText(row, effectiveScoreIndex, formatter);
                BigDecimal credit = parseDecimal(getCellText(row, creditIndex, formatter));
                if ((credit == null || credit.compareTo(BigDecimal.ZERO) == 0)
                        && (effectiveScoreText == null || effectiveScoreText.isBlank())) {
                    skippedCount++;
                    continue;
                }

                String normalizedCourseName = courseName.trim();
                TermScoreCache cachedTermScores = termScoreCache.computeIfAbsent(
                        buildTermCacheKey(studentInfo.getId(), academicTerm),
                        key -> loadTermScoreCache(studentInfo.getId(), academicTerm)
                );
                CourseScore score = findExistingScore(normalizedCourseName, courseCode, cachedTermScores, duplicateIdsToRemove);
                boolean exists = score != null && score.getId() != null;
                if (score == null) {
                    score = new CourseScore();
                    score.setStudentId(studentInfo.getId());
                }

                String examStatus = getCellText(row, examStatusIndex, formatter);
                String examType = getCellText(row, examTypeIndex, formatter);
                String invalidScore = getCellText(row, invalidScoreIndex, formatter);
                BigDecimal earnedCredit = parseDecimal(getCellText(row, earnedCreditIndex, formatter));
                BigDecimal numericScore = parseEffectiveScore(effectiveScoreText);
                String scoreText = buildScoreText(effectiveScoreText, numericScore);

                score.setCourseName(normalizedCourseName);
                score.setCourseCode(courseCode);
                score.setAcademicYear(academicTerm.academicYear());
                score.setSemester(academicTerm.semester());
                score.setCredit(credit);
                score.setCourseType(parseCourseType(getCellText(row, courseTypeIndex, formatter)));
                score.setScore(numericScore);
                score.setScoreText(scoreText);
                score.setGpa(hasGpaColumn ? parseDecimal(getCellText(row, gpaIndex, formatter)) : null);
                score.setRemark(buildRemark(currentTerm, examType, examStatus, invalidScore, earnedCredit, originalFilename));
                fillStudentSnapshot(score, studentSnapshotCache);

                if (exists) {
                    updateScores.add(score);
                    updatedCount++;
                } else {
                    newScores.add(score);
                    importedCount++;
                }
                cachedTermScores.put(score);
            }

            // 通过代理调用使 @Transactional 生效，确保三步写入在同一事务中
            self.persistImportResults(newScores, updateScores, duplicateIdsToRemove);

            CourseScoreImportResult result = new CourseScoreImportResult();
            result.setImported(importedCount);
            result.setUpdated(updatedCount);
            result.setSkipped(skippedCount);
            result.setMessage(String.format("导入完成，新增 %d 条，更新 %d 条，跳过 %d 条", importedCount, updatedCount, skippedCount));

            log.info("导入课程成绩完成, studentId={}, importedCount={}, updatedCount={}, skippedCount={}",
                    studentInfo.getId(), importedCount, updatedCount, skippedCount);
            return result;
        }
    }

    /**
     * 仅包裹 DB 写入操作的事务方法。
     *
     * <p>通过 self 代理调用以使 {@code @Transactional} 生效，
     * Excel 解析、缓存加载等纯内存操作不占用数据库连接和事务。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    void persistImportResults(List<CourseScore> newScores, List<CourseScore> updateScores,
                                     List<Long> duplicateIdsToRemove) {
        if (!newScores.isEmpty()) {
            super.saveBatch(newScores, 200);
        }
        if (!updateScores.isEmpty()) {
            super.updateBatchById(updateScores, 200);
        }
        if (!duplicateIdsToRemove.isEmpty()) {
            super.removeBatchByIds(duplicateIdsToRemove, 200);
        }
    }

    /**
     * 构建查询条件
     *
     * @param query 查询参数
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<CourseScore> buildQueryWrapper(CourseScoreQuery query) {
        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentIds() != null && !query.getStudentIds().isEmpty()) {
            wrapper.in(CourseScore::getStudentId, query.getStudentIds());
        } else if (query.getStudentId() != null) {
            wrapper.eq(CourseScore::getStudentId, query.getStudentId());
        }
        if (query.getAcademicYear() != null) {
            wrapper.eq(CourseScore::getAcademicYear, query.getAcademicYear());
        }
        if (query.getSemester() != null) {
            wrapper.eq(CourseScore::getSemester, query.getSemester());
        }
        if (query.getCourseName() != null) {
            wrapper.like(CourseScore::getCourseName, query.getCourseName());
        }

        return wrapper;
    }

    private void fillStudentSnapshot(CourseScore score) {
        fillStudentSnapshot(score, null);
    }

    private void fillStudentSnapshot(CourseScore score, Map<Long, StudentInfo> studentCache) {
        if (score == null || score.getStudentId() == null) {
            return;
        }
        StudentInfo student = getStudentSnapshot(score.getStudentId(), studentCache);
        if (student == null) {
            return;
        }
        score.setStudentNo(student.getStudentNo());
        score.setStudentName(student.getName());
    }

    private StudentInfo getStudentSnapshot(Long studentId, Map<Long, StudentInfo> studentCache) {
        if (studentId == null) {
            return null;
        }
        if (studentCache != null) {
            StudentInfo cached = studentCache.get(studentId);
            if (cached != null) return cached;
        }
        StudentInfo student = studentInfoMapper.selectById(studentId);
        if (studentCache != null) {
            studentCache.put(studentId, student);
        }
        return student;
    }

    private int findHeaderRowIndex(Sheet sheet, DataFormatter formatter) {
        int lastRow = Math.min(sheet.getLastRowNum(), 20);
        for (int rowIndex = 0; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headerIndexes = buildHeaderIndexes(row, formatter);
            if (findHeaderIndex(headerIndexes, COURSE_NAME_HEADERS) != null
                    && findHeaderIndex(headerIndexes, CREDIT_HEADERS) != null) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Map<String, Integer> buildHeaderIndexes(Row row, DataFormatter formatter) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        short lastCellNum = row.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            String header = normalizeHeader(getCellText(row, cellIndex, formatter));
            if (!header.isBlank()) {
                headers.putIfAbsent(header, cellIndex);
            }
        }
        return headers;
    }

    private Integer findHeaderIndex(Map<String, Integer> headers, List<String> aliases) {
        for (String alias : aliases) {
            Integer index = headers.get(normalizeHeader(alias));
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    private String normalizeHeader(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("：", "")
                .replace(":", "")
                .replace("（", "(")
                .replace("）", ")")
                .replace(" ", "")
                .trim();
    }

    private String getCellText(Row row, Integer cellIndex, DataFormatter formatter) {
        if (row == null || cellIndex == null || cellIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        short lastCellNum = row.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            String value = getCellText(row, cellIndex, formatter);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSummaryRow(String courseName) {
        if (courseName == null || courseName.isBlank()) {
            return false;
        }
        return courseName.contains("加权平均分")
                || courseName.contains("专业排名")
                || courseName.contains("班级排名");
    }

    private AcademicTerm parseAcademicTerm(String termText) {
        if (termText == null || termText.isBlank()) {
            return null;
        }
        String normalized = termText.replace(" ", "").trim();
        Matcher matcher = TERM_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        int year = Integer.parseInt(matcher.group(1));
        String label = matcher.group(2);
        if ("秋".equals(label) || "上".equals(label)) {
            return new AcademicTerm(String.valueOf(year), 1);
        }
        if ("春".equals(label) || "下".equals(label)) {
            return new AcademicTerm(String.valueOf(year - 1), 2);
        }
        if ("夏".equals(label)) {
            return new AcademicTerm(String.valueOf(year - 1), 3);
        }
        if ("冬".equals(label)) {
            return new AcademicTerm(String.valueOf(year), 1);
        }
        return null;
    }

    private Integer parseCourseType(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (text.contains("必修")) {
            return 1;
        }
        if (text.contains("选修")) {
            return 2;
        }
        if (text.contains("任选")) {
            return 3;
        }
        return null;
    }

    private BigDecimal parseEffectiveScore(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String normalized = rawText.trim();
        if (normalized.contains("合格") || normalized.contains("通过")) {
            return null;
        }
        return parseDecimal(normalized);
    }

    private BigDecimal parseDecimal(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        Matcher matcher = DECIMAL_PATTERN.matcher(rawText.replace(",", ""));
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group());
    }

    private String buildScoreText(String rawText, BigDecimal numericScore) {
        if (rawText != null && !rawText.isBlank()) {
            return rawText.trim();
        }
        if (numericScore != null) {
            return numericScore.stripTrailingZeros().toPlainString();
        }
        return null;
    }

    private void applyDefaultSort(LambdaQueryWrapper<CourseScore> wrapper) {
        wrapper.orderByDesc(CourseScore::getUpdateTime)
                .orderByDesc(CourseScore::getAcademicYear)
                .orderByDesc(CourseScore::getSemester)
                .orderByDesc(CourseScore::getId);
    }

    private String buildRemark(String termText, String examType, String examStatus, String invalidScore,
                               BigDecimal earnedCredit, String originalFilename) {
        List<String> parts = new ArrayList<>();
        if (termText != null && !termText.isBlank()) {
            parts.add("导入学期：" + termText.trim());
        }
        if (examType != null && !examType.isBlank()) {
            parts.add("考试类型：" + examType.trim());
        }
        if (examStatus != null && !examStatus.isBlank()) {
            parts.add("考试情况：" + examStatus.trim());
        }
        if (invalidScore != null && !invalidScore.isBlank()) {
            parts.add("无效成绩：" + invalidScore.trim());
        }
        if (earnedCredit != null) {
            parts.add("已获取学分：" + earnedCredit.stripTrailingZeros().toPlainString());
        }
        if (originalFilename != null && !originalFilename.isBlank()) {
            parts.add("来源文件：" + originalFilename);
        }
        return String.join("；", parts);
    }

    private String buildTermCacheKey(Long studentId, AcademicTerm term) {
        return studentId + "_" + term.academicYear() + "_" + term.semester();
    }

    private TermScoreCache loadTermScoreCache(Long studentId, AcademicTerm term) {
        LambdaQueryWrapper<CourseScore> wrapper = baseTermWrapper(studentId, term);
        applyDefaultSort(wrapper);
        return new TermScoreCache(list(wrapper));
    }

    private CourseScore findExistingScore(String courseName, String courseCode, TermScoreCache termScoreCache,
                                          List<Long> duplicateIdsToRemove) {
        CourseScore codeMatched = termScoreCache.findByCourseCode(courseCode);
        CourseScore nameMatched = termScoreCache.findByCourseName(courseName);

        if (codeMatched != null && nameMatched != null && !Objects.equals(codeMatched.getId(), nameMatched.getId())) {
            mergeDuplicateScore(codeMatched, nameMatched, termScoreCache, duplicateIdsToRemove);
            return codeMatched;
        }
        if (codeMatched != null) {
            return codeMatched;
        }
        return nameMatched;
    }

    private LambdaQueryWrapper<CourseScore> baseTermWrapper(Long studentId, AcademicTerm term) {
        return new LambdaQueryWrapper<CourseScore>()
                .eq(CourseScore::getStudentId, studentId)
                .eq(CourseScore::getAcademicYear, term.academicYear())
                .eq(CourseScore::getSemester, term.semester());
    }

    private void mergeDuplicateScore(CourseScore codeMatched, CourseScore nameMatched, TermScoreCache termScoreCache,
                                     List<Long> duplicateIdsToRemove) {
        String normalizedCode = codeMatched.getCourseCode() == null ? null : codeMatched.getCourseCode().trim();
        String duplicateCode = nameMatched.getCourseCode() == null ? null : nameMatched.getCourseCode().trim();
        boolean removableLegacy = duplicateCode == null || duplicateCode.isBlank() || Objects.equals(duplicateCode, normalizedCode);
        if (removableLegacy) {
            duplicateIdsToRemove.add(nameMatched.getId());
            termScoreCache.remove(nameMatched);
        }
    }

    private static class TermScoreCache {
        private final Map<String, CourseScore> byCourseCode = new HashMap<>();
        private final Map<String, CourseScore> byCourseName = new HashMap<>();

        private TermScoreCache(List<CourseScore> scores) {
            for (CourseScore score : scores) {
                put(score);
            }
        }

        private CourseScore findByCourseCode(String courseCode) {
            if (courseCode == null || courseCode.isBlank()) {
                return null;
            }
            return byCourseCode.get(courseCode.trim());
        }

        private CourseScore findByCourseName(String courseName) {
            if (courseName == null || courseName.isBlank()) {
                return null;
            }
            return byCourseName.get(courseName);
        }

        private void put(CourseScore score) {
            if (score == null) {
                return;
            }
            String courseCode = score.getCourseCode();
            if (courseCode != null && !courseCode.isBlank()) {
                byCourseCode.putIfAbsent(courseCode.trim(), score);
            }
            String courseName = score.getCourseName();
            if (courseName != null && !courseName.isBlank()) {
                byCourseName.putIfAbsent(courseName, score);
            }
        }

        private void remove(CourseScore score) {
            if (score == null) {
                return;
            }
            String courseCode = score.getCourseCode();
            if (courseCode != null && !courseCode.isBlank()) {
                byCourseCode.remove(courseCode.trim(), score);
            }
            String courseName = score.getCourseName();
            if (courseName != null && !courseName.isBlank()) {
                byCourseName.remove(courseName, score);
            }
        }
    }

    @AllArgsConstructor
    private static class AcademicTerm {
        private final String academicYear;
        private final Integer semester;

        public String academicYear() {
            return academicYear;
        }

        public Integer semester() {
            return semester;
        }
    }
}
