package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导师端学生列表视图对象
 */
@Data
@Schema(description = "导师端学生列表")
public class TutorStudentVO {

    @Schema(description = "学生ID")
    private Long id;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "年级展示")
    private String grade;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "研究方向")
    private String direction;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "论文数量")
    private Integer paperCount;

    @Schema(description = "专利数量")
    private Integer patentCount;

    @Schema(description = "项目数量")
    private Integer projectCount;

    @Schema(description = "竞赛数量")
    private Integer competitionCount;
}
