package com.west2.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.west2.common.CommonResult;
import com.west2.component.delay.DelayQueueManager;
import com.west2.component.delay.DelayTask;
import com.west2.component.delay.LiveLeapTask;
import com.west2.entity.Order;
import com.west2.entity.OrderReplay;
import com.west2.entity.OrderTeam;
import com.west2.entity.vo.*;
import com.west2.service.*;
import com.west2.utils.*;
import com.west2.common.MsgCodeUtil;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/order")
@Api(value = "OrderController", tags = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderReplayService orderReplayService;
    @Autowired
    private OrderTeamService orderTeamService;
    @Autowired
    private UserCollectionService userCollectionService;
    @Autowired
    private WXService wxService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SignUtil signUtil;
    @Autowired
    private VodService vodService;
    @Autowired
    private ReplayCollectionService replayCollectionService;
    @Autowired
    private DelayQueueManager delayQueueManager;

    @ApiOperation(value = "新建订单", notes = "")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public CommonResult create(@Validated @RequestBody @ApiParam("订单vo")OrderVO vo, BindingResult bindingResult) {
        CommonResult result = (new CommonResult()).init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (!projectService.isProjectAvailable(vo)) {
            return (CommonResult) result.failCustom("该时段有直播或该场地有未完成的订单");
        }
        Order order = new Order();
        order.setId(IdWorker.getId(order)+"");
        order.setNewRecord(true);
        order.setPayFlag(Order.UNPAYED).setScoreA(0).setScoreB(0);
        BeanCustomUtil.copyProperties(vo,order);
        order.setTeamA(orderTeamService.getTeamId(vo.getTeamA()));
        order.setTeamB(orderTeamService.getTeamId(vo.getTeamB()));
//        log.info(vo.toString());
//        log.info(order.toString());
        if (0 < orderService.save(order)) {
            result.success("order", order);
            log.info("创建新订单: {}成功！", vo.toString());
        } else{
            result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("创建新订单: {}失败！", vo.toString());
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "增时订单", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "父订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "stopTime",
                    value = "延长后结束时间戳",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "extend/{orderId}/{stopTime}", method = RequestMethod.POST)
    public CommonResult extendOrder(@PathVariable String orderId, @PathVariable String stopTime) {
        CommonResult result = (new CommonResult()).init();
        Order order = orderService.get(orderId);
        if (order==null) {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST, "该订单不存在").end();
        }
        if (order.getStopTime().compareTo(DateTimeUtil.nowTimeStamp()+"") < 0) {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "该直播已经结束").end();
        }
        if (0 < order.getStopTime().compareTo(stopTime)) {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "延长后结束时间比原订单早").end();
        }
        Order extendOrder = new Order();
        BeanCustomUtil.copyProperties(order, extendOrder);
        extendOrder.setPreOrderId(orderId).
                setPayFlag(Order.UNPAYED).
                setStartTime(order.getStopTime()).
                setStopTime(stopTime).
                setTotal(orderService.caculatePrice(order.getProjectId(), DateTimeUtil.nowTimeStamp()+"", stopTime)+"").
                setId(IdWorker.getId(extendOrder)+"");

        extendOrder.setNewRecord(true);
        if (0 < orderService.save(extendOrder)) {
            result.success("order", extendOrder);
            log.info("创建增时订单: {}成功！", extendOrder.toString());
        } else{
            result.error(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("创建增时订单: {}失败！", extendOrder.toString());
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "增时订单支付成功")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "订单id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "extend/{id}", method = RequestMethod.POST)
    public CommonResult updateExtendOrder(@PathVariable String id) {
        CommonResult result = new CommonResult().init();
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        Order extendOrder = orderService.get(id);
        if (extendOrder==null) {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST,"不存在该订单");
        }
        Order order = orderService.get(extendOrder.getPreOrderId());
        if (order!=null) {
            order.setStopTime(extendOrder.getStopTime());
            extendOrder.setPayFlag(Order.PAYED);
            if (0 < orderService.save(order) && 0 < orderService.save(extendOrder) ) {
                result.success();
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            }
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST,"不存在该订单");
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
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public CommonResult update(@PathVariable String id, @Validated @RequestBody @ApiParam("订单修改vo") OrderUpdVO vo, BindingResult bindingResult) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (StringUtils.isBlank(id)) {
            result.fail(MsgCodeUtil.MSG_CODE_ID_IS_NULL);
            return (CommonResult) result.end();
        }
        Order order = orderService.get(id);
        BeanCustomUtil.copyProperties(vo, order);
        if (StringUtils.isNotBlank(vo.getTeamA())) {
            order.setTeamA(orderTeamService.getTeamId(vo.getTeamA()));
        }
        if (StringUtils.isNotBlank(vo.getTeamB())) {
            order.setTeamB(orderTeamService.getTeamId(vo.getTeamB()));
        }


//        JSONObject requestResult = orderService.savePushStream(order);
        if (0 < orderService.save(order) && orderService.savePushStream(order).getJSONObject("error").getIntValue("code")==0) {
            result.success("order",order);
//            Map<Object, Object> playMap = redisUtil.hmget(order.getId());
//            String playUrl = (String) playMap.get("playUrl");
//            result.putItem("playUrl", playUrl);
            log.info("更新订单信息：{}成功！",vo.toString());
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("更新订单信息：{}失败！",vo.toString());
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "删除订单信息")
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
        Order order = orderService.get(id);
        // 删除腾讯点播视频
        vodService.deleteBatchMediaByOrder(order.getId());
        // 删除回放表记录
        orderReplayService.deleteOrderReplayByOrderId(order.getId());
        // 删除回放收藏表记录
        replayCollectionService.deleteReplayCollectByOrder(order.getId());
        // 删除订单收藏表记录
        userCollectionService.deleteOrderCollectByOrder(order.getId());
        // 直播机器断流
        orderService.deletePushStream(order.getId());
        // 删除数据库订单记录
        orderService.delete(order);
        result.success();
        log.info("删除订单信息：{}成功！", id);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "批量删除订单信息")
    @RequestMapping(value = "batchDelete", method = RequestMethod.DELETE)
    public CommonResult deleteBatch(@RequestBody OrderDeleteVO vo) {
        CommonResult result = new CommonResult().init();
        List<String> orderIdList = vo.getOrderId();
        int failed = 0;
        for (String orderId : orderIdList) {
            Order order = orderService.get(orderId);
            if (order==null) {
                result.putItem("failItem"+failed, orderId);
                continue;
            }
            // 删除腾讯点播视频
            vodService.deleteBatchMediaByOrder(orderId);
            // 删除回放表记录
            orderReplayService.deleteOrderReplayByOrderId(orderId);
            // 删除回放收藏表记录
            replayCollectionService.deleteReplayCollectByOrder(orderId);
            // 删除订单收藏表记录
            userCollectionService.deleteOrderCollectByOrder(orderId);
            // 直播机器断流
            orderService.deletePushStream(orderId);
            // 删除数据库订单记录
            orderService.delete(order);
        }
        result.success();
        log.info("批量删除订单信息：{}成功！", vo);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取单条订单信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "id",
                    value = "订单id",
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
            Order order = orderService.get(id);
            result.success("order",order);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation("按名称模糊查询订单")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "content",
                    value = "查询词",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openid",
                    paramType = "query",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "pageNum",
                    value = "页数(1开始)",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "1"),
            @ApiImplicitParam(
                    name = "pageSize",
                    value = "一页数据条数",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "5")
    })
    @RequestMapping(value = "search/{content}", method = RequestMethod.GET)
    public CommonResult searchOrder(@PathVariable String content
            ,@RequestParam String userId, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.isNull("pre_order_id");
        wrapper.like("game_name", content);
        List<Order> orderList = orderService.findList(wrapper);
        result.putItem("total", orderList.size());
        if (0 < orderList.size()) {
            wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
            orderList = orderService.findList(wrapper);
            List<OrderShowVO> list = orderService.order2ShowVO(orderList, userId);
            result.success("order", list);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取多条订单信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户openid",
                    paramType = "query",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "pageNum",
                    value = "页数(1开始)",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "1"),
            @ApiImplicitParam(
                    name = "pageSize",
                    value = "一页数据条数",
                    paramType = "query",
                    dataType = "int",
                    required = true,
                    defaultValue = "5")
    })
    @RequestMapping(value = {"", "list"}, method = RequestMethod.GET)
    public CommonResult selectList(@RequestParam String userId, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.isNull("pre_order_id");
        wrapper.orderByDesc("update_date");
        int total = orderService.findList(wrapper).size();
        result.putItem("total",total);
        wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
        List<Order> orderList = orderService.findList(wrapper);
        List<OrderShowVO> list = orderService.order2ShowVO(orderList, userId);
        result.success("order",list);

        return (CommonResult) result.end();
    }




    @ApiOperation(value = "微信支付下单获取prepay_id")
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
                    required = true),
    })
    @RequestMapping(value = "pay/{userId}/{orderId}", method = RequestMethod.POST)
    public CommonResult prepay(@PathVariable String userId, @PathVariable String orderId, @RequestBody PayVO vo) {
        CommonResult result = new CommonResult().init();
        if (vo.getTotal()==null) {
            vo.setTotal(orderService.caculatePrice(orderId));
        }
        String prepay = wxService.prepay(orderId, userId, vo.getDescription(), vo.getTotal());
        Order order = orderService.get(orderId);

        int save = 0;
        if (order!=null) {
            order.setTotal(vo.getTotal().toString());
            save = orderService.save(order);
        }
        SignVO sign = null;
        try {
             sign = signUtil.getSign(prepay);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("签名异常");
        }
        if (sign==null && 0 < save) {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
        } else {
            result.putItem("nonceStr", sign.getNonceStr());
            result.putItem("paySign", sign.getPaySign());
            result.putItem("prepayId", sign.getPrepay());
            result.putItem("package", sign.getPack());
            result.putItem("timeStamp", sign.getTimeStamp());
            result.putItem("signType", sign.getSignType());
            result.success();
            log.info("成功获取prepay_id");
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "获取当前支付价格")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "projectId",
                    value = "场地id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "startTime",
                    value = "开始时间",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "stopTime",
                    value = "结束时间",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "price/{projectId}/{startTime}/{stopTime}", method = RequestMethod.GET)
    public CommonResult getPriceByTime(@PathVariable String projectId ,@PathVariable String startTime, @PathVariable String stopTime) {
        CommonResult result = new CommonResult().init();
        Integer price = orderService.caculatePrice(projectId, startTime, stopTime);
        result.success("price", price);
        log.info("获取当前价格成功: {}~{} = {}分", startTime, stopTime, price);
        return (CommonResult) result.end();
    }



    @ApiOperation(value = "支付成功开启设备")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = "是否开启时移(0-不开启, 1-开启)",
                    paramType = "path",
                    dataType = "int",
                    required = true
            )
    })
    @RequestMapping(value = "success/{orderId}/{type}", method = RequestMethod.POST)
    public CommonResult openVenue(@PathVariable String orderId, @PathVariable Integer type) {
        CommonResult result = new CommonResult().init();
        log.info("支付成功开启设备");
        Order order = orderService.get(orderId);
        order.setPayFlag(Order.PAYED);
        JSONObject requestResult = null;
        if (type==1) {
            requestResult = orderService.savePushStream(order, true);
        } else if (type==0) {
            requestResult = orderService.savePushStream(order, false);
        } else {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "type错误").end();
        }

        int respCode = requestResult.getJSONObject("error").getIntValue("code");
        log.info(requestResult.toJSONString());

        if (respCode==0 && 0 < orderService.save(order)) {
            Map<Object, Object> playMap = redisUtil.hmget(order.getId());
            String playUrl = (String) playMap.get("playUrl");
            result.putItem("playUrl", playUrl);
            result.success("order",order);
            log.info("更新订单信息：{}成功！",order.toString());
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            String errMsg = requestResult.getJSONObject("error").getString("message");
            result.failCustom(errMsg);
            log.info("更新订单信息：{}失败！",order.toString());

        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "停止直播")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "stop/{orderId}", method = RequestMethod.POST)
    public CommonResult stopVunue(@PathVariable String orderId) {
        CommonResult result = new CommonResult().init();

        JSONObject jsonObject = orderService.deletePushStream(orderId);
        if (0 < orderService.stopOrder(orderId)) {
            log.info("订单结束时间已修改");
        } else {
            return (CommonResult) result.failCustom("结束时间修改失败").end();
        }
        JSONObject error = jsonObject.getJSONObject("error");
        if (error.getIntValue("code")==0) {
            result.success("message", error.getString("message"));
            log.info("停止直播: {}成功", orderId);
        } else {
            result.failCustom(error.getString("message"));
            log.info("停止直播: {}失败", orderId);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "支付失败")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "fail/{orderId}", method = RequestMethod.POST)
    public CommonResult payFail(@PathVariable String orderId) {
        CommonResult result = new CommonResult().init();
        Order order = orderService.get(orderId);
        log.info("支付失败");
        if (order!=null) {
            order.setPayFlag(Order.PAYFAIL);
            if (0 < orderService.save(order)) {
                result.success("order",order);
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            }
        } else {
            result.failCustom("不存在该订单");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改比分")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "team",
                    value = "主/客队(0为主队/1为客队)",
                    paramType = "path",
                    dataType = "int",
                    required = true),
            @ApiImplicitParam(
                    name = "score",
                    value = "更新后分值",
                    paramType = "path",
                    dataType = "int",
                    required = true)
    })
    @RequestMapping(value = "score/{orderId}/{team}/{score}", method = RequestMethod.PUT)
    public CommonResult updateScore(@PathVariable String orderId, @PathVariable Integer team, @PathVariable Integer score) {
        CommonResult result = new CommonResult().init();
        Order order = orderService.get(orderId);
        if (order!=null) {
            if (0 <= team && team <=1) {
                switch (team) {
                    case 0:
                        order.setScoreA(score);
                        break;
                    case 1:
                        order.setScoreB(score);
                        break;
                    default:
                }
                if (0 < orderService.save(order)) {
                    result.success("order", order);
                    log.info("修改比分成功: {}", order);
                } else {
                    result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                    log.info("修改比分失败: {}", order);
                }
            } else {
                result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "team参数需为0/1(0为主队，1为客队)");
            }
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST, "不存在该订单");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改颜色")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "team",
                    value = "主/客队(0为主队/1为客队)",
                    paramType = "path",
                    dataType = "int",
                    required = true),
            @ApiImplicitParam(
                    name = "color",
                    value = "更新后颜色",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "color/{orderId}/{team}/{color}", method = RequestMethod.PUT)
    public CommonResult updateColor(@PathVariable String orderId, @PathVariable Integer team, @PathVariable String color) {
        CommonResult result = new CommonResult().init();
        boolean r = false;
        switch (team) {
            case 0:
                r = redisUtil.hset(orderId, "colorA", color);
                break;
            case 1:
                r = redisUtil.hset(orderId, "colorB", color);
                break;
            default:
                result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "team参数需为0/1(0为主队，1为客队)");
                break;
        }
        if (r) {
            result.success();
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "计分板是否显示比分")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = "0显示比分, 1隐藏比分",
                    paramType = "path",
                    dataType = "int",
                    required = true),
    })
    @RequestMapping(value = "score/show/{orderId}/{type}", method = RequestMethod.POST)
    public CommonResult setScoreShow(@PathVariable String orderId, @PathVariable Integer type) {
        CommonResult result = new CommonResult().init();
        Object show = redisUtil.hget(orderId, "show");
        if (type==0) {
            if (show==null) {
                orderService.showScore(orderId);
            } else {
                return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "已显示比分").end();
            }
        } else if (type==1) {
            if (show!=null) {
                orderService.unshowScore(orderId);
            } else {
                return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "已隐藏比分").end();
            }
        } else {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "type参数错误").end();
        }
        result.success();

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "积分版数据")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "score/{orderId}", method = RequestMethod.GET)
    public CommonResult getScoreboard(@PathVariable String orderId) {
        CommonResult result = new CommonResult().init();
        Order order = orderService.get(orderId);
        if (order!=null) {
            ScoreboardVO vo = new ScoreboardVO();
            BeanUtils.copyProperties(order, vo);
            vo.setScoreA(order.getScoreA().toString())
                    .setScoreB(order.getScoreB().toString())
                    .setProjectName(projectService.getProjectShowById(order.getProjectId()).getName())
                    .setGameName(order.getGameName())
                    .setTeamA(orderTeamService.getTeamName(vo.getTeamA()))
                    .setTeamB(orderTeamService.getTeamName(vo.getTeamB()))
                    .setTime(orderService.caculateTick(orderId))
                    .setCourse(ScoreboardVO.getCourse((Integer) redisUtil.hget(orderId, "course")))
                    .setShowed(orderService.getShowed(orderId))
                    .setStopFlag(orderService.isTickStarted(orderId)&&orderService.isTickStoped(orderId))
                    .setACuts((Integer) redisUtil.hget(orderId,"aCuts"))
                    .setBCuts((Integer) redisUtil.hget(orderId,"bCuts"));
            String colorA = (String) redisUtil.hget(orderId, "colorA");
            String colorB = (String) redisUtil.hget(orderId, "colorB");
            vo.setColorA(colorA).setColorB(colorB);
            result.success("scoreboard", vo);
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST, "不存在该订单");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改赛事名称")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "title",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "title/{orderId}/{title}", method = RequestMethod.PUT)
    public CommonResult updateOrderTitle(@PathVariable String orderId, @PathVariable String title) {
        CommonResult result = new CommonResult().init();
        Order order = orderService.get(orderId);
        if (order!=null && StringUtils.isNotBlank(title)) {
            order.setGameName(title);
            if (0 < orderService.save(order)) {
                result.success();
                log.info("修改赛事名称成功: {}", order);
            } else {
                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
                log.info("修改赛事名称失败: {}", order);
            }
        } else {
            result.failCustom("参数错误(订单不存在或title为空)");
            log.info("参数错误(订单不存在或title为空)");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改计分板节数")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = "0 节数+1|1 节数-1",
                    paramType = "path",
                    dataType = "int",
                    required = true),
    })
    @RequestMapping(value = "board/course/{orderId}/{type}", method = RequestMethod.POST)
    public CommonResult scoreCourse(@PathVariable String orderId, @PathVariable Integer type) {
        CommonResult result = new CommonResult().init();
        Integer courseNo;
        if (type==0) {
            courseNo = orderService.courseInc(orderId);
        } else if (type==1) {
            courseNo = orderService.courseDecr(orderId);
        } else {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "type参数错误").end();
        }
        result.success("course", ScoreboardVO.getCourse(courseNo));
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "计分板时间")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = "0开始计时|1停止计时|2重新开始计时|3结束计时",
                    paramType = "path",
                    dataType = "int",
                    required = true),
            @ApiImplicitParam(
                    name = "start",
                    value = "开始计时时设定的时间",
                    paramType = "query",
                    dataType = "string",
                    required = false)
    })
    @RequestMapping(value = "board/time/{orderId}/{type}", method = RequestMethod.POST)
    public CommonResult scoreboardTime(@PathVariable String orderId, @PathVariable Integer type, @RequestParam(required = false) String start) {
        CommonResult result = new CommonResult().init();
        if (0 <= type && type <= 3) {
            if (type==0) {
                if (!orderService.isTickStarted(orderId)) {
                    orderService.startTick(orderId, start);
                } else {
                    result.failCustom("时间已开始计时");
                }
            } else if (type==1) {
                if (!orderService.isTickStoped(orderId)) {
                    orderService.stopTick(orderId);
                } else {
                    result.failCustom("时间已停止计时");
                }
            } else if (type==2) {
                if (orderService.isTickStoped(orderId)) {
                    orderService.restartTick(orderId, start);
                } else {
                    result.failCustom("时间已开始计时");
                }
            } else {
                if (!orderService.isTickStoped(orderId)) {
                    orderService.stopTick(orderId);
                } else {
                    orderService.endTick(orderId);
                }
            }
            String time = orderService.caculateTick(orderId);
            result.success("time", time);

        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
        }

        return (CommonResult) result.end();
    }

    @ApiOperation("获取计分板时间")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单Id",
                    paramType = "path",
                    dataType = "string",
                    required = true),
    })
    @RequestMapping(value = "board/time/{orderId}", method = RequestMethod.GET)
    public CommonResult getScoreboardTime(@PathVariable String orderId) {
        CommonResult result = new CommonResult().init();
        String tick = orderService.caculateTick(orderId);
        if (StringUtils.isNotBlank(tick)) {
            result.success("time", tick);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "模糊查询队伍名称")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "content",
                    value = "查询词",
                    paramType = "path",
                    dataType = "string",
                    required = true)
    })
    @RequestMapping(value = "team/search/{content}", method = RequestMethod.GET)
    public CommonResult searchTeam(@PathVariable String content) {
        CommonResult result = new CommonResult().init();
        QueryWrapper<OrderTeam> wrapper = new QueryWrapper<>();
        wrapper.like("name", content);
        List<OrderTeam> list = orderTeamService.findList(wrapper);
        int total = list.size();
        if (0 < total) {
            result.success("team", list);
            result.putItem("total", total);
        } else {
            result.failCustom(MsgCodeUtil.MSG_CODE_DATA_NOT_EXIST, "未查询到任何队伍");
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "修改队伍名称")
    @RequestMapping(value = "team", method = RequestMethod.PUT)
    public CommonResult updateTeam(@Validated @RequestBody @ApiParam("队伍修改vo") OrderTeamVO vo, BindingResult bindingResult) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (0 < orderTeamService.updateTeamName(vo)) {
            result.success();
            log.info("队伍名称修改成功: {}", vo);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("队伍名称修改失败: {}", vo);
        }


        return (CommonResult) result.end();
    }

    @ApiOperation(value = "截取精彩片段(前80~50s的片段) (需要推迟60s调这个接口)")
    @RequestMapping(value = "cuts", method = RequestMethod.POST)
    public CommonResult getLiveCuts(@Validated @RequestBody @ApiParam("即时剪辑vo") LiveCutVO vo, BindingResult bindingResult) {
        CommonResult result = new CommonResult().init();
        // 参数验证
        if (bindingResult.hasErrors()) {
            return (CommonResult) result.failIllegalArgument(bindingResult.getFieldErrors()).end();
        }
        if (1 <= vo.getType() && vo.getType() <= 4) {
            if (vo.getType() == 1 || vo.getType() == 3) {
                redisUtil.hincr(vo.getOrderId(),"aCuts", 1);
            }
            if (vo.getType() == 2 || vo.getType() == 4) {
                redisUtil.hincr(vo.getOrderId(),"bCuts", 1);
            }
            LiveLeapTask liveLeapTask = new LiveLeapTask();
            liveLeapTask.setOrderId(vo.getOrderId())
                        .setType(vo.getType())
                        .setTitle(vo.getTitle())
                        .setIdentifier(vo.getOrderId());
            delayQueueManager.put(new DelayTask(liveLeapTask, 60 * 1000));
//            if (0 < orderReplayService.saveCuts(vo)) {
//                log.info("截取精彩片段成功");
//                result.success();
//            } else {
//                log.info("截取精彩片段失败");
//                result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
//            }
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT);
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "精彩集锦列表")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单id",
                    paramType = "path",
                    dataType ="string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = " (0-全程回放, 1-进球集锦, 2-主队集锦, 3-客队集锦, 4-全场集锦)",
                    paramType = "path",
                    dataType = "int",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户id",
                    paramType = "query",
                    dataType = "string",
                    required = false)
    })
    @RequestMapping(value = "cuts/{orderId}/{type}", method = RequestMethod.GET)
    public CommonResult getVideoList(@PathVariable String orderId, @PathVariable Integer type, @RequestParam(required = false) String userId) {
        CommonResult result = new CommonResult().init();
        String t = null;
        switch (type) {
            case 0:
                t = OrderReplay.ORIGIN;
                break;
            case 1:
                t = OrderReplay.SCORECOLLECTION;
                break;
            case 2:
                t = OrderReplay.ACOMPOSE;
                break;
            case 3:
                t = OrderReplay.BCOMPOSE;
                break;
            case 4:
                t = OrderReplay.ALLCOLLECTION;
                break;
            default:
                result.failCustom(MsgCodeUtil.MSG_CODE_ILLEGAL_ARGUMENT, "type错误");
        }
        OrderReplay replay = orderReplayService.collectionList(orderId, t);
        if (replay!= null) {
            OrderReplayVO vo = orderReplayService.orderReplay2VO(replay, userId);
            result.success("replay", vo);
        } else {
            result.success("replay", replay);
        }

        log.info("获取集锦成功: {}", replay);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "精彩集锦明细列表")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单id",
                    paramType = "path",
                    dataType ="string",
                    required = true),
            @ApiImplicitParam(
                    name = "type",
                    value = " (0-全部, 1-得分, 2-精彩)",
                    paramType = "path",
                    dataType = "int",
                    required = true),
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户id",
                    paramType = "query",
                    dataType = "string",
                    required = false)
    })
    @RequestMapping(value = "cutsdetail/{orderId}/{type}", method = RequestMethod.GET)
    public CommonResult getVideoDetailList(@PathVariable String orderId, @PathVariable Integer type, @RequestParam(required = false) String userId) {
        CommonResult result = new CommonResult().init();
        List<OrderReplay> list = null;
        if (type==0) {
            list = orderReplayService.detailList(orderId);
        } else if(type==1) {
            list = orderReplayService.replayList(orderId, OrderReplay.ASCORE, OrderReplay.BSCORE);
        } else if(type==2) {
            list = orderReplayService.replayList(orderId, OrderReplay.ACUTS, OrderReplay.BCUTS);
        } else {
            return (CommonResult) result.failCustom(MsgCodeUtil.MSG_CODE_UNKNOWN, "type参数错误");
        }
        if (list!=null) {
            List<OrderReplayVO> voList = orderReplayService.orderReplayList2VO(list, userId);
            result.success("replay", voList);
            result.putItem("total", list.size());
            log.info("集锦明细查询成功: {}", voList);
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
        }
        return (CommonResult) result.end();
    }



}
