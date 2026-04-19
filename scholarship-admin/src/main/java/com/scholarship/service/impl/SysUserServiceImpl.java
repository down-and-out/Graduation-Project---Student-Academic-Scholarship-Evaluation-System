package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.SysUserService;
import com.scholarship.vo.SysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scholarship.entity.StudentInfo;
import com.scholarship.service.StudentInfoService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final StudentInfoService studentInfoService;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder, StudentInfoService studentInfoService) {
        this.passwordEncoder = passwordEncoder;
        this.studentInfoService = studentInfoService;
    }

    @Override
    public IPage<SysUserVO> pageUserVOs(Long current, Long size, String keyword, Integer userType, Integer status) {
        IPage<SysUser> userPage = pageUsers(current, size, keyword, userType, status);
        return userPage.convert(SysUserVO::fromEntity);
    }

    @Override
    public IPage<SysUser> pageUsers(Long current, Long size, String keyword, Integer userType, Integer status) {
        log.debug("分页查询用户，current={}, size={}, keyword={}, userType={}, status={}", current, size, keyword, userType, status);

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (userType != null) {
            wrapper.eq(SysUser::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public SysUser getByUsername(String username) {
        log.debug("根据用户名查询用户，username={}", username);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user, String major) {
        log.info("创建用户，username={}, realName={}, userType={}, major={}", user.getUsername(), user.getRealName(), user.getUserType(), major);

        // 检查用户名是否存在
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            // 默认密码为 a123456789
            user.setPassword(passwordEncoder.encode("a123456789"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 默认状态为正常
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 保存用户
        boolean success = save(user);
        if (!success) {
            return false;
        }

        // 确保 ID 已生成
        if (user.getId() == null) {
            throw new RuntimeException("用户保存失败：未能获取生成的用户ID");
        }

        // 如果是学生类型（userType=1），自动创建 student_info 记录
        if (user.getUserType() != null && user.getUserType() == 1) {
            createStudentInfo(user, major);
        }

        return true;
    }

    /**
     * 创建学生档案信息
     *
     * @param user 系统用户信息
     * @param major 专业
     */
    private void createStudentInfo(SysUser user, String major) {
        log.info("自动创建学生档案，userId={}, username={}, major={}", user.getId(), user.getUsername(), major);

        // 校验必要字段
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new RuntimeException("创建学生档案失败：姓名为空，请填写真实姓名");
        }

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setUserId(user.getId());
        // 默认学号使用用户名
        studentInfo.setStudentNo(user.getUsername());
        // 姓名从 real_name 获取
        studentInfo.setName(user.getRealName());
        // 院系
        studentInfo.setDepartment(user.getDepartment());
        // 专业
        studentInfo.setMajor(major);
        // 电话、邮箱同步
        studentInfo.setPhone(user.getPhone());
        studentInfo.setEmail(user.getEmail());
        // 默认入学年份为当前年份
        studentInfo.setEnrollmentYear(java.time.Year.now().getValue());
        // 默认学历为硕士
        studentInfo.setEducationLevel(1);
        // 默认培养方式为全日制
        studentInfo.setTrainingMode(1);
        // 默认学籍状态为在读
        studentInfo.setStatus(1);
        // 默认性别为男（可在后续修改）
        studentInfo.setGender(1);

        boolean success = studentInfoService.save(studentInfo);
        if (!success) {
            throw new RuntimeException("创建学生档案失败：数据库保存失败");
        }
        log.info("学生档案创建完成，studentInfoId={}", studentInfo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        log.info("更新用户，id={}, username={}", user.getId(), user.getUsername());

        // 检查用户名是否存在（排除自己）
        if (user.getUsername() != null) {
            SysUser existUser = getByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new RuntimeException("用户名已存在");
            }
        }

        // 不允许直接修改密码，使用 resetPassword 方法
        user.setPassword(null);

        // 更新用户
        boolean success = updateById(user);
        if (!success) {
            return false;
        }

        // 如果更新了姓名、院系、电话、邮箱，同步到 student_info
        syncToStudentInfo(user);

        return true;
    }

    /**
     * 同步更新到 student_info 表
     *
     * @param user 系统用户信息
     */
    private void syncToStudentInfo(SysUser user) {
        // 查询该用户是否有关联的学生档案
        StudentInfo studentInfo = studentInfoService.getByUserId(user.getId());
        if (studentInfo == null) {
            return;
        }

        log.info("同步更新 student_info，userId={}", user.getId());

        // 只更新非空字段
        if (user.getRealName() != null) {
            studentInfo.setName(user.getRealName());
        }
        if (user.getDepartment() != null) {
            studentInfo.setDepartment(user.getDepartment());
        }
        if (user.getPhone() != null) {
            studentInfo.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            studentInfo.setEmail(user.getEmail());
        }
        studentInfo.setUpdateTime(java.time.LocalDateTime.now());

        studentInfoService.updateById(studentInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) {
            log.warn("删除用户失败：id 不能为空");
            return false;
        }

        log.info("删除用户，id={}", id);

        // 1. 先查询该用户是否有关联的学生档案
        StudentInfo studentInfo = studentInfoService.getByUserId(id);
        if (studentInfo != null) {
            // 2. 如果存在学生档案，先删除学生档案
            log.info("级联删除学生档案，studentInfoId={}", studentInfo.getId());
            boolean studentDeleted = studentInfoService.removeById(studentInfo.getId());
            if (!studentDeleted) {
                log.error("级联删除学生档案失败，studentInfoId={}，触发事务回滚", studentInfo.getId());
                throw new RuntimeException("级联删除学生档案失败，userId=" + id);
            }
            log.info("级联删除学生档案成功");
        }

        // 3. 删除用户
        boolean userDeleted = removeById(id);
        if (!userDeleted) {
            log.error("删除用户失败，id={}，触发事务回滚", id);
            throw new RuntimeException("删除用户失败，id=" + id);
        }

        log.info("删除用户成功，id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        log.info("重置用户密码，id={}", id);

        SysUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        // 限制日志输出长度，避免大量数据导致日志过长
        String idsStr;
        if (ids.size() > 10) {
            List<Long> firstTen = ids.subList(0, 10);
            idsStr = firstTen.toString() + "... (共" + ids.size() + "个)";
        } else {
            idsStr = ids.toString();
        }
        log.info("批量删除用户，count={}，ids={}", ids.size(), idsStr);

        // 1. 查询这些用户关联的所有学生档案
        LambdaQueryWrapper<StudentInfo> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(StudentInfo::getUserId, ids);
        List<StudentInfo> studentInfos = studentInfoService.list(studentWrapper);

        // 2. 如果存在学生档案，批量删除
        if (studentInfos != null && !studentInfos.isEmpty()) {
            List<Long> studentInfoIds = studentInfos.stream()
                .map(StudentInfo::getId)
                .collect(Collectors.toList());
            log.info("级联批量删除学生档案，count={}", studentInfoIds.size());
            boolean studentsDeleted = studentInfoService.removeByIds(studentInfoIds);
            if (!studentsDeleted) {
                log.error("级联批量删除学生档案失败，触发事务回滚");
                throw new RuntimeException("级联批量删除学生档案失败");
            }
            log.info("级联批量删除学生档案成功");
        }

        // 3. 批量删除用户
        boolean usersDeleted = removeByIds(ids);
        if (!usersDeleted) {
            log.error("批量删除用户失败，触发事务回滚");
            throw new RuntimeException("批量删除用户失败");
        }

        log.info("批量删除用户成功，count={}", ids.size());
        return true;
    }
}
