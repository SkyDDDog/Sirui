package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "PayVO", description = "预付款VO")
public class PayVO {

    @ApiModelProperty(value = "description", notes = "商品描述", example = "Image形象店-深圳腾大-QQ公仔")
    @NotBlank(message = "商品描述不能为空")
    String description;

    @ApiModelProperty(value = "total", notes = "订单总金额，单位为分", example = "100")
//    @NotBlank(message = "订单总价不能为空")
    Integer total;




}
