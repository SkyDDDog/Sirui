package com.west2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.west2.common.CommonResult;
import com.west2.common.MsgCodeUtil;
import com.west2.entity.AdminUser;
import com.west2.entity.Order;
import com.west2.entity.Project;
import com.west2.entity.ProjectShare;
import com.west2.entity.base.BaseEntity;
import com.west2.entity.vo.*;
import com.west2.service.AdminUserService;
import com.west2.service.OrderService;
import com.west2.service.ProjectService;
import com.west2.service.ProjectShareService;
import com.west2.utils.BeanCustomUtil;
import com.west2.utils.JwtUtil;
import com.west2.utils.RedisUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/admin")
@Api(value = "AdminController", tags = "后台管理接口")
public class AdminController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectShareService projectShareService;
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "新增场地", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "project", method = RequestMethod.POST)
    public CommonResult createProject(@Validated @RequestBody @ApiParam("场地创建vo") ProjectVO vo,
                                      BindingResult bindingResult, @RequestHeader HttpHeaders headers) {
        CommonResult result = (new CommonResult()).init();
        String userId = jwtUtil.getUserIdFromToken(headers);
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        Project project = projectService.getProjectById(vo.getId());

        // 管理员才可以创建场地
        if (jwtUtil.isAdmin(userId)) {
            if (project!=null) {
                result.failCustom(MsgCodeUtil.MSG_CODE_DATA_EXIST,"projectId已存在");
            } else {
                project = new Project();
                BeanCustomUtil.copyProperties(vo, project);
                project.setId(IdWorker.getId(project)+"");
                project.setProjectId(vo.getId());
                project.setOverlayFlag(vo.getOverFlag()+"");
                project.setNewRecord(true);

                if (0 < projectService.save(project)) {
                    result.success("project", project);
                    log.info("创建新场地: {}成功！", vo.toString());
                } else{
                    result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
                    log.info("创建新场地: {}失败！", vo.toString());
                }
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }

        return (CommonResult) result.end();
    }


    @ApiOperation("共享场地给子账户")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "projectId",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "子用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "project/share/{projectId}/{userId}", method = RequestMethod.POST)
    public CommonResult shareProject(@PathVariable String projectId,@PathVariable String userId,
                                     @RequestHeader HttpHeaders headers) {
        CommonResult result = (new CommonResult()).init();
        // 管理员才可以共享场地给其他人
        if (jwtUtil.isAdmin(headers)) {
            QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", projectId)
                    .eq("user_id", userId);
            List<ProjectShare> list = projectShareService.findAllList(wrapper);
            if (0 < list.size()) {
                ProjectShare projectShare = list.get(0);
                // 是否被逻辑删除
                if (BaseEntity.DEL_FLAG_DELETE.equals(projectShare.getDelFlag())) {
                    // 恢复被删除数据
                    projectShare.setDelFlag(BaseEntity.DEL_FLAG_NORMAL);
                    if (0 < projectShareService.save(projectShare)) {
                        log.info("场地共享: {}成功", projectShare);
                        result.success();
                    } else {
                        log.info("场地共享: {}失败", projectShare);
                        result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                    }
                } else {
                    result.failCustom("该子账户已经共享该场地");
                }
            } else {
                ProjectShare projectShare = new ProjectShare();
                projectShare.setProjectId(projectId)
                        .setUserId(userId);
                if (0 < projectShareService.save(projectShare)) {
                    log.info("场地共享: {}成功", projectShare);
                    result.success();
                } else {
                    log.info("场地共享: {}失败", projectShare);
                    result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                }
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation("取消共享场地给子账户")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "projectId",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "子用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "project/share/{projectId}/{userId}", method = RequestMethod.DELETE)
    public CommonResult unshareProject(@PathVariable String projectId,@PathVariable String userId) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
                .eq("user_id", userId);
        List<ProjectShare> list = projectShareService.findList(wrapper);
        if (0 < list.size()) {
            ProjectShare projectShare = list.get(0);
            if (0 < projectShareService.delete(projectShare)) {
                log.info("取消场地共享: {}成功", projectShare);
                result.success();
            } else {
                log.info("取消场地共享: {}失败", projectShare);
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            }
        } else {
            result.failCustom("该子用户未被共享该场地");
        }

        return (CommonResult) result.end();
    }



    @ApiOperation(value = "更新场地信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "id",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "name",
                    value = "新场地名称",
                    paramType = "query",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "project/{id}", method = RequestMethod.PUT)
    public CommonResult updateProject(@PathVariable String id, @RequestParam String name,
                                      @RequestHeader HttpHeaders headers) {
        CommonResult result = (new CommonResult()).init();
        String userId = jwtUtil.getUserIdFromToken(headers);
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        if (jwtUtil.isAdmin(headers) || !projectService.isOwner(userId,id)) {
            result.failCustom(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION, "您不是场地主人");
        }
        Project project = projectService.getProjectById(id);
        if (StringUtils.isNotBlank(name)) {
            project.setName(name);
        }
        if (0 < projectService.save(project)) {
            result.success("project",project);
            log.info("更新场地信息：{}成功！",project.toString());
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("更新场地信息：{}失败！",project.toString());
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "删除场地信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "id",
                    value = "场地projectId",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "project/{id}", method = RequestMethod.DELETE)
    public CommonResult deleteProject(@PathVariable String id,
                                      @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        String userId = jwtUtil.getUserIdFromToken(headers);
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        if (jwtUtil.isAdmin(headers) || !projectService.isOwner(userId,id)) {
            result.failCustom(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION, "您不是场地主人");
        }
        Project project = projectService.getProjectById(id);
        if (0 < projectService.delete(project)) {
            QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id",project.getProjectId());
            List<ProjectShare> shareList = projectShareService.findList(wrapper);
            for (ProjectShare projectShare : shareList) {
                projectShareService.delete(projectShare);
            }
            result.success();
            log.info("删除场地信息：{}成功！", id);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("删除场地信息：{}失败！", id);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取单条场地信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "id",
                    value = "订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "project/{id}", method = RequestMethod.GET)
    public CommonResult select(@PathVariable String id,
                               @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
        } else {
            Project project = projectService.getProjectById(id);
            result.success("project",project);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取多条场地信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
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
    @RequestMapping(value = {"project", "project/list"}, method = RequestMethod.GET)
    public CommonResult selectList(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                   @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        int total = 0;
        String userId = jwtUtil.getUserIdFromToken(headers);
        if (jwtUtil.isAdmin(headers)) {
            QueryWrapper<Project> wrapper = new QueryWrapper<>();
            total = projectService.findList(wrapper).size();
            List<ProjectShareVO> list = new ArrayList<>();
            wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
            List<Project> projectList = projectService.findList(wrapper);
            for (Project project : projectList) {
                QueryWrapper<ProjectShare> shareWrapper = new QueryWrapper<>();
                shareWrapper.eq("project_id", project.getProjectId());
                List<ProjectShare> shareList = projectShareService.findList(shareWrapper);
                ArrayList<AdminUser> userList = new ArrayList<>(shareList.size());
                for (ProjectShare share : shareList) {
                    AdminUser user = adminUserService.get(share.getUserId());
                    userList.add(user);
                }
                ProjectShareVO projectShareVO = new ProjectShareVO();
                BeanUtils.copyProperties(project, projectShareVO);
                projectShareVO.setShareUser(userList);
                list.add(projectShareVO);
            }
            result.putItem("project", list);
        } else {
            QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            total = projectShareService.findList(wrapper).size();
            wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
            List<ProjectShare> shareList = projectShareService.findList(wrapper);
            ArrayList<ProjectShowVO> list = new ArrayList<>(pageSize);
            for (ProjectShare projectShare : shareList) {
                ProjectShowVO project = projectService.getProjectShowById(projectShare.getProjectId());
                list.add(project);
            }
            result.putItem("project", list);
        }


        result.success("total", total);
        return (CommonResult) result.end();
    }

    @ApiOperation("子用户id获取场地列表")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "子用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
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
    @RequestMapping(value = {"project/subuser/{userId}"}, method = RequestMethod.GET)
    public CommonResult selectList(@PathVariable String userId, @RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                   @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (jwtUtil.isAdmin(headers)) {
            QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            int total = projectShareService.findList(wrapper).size();
            wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
            List<ProjectShare> shareList = projectShareService.findList(wrapper);
            ArrayList<ProjectShowVO> list = new ArrayList<>(pageSize);
            for (ProjectShare projectShare : shareList) {
                ProjectShowVO project = projectService.getProjectShowById(projectShare.getProjectId());
                list.add(project);
            }
            result.putItem("project", list);
            result.success("total", total);
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION, "您不是管理员");
        }



        return (CommonResult) result.end();
    }


    @ApiOperation(value = "创建子账户")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "user/register", method = RequestMethod.POST)
    public CommonResult createUser(@Validated @RequestBody @ApiParam("新建用户vo") AdminUserVO vo,
                                   BindingResult bindingResult ,@RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        AdminUser user = adminUserService.getByLoginUsername(vo.getUsername());
        if (jwtUtil.isAdmin(headers)) {
            if (user!=null) {
                result.failCustom(MsgCodeUtil.MSG_CODE_USERNAME_OR_PASSWORD_INCORRECT, "用户名已存在");
            } else {
                user = new AdminUser();
                BeanCustomUtil.copyProperties(vo, user);
                user.setPassword(AdminUserService.entryptPassword(vo.getPassword()))
                        .setRole(AdminUser.ROLE_SUBACCOUNT);
                if (0 < adminUserService.save(user)) {
                    result.success("user", user);
                    log.info("创建子用户: {}成功！", vo.toString());
                } else{
                    result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
                    log.info("创建子用户: {}失败！", vo.toString());
                }
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "子用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
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
    @RequestMapping(value = "user/sub", method = RequestMethod.GET)
    public CommonResult subUserList(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                    @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (jwtUtil.isAdmin(headers)) {
            QueryWrapper<AdminUser> wrapper = new QueryWrapper<>();
            wrapper.eq("role", AdminUser.ROLE_SUBACCOUNT);
            int total = adminUserService.findList(wrapper).size();
            List<AdminUserProjectVO> list = adminUserService.getSubUserProject(pageNum, pageSize);
            result.success("user" ,list);
            result.putItem("total", total);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "删除子用户")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "id",
                    value = "子用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "user/sub/{id}", method = RequestMethod.DELETE)
    public CommonResult deleteSubUser(@PathVariable String id,
                                      @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        if (jwtUtil.isAdmin(headers)) {
            AdminUser subUser = adminUserService.get(id);
            if (0 < adminUserService.delete(subUser)) {
                result.success();
                log.info("删除子用户信息：{}成功！", id);
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                log.info("删除子用户信息：{}失败！", id);
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改子用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "id",
                    value = "子用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "user/sub/{id}", method = RequestMethod.PUT)
    public CommonResult updateSubUser(@PathVariable String id ,@Validated @RequestBody @ApiParam("修改用户vo") AdminUserVO vo,
                                      BindingResult bindingResult, @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (jwtUtil.isAdmin(headers)) {
            AdminUser subUser = adminUserService.get(id);
            BeanCustomUtil.copyProperties(vo, subUser);
            subUser.setPassword(AdminUserService.entryptPassword(vo.getPassword()));
            if (0 < adminUserService.save(subUser)) {
                result.success();
                log.info("修改子用户信息：{}成功！", id);
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                log.info("修改子用户信息：{}失败！", id);
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "登录")
    @RequestMapping(value = "user/login", method = RequestMethod.POST)
    public CommonResult loginUser(@RequestBody @ApiParam("用户vo") AdminUserVO vo) {
        CommonResult result = new CommonResult().init();
        AdminUser user = adminUserService.getByLoginUsername(vo.getUsername());
        if (user!=null && AdminUserService.validatePassword(vo.getPassword(), user.getPassword())) {
            result.success("user", user);
            String token = jwtUtil.createToken(user.getId(), user.getUsername());
            result.putItem("token", token);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_USERNAME_OR_PASSWORD_INCORRECT);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "更新订单信息(vo信息选填)")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "order/{id}", method = RequestMethod.PUT)
    public CommonResult updateOrder(@PathVariable String id, @Validated @RequestBody @ApiParam("订单修改vo") OrderUpdVO vo,
                               BindingResult bindingResult, @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        String userId = jwtUtil.getUserIdFromToken(headers);
        Order order = orderService.get(id);
        BeanCustomUtil.copyProperties(vo, order);
        if (jwtUtil.isAdmin(userId) || projectShareService.havePermission(userId, order.getProjectId())) {
            if (0 < orderService.save(order) && orderService.savePushStream(order).getJSONObject("error").getIntValue("code")==0) {
                result.success("order",order);
                log.info("后台修改订单信息：{}成功！",vo.toString());
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                log.info("后台修改订单信息：{}失败！",vo.toString());
            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
            log.info("后台修改订单信息失败: 权限不足");
        }


        return (CommonResult) result.end();
    }

    @ApiOperation(value = "场地订单记录")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
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
    @RequestMapping(value = "order", method = RequestMethod.GET)
    public CommonResult getOrderList(@RequestHeader HttpHeaders headers, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        String userId = jwtUtil.getUserIdFromToken(headers);
        CommonResult result = new CommonResult().init();
        List<Order> orderList;

        int total = 0;
        if (jwtUtil.isAdmin(headers)) {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.isNull("pre_order_id");
            orderWrapper.orderByDesc("update_date");
            orderWrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
            orderList = orderService.findList(orderWrapper);
            total = orderService.findList(new QueryWrapper<>()).size();
        } else {
            orderList = new ArrayList<>();
//            QueryWrapper<Project> projectWrapper = new QueryWrapper<>();
//            projectWrapper.eq("create_by", userId);
//            List<Project> projectList = projectService.findList(projectWrapper);
            QueryWrapper<ProjectShare> projectWrapper = new QueryWrapper<>();
            projectWrapper.eq("user_id", userId);
            List<ProjectShare> projectList = projectShareService.findList(projectWrapper);
            for (ProjectShare project : projectList) {
                QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
                orderWrapper.isNull("pre_order_id");
                orderWrapper.orderByDesc("update_date");
                orderWrapper.eq("project_id",project.getProjectId());
                List<Order> list = orderService.findList(orderWrapper);
//                orderList.addAll(list);
                total += list.size();
            }
            orderList = orderService.getByProjectOwner(userId,pageNum,pageSize);
        }
        List<OrderShowVO> vo = orderService.order2ShowVO(orderList, null);
        result.success("order", vo);
        result.putItem("total", total);

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改价格(每半小时)")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "projectId",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "price",
                    value = "价格(分/0.5h)",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "price/{projectId}/{price}", method = RequestMethod.POST)
    public CommonResult updatePrice(@PathVariable String projectId, @PathVariable String price,
                                    @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (jwtUtil.isAdmin(headers)) {
            Project project = projectService.getProjectById(projectId);
            project.setPrice(price);
            if (0 < projectService.save(project)) {
                log.info("修改场地[{}]价格{} 分/0.5h: 成功", project, price);
                result.success();
            } else {
                log.info("修改场地[{}]价格{} 分/0.5h: 失败", project, price);
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            }
//            redisUtil.setPrice(price);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改场地类型")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "用户访问token",
                    paramType = "header",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "projectId",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "overlay",
                    value = "0-默认,1-足球,2-篮球",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "overlay/{projectId}/{overlay}", method = RequestMethod.POST)
    public CommonResult updateOverlay(@PathVariable String projectId, @PathVariable Integer overlay,
                                    @RequestHeader HttpHeaders headers) {
        CommonResult result = new CommonResult().init();
        if (overlay < 0 || 2 < overlay) {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "overlay参数错误").end();
        }
        if (jwtUtil.isAdmin(headers)) {
            Project project = projectService.getProjectById(projectId);
            project.setOverlayFlag(overlay+"");
            if (0 < projectService.save(project)) {
                log.info("修改场地计分板类型{} : 成功", project);
                result.success();
            } else {
                log.info("修改场地计分板类型{} : 失败", project);
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            }
//            redisUtil.setPrice(price);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_SYSTEM_NOT_SUPER_ADMIN_PERMISSION);
        }

        return (CommonResult) result.end();
    }

//    @ApiOperation(value = "获取当前价格(每半小时)")
//    @ApiImplicitParams({
//            @ApiImplicitParam(
//                    name = "token",
//                    value = "用户访问token",
//                    paramType = "header",
//                    dataType = "string",
//                    required = true),
//    })
//    @RequestMapping(value = "price", method = RequestMethod.GET)
//    public CommonResult getPrice() {
//        CommonResult result = new CommonResult().init();
//        String price = redisUtil.getPrice();
//        result.success("price", price);
//        return (CommonResult) result.end();
//    }


}
