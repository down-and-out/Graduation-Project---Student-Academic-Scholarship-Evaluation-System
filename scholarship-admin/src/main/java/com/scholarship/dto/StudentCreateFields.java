package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生创建字段 DTO
 * 用于封装创建学生用户时的学生相关信息
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@Schema(description = "学生创建字段")
public class StudentCreateFields {

    /**
     * 从 UserCreateRequest 构造
     *
     * @param request 用户创建请求
     */
    public StudentCreateFields(UserCreateRequest request) {
        if (request == null) {
            return;
        }
        this.studentNo = request.getStudentNo();
        this.gender = request.getGender();
        this.idCard = request.getIdCard();
        this.enrollmentYear = request.getEnrollmentYear();
        this.educationLevel = request.getEducationLevel();
        this.trainingMode = request.getTrainingMode();
        this.nativePlace = request.getNativePlace();
        this.address = request.getAddress();
        this.status = request.getStatus();
    }

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 性别：0-女 1-男
     */
    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCard;

    /**
     * 入学年份
     */
    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    /**
     * 学历层次：1-硕士 2-博士
     */
    @Schema(description = "学历层次：1-硕士 2-博士")
    private Integer educationLevel;

    /**
     * 培养方式：1-全日制 2-非全日制
     */
    @Schema(description = "培养方式：1-全日制 2-非全日制")
    private Integer trainingMode;

    /**
     * 籍贯
     */
    @Schema(description = "籍贯")
    private String nativePlace;

    /**
     * 家庭住址
     */
    @Schema(description = "家庭住址")
    private String address;

    /**
     * 学籍状态：0-休学 1-在读 2-毕业 3-退学
     */
    @Schema(description = "学籍状态：0-休学 1-在读 2-毕业 3-退学")
    private Integer status;
}
