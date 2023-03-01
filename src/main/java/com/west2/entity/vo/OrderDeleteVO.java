package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "OrderDeleteVO", description = "OrderDeleteVO")
public class OrderDeleteVO {

    List<String> orderId;

}
