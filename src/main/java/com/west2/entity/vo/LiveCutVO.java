package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
@ApiModel(value = "LiveCutVO", description = "LiveCutVO")
public class LiveCutVO {

    @ApiModelProperty(value = "订单id", example = "1111111111111111111")
    @NotBlank(message = "订单id不能为空")
    private String orderId;

    @ApiModelProperty(value = "类型(1-A队精彩片段, 2-B队精彩片段, 3-A队进球, 4-B队进球)" , example = "1")
    private Integer type;

    @ApiModelProperty(value = "视频标题" , example = "标题捏")
    @NotBlank(message = "视频标题不能为空")
    private String title;

}
