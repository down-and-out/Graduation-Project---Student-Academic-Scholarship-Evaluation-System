package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.StudentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo> implements StudentInfoService {

    private final StudentInfoMapper studentInfoMapper;
    private final SysUserMapper sysUserMapper;

    public StudentInfoServiceImpl(StudentInfoMapper studentInfoMapper, SysUserMapper sysUserMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, List<String> departments, List<Integer> statuses) {
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
