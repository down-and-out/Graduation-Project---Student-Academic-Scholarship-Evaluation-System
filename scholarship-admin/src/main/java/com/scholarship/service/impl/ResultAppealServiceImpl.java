package com.scholarship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.ResultAppeal;
import com.scholarship.entity.StudentInfo;
import com.scholarship.enums.ResultStatusEnum;
import com.scholarship.enums.AppealStatusEnum;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.mapper.ResultAppealMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResultAppealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 结果异议服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultAppealServiceImpl extends ServiceImpl<ResultAppealMapper, ResultAppeal>
        implements ResultAppealService {

    private final EvaluationResultMapper evaluationResultMapper;
    private final StudentInfoMapper studentInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAppeal(ResultAppeal appeal, LoginUser loginUser) {
        if (loginUser == null || !UserTypeEnum.isStudent(loginUser.getUserType())) {
            throw new BusinessException("只有学生可以提交结果异议");
        }

        EvaluationResult result = evaluationResultMapper.selectById(appeal.getResultId());
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        StudentInfo student = studentInfoMapper.selectById(result.getStudentId());
        if (student == null || !loginUser.getUserId().equals(student.getUserId())) {
            throw new BusinessException("只能对本人的评定结果提交异议");
        }
        if (!ResultStatusEnum.PUBLICITY.getCode().equals(result.getResultStatus())) {
            throw new BusinessException("当前评定结果不在可申诉状态");
        }

        log.info("提交结果异议: userId={}, studentId={}, resultId={}",
                loginUser.getUserId(), result.getStudentId(), result.getId());

        appeal.setId(null);
        appeal.setResultId(result.getId());
        appeal.setBatchId(result.getBatchId());
        appeal.setStudentId(result.getStudentId());
        appeal.setStudentName(result.getStudentName());
        appeal.setAppealStatus(AppealStatusEnum.PENDING.getCode());
        return save(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAppeal(Long id, String handleOpinion, LoginUser loginUser) {
        log.info("处理结果异议: id={}, handlerId={}", id, loginUser == null ? null : loginUser.getUserId());

        ResultAppeal appeal = getById(id);
        if (appeal == null) {
            throw new BusinessException("异议不存在");
        }

        appeal.setAppealStatus(AppealStatusEnum.PROCESSED.getCode());
        appeal.setHandleOpinion(handleOpinion);
        appeal.setHandlerId(loginUser.getUserId());
        appeal.setHandlerName(loginUser.getRealName());
        appeal.setHandleTime(LocalDateTime.now());
        return updateById(appeal);
    }
}
