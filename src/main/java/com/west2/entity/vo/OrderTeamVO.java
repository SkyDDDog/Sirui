package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("OrderTeamVO")
public class OrderTeamVO {

    @ApiModelProperty(value = "订单id")
    @NotBlank(message = "订单id不能为空")
    private String orderId;

    @ApiModelProperty(value = "主队名称", example = "host")
    private String teamA;
    @ApiModelProperty(value = "客队名称", example = "quest")
    private String teamB;

}
