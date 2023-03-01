package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "ProjectVO", description = "ProjectVO")
public class ProjectVO {

    @ApiModelProperty(value = "场地projectId", example = "114514")
    @NotBlank(message = "场地projectId不能为空")
    private String id;


    @ApiModelProperty(value = "场地名", example = "福州大学第一田径场")
    @NotBlank(message = "场地名不能为空")
    private String name;

    @ApiModelProperty(value = "场地直播价格(分/0.5h)", example = "2500")
//    @NotBlank(message = "场地直播价格不能为空")
    private String price;

    @ApiModelProperty(value = "计分板类型 (0-默认,1-足球,2-篮球)", example = "0")
    @Max(value = 2, message = "计分板类型错误")
    @Min(value = 0, message = "计分板类型错误")
    private Integer overFlag;


}
