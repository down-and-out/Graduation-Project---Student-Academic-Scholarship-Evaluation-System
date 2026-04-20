package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.StudentInfo;

import java.util.List;
import java.util.Map;

/**
 * 研究生信息服务接口
 */
public interface StudentInfoService extends IService<StudentInfo> {

    IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, List<String> departments, List<Integer> statuses);

    StudentInfo getByUserId(Long userId);

    Map<Long, StudentInfo> mapByUserIds(List<Long> userIds);

    Map<Long, StudentInfo> mapByIds(List<Long> ids);

    boolean updateStudentWithSync(StudentInfo studentInfo, boolean syncToUser);

    boolean updateByUserId(Long userId, String phone, String email, String direction, String idCard, String nativePlace, String address);

    int deleteWithCascade(Long id);
}
