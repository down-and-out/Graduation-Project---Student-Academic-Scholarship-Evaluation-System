package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.service.StudentInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 研究生信息服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo> implements StudentInfoService {

    private final StudentInfoMapper studentInfoMapper;

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
}
