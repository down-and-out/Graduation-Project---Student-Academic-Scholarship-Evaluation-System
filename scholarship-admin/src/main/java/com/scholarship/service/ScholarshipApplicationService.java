package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.vo.ApplicationAchievementVO;
import com.scholarship.vo.ScholarshipApplicationDetailVO;

import java.util.List;

/**
 * 奖学金申请服务接口
 */
public interface ScholarshipApplicationService extends IService<ScholarshipApplication> {

    /**
     * 分页查询申请记录
     */
    IPage<ScholarshipApplication> pageApplications(Long current, Long size, Long batchId, Long studentId, Integer status);

    /**
     * 提交申请及关联成果
     */
    boolean submitApplication(ScholarshipApplicationSubmitRequest request, Long userId);

    /**
     * 获取申请详情及关联成果
     */
    ScholarshipApplicationDetailVO getDetailById(Long applicationId);

    /**
     * 获取当前学生可用于申请的已通过成果
     */
    List<ApplicationAchievementVO> listAvailableAchievements(Long userId);

    /**
     * 导师审核申请
     */
    boolean tutorReview(Long applicationId, String opinion, Long tutorId);
}
