package com.west2.entity.vo;

import com.west2.entity.AdminUser;
import com.west2.entity.Project;
import com.west2.entity.ProjectShare;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "ProjectShareVO", description = "ProjectShareVO")
public class ProjectShareVO extends Project {

    @ApiModelProperty(value = "场地projectId", example = "4107")
    private List<AdminUser> shareUser;

}
