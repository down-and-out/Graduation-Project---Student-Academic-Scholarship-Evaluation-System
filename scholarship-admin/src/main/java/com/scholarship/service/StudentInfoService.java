package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.vo.TutorStudentVO;

import java.util.List;
import java.util.Map;

/**
 * 研究生信息服务接口
 */
public interface StudentInfoService extends IService<StudentInfo> {

    IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, List<String> departments, String enrollmentYear, List<Integer> statuses);

    IPage<TutorStudentVO> pageTutorStudents(Long tutorUserId, Long current, Long size, String keyword, String grade);

    StudentInfo getByUserId(Long userId);

    Map<Long, StudentInfo> mapByUserIds(List<Long> userIds);

    Map<Long, StudentInfo> mapByIds(List<Long> ids);

    boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser);

    boolean updateByUserId(Long userId, String phone, String email, String direction, String idCard, String nativePlace, String address);

    int deleteWithCascade(Long id);

    /**
     * 根据学生ID获取详情（带权限校验和导师姓名填充）
     *
     * @param id        学生ID
     * @param loginUser 当前登录用户
     * @return 学生信息，不存在返回 null
     */
    StudentInfo getStudentDetailById(Long id, LoginUser loginUser);

    /**
     * 获取当前导师名下学生的去重年级列表
     *
     * @param tutorUserId 导师用户ID
     * @return 去重后的年级列表（入学年份字符串），按年份倒序排列
     */
    List<String> listTutorGrades(Long tutorUserId);
}
