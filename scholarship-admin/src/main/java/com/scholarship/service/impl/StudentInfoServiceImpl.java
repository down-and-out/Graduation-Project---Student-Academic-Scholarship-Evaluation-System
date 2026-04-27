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

    @Override
    public IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, List<String> departments, String enrollmentYear, List<Integer> statuses) {
        Page<StudentInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StudentInfo::getName, keyword));
        }

        if (departments != null && !departments.isEmpty()) {
            if (departments.size() == 1) {
                wrapper.eq(StudentInfo::getDepartment, departments.get(0));
            } else {
                wrapper.in(StudentInfo::getDepartment, departments);
            }
        }

        String normalizedEnrollmentYear = normalizeStudentNoEnrollmentYear(enrollmentYear);
        if (normalizedEnrollmentYear != null) {
            wrapper.likeRight(StudentInfo::getStudentNo, normalizedEnrollmentYear);
        }

        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                wrapper.eq(StudentInfo::getStatus, statuses.get(0));
            } else {
                wrapper.in(StudentInfo::getStatus, statuses);
            }
        }

        wrapper.orderByDesc(StudentInfo::getEnrollmentYear);
        return studentInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<TutorStudentVO> pageTutorStudents(Long tutorUserId, Long current, Long size, String keyword, String grade) {
        Page<StudentInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTutorId, tutorUserId);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StudentInfo::getName, keyword));
        }

        Integer enrollmentYear = normalizeEnrollmentYear(grade);
        if (enrollmentYear != null) {
            wrapper.eq(StudentInfo::getEnrollmentYear, enrollmentYear);
        }

        wrapper.orderByDesc(StudentInfo::getEnrollmentYear)
                .orderByAsc(StudentInfo::getStudentNo);

        Page<StudentInfo> studentPage = studentInfoMapper.selectPage(page, wrapper);
        Page<TutorStudentVO> resultPage = new Page<>(studentPage.getCurrent(), studentPage.getSize(), studentPage.getTotal());

        List<StudentInfo> students = studentPage.getRecords();
        if (students.isEmpty()) {
            resultPage.setRecords(List.of());
            return resultPage;
        }

        List<Long> studentIds = students.stream().map(StudentInfo::getId).toList();
        Map<Long, Integer> paperCountMap = researchPaperService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        Map<Long, Integer> patentCountMap = researchPatentService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        Map<Long, Integer> projectCountMap = researchProjectService.mapByStudentIds(studentIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

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

    private String normalizeStudentNoEnrollmentYear(String enrollmentYear) {
        if (StringUtils.isBlank(enrollmentYear)) {
            return null;
        }

        String normalized = enrollmentYear.trim();
        return normalized.length() == 4 && StringUtils.isNumeric(normalized) ? normalized : null;
    }

    private Integer normalizeEnrollmentYear(String grade) {
        if (StringUtils.isBlank(grade)) {
            return null;
        }

        String normalized = grade.trim()
                .replace("级", "")
                .replace(" ", "");

        return StringUtils.isNumeric(normalized) ? Integer.parseInt(normalized) : null;
    }

    @Override
    public StudentInfo getByUserId(Long userId) {
        return studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>()
                        .eq(StudentInfo::getUserId, userId)
        );
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser) {
        log.info("更新学生信息并同步到 sys_user, studentId={}", studentInfo.getId());

        boolean success = updateById(studentInfo);
        if (!success) {
            log.error("更新 student_info 失败");
            return false;
        }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByUserId(Long userId, String phone, String email, String direction, String idCard, String nativePlace, String address) {
        log.info("学生更新个人信息, userId={}", userId);

        StudentInfo studentInfo = getByUserId(userId);
        if (studentInfo == null) {
            log.error("学生信息不存在, userId={}", userId);
            return false;
        }

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

        SysUser user = new SysUser();
        user.setId(userId);
        user.setPhone(phone);
        user.setEmail(email);
        sysUserMapper.updateById(user);

        log.info("学生个人信息更新完成，已同步到 sys_user");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWithCascade(Long id) {
        log.info("原子删除学生信息, id={}", id);

        StudentInfo studentInfo = getById(id);
        if (studentInfo == null) {
            log.warn("删除失败：学生信息不存在, id={}", id);
            return 1;
        }

        Long userId = studentInfo.getUserId();

        boolean studentDeleted = removeById(id);
        if (!studentDeleted) {
            log.error("删除学生档案失败, id={}", id);
            throw new RuntimeException("删除学生档案失败");
        }

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
