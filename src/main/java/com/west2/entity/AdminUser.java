package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("`admin_user`")
@ApiModel(value = "AdminUser", description = "AdminUser")
public class AdminUser extends DataEntity<AdminUser> {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty("用户权限角色")
    private String role;

    @TableField(exist = false)
    public static String ROLE_SUBACCOUNT = "ROLE_SUB";
    @TableField(exist = false)
    public static String ROLE_ADMIN = "ROLE_ADMIN";

    @Override
    public String toString() {
        return "AdminUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
