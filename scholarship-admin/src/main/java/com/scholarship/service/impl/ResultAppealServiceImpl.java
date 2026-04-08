package com.scholarship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ResultAppeal;
import com.scholarship.enums.AppealStatusEnum;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.ResultAppealMapper;
import com.scholarship.service.ResultAppealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 结果异议服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ResultAppealServiceImpl extends ServiceImpl<ResultAppealMapper, ResultAppeal> implements ResultAppealService {

    @Override
    @Transactional
    public boolean submitAppeal(ResultAppeal appeal) {
        log.info("提交异议，studentId={}, resultId={}", appeal.getStudentId(), appeal.getResultId());

        // 设置初始状态为待处理
        appeal.setAppealStatus(AppealStatusEnum.PENDING.getCode());

        return save(appeal);
    }

    @Override
    @Transactional
    public boolean handleAppeal(Long id, String handleResult) {
        log.info("处理异议，id={}", id);

        ResultAppeal appeal = getById(id);
        if (appeal == null) {
            throw new BusinessException("异议不存在");
        }

        appeal.setAppealStatus(AppealStatusEnum.PROCESSED.getCode());
        appeal.setHandleResult(handleResult);

        return updateById(appeal);
    }
}
