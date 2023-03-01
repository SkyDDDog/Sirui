package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "UserVO", description = "UserVO")
public class UserVO {

    @ApiModelProperty(value = "username", example = "二火")
    private String username;

    @ApiModelProperty(value = "用户头像Url")
    private String avatar;



}
