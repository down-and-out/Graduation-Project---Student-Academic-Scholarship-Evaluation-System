package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.service.StudentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 研究生信息服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
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
    public IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, String department, Integer status) {
        // 创建分页对象
        Page<StudentInfo> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();

        // 学号或姓名模糊查询
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StudentInfo::getName, keyword));
        }

        // 院系筛选
        if (StringUtils.isNotBlank(department)) {
            wrapper.eq(StudentInfo::getDepartment, department);
        }

        // 学籍状态筛选
        if (status != null) {
            wrapper.eq(StudentInfo::getStatus, status);
        }

        // 按入学年份降序排序
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

    /**
     * 更新学生信息并同步到 sys_user 表
     * <p>
     * 方案A：保持两表数据一致，同步更新 phone、email、name、department
     * </p>
     *
     * @param studentInfo 学生信息
     * @param syncToUser 是否同步到 sys_user
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser) {
        log.info("更新学生信息并同步到 sys_user，studentId={}", studentInfo.getId());

        // 1. 更新 student_info 表
        boolean success = updateById(studentInfo);
        if (!success) {
            log.error("更新 student_info 失败");
            return false;
        }

        // 2. 如果需要，同步更新 sys_user 表
        if (syncToUser && studentInfo.getUserId() != null) {
            SysUser user = new SysUser();
            user.setId(studentInfo.getUserId());
            user.setRealName(studentInfo.getName());
            user.setDepartment(studentInfo.getDepartment());
            user.setPhone(studentInfo.getPhone());
            user.setEmail(studentInfo.getEmail());

            // 使用 selective update，只更新非 null 字段
            sysUserMapper.updateById(user);
            log.info("同步更新 sys_user 完成，userId={}", studentInfo.getUserId());
        }

        return true;
    }

    /**
     * 根据用户 ID 更新学生信息（用于学生自助修改）
     *
     * @param userId 用户ID
     * @param phone 电话
     * @param email 邮箱
     * @param direction 研究方向
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByUserId(Long userId, String phone, String email, String direction) {
        log.info("学生更新个人信息，userId={}", userId);

        // 1. 查询学生信息
        StudentInfo studentInfo = getByUserId(userId);
        if (studentInfo == null) {
            log.error("学生信息不存在，userId={}", userId);
            return false;
        }

        // 2. 更新 student_info
        studentInfo.setPhone(phone);
        studentInfo.setEmail(email);
        if (direction != null) {
            studentInfo.setDirection(direction);
        }
        studentInfo.setUpdateTime(java.time.LocalDateTime.now());

        boolean success = updateById(studentInfo);
        if (!success) {
            return false;
        }

        // 3. 同步更新 sys_user
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPhone(phone);
        user.setEmail(email);
        sysUserMapper.updateById(user);

        log.info("学生个人信息更新完成，已同步到 sys_user");
        return true;
    }

    /**
     * 删除学生信息并级联删除关联的用户账号（原子操作）
     * <p>
     * 使用事务保证两个表的删除同时成功或同时失败
     * </p>
     *
     * @param id 学生ID
     * @return 删除结果：0-成功, 1-学生不存在, 2-删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWithCascade(Long id) {
        log.info("原子删除学生信息，id={}", id);

        // 1. 查询学生信息
        StudentInfo studentInfo = getById(id);
        if (studentInfo == null) {
            log.warn("删除失败：学生信息不存在，id={}", id);
            return 1; // 学生不存在
        }

        Long userId = studentInfo.getUserId();

        // 2. 删除学生档案
        boolean studentDeleted = removeById(id);
        if (!studentDeleted) {
            log.error("删除学生档案失败，id={}", id);
            throw new RuntimeException("删除学生档案失败"); // 触发回滚
        }

        // 3. 级联删除用户账号
        if (userId != null) {
            int userDeleted = sysUserMapper.deleteById(userId);
            if (userDeleted <= 0) {
                log.error("级联删除用户账号失败，userId={}，触发事务回滚", userId);
                throw new RuntimeException("级联删除用户账号失败"); // 触发回滚
            }
            log.info("级联删除用户账号成功，userId={}", userId);
        }

        log.info("原子删除完成：学生档案和用户账号均已删除");
        return 0; // 成功
    }
}
