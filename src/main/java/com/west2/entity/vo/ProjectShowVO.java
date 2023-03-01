package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "ProjectShowVO", description = "ProjectShowVO")
public class ProjectShowVO {

    @ApiModelProperty(value = "主键", example = "4107")
    private String id;

    @ApiModelProperty(value = "场地projectId", example = "4107")
    private String projectId;

    @ApiModelProperty(value = "场地名称", example = "毕节南山云端足球公园-8人场")
    private String name;

    @ApiModelProperty(value = "设备状态")
    private String status;

    @ApiModelProperty(value = "场地直播价格(分/0.5h)")
    private String price;

    @ApiModelProperty(value = "上一次修改时间")
    private String lastUpdate;

    @ApiModelProperty(value = "是否使用overlay")
    private String overlayFlag;

//    @ApiModelProperty(value = "新建场地人id")
//    private String createBy;


}
