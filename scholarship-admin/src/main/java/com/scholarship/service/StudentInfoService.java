package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.StudentInfo;

import java.util.List;
import java.util.Map;

/**
 * 研究生信息服务接口
 * <p>
 * 提供研究生信息的业务逻辑处理
 * 继承MyBatis-Plus的IService，获得基础的CRUD能力
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface StudentInfoService extends IService<StudentInfo> {

    /**
     * 分页查询研究生信息
     *
     * @param current  当前页
     * @param size     每页大小
     * @param keyword  搜索关键字（学号或姓名）
     * @param department 院系
     * @param status   学籍状态
     * @return 分页结果
     */
    IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, String department, Integer status);

    /**
     * 根据用户ID获取研究生信息
     *
     * @param userId 用户ID
     * @return 研究生信息
     */
    StudentInfo getByUserId(Long userId);

    /**
     * 批量查询学生信息
     *
     * @param userIds 用户 ID 列表
     * @return 按用户 ID 分组的学生信息
     */
    Map<Long, StudentInfo> mapByUserIds(List<Long> userIds);

    /**
     * 更新学生信息并同步到 sys_user 表
     * <p>
     * 保持两表数据一致，同步更新 phone、email、name、department
     * </p>
     *
     * @param studentInfo 学生信息
     * @param syncToUser 是否同步到 sys_user
     * @return 是否成功
     */
    boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser);

    /**
     * 根据用户 ID 更新学生信息（用于学生自助修改）
     * <p>
     * 同时同步更新到 sys_user 表
     * </p>
     *
     * @param userId 用户ID
     * @param phone 电话
     * @param email 邮箱
     * @param direction 研究方向
     * @return 是否成功
     */
    boolean updateByUserId(Long userId, String phone, String email, String direction);

    /**
     * 删除学生信息并级联删除关联的用户账号（原子操作）
     * <p>
     * 使用事务保证两个表的删除同时成功或同时失败
     * </p>
     *
     * @param id 学生ID
     * @return 删除结果：0-成功, 1-学生不存在, 2-删除失败
     */
    int deleteWithCascade(Long id);
}
