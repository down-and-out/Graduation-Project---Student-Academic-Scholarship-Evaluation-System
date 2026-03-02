package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ScholarshipApplication;

/**
 * 奖学金申请服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface ScholarshipApplicationService extends IService<ScholarshipApplication> {

    /**
     * 分页查询申请记录
     *
     * @param current   当前页
     * @param size      每页大小
     * @param batchId   批次ID
     * @param studentId 学生ID
     * @param status    状态
     * @return 分页结果
     */
    IPage<ScholarshipApplication> pageApplications(Long current, Long size, Long batchId, Long studentId, Integer status);

    /**
     * 提交申请
     *
     * @param application 申请信息
     * @param studentId 学生 ID
     * @return 是否成功
     */
    boolean submitApplication(ScholarshipApplication application, Long studentId);

    /**
     * 审核申请（导师审核）
     *
     * @param applicationId 申请ID
     * @param opinion       审核意见
     * @param tutorId       导师ID
     * @return 是否成功
     */
    boolean tutorReview(Long applicationId, String opinion, Long tutorId);
}
