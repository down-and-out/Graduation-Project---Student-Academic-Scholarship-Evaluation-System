package com.scholarship.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 评定结果导出 VO
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@ColumnWidth(15)
@HeadRowHeight(20)
public class EvaluationResultExportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("序号")
    @ColumnWidth(10)
    private Integer index;

    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("院系")
    private String department;

    @ExcelProperty("专业")
    private String major;

    @ExcelProperty("课程成绩")
    @ColumnWidth(12)
    private BigDecimal courseScore;

    @ExcelProperty("科研成果")
    @ColumnWidth(12)
    private BigDecimal researchScore;

    @ExcelProperty("竞赛获奖")
    @ColumnWidth(12)
    private BigDecimal competitionScore;

    @ExcelProperty("综合素质")
    @ColumnWidth(12)
    private BigDecimal qualityScore;

    @ExcelProperty("总分")
    @ColumnWidth(12)
    private BigDecimal totalScore;

    @ExcelProperty("院系排名")
    @ColumnWidth(10)
    private Integer departmentRank;

    @ExcelProperty("专业排名")
    @ColumnWidth(10)
    private Integer majorRank;

    @ExcelProperty("获奖等级")
    @ColumnWidth(12)
    private String awardLevelName;

    @ExcelProperty("奖学金金额")
    @ColumnWidth(15)
    private BigDecimal awardAmount;

    @ExcelProperty("结果状态")
    @ColumnWidth(12)
    private String resultStatusName;
}
