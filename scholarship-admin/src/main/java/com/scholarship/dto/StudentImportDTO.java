package com.scholarship.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生信息导入 DTO
 * <p>
 * 用于 Excel 批量导入学生信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@ColumnWidth(15)
public class StudentImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别(1-男/0-女)")
    private Integer gender;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("入学年份")
    private Integer enrollmentYear;

    @ExcelProperty("学历层次(1-硕士/2-博士)")
    private Integer educationLevel;

    @ExcelProperty("培养方式(1-全日制/2-非全日制)")
    private Integer trainingMode;

    @ExcelProperty("院系")
    private String department;

    @ExcelProperty("专业")
    private String major;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("导师ID")
    private Long tutorId;

    @ExcelProperty("研究方向")
    private String direction;

    @ExcelProperty("政治面貌")
    private String politicalStatus;

    @ExcelProperty("民族")
    private String nation;

    @ExcelProperty("籍贯")
    private String nativePlace;

    @ExcelProperty("家庭住址")
    private String address;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("电子邮箱")
    private String email;
}
