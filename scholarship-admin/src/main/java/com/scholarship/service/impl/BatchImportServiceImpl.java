package com.scholarship.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.dto.StudentImportDTO;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.BatchImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量导入服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchImportServiceImpl implements BatchImportService {

    private final StudentInfoMapper studentInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudents(List<StudentImportDTO> students) {
        log.info("开始批量导入学生信息，数量：{}", students.size());

        List<String> successNames = new ArrayList<>();
        List<Map<String, String>> failures = new ArrayList<>();

        for (StudentImportDTO dto : students) {
            try {
                LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(StudentInfo::getStudentNo, dto.getStudentNo());
                StudentInfo existing = studentInfoMapper.selectOne(wrapper);

                if (existing != null) {
                    Map<String, String> failRecord = new HashMap<>();
                    failRecord.put("studentNo", dto.getStudentNo());
                    failRecord.put("name", dto.getName());
                    failRecord.put("reason", "学号已存在");
                    failures.add(failRecord);
                    continue;
                }

                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setStudentNo(dto.getStudentNo());
                studentInfo.setName(dto.getName());
                studentInfo.setGender(dto.getGender());
                studentInfo.setIdCard(dto.getIdCard());
                studentInfo.setEnrollmentYear(dto.getEnrollmentYear());
                studentInfo.setEducationLevel(dto.getEducationLevel());
                studentInfo.setTrainingMode(dto.getTrainingMode());
                studentInfo.setDepartment(dto.getDepartment());
                studentInfo.setMajor(dto.getMajor());
                studentInfo.setClassName(dto.getClassName());
                studentInfo.setTutorId(dto.getTutorId());
                studentInfo.setDirection(dto.getDirection());
                studentInfo.setPoliticalStatus(dto.getPoliticalStatus());
                studentInfo.setNation(dto.getNation());
                studentInfo.setNativePlace(dto.getNativePlace());
                studentInfo.setAddress(dto.getAddress());
                studentInfo.setPhone(dto.getPhone());
                studentInfo.setEmail(dto.getEmail());
                studentInfo.setStatus(1);
                studentInfo.setDeleted(0);

                studentInfoMapper.insert(studentInfo);
                successNames.add(dto.getName());

            } catch (Exception e) {
                log.error("导入学生失败：{}，错误：{}", dto.getStudentNo(), e.getMessage());
                Map<String, String> failRecord = new HashMap<>();
                failRecord.put("studentNo", dto.getStudentNo());
                failRecord.put("name", dto.getName());
                failRecord.put("reason", "系统错误：" + e.getMessage());
                failures.add(failRecord);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successNames.size());
        result.put("failCount", failures.size());
        result.put("successNames", successNames);
        result.put("failures", failures);

        log.info("批量导入完成：成功 {} 条，失败 {} 条", successNames.size(), failures.size());
        return result;
    }

    @Override
    public byte[] getImportTemplate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, StudentImportDTO.class)
                .sheet("学生信息导入模板")
                .doWrite(List.of());
        return out.toByteArray();
    }
}
