package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "OrderVO", description = "OrderVO")
public class OrderUpdVO {

    @ApiModelProperty(value = "用户openid")
    private String openid;

    @ApiModelProperty(value = "队伍A名称", example = "利物浦")
    private String teamA;

    @ApiModelProperty(value = "队伍B名称", example = "曼联")
    private String teamB;

    @ApiModelProperty(value = "开始时间", example = "1669634676")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "1669635676")
    private String stopTime;

}
