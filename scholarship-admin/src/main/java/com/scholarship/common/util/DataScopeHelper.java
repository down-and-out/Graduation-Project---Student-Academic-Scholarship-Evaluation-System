package com.scholarship.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据作用域工具类
 * <p>
 * 统一处理科研成果服务中按用户类型（管理员/学生/导师）的数据权限控制逻辑，
 * 消除 4 个服务间的重复实现。
 * </p>
 */
public class DataScopeHelper {

    /**
     * 根据用户 ID 查找学生信息
     *
     * @param userId 用户 ID
     * @param mapper StudentInfoMapper
     * @return 学生信息，不存在时返回 null
     */
    public static StudentInfo findStudentByUserId(Long userId, StudentInfoMapper mapper) {
        return mapper.selectOne(new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getUserId, userId));
    }

    /**
     * 根据用户 ID 查找学生信息，不存在时抛出 BusinessException
     *
     * @param userId 用户 ID
     * @param mapper StudentInfoMapper
     * @return 学生信息
     * @throws BusinessException 学生信息不存在时抛出
     */
    public static StudentInfo requireStudentByUserId(Long userId, StudentInfoMapper mapper) {
        StudentInfo student = findStudentByUserId(userId, mapper);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        return student;
    }

    /**
     * 根据学生 ID 查找学生信息，不存在或 ID 为空时抛出 BusinessException
     *
     * @param studentId 学生 ID
     * @param mapper    StudentInfoMapper
     * @return 学生信息
     * @throws BusinessException 学生 ID 为空或学生信息不存在时抛出
     */
    public static StudentInfo requireStudentById(Long studentId, StudentInfoMapper mapper) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空");
        }
        StudentInfo student = mapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        return student;
    }

    /**
     * 根据导师用户 ID 查询其指导的所有学生 ID 列表
     *
     * @param tutorUserId 导师用户 ID
     * @param mapper      StudentInfoMapper
     * @return 学生 ID 列表
     */
    public static List<Long> listStudentIdsByTutorId(Long tutorUserId, StudentInfoMapper mapper) {
        return mapper.selectList(new LambdaQueryWrapper<StudentInfo>()
                        .eq(StudentInfo::getTutorId, tutorUserId))
                .stream()
                .map(StudentInfo::getId)
                .toList();
    }

    /**
     * 对查询 wrapper 应用数据作用域限制
     * <p>
     * 根据用户类型自动添加查询条件：
     * <ul>
     *   <li>管理员或未登录：按 requestedStudentId 精确过滤</li>
     *   <li>学生：仅查询自己的成果</li>
     *   <li>导师：查询名下所有学生的成果</li>
     * </ul>
     *
     * @param wrapper            查询 wrapper
     * @param requestedStudentId 请求中指定的学生 ID（可为 null）
     * @param loginUser          当前登录用户
     * @param studentIdGetter    实体中 studentId 字段的 getter 引用（如 ResearchPatent::getStudentId）
     * @param mapper             StudentInfoMapper
     * @param <T>                实体类型
     */
    public static <T> void applyDataScope(LambdaQueryWrapper<T> wrapper, Long requestedStudentId,
                                          LoginUser loginUser, SFunction<T, Long> studentIdGetter,
                                          StudentInfoMapper mapper) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            wrapper.eq(requestedStudentId != null, studentIdGetter, requestedStudentId);
            return;
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = findStudentByUserId(loginUser.getUserId(), mapper);
            wrapper.eq(studentIdGetter, currentStudent == null ? -1L : currentStudent.getId());
            return;
        }

        if (UserTypeEnum.isTutor(loginUser.getUserType())) {
            List<Long> studentIds = listStudentIdsByTutorId(loginUser.getUserId(), mapper);
            if (studentIds.isEmpty()) {
                wrapper.eq(studentIdGetter, -1L);
            } else {
                wrapper.in(studentIdGetter, studentIds);
            }
        }
    }

    /**
     * 校验当前用户是否有权查看指定所属学生的资源
     * <p>
     * 根据用户类型检查：
     * <ul>
     *   <li>管理员或未登录：直接通过</li>
     *   <li>学生：仅能查看自己的资源</li>
     *   <li>导师：仅能查看名下学生的资源</li>
     * </ul>
     *
     * @param ownerStudentId 资源所属学生 ID
     * @param loginUser      当前登录用户
     * @param resourceName   资源名称（用于错误提示，如"专利成果"）
     * @param mapper         StudentInfoMapper
     * @throws BusinessException 无权查看时抛出
     */
    public static void ensureReadable(Long ownerStudentId, LoginUser loginUser,
                                      String resourceName, StudentInfoMapper mapper) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            return;
        }
        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId(), mapper);
            if (!ownerStudentId.equals(currentStudent.getId())) {
                throw new BusinessException("无权查看该" + resourceName);
            }
            return;
        }
        if (UserTypeEnum.isTutor(loginUser.getUserType())
                && listStudentIdsByTutorId(loginUser.getUserId(), mapper).contains(ownerStudentId)) {
            return;
        }
        throw new BusinessException("无权查看该" + resourceName);
    }

    /**
     * 根据关键字（学号或姓名）搜索学生信息
     *
     * @param keyword 搜索关键字
     * @param mapper  StudentInfoMapper
     * @return 匹配的学生列表
     */
    public static List<StudentInfo> listStudentsByKeyword(String keyword, StudentInfoMapper mapper) {
        return mapper.selectList(new LambdaQueryWrapper<StudentInfo>()
                .and(w -> w.like(StudentInfo::getStudentNo, keyword)
                        .or()
                        .like(StudentInfo::getName, keyword)));
    }

    /**
     * 为成果记录列表附加学生信息（学号和姓名）
     *
     * @param records           成果记录列表
     * @param studentIdExtractor 从记录中提取学生 ID 的函数
     * @param studentNoSetter    设置学号的消费者
     * @param studentNameSetter  设置姓名的消费者
     * @param mapper             StudentInfoMapper
     * @param <T>                成果实体类型
     */
    public static <T> void attachStudentInfo(List<T> records,
                                             Function<T, Long> studentIdExtractor,
                                             BiConsumer<T, String> studentNoSetter,
                                             BiConsumer<T, String> studentNameSetter,
                                             StudentInfoMapper mapper) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Map<Long, StudentInfo> studentMap = mapper.selectList(new LambdaQueryWrapper<StudentInfo>()
                        .in(StudentInfo::getId, records.stream().map(studentIdExtractor).distinct().toList()))
                .stream()
                .collect(Collectors.toMap(StudentInfo::getId, Function.identity(), (a, b) -> a));
        records.forEach(record -> {
            StudentInfo student = studentMap.get(studentIdExtractor.apply(record));
            if (student != null) {
                studentNoSetter.accept(record, student.getStudentNo());
                studentNameSetter.accept(record, student.getName());
            }
        });
    }

    private DataScopeHelper() {
        // 工具类，防止实例化
    }
}
