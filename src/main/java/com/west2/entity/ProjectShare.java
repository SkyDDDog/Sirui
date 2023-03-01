package com.west2.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @desc 场地共享表
 * @date 2023/1/3
 */
@Data
@Accessors(chain = true)
@TableName("`project_share`")
@ApiModel(value = "ProjectShare", description = "ProjectShare")
public class ProjectShare extends DataEntity<ProjectShare> {

    @ApiModelProperty(value = "project_id")
    private String projectId;

    @ApiModelProperty(value = "user_id")
    private String userId;

}
