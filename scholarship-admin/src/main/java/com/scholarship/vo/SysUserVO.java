package com.scholarship.vo;

import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 * <p>
 * 返回给前端的用户信息，排除敏感字段：
 * - password（密码）
 * - deleted（逻辑删除标记）
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息VO")
public class SysUserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "院系/部门")
    private String department;

    @Schema(description = "用户类型：1-研究生 2-导师 3-管理员")
    private Integer userType;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    /**
     * 从 Entity 转换为 VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    public static SysUserVO fromEntity(SysUser user) {
        return fromEntity(user, null);
    }

    public static SysUserVO fromEntity(SysUser user, StudentInfo studentInfo) {
        if (user == null) {
            return null;
        }

        String realName = user.getRealName();
        String department = user.getDepartment();
        String email = user.getEmail();
        String phone = user.getPhone();

        if (studentInfo != null) {
            if (studentInfo.getName() != null && !studentInfo.getName().isBlank()) {
                realName = studentInfo.getName();
            }
            if (studentInfo.getDepartment() != null && !studentInfo.getDepartment().isBlank()) {
                department = studentInfo.getDepartment();
            }
            if (studentInfo.getEmail() != null && !studentInfo.getEmail().isBlank()) {
                email = studentInfo.getEmail();
            }
            if (studentInfo.getPhone() != null && !studentInfo.getPhone().isBlank()) {
                phone = studentInfo.getPhone();
            }
        }

        return SysUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(realName)
                .department(department)
                .userType(user.getUserType())
                .email(email)
                .phone(phone)
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .remark(user.getRemark())
                .build();
    }
}
