package com.west2.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tencentcloudapi.vod.v20180717.models.ProcessMediaResponse;
import com.west2.common.CommonResult;
import com.west2.common.MsgCodeUtil;
import com.west2.entity.OrderReplay;
import com.west2.entity.vo.RecordCallbackVO;
import com.west2.service.OrderReplayService;
import com.west2.service.VodService;
import com.west2.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/common")
@Api(value = "CommonController", tags = "其他数据接口")
public class CommonController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrderReplayService orderReplayService;
    @Autowired
    private VodService vodService;

    @ApiOperation(value = "获取access-token(定期刷新)", notes = "小程序全局唯一后台接口调用凭据，调用绝大多数后台接口时都需使用(前端可能用不上)")
    @RequestMapping(value = "/access-token", method = RequestMethod.GET)
    public CommonResult getAccessToken() {
        CommonResult result = (new CommonResult()).init();
        String accessToken = (String) redisUtil.get("access-token");
        result.success("access-token",accessToken);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "腾讯云点播录制回调接口")
    @RequestMapping(value = "VodCallback", method = RequestMethod.POST)
    public CommonResult recordCallbackByVod(@RequestBody RecordCallbackVO vo) {
        log.info("云点播录制回调");
        CommonResult result = new CommonResult().init();
        if (StringUtils.isNotBlank(vo.getStream_id())) {
            if (vodService.editMedia(vo.getStream_id(), OrderReplay.ACUTS) != null) {
                log.info("主队集锦生成中！订单id: {}", vo.getStream_id());
            }
            if (vodService.editMedia(vo.getStream_id(), OrderReplay.BCUTS) != null) {
                log.info("客队集锦生成中！订单id: {}", vo.getStream_id());
            }
            if (vodService.editMedia(vo.getStream_id(), OrderReplay.ASCORE, OrderReplay.BSCORE) != null) {
                log.info("进球集锦生成中！订单id: {}", vo.getStream_id());
            }
            if (vodService.editMedia(vo.getStream_id(), OrderReplay.ACUTS, OrderReplay.BCUTS, OrderReplay.ASCORE, OrderReplay.BSCORE) != null) {
                log.info("全场集锦生成中！订单id: {}", vo.getStream_id());
            }
        }
        if (0 < orderReplayService.saveReplayVideoInfo(vo)) {
            List<OrderReplay> replayList = orderReplayService.getFullReplay(vo.getStream_id());
            if (1 < replayList.size()) {
                String[] fileIds = new String[replayList.size()];
                for (int i = 0; i < replayList.size(); i++) {
                    fileIds[i] = replayList.get(i).getFileId();
                    orderReplayService.delete(replayList.get(i));
                }
                String resp = vodService.fixReplay(vo.getStream_id(), fileIds);
                log.info("修复全程回放任务: {}", resp.toString());
            }

            result.success();
            log.info("更新订单直播回放成功: {}",vo.toString());
        } else {
            result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
            log.info("更新订单直播回放失败: {}",vo.toString());
        }
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "腾讯云点播任务回调接口")
    @RequestMapping(value = "TaskCallback", method = RequestMethod.POST)
    public CommonResult transCallback(@RequestBody JSONObject jsonObject) {
        log.info("腾讯云点播回调");
        log.info(jsonObject.toJSONString());
        CommonResult result = new CommonResult().init();
        if ("EditMediaComplete".equals(jsonObject.getString("EventType"))) {
            log.info("剪辑完成");
            JSONObject editMediaCompleteEvent = jsonObject.getJSONObject("EditMediaCompleteEvent");
            OrderReplay orderReplay = new OrderReplay();
            String ctx = editMediaCompleteEvent.getString("SessionContext");
            if (StringUtils.isNotBlank(ctx)) {
                String[] split = ctx.split("/");
                if (split.length == 2) {
                    orderReplay.setOrderId(split[0]);
                    orderReplay.setType(split[1]);
                } else {
                    log.info("剪辑异常");
                }
                JSONObject output = editMediaCompleteEvent.getJSONObject("Output");
                if (output!=null) {
                    String url = output.getString("FileUrl");
                    orderReplay.setReplay(url.replaceAll("http", "https"));
                    orderReplay.setFileId(output.getString("FileId"));
                }
                if (0 < orderReplayService.save(orderReplay)) {
                    log.info("精彩片段剪辑完成");
                }
            }
        }
        if ("ProcedureStateChanged".equals(jsonObject.getString("EventType"))) {
            JSONObject procedureStateChangeEvent = jsonObject.getJSONObject("ProcedureStateChangeEvent");
            JSONArray mediaProcessResultSet = procedureStateChangeEvent.getJSONArray("MediaProcessResultSet");
            if (!mediaProcessResultSet.isEmpty() && "Transcode".equals(mediaProcessResultSet.getJSONObject(0).getString("Type"))) {
                log.info("收到转码任务回调");
                JSONObject transcodeTask = mediaProcessResultSet.getJSONObject(0).getJSONObject("TranscodeTask");
                String playUrl = transcodeTask.getJSONObject("Output").getString("Url");
                playUrl = playUrl.replace("http", "https");
                String id = procedureStateChangeEvent.getString("SessionContext");
                OrderReplay replay = orderReplayService.get(id);
                if (replay!=null) {
                    replay.setReplay(playUrl);
                    if (0 < orderReplayService.save(replay)) {
                        log.info("回放转码成功!");
                    }
                } else {
                    log.info("找不到该回放记录: {}", id);
                }

            }
        }

        return (CommonResult) result.end();
    }

    @ApiOperation(value = "测试接口")
    @RequestMapping(value = "test/{id}", method = RequestMethod.POST)
    public CommonResult test(@PathVariable String id) {
        CommonResult result = new CommonResult().init();
        ProcessMediaResponse resp = vodService.liveCutTranscode(id);
        result.success("resp", resp);

        return (CommonResult) result.end();
    }
//
//    @ApiOperation(value = "批量测试接口")
//    @RequestMapping(value = "batchTest", method = RequestMethod.POST)
//    public CommonResult batchTest() {
//        CommonResult result = new CommonResult().init();
//        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
//        wrapper.eq("type", OrderReplay.ACUTS).or()
//                .eq("type", OrderReplay.BCUTS).or()
//                .eq("type", OrderReplay.ASCORE).or()
//                .eq("type", OrderReplay.BSCORE);
//        List<OrderReplay> list = orderReplayService.findList(wrapper);
//        ArrayList<ProcessMediaResponse> respList = new ArrayList<>(list.size());
//        for (OrderReplay replay : list) {
//            ProcessMediaResponse resp = vodService.liveCutTranscode(replay.getId());
//            respList.add(resp);
//        }
//        result.success("resp", respList);
//        return (CommonResult) result.end();
//    }



}
