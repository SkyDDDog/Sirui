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

/**
 * @desc 用户实体类
 * @date 2022/11/26
 */
@Data
@Accessors(chain = true)
@TableName("`user`")
@ApiModel(value = "User", description = "User")
public class User extends DataEntity<User> {

    @ApiModelProperty(value = "开放平台用户唯一标识")
    private String unionid;

    @ApiModelProperty(value = "会话密钥")
    private String sessionKey;

    @ApiModelProperty(value = "用户昵称")
    private String username;

    @ApiModelProperty(value = "用户头像")
    private String avatar;


    @Override
    public String toString() {
        return "User{" +
                "unionid='" + unionid + '\'' +
                ", sessionKey='" + sessionKey + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
