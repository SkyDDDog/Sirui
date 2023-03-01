package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "OrderVO", description = "OrderVO")
public class OrderVO {

    @ApiModelProperty(value = "用户openid", example = "odHM15IeekiXGyC303dmpZ9RbM6o")
    @NotBlank(message = "下单用户id不能为空")
    private String openid;

    @ApiModelProperty(value = "场地Id" , example = "4103")
    @NotBlank(message = "场地Id不能为空")
    private String projectId;

    @ApiModelProperty(value = "赛事名称")
    private String gameName;

    @ApiModelProperty(value = "队伍A名称", example = "利物浦")
    @NotNull(message = "队伍A名称不能为空")
    private String teamA;

    @ApiModelProperty(value = "队伍B名称", example = "曼联")
    @NotNull(message = "队伍B名称不能为空")
    private String teamB;

    @ApiModelProperty(value = "开始时间时间戳(秒)")
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @ApiModelProperty(value = "开始时间时间戳(秒)")
    @NotNull(message = "结束时间不能为空")
    private String stopTime;

}
