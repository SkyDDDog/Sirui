package com.west2.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.common.CommonResult;
import com.west2.entity.*;
import com.west2.entity.base.BaseEntity;
import com.west2.entity.vo.OrderReplayVO;
import com.west2.entity.vo.OrderShowVO;
import com.west2.entity.vo.UserVO;
import com.west2.service.*;
import com.west2.utils.BeanCustomUtil;
import com.west2.common.MsgCodeUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/user")
@Api(value = "UserController", tags = "用户接口")
public class UserController {

    @Autowired
    private WXService wxService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCollectionService userCollectionService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderReplayService orderReplayService;
    @Autowired
    private ReplayCollectionService replayCollectionService;

    @ApiOperation(value = "小程序登录认证", notes = "登录凭证校验。通过 wx.login 接口获得临时登录凭证 code 后传到开发者服务器调用此接口完成登录流程。")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "jsCode",
                    value = "登录时获取的code",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "/login/{jsCode}", method = RequestMethod.POST)
    public CommonResult auth(@PathVariable String jsCode) {
        CommonResult result = new CommonResult().init();
        JSONObject json = wxService.code2Session(jsCode);
        log.info(jsCode);
        log.info(json.toJSONString());
        String openid = (String) json.get("openid");
        String unionid = (String) json.get("unionid");
        String session_key = (String) json.get("session_key");
        // 查询库中是已有记录
        User user = userService.get(openid);
        // 无记录
        if (user==null) {
            // 插入新纪录
            user = new User();
            user.setUnionid(unionid)
                    .setSessionKey(session_key)
                    .setNewRecord(true);
            user.setId(openid);
            if (0 < userService.save(user)) {
                result.putItem("firstLogin", true);
                log.info("新用户注册成功:{}",user);
            } else {
                log.error("新用户注册失败:{}",user);
            }
        } else {
            result.putItem("firstLogin", false);
            log.info("老用户登录成功:{}",user);
        }
        result.success("user",user);

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "更新用户信息|初次登录调用wx.getUserProfile获取用户基本信息后端存储")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public CommonResult update(@PathVariable String id, @Validated @RequestBody @ApiParam("用户vo") UserVO vo, BindingResult bindingResult) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        User user = userService.get(id);
        BeanCustomUtil.copyProperties(vo, user);
        if (0 < userService.save(user)) {
            result.success("user",user);
            log.info("更新用户信息：{}成功！",vo.toString());
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("更新用户信息：{}失败！",vo.toString());
        }
        return (CommonResult) result.end();
    }


    @ApiOperation(value = "删除用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "用户id",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public CommonResult delete(@PathVariable String id) {
        CommonResult result = new CommonResult().init();
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        User user = userService.get(id);
        if (0 < userService.delete(user)) {
            result.success();
            log.info("删除用户信息：{}成功！", id);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("删除用户信息：{}失败！", id);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取单条用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "用户id",
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
            User user = userService.get(id);
            result.success("user",user);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取多条用户信息")
    @RequestMapping(value = {"", "list"}, method = RequestMethod.GET)
    public CommonResult selectList() {
        CommonResult result = new CommonResult().init();
        List<User> list = userService.findList(new QueryWrapper<>());
        result.success("user",list);
        result.putItem("total",list.size());
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "收藏订单(回放)")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openid",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true
            )
    })
    @RequestMapping(value = "collect/{userId}/{orderId}", method = RequestMethod.POST)
    public CommonResult collectOrder(@PathVariable String userId, @PathVariable String orderId) {
        CommonResult result = new CommonResult().init();
        UserCollection userCollection = null;
        QueryWrapper<UserCollection> ucWrapper = new QueryWrapper<>();
        ucWrapper.eq("openid", userId)
                .eq("orderid", orderId);
        List<UserCollection> list = userCollectionService.findAllList(ucWrapper);
        if (list.size()==1 && BaseEntity.DEL_FLAG_NORMAL.equals(list.get(0).getDelFlag())) {
            return (CommonResult) result.failCustom("该订单已收藏");
        } else if (list.size()==1 && BaseEntity.DEL_FLAG_DELETE.equals(list.get(0).getDelFlag())) {
            userCollection = list.get(0);
            userCollection.setDelFlag(BaseEntity.DEL_FLAG_NORMAL);
        } else {
            userCollection = new UserCollection();
            userCollection.setOpenid(userId)
                    .setOrderid(orderId);
        }

        if (0 < userCollectionService.save(userCollection)) {
            result.success("userCollection", userCollection);
            log.info("创建新收藏: {}成功！", userCollection.toString());
        } else{
            result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("创建新收藏: {}失败！", userCollection.toString());
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "取消收藏订单(回放)")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openid",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true
            )
    })
    @RequestMapping(value = "collect/{userId}/{orderId}", method = RequestMethod.PUT)
    public CommonResult uncollectOrder(@PathVariable String userId, @PathVariable String orderId) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<UserCollection> ucWrapper = new QueryWrapper<>();
        ucWrapper.eq("openid", userId)
                .eq("orderid", orderId);
        List<UserCollection> list = userCollectionService.findList(ucWrapper);

        if (!list.isEmpty() && 0 < userCollectionService.delete(list.get(0))) {
            log.info("删除收藏信息: {}成功！", list.get(0).getId());
        } else{
            result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("删除收藏信息: 失败！");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "收藏视频操作")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户",
                    paramType = "path",
                    dataType ="string",
                    required = true),
            @ApiImplicitParam(
                    name = "replayId",
                    value = "视频id",
                    paramType = "path",
                    dataType ="string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = " (1-收藏, 2-取消收藏)",
                    paramType = "path",
                    dataType = "int",
                    required = true)
    })
    @RequestMapping(value = "collect/replay/{userId}/{replayId}/{type}", method = RequestMethod.POST)
    public CommonResult replayCollect(@PathVariable String userId, @PathVariable String replayId, @PathVariable Integer type) {
        CommonResult result = new CommonResult().init();
        if (type==1) {
            if (replayCollectionService.collectReplay(userId, replayId)) {
                result.success();
                log.info("用户{} 收藏视频{} 成功", userId, replayId);
            } else {
                result.failCustom(MsgCodeUtil.MSG_CODE_UNKNOWN, "已收藏");
                log.info("用户{} 收藏视频{} 失败", userId, replayId);
            }
        } else if (type==2) {
            if (replayCollectionService.uncollectReplay(userId, replayId)) {
                result.success();
                log.info("用户{} 取消收藏视频{} 成功", userId, replayId);
            } else {
                result.failCustom(MsgCodeUtil.MSG_CODE_UNKNOWN, "已收藏");
                log.info("用户{} 取消收藏视频{} 失败", userId, replayId);
            }
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_UNKNOWN, "type参数错误");
        }


        return (CommonResult) result.end();
    }


    @ApiOperation(value = "获取用户收藏订单(回放)")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openid",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "my/collect/{userId}", method = RequestMethod.GET)
    public CommonResult getCollectedOrder(@PathVariable String userId) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<UserCollection> ucWrapper = new QueryWrapper<>();
        ucWrapper.eq("openid", userId).orderByDesc("update_date");
        List<UserCollection> orderList = userCollectionService.findList(ucWrapper);
        ArrayList<Order> orders = new ArrayList<>();
        for (UserCollection userCollection : orderList) {
            Order order = orderService.get(userCollection.getOrderid());
            if (order!=null) {
                orders.add(order);
            }
        }
        List<OrderShowVO> list = orderService.order2ShowVO(orders, userId);
        result.success("order",list);
        result.putItem("total",orders.size());

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openId",
                    paramType = "path",
                    dataType = "string",
                    required = true
            ),
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
    @RequestMapping(value = "my/order/{userId}", method = RequestMethod.GET)
    public CommonResult getOrderByUser(@PathVariable String userId, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.isNull("pre_order_id");
        wrapper.eq("openid", userId)
                .eq("pay_flag", Order.PAYED)
                .orderByDesc("update_date");
        int total = orderService.findList(wrapper).size();
        wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
        List<Order> listOrder = orderService.findList(wrapper);
        List<OrderShowVO> list = orderService.order2ShowVO(listOrder, userId);
        result.success("order",list);
        result.putItem("total",total);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取用户收藏视频")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openId",
                    paramType = "path",
                    dataType = "string",
                    required = true
            )
    })
    @RequestMapping(value = "my/play/{userId}", method = RequestMethod.GET)
    public CommonResult getPlayByUser(@PathVariable String userId) {
        CommonResult result = new CommonResult().init();
        List<OrderReplay> replays = replayCollectionService.getReplaysByUser(userId);
        List<OrderReplayVO> list = orderReplayService.orderReplayList2VO(replays, userId);
        result.success("play",list);
        result.putItem("total",list.size());
        return (CommonResult) result.end();
    }


}
