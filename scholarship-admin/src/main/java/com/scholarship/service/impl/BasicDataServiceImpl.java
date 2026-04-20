package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.service.BasicDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础数据服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicDataServiceImpl implements BasicDataService {

    private final StudentInfoMapper studentInfoMapper;

    @Override
    public List<String> getDepartments() {
        log.debug("查询院系列表");

        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(StudentInfo::getDepartment)
               .isNotNull(StudentInfo::getDepartment)
               .ne(StudentInfo::getDepartment, "")
               .eq(StudentInfo::getDeleted, 0)
               .groupBy(StudentInfo::getDepartment);

        List<StudentInfo> list = studentInfoMapper.selectList(wrapper);

        return list.stream()
                .map(StudentInfo::getDepartment)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getMajors(String department) {
        log.debug("查询专业列表，department={}", department);

        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(StudentInfo::getMajor)
               .isNotNull(StudentInfo::getMajor)
               .ne(StudentInfo::getMajor, "")
               .eq(StudentInfo::getDeleted, 0)
               .groupBy(StudentInfo::getMajor);

        if (department != null && !department.trim().isEmpty()) {
            wrapper.eq(StudentInfo::getDepartment, department);
        }

        List<StudentInfo> list = studentInfoMapper.selectList(wrapper);

        return list.stream()
                .map(StudentInfo::getMajor)
                .sorted()
                .collect(Collectors.toList());
    }
}
