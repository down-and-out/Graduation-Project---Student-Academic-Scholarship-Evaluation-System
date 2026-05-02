package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.result.ResultCode;
import com.scholarship.config.ScholarshipProperties;
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
 *
 * 功能说明：
 * - 分页查询用户列表（支持关键词、角色、院系、状态筛选）
 * - 创建用户（自动创建学生档案）
 * - 更新用户信息（同步到学生档案）
 * - 删除用户（级联删除学生档案）
 * - 重置用户密码
 * - 批量删除用户（级联删除学生档案）
 *
 * 事务说明：
 * - 所有涉及多表操作的方法均使用 @Transactional 保证事务一致性
 * - 删除用户时会级联删除关联的学生档案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final StudentInfoService studentInfoService;
    private final ScholarshipProperties scholarshipProperties;

    /**
     * 分页查询用户列表（返回 VO，包含学生信息）
     *
     * @param current    当前页码
     * @param size       每页条数
     * @param keyword    搜索关键字（匹配用户名或姓名）
     * @param departments 院系列表
     * @param userTypes  用户类型列表
     * @param statuses   状态列表
     * @return 分页后的用户VO列表
     */
    @Override
    public IPage<SysUserVO> pageUserVOs(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses) {
        // 1. 查询用户分页数据
        IPage<SysUser> userPage = pageUsers(current, size, keyword, departments, userTypes, statuses);
        // 2. 批量获取学生信息（仅学生类型）
        Map<Long, StudentInfo> studentInfoMap = getStudentInfoMap(userPage.getRecords());
        // 3. 转换为 VO
        return userPage.convert(user -> SysUserVO.fromEntity(user, studentInfoMap.get(user.getId())));
    }

    /**
     * 分页查询用户列表（返回实体）
     */
    @Override
    public IPage<SysUser> pageUsers(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses) {
        log.debug("分页查询用户，current={}, size={}, keyword={}, departments={}, userTypes={}, statuses={}", current, size, keyword, departments, userTypes, statuses);

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：用户名或姓名模糊匹配
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        // 院系列表筛选
        applyIntegerFilter(wrapper, SysUser::getDepartment, departments);
        // 用户类型筛选
        applyIntegerFilter(wrapper, SysUser::getUserType, userTypes);
        // 状态筛选
        applyIntegerFilter(wrapper, SysUser::getStatus, statuses);

        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 通用整数列表筛选条件
     * - 单个值：精确匹配
     * - 多个值：IN 查询
     */
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

    /**
     * 批量获取学生信息（以 userId 为 key 的 Map）
     * 仅获取学生类型的用户
     */
    private Map<Long, StudentInfo> getStudentInfoMap(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }

        // 筛选出学生类型的用户ID
        List<Long> studentUserIds = users.stream()
                .filter(user -> user != null && UserTypeEnum.isStudent(user.getUserType()))
                .map(SysUser::getId)
                .collect(Collectors.toList());

        if (studentUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return studentInfoService.mapByUserIds(studentUserIds);
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public SysUser getByUsername(String username) {
        log.debug("根据用户名查询用户，username={}", username);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    /**
     * 创建用户（简化版本，无需学生专属字段）
     * 密码默认为系统配置中的默认密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user, String major) {
        return createUser(user, major, null);
    }

    /**
     * 创建用户（完整版本）
     * - 如果用户名已存在，抛出异常
     * - 密码加密（使用默认密码或指定密码）
     * - 如果用户类型为学生，自动创建学生档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user, String major, StudentCreateFields studentFields) {
        log.info("创建用户，username={}, realName={}, userType={}, major={}", user.getUsername(), user.getRealName(), user.getUserType(), major);

        // 1. 检查用户名是否已存在
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        // 2. 加密密码（使用指定密码或系统默认密码）
        user.setPassword(passwordEncoder.encode(
                user.getPassword() != null && !user.getPassword().trim().isEmpty()
                        ? user.getPassword()
                        : scholarshipProperties.getSystem().getDefaultPassword()
        ));

        // 3. 设置默认状态（如果未指定）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 4. 保存用户
        boolean success = save(user);
        if (!success) {
            return false;
        }

        if (user.getId() == null) {
            throw new BusinessException(ResultCode.ERROR, "用户保存失败：未能获取生成的用户ID");
        }

        // 5. 如果是学生类型，自动创建学生档案
        if (UserTypeEnum.isStudent(user.getUserType())) {
            createStudentInfo(user, major, studentFields);
        }

        return true;
    }

    /**
     * 自动创建学生档案
     * 学生档案的字段从用户信息和学生专属字段中提取
     */
    private void createStudentInfo(SysUser user, String major, StudentCreateFields studentFields) {
        log.info("自动创建学生档案，userId={}, username={}, major={}", user.getId(), user.getUsername(), major);

        // 校验姓名不能为空
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.ERROR, "创建学生档案失败：姓名为空，请填写真实姓名");
        }

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setUserId(user.getId());
        // 学号：优先使用学生专属字段，否则使用用户名
        studentInfo.setStudentNo(studentFields != null && studentFields.getStudentNo() != null && !studentFields.getStudentNo().isEmpty()
                ? studentFields.getStudentNo() : user.getUsername());
        studentInfo.setName(user.getRealName());
        studentInfo.setDepartment(user.getDepartment());
        studentInfo.setMajor(major);
        studentInfo.setPhone(user.getPhone());
        studentInfo.setEmail(user.getEmail());
        // 性别：默认1（男）
        studentInfo.setGender(studentFields != null && studentFields.getGender() != null ? studentFields.getGender() : 1);
        studentInfo.setIdCard(studentFields != null ? studentFields.getIdCard() : null);
        // 入学年份：默认当前年份
        studentInfo.setEnrollmentYear(studentFields != null && studentFields.getEnrollmentYear() != null
                ? studentFields.getEnrollmentYear() : Year.now().getValue());
        // 学历层次：默认1（硕士）
        studentInfo.setEducationLevel(studentFields != null && studentFields.getEducationLevel() != null
                ? studentFields.getEducationLevel() : 1);
        // 培养方式：默认1（全日制）
        studentInfo.setTrainingMode(studentFields != null && studentFields.getTrainingMode() != null
                ? studentFields.getTrainingMode() : 1);
        studentInfo.setNativePlace(studentFields != null ? studentFields.getNativePlace() : null);
        studentInfo.setAddress(studentFields != null ? studentFields.getAddress() : null);
        // 学籍状态：默认1（在校）
        studentInfo.setStatus(studentFields != null && studentFields.getStatus() != null ? studentFields.getStatus() : 1);

        boolean success = studentInfoService.save(studentInfo);
        if (!success) {
            throw new BusinessException(ResultCode.ERROR, "创建学生档案失败：数据库保存失败");
        }
        log.info("学生档案创建完成，studentInfoId={}", studentInfo.getId());
    }

    /**
     * 更新用户信息
     * - 不允许修改密码（密码字段置空）
     * - 同步更新到学生档案（姓名、院系、电话、邮箱）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        log.info("更新用户，id={}, username={}", user.getId(), user.getUsername());

        // 1. 检查用户名是否被其他用户占用
        if (user.getUsername() != null) {
            SysUser existUser = getByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
            }
        }

        // 2. 密码字段置空（不允许通过此接口修改密码）
        user.setPassword(null);

        // 3. 更新用户
        boolean success = updateById(user);
        if (!success) {
            return false;
        }

        // 4. 同步更新学生档案
        syncToStudentInfo(user);
        return true;
    }

    /**
     * 同步用户信息到学生档案
     * 仅同步基本信息：姓名、院系、电话、邮箱
     */
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

    /**
     * 删除用户
     * - 级联删除关联的学生档案
     * - 如果学生档案删除失败，事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) {
            log.warn("删除用户失败，id 不能为空");
            return false;
        }

        log.info("删除用户，id={}", id);

        // 1. 查询关联的学生档案
        StudentInfo studentInfo = studentInfoService.getByUserId(id);
        if (studentInfo != null) {
            log.info("级联删除学生档案，studentInfoId={}", studentInfo.getId());
            // 2. 级联删除学生档案
            boolean studentDeleted = studentInfoService.removeById(studentInfo.getId());
            if (!studentDeleted) {
                log.error("级联删除学生档案失败，studentInfoId={}，触发事务回滚", studentInfo.getId());
                throw new BusinessException(ResultCode.ERROR, "级联删除学生档案失败，userId=" + id);
            }
            log.info("级联删除学生档案成功");
        }

        // 3. 删除用户
        boolean userDeleted = removeById(id);
        if (!userDeleted) {
            log.error("删除用户失败，id={}，触发事务回滚", id);
            throw new BusinessException(ResultCode.ERROR, "删除用户失败，id=" + id);
        }

        log.info("删除用户成功，id={}", id);
        return true;
    }

    /**
     * 重置用户密码
     * 将密码设置为指定值或系统默认密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        log.info("重置用户密码，id={}", id);

        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 使用指定密码或系统默认密码
        String password = (newPassword != null && !newPassword.trim().isEmpty())
                ? newPassword
                : scholarshipProperties.getSystem().getDefaultPassword();
        user.setPassword(passwordEncoder.encode(password));
        return updateById(user);
    }

    /**
     * 批量删除用户
     * - 级联删除关联的学生档案
     * - 如果任何删除操作失败，事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        // 日志记录（如果数量过多只显示前10个）
        String idsStr;
        if (ids.size() > 10) {
            List<Long> firstTen = ids.subList(0, 10);
            idsStr = firstTen + "... (共" + ids.size() + " 个)";
        } else {
            idsStr = ids.toString();
        }
        log.info("批量删除用户，count={}，ids={}", ids.size(), idsStr);

        // 1. 查询所有待删除用户关联的学生档案
        LambdaQueryWrapper<StudentInfo> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(StudentInfo::getUserId, ids);
        List<StudentInfo> studentInfos = studentInfoService.list(studentWrapper);

        // 2. 级联批量删除学生档案
        if (studentInfos != null && !studentInfos.isEmpty()) {
            List<Long> studentInfoIds = studentInfos.stream()
                    .map(StudentInfo::getId)
                    .collect(Collectors.toList());
            log.info("级联批量删除学生档案，count={}", studentInfoIds.size());
            boolean studentsDeleted = studentInfoService.removeByIds(studentInfoIds);
            if (!studentsDeleted) {
                log.error("级联批量删除学生档案失败，触发事务回滚");
                throw new BusinessException(ResultCode.ERROR, "级联批量删除学生档案失败");
            }
            log.info("级联批量删除学生档案成功");
        }

        // 3. 批量删除用户
        boolean usersDeleted = removeByIds(ids);
        if (!usersDeleted) {
            log.error("批量删除用户失败，触发事务回滚");
            throw new BusinessException(ResultCode.ERROR, "批量删除用户失败");
        }

        log.info("批量删除用户成功，count={}", ids.size());
        return true;
    }
}
