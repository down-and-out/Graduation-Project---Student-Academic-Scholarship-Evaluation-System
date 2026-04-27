package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.StudentCreateFields;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.StudentInfoService;
import com.scholarship.service.SysUserService;
import com.scholarship.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final StudentInfoService studentInfoService;

    @Override
    public IPage<SysUserVO> pageUserVOs(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses) {
        IPage<SysUser> userPage = pageUsers(current, size, keyword, departments, userTypes, statuses);
        Map<Long, StudentInfo> studentInfoMap = getStudentInfoMap(userPage.getRecords());
        return userPage.convert(user -> SysUserVO.fromEntity(user, studentInfoMap.get(user.getId())));
    }

    @Override
    public IPage<SysUser> pageUsers(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses) {
        log.debug("分页查询用户，current={}, size={}, keyword={}, departments={}, userTypes={}, statuses={}", current, size, keyword, departments, userTypes, statuses);

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        applyIntegerFilter(wrapper, SysUser::getDepartment, departments);
        applyIntegerFilter(wrapper, SysUser::getUserType, userTypes);
        applyIntegerFilter(wrapper, SysUser::getStatus, statuses);

        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    private <T> void applyIntegerFilter(LambdaQueryWrapper<SysUser> wrapper,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<SysUser, T> column,
                                        List<T> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        if (values.size() == 1) {
            wrapper.eq(column, values.get(0));
            return;
        }

        wrapper.in(column, values);
    }

    private Map<Long, StudentInfo> getStudentInfoMap(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> studentUserIds = users.stream()
                .filter(user -> user != null && Integer.valueOf(1).equals(user.getUserType()))
                .map(SysUser::getId)
                .collect(Collectors.toList());

        if (studentUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return studentInfoService.mapByUserIds(studentUserIds);
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
        return createUser(user, major, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user, String major, StudentCreateFields studentFields) {
        log.info("创建用户，username={}, realName={}, userType={}, major={}", user.getUsername(), user.getRealName(), user.getUserType(), major);

        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode("a123456789"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        boolean success = save(user);
        if (!success) {
            return false;
        }

        if (user.getId() == null) {
            throw new RuntimeException("用户保存失败：未能获取生成的用户ID");
        }

        if (user.getUserType() != null && user.getUserType() == 1) {
            createStudentInfo(user, major, studentFields);
        }

        return true;
    }

    private void createStudentInfo(SysUser user, String major, StudentCreateFields studentFields) {
        log.info("自动创建学生档案，userId={}, username={}, major={}", user.getId(), user.getUsername(), major);

        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new RuntimeException("创建学生档案失败：姓名为空，请填写真实姓名");
        }

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setUserId(user.getId());
        studentInfo.setStudentNo(studentFields != null && studentFields.getStudentNo() != null && !studentFields.getStudentNo().isEmpty()
                ? studentFields.getStudentNo() : user.getUsername());
        studentInfo.setName(user.getRealName());
        studentInfo.setDepartment(user.getDepartment());
        studentInfo.setMajor(major);
        studentInfo.setPhone(user.getPhone());
        studentInfo.setEmail(user.getEmail());
        studentInfo.setGender(studentFields != null && studentFields.getGender() != null ? studentFields.getGender() : 1);
        studentInfo.setIdCard(studentFields != null ? studentFields.getIdCard() : null);
        studentInfo.setEnrollmentYear(studentFields != null && studentFields.getEnrollmentYear() != null
                ? studentFields.getEnrollmentYear() : Year.now().getValue());
        studentInfo.setEducationLevel(studentFields != null && studentFields.getEducationLevel() != null
                ? studentFields.getEducationLevel() : 1);
        studentInfo.setTrainingMode(studentFields != null && studentFields.getTrainingMode() != null
                ? studentFields.getTrainingMode() : 1);
        studentInfo.setNativePlace(studentFields != null ? studentFields.getNativePlace() : null);
        studentInfo.setAddress(studentFields != null ? studentFields.getAddress() : null);
        studentInfo.setStatus(studentFields != null && studentFields.getStatus() != null ? studentFields.getStatus() : 1);

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

        if (user.getUsername() != null) {
            SysUser existUser = getByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new RuntimeException("用户名已存在");
            }
        }

        user.setPassword(null);

        boolean success = updateById(user);
        if (!success) {
            return false;
        }

        syncToStudentInfo(user);
        return true;
    }

    private void syncToStudentInfo(SysUser user) {
        StudentInfo studentInfo = studentInfoService.getByUserId(user.getId());
        if (studentInfo == null) {
            return;
        }

        log.info("同步更新 student_info，userId={}", user.getId());

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
        studentInfo.setUpdateTime(LocalDateTime.now());

        studentInfoService.updateById(studentInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) {
            log.warn("删除用户失败，id 不能为空");
            return false;
        }

        log.info("删除用户，id={}", id);

        StudentInfo studentInfo = studentInfoService.getByUserId(id);
        if (studentInfo != null) {
            log.info("级联删除学生档案，studentInfoId={}", studentInfo.getId());
            boolean studentDeleted = studentInfoService.removeById(studentInfo.getId());
            if (!studentDeleted) {
                log.error("级联删除学生档案失败，studentInfoId={}，触发事务回滚", studentInfo.getId());
                throw new RuntimeException("级联删除学生档案失败，userId=" + id);
            }
            log.info("级联删除学生档案成功");
        }

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

        String idsStr;
        if (ids.size() > 10) {
            List<Long> firstTen = ids.subList(0, 10);
            idsStr = firstTen + "... (共" + ids.size() + " 个)";
        } else {
            idsStr = ids.toString();
        }
        log.info("批量删除用户，count={}，ids={}", ids.size(), idsStr);

        LambdaQueryWrapper<StudentInfo> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(StudentInfo::getUserId, ids);
        List<StudentInfo> studentInfos = studentInfoService.list(studentWrapper);

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

        boolean usersDeleted = removeByIds(ids);
        if (!usersDeleted) {
            log.error("批量删除用户失败，触发事务回滚");
            throw new RuntimeException("批量删除用户失败");
        }

        log.info("批量删除用户成功，count={}", ids.size());
        return true;
    }
}
