package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.ResearchPatentService;
import com.scholarship.service.ResearchProjectService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.TutorStudentVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 研究生信息管理服务实现类
 *
 * 功能说明：
 * - 分页查询研究生信息（支持关键字、院系、入学年份、学籍状态筛选）
 * - 导师查询指导学生（含论文/专利/项目成果统计）
 * - 根据 userId 查询学生信息
 * - 更新学生信息（可选同步到用户表）
 * - 学生更新自己的信息（仅部分字段）
 * - 删除学生信息（级联删除用户账号）
 * - 批量获取学生信息（以 userId 或 id 为 key 的 Map）
 *
 * 事务说明：
 * - 更新和删除操作使用 @Transactional 保证事务一致性
 */
@Slf4j
@Service
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo> implements StudentInfoService {

    private final StudentInfoMapper studentInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final ResearchPaperService researchPaperService;
    private final ResearchPatentService researchPatentService;
    private final ResearchProjectService researchProjectService;

    public StudentInfoServiceImpl(
            StudentInfoMapper studentInfoMapper,
            SysUserMapper sysUserMapper,
            ResearchPaperService researchPaperService,
            ResearchPatentService researchPatentService,
            ResearchProjectService researchProjectService
    ) {
        this.studentInfoMapper = studentInfoMapper;
        this.sysUserMapper = sysUserMapper;
        this.researchPaperService = researchPaperService;
        this.researchPatentService = researchPatentService;
        this.researchProjectService = researchProjectService;
    }

    /**
     * 分页查询研究生信息
     *
     * @param current        当前页码
     * @param size          每页条数
     * @param keyword       搜索关键字（匹配学号或姓名）
     * @param departments    院系列表
     * @param enrollmentYear 入学年份（按学号前4位筛选）
     * @param statuses      学籍状态列表
     * @return 分页后的学生信息列表
     */
    @Override
    public IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, List<String> departments, String enrollmentYear, List<Integer> statuses) {
        Page<StudentInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索：学号或姓名模糊匹配
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StudentInfo::getName, keyword));
        }

        // 院系列表筛选
        if (departments != null && !departments.isEmpty()) {
            if (departments.size() == 1) {
                wrapper.eq(StudentInfo::getDepartment, departments.get(0));
            } else {
                wrapper.in(StudentInfo::getDepartment, departments);
            }
        }

        // 入学年份筛选（按学号前4位匹配）
        String normalizedEnrollmentYear = normalizeStudentNoEnrollmentYear(enrollmentYear);
        if (normalizedEnrollmentYear != null) {
            wrapper.likeRight(StudentInfo::getStudentNo, normalizedEnrollmentYear);
        }

        // 学籍状态筛选
        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                wrapper.eq(StudentInfo::getStatus, statuses.get(0));
            } else {
                wrapper.in(StudentInfo::getStatus, statuses);
            }
        }

        // 按入学年份倒序
        wrapper.orderByDesc(StudentInfo::getEnrollmentYear);
        return studentInfoMapper.selectPage(page, wrapper);
    }

    /**
     * 导师分页查询指导学生
     * 仅返回当前导师名下学生，并附带论文/专利/项目成果统计
     *
     * @param tutorUserId 导师的用户ID
     * @param current     当前页码
     * @param size        每页条数
     * @param keyword     搜索关键字（匹配学号或姓名）
     * @param grade       年级（筛选同一年级学生）
     * @return 分页后的指导学生列表（含成果统计）
     */
    @Override
    public IPage<TutorStudentVO> pageTutorStudents(Long tutorUserId, Long current, Long size, String keyword, String grade) {
        Page<StudentInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTutorId, tutorUserId);

        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StudentInfo::getName, keyword));
        }

        // 年级筛选（归一化为入学年份）
        Integer enrollmentYear = normalizeEnrollmentYear(grade);
        if (enrollmentYear != null) {
            wrapper.eq(StudentInfo::getEnrollmentYear, enrollmentYear);
        }

        // 按入学年份倒序、学号正序
        wrapper.orderByDesc(StudentInfo::getEnrollmentYear)
                .orderByAsc(StudentInfo::getStudentNo);

        // 执行分页查询
        Page<StudentInfo> studentPage = studentInfoMapper.selectPage(page, wrapper);
        Page<TutorStudentVO> resultPage = new Page<>(studentPage.getCurrent(), studentPage.getSize(), studentPage.getTotal());

        List<StudentInfo> students = studentPage.getRecords();
        if (students.isEmpty()) {
            resultPage.setRecords(List.of());
            return resultPage;
        }

        // 批量获取成果统计数据
        List<Long> studentIds = students.stream().map(StudentInfo::getId).toList();
        Map<Long, Integer> paperCountMap = researchPaperService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        Map<Long, Integer> patentCountMap = researchPatentService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        Map<Long, Integer> projectCountMap = researchProjectService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        // 组装 VO
        List<TutorStudentVO> records = new ArrayList<>();
        for (StudentInfo student : students) {
            TutorStudentVO vo = new TutorStudentVO();
            vo.setId(student.getId());
            vo.setStudentNo(student.getStudentNo());
            vo.setName(student.getName());
            vo.setGender(student.getGender());
            vo.setEnrollmentYear(student.getEnrollmentYear());
            vo.setGrade(student.getEnrollmentYear() == null ? "" : String.valueOf(student.getEnrollmentYear()));
            vo.setMajor(student.getMajor());
            vo.setDirection(student.getDirection());
            vo.setPhone(student.getPhone());
            vo.setEmail(student.getEmail());
            vo.setPaperCount(paperCountMap.getOrDefault(student.getId(), 0));
            vo.setPatentCount(patentCountMap.getOrDefault(student.getId(), 0));
            vo.setProjectCount(projectCountMap.getOrDefault(student.getId(), 0));
            records.add(vo);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 归一化学号入学年份
     * 如果输入是4位数字，则视为入学年份，返回该年份
     * 否则返回 null（不进行筛选）
     */
    private String normalizeStudentNoEnrollmentYear(String enrollmentYear) {
        if (StringUtils.isBlank(enrollmentYear)) {
            return null;
        }

        String normalized = enrollmentYear.trim();
        return normalized.length() == 4 && StringUtils.isNumeric(normalized) ? normalized : null;
    }

    /**
     * 归一化年级为入学年份
     * 去掉"级"字和空格后，如果仍是纯数字，则视为年份
     */
    private Integer normalizeEnrollmentYear(String grade) {
        if (StringUtils.isBlank(grade)) {
            return null;
        }

        String normalized = grade.trim()
                .replace("级", "")
                .replace(" ", "");

        return StringUtils.isNumeric(normalized) ? Integer.parseInt(normalized) : null;
    }

    /**
     * 根据 userId 查询学生信息
     */
    @Override
    public StudentInfo getByUserId(Long userId) {
        return studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>()
                        .eq(StudentInfo::getUserId, userId)
        );
    }

    /**
     * 批量获取学生信息（以 userId 为 key 的 Map）
     */
    @Override
    public Map<Long, StudentInfo> mapByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<StudentInfo> students = list(new LambdaQueryWrapper<StudentInfo>()
                .in(StudentInfo::getUserId, userIds));

        return students.stream()
                .collect(Collectors.toMap(StudentInfo::getUserId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量获取学生信息（以 id 为 key 的 Map）
     */
    @Override
    public Map<Long, StudentInfo> mapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<StudentInfo> students = list(new LambdaQueryWrapper<StudentInfo>()
                .in(StudentInfo::getId, ids));

        return students.stream()
                .collect(Collectors.toMap(StudentInfo::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 更新学生信息
     * 可选择是否同步到用户表（姓名、院系、电话、邮箱）
     *
     * @param studentInfo 学生信息（含ID）
     * @param syncToUser  是否同步到用户表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser) {
        log.info("更新学生信息并同步到 sys_user, studentId={}", studentInfo.getId());

        // 1. 更新学生信息
        boolean success = updateById(studentInfo);
        if (!success) {
            log.error("更新 student_info 失败");
            return false;
        }

        // 2. 同步到用户表
        if (syncToUser && studentInfo.getUserId() != null) {
            SysUser user = new SysUser();
            user.setId(studentInfo.getUserId());
            user.setRealName(studentInfo.getName());
            user.setDepartment(studentInfo.getDepartment());
            user.setPhone(studentInfo.getPhone());
            user.setEmail(studentInfo.getEmail());
            sysUserMapper.updateById(user);
            log.info("同步更新 sys_user 完成, userId={}", studentInfo.getUserId());
        }

        return true;
    }

    /**
     * 学生更新自己的信息
     * 学生只能修改部分字段，同时同步到用户表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByUserId(Long userId, String phone, String email, String direction, String idCard, String nativePlace, String address) {
        log.info("学生更新个人信息, userId={}", userId);

        // 1. 获取学生信息
        StudentInfo studentInfo = getByUserId(userId);
        if (studentInfo == null) {
            log.error("学生信息不存在, userId={}", userId);
            return false;
        }

        // 2. 更新学生信息的部分字段
        studentInfo.setPhone(phone);
        studentInfo.setEmail(email);
        if (direction != null) {
            studentInfo.setDirection(direction);
        }
        if (idCard != null) {
            studentInfo.setIdCard(idCard);
        }
        if (nativePlace != null) {
            studentInfo.setNativePlace(nativePlace);
        }
        if (address != null) {
            studentInfo.setAddress(address);
        }
        studentInfo.setUpdateTime(java.time.LocalDateTime.now());

        boolean success = updateById(studentInfo);
        if (!success) {
            return false;
        }

        // 3. 同步到用户表（仅电话和邮箱）
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPhone(phone);
        user.setEmail(email);
        sysUserMapper.updateById(user);

        log.info("学生个人信息更新完成，已同步到 sys_user");
        return true;
    }

    /**
     * 删除学生信息（级联删除用户账号）
     * 返回值：0=成功，1=学生信息不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWithCascade(Long id) {
        log.info("原子删除学生信息, id={}", id);

        // 1. 获取学生信息
        StudentInfo studentInfo = getById(id);
        if (studentInfo == null) {
            log.warn("删除失败：学生信息不存在, id={}", id);
            return 1;
        }

        Long userId = studentInfo.getUserId();

        // 2. 删除学生档案
        boolean studentDeleted = removeById(id);
        if (!studentDeleted) {
            log.error("删除学生档案失败, id={}", id);
            throw new RuntimeException("删除学生档案失败");
        }

        // 3. 级联删除用户账号
        if (userId != null) {
            int userDeleted = sysUserMapper.deleteById(userId);
            if (userDeleted <= 0) {
                log.error("级联删除用户账号失败, userId={}, 触发事务回滚", userId);
                throw new RuntimeException("级联删除用户账号失败");
            }
            log.info("级联删除用户账号成功, userId={}", userId);
        }

        log.info("原子删除完成：学生档案和用户账号均已删除");
        return 0;
    }
}
