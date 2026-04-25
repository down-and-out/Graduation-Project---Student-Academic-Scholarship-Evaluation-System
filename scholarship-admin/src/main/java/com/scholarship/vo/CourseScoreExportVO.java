package com.scholarship.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.scholarship.entity.CourseScore;
import lombok.Data;

/**
 * 课程成绩导出视图对象
 */
@Data
public class CourseScoreExportVO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("课程代码")
    private String courseCode;

    @ExcelProperty("课程性质")
    private String courseType;

    @ExcelProperty("学分")
    private String credit;

    @ExcelProperty("成绩")
    private String score;

    @ExcelProperty("绩点")
    private String gpa;

    @ExcelProperty("学年")
    private String academicYear;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("备注")
    private String remark;

    public static CourseScoreExportVO from(CourseScore source) {
        CourseScoreExportVO vo = new CourseScoreExportVO();
        vo.setStudentNo(source.getStudentNo());
        vo.setStudentName(source.getStudentName());
        vo.setCourseName(source.getCourseName());
        vo.setCourseCode(source.getCourseCode());
        vo.setCourseType(formatCourseType(source.getCourseType()));
        vo.setCredit(formatDecimal(source.getCredit()));
        vo.setScore(resolveScoreDisplay(source));
        vo.setGpa(formatDecimal(source.getGpa()));
        vo.setAcademicYear(source.getAcademicYear());
        vo.setSemester(formatSemester(source.getSemester()));
        vo.setRemark(source.getRemark());
        return vo;
    }

    private static String resolveScoreDisplay(CourseScore source) {
        if (source.getScoreText() != null && !source.getScoreText().isBlank()) {
            return source.getScoreText();
        }
        return formatDecimal(source.getScore());
    }

    private static String formatDecimal(java.math.BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String formatSemester(Integer semester) {
        if (semester == null) {
            return "";
        }
        return switch (semester) {
            case 1 -> "第一学期";
            case 2 -> "第二学期";
            case 3 -> "夏季学期";
            default -> String.valueOf(semester);
        };
    }

    private static String formatCourseType(Integer courseType) {
        if (courseType == null) {
            return "";
        }
        return switch (courseType) {
            case 1 -> "必修";
            case 2 -> "选修";
            case 3 -> "任选";
            default -> String.valueOf(courseType);
        };
    }
}
