package com.west2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.common.CommonResult;
import com.west2.common.MsgCodeUtil;
import com.west2.entity.Order;
import com.west2.entity.Project;
import com.west2.entity.vo.PlayingListVO;
import com.west2.entity.vo.ProjectShowVO;
import com.west2.service.OrderService;
import com.west2.service.ProjectService;
import com.west2.utils.DateTimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/project")
@Api(value = "ProjectController", tags = "场地接口")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "获取单个场地信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public CommonResult select(@PathVariable String id) {
        CommonResult result = new CommonResult().init();

        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
        } else {
            ProjectShowVO project = projectService.getProjectShowById(id);
            result.success("project", project);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取多条场地信息")
    @RequestMapping(value = {"", "list"}, method = RequestMethod.GET)
    public CommonResult selectList() {
        CommonResult result = new CommonResult().init();
//        List<Project> list = projectService.findList(new QueryWrapper<>());
        List<ProjectShowVO> list = projectService.getProjects();
        result.success("project", list);
        result.putItem("total", list.size());
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "模糊查询场地名称")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "content",
                    value = "查询词",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "search/{content}", method = RequestMethod.GET)
    public CommonResult searchList(@PathVariable String content) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        wrapper.like("name", content);
        List<ProjectShowVO> list = projectService.getProjectList(wrapper);
        result.success("project", list);
        result.putItem("total", list.size());
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "直播列表")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "pageNum",
                    value = "页数(1开始)",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "1"
            ),
            @ApiImplicitParam(
                    name = "pageSize",
                    value = "一页数据条数",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "5"

            )
    })
    @RequestMapping(value = "playList", method = RequestMethod.GET)
    public CommonResult getPlayingList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.isNull("pre_order_id");
        wrapper.ge("stop_time", DateTimeUtil.nowTimeStamp())
                .eq("pay_flag", Order.PAYED)
                .orderByAsc("start_time")
                .last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
        List<Order> orderList = orderService.findList(wrapper);
        List<PlayingListVO> list = orderService.order2PlayingVO(orderList);
        result.success("playList", list);
        result.putItem("total", list.size());
        return (CommonResult) result.end();
    }

}
