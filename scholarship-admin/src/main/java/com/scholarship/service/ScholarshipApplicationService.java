package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.vo.ApplicationAchievementVO;
import com.scholarship.vo.ScholarshipApplicationDetailVO;

import java.util.List;

/**
 * 奖学金申请服务接口。
 */
public interface ScholarshipApplicationService extends IService<ScholarshipApplication> {

    IPage<ScholarshipApplication> pageApplications(Long current, Long size, Long batchId, Long studentId, Integer status);

    boolean submitApplication(ScholarshipApplicationSubmitRequest request, Long userId);

    ScholarshipApplicationDetailVO getDetailById(Long applicationId);

    List<ApplicationAchievementVO> listAvailableAchievements(Long userId);

    boolean reviewApplication(Long applicationId, String opinion, Long reviewerId, String reviewerName, Integer reviewerUserType);
}
