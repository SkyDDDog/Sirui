package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "AdminUserVO", description = "AdminUserVO")
public class AdminUserVO {

    @ApiModelProperty(value = "用户名", example = "testUser")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码" , example = "123456")
    @NotBlank(message = "用户密码不能为空")
    private String password;




}
