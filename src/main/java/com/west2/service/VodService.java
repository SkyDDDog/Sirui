package com.west2.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import com.west2.config.RuisConfig;
import com.west2.entity.OrderReplay;
import com.west2.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VodService {


    @Autowired
    private OrderReplayService orderReplayService;

    /**
     * @desc 发起转码任务
     * @param fieldId 文件field_id
     */
    public void transcodeVideo(String fieldId) {
        try{
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ProcessMediaByProcedureRequest req = new ProcessMediaByProcedureRequest();
            req.setFileId(fieldId);
            req.setProcedureName("flv-mp4");
            // 返回的resp是一个ProcessMediaByProcedureResponse的实例，与请求对象对应
            ProcessMediaByProcedureResponse resp = vodClient.ProcessMediaByProcedure(req);
            // 输出json格式的字符串回包
            System.out.println(ProcessMediaByProcedureResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * @desc 直播即时剪辑(精彩片段)
     * @param orderId   订单id
     * @param startTime 精彩片段开始时间
     * @param endTime   精彩片段结束时间
     * @return  腾讯云vod api返回结果
     */
    public String liveRealTimeClip(String orderId, String startTime, String endTime) {
        try{
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);

            // 实例化一个请求对象,每个接口都会对应一个request对象
            LiveRealTimeClipRequest req = new LiveRealTimeClipRequest();
            req.setStreamId(orderId);
            req.setStartTime(DateTimeUtil.stamp2ISO8601(startTime));
            req.setEndTime(DateTimeUtil.stamp2ISO8601(endTime));
            req.setIsPersistence(1L);
            req.setHost("replay.ruisport.cn");
            req.setSourceContext("精彩片段");
            // 返回的resp是一个LiveRealTimeClipResponse的实例，与请求对象对应
            log.info("请求: {} - {}", req.getStartTime(), req.getEndTime());
            LiveRealTimeClipResponse resp = vodClient.LiveRealTimeClip(req);
            return LiveRealTimeClipResponse.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            log.error(e.toString());
        }
        return "";
    }

    /**
     * @desc 直播即时剪辑(精彩片段)
     * @param orderId 订单id
     * @param type  类型(1-A队精彩片段, 2-B队精彩片段, 3-A队进球, 4-B队进球)
     * @return
     */
    public OrderReplay liveRealTimeClip(String orderId, Integer type) {
        long now = DateTimeUtil.nowTimeStamp();
        String startTime = now-80+"";
        String endTime = now-50+"";
        String resp = this.liveRealTimeClip(orderId, startTime, endTime);
        if (StringUtils.isNotBlank(resp)) {
            JSONObject json = JSON.parseObject(resp);
            String url = json.getString("Url");
            String fileId = json.getString("FileId");
            OrderReplay orderCut = new OrderReplay();
            orderCut.setOrderId(orderId)
                    .setFileId(fileId)
                    .setReplay(url);
            if (type==1) {
                orderCut.setType(OrderReplay.ACUTS);
            } else if (type == 2) {
                orderCut.setType(OrderReplay.BCUTS);
            } else if (type == 3) {
                orderCut.setType(OrderReplay.ASCORE);
            } else if (type == 4) {
                orderCut.setType(OrderReplay.BSCORE);
            } else {
                return null;
            }
            return orderCut;
        }
        return null;
    }

    /**
     * @desc 剪辑(拼接)视频
     * @param orderId   订单id
     * @param type 类型(根据传入的OrderReplay.常数，拼接所有该订单下该类型集锦)
     * @return 腾讯云vod api返回结果
     */
    public String editMedia(String orderId, String ...type) {
        try {
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);

            EditMediaRequest req = new EditMediaRequest();
            req.setInputType("File");

            List<OrderReplay> cutList = orderReplayService.getCutsByOrder(orderId, type);
            if (cutList.size() < 1) {
                return null;
            }

            EditMediaFileInfo[] editMediaFileInfos = new EditMediaFileInfo[cutList.size()];
            for (int i = 0; i < cutList.size(); i++) {
                EditMediaFileInfo editMediaFileInfo = new EditMediaFileInfo();
                editMediaFileInfo.setFileId(cutList.get(i).getFileId());
                editMediaFileInfos[i] = editMediaFileInfo;
            }
            req.setFileInfos(editMediaFileInfos);
            if (type.length==1) {
                if (OrderReplay.ACUTS.equals(type[0])) {
                    req.setSessionContext(orderId+"/"+OrderReplay.ACOMPOSE);
                }
                if (OrderReplay.BCUTS.equals(type[0])) {
                    req.setSessionContext(orderId+"/"+OrderReplay.BCOMPOSE);
                }
            }  else if (type.length==2) {
                req.setSessionContext(orderId+"/"+OrderReplay.SCORECOLLECTION);
            } else {
                req.setSessionContext(orderId+"/"+OrderReplay.ALLCOLLECTION);
            }

            // 返回的resp是一个EditMediaResponse的实例，与请求对象对应
            EditMediaResponse resp = vodClient.EditMedia(req);
            // 输出json格式的字符串回包
            log.info(EditMediaResponse.toJsonString(resp));
            return EditMediaResponse.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            log.info(e.toString());
            return null;
        }
    }

    /**
     * @desc 直播异常断流后，拼接所有直播回放成一个新的直播回放
     * @param orderId   订单id
     * @param fileId    分段的直播回放文件id
     * @return  腾讯云vod api返回结果
     */
    public String fixReplay(String orderId, String ...fileId) {
        try {
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);

            EditMediaRequest req = new EditMediaRequest();
            req.setInputType("File");

            EditMediaFileInfo[] editMediaFileInfos = new EditMediaFileInfo[fileId.length];
            for (int i = 0; i < fileId.length; i++) {
                EditMediaFileInfo editMediaFileInfo = new EditMediaFileInfo();
                editMediaFileInfo.setFileId(fileId[i]);
                editMediaFileInfos[i] = editMediaFileInfo;
            }
            req.setFileInfos(editMediaFileInfos);
            req.setSessionContext(orderId+"/"+OrderReplay.ORIGIN);

            // 返回的resp是一个EditMediaResponse的实例，与请求对象对应
            EditMediaResponse resp = vodClient.EditMedia(req);
            // 输出json格式的字符串回包
            log.info(EditMediaResponse.toJsonString(resp));
            return EditMediaResponse.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            log.info(e.toString());
            return null;
        }
    }

    /**
     * @desc    视频转码
     * @param id    orderReplay表主键
     * @return  腾讯云vod api返回结果
     */
    public ProcessMediaResponse liveCutTranscode(String id) {
        try {
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);

            // 实例化一个请求对象,每个接口都会对应一个request对象
            ProcessMediaRequest req = new ProcessMediaRequest();
            OrderReplay replay = orderReplayService.get(id);
            if (replay==null) {
                log.info("不存在该回放: {}", id);
                return null;
            }
            log.info("要更改的回放: {}",replay.toString());
            req.setFileId(replay.getFileId());
            MediaProcessTaskInput mediaProcessTaskInput1 = new MediaProcessTaskInput();

            TranscodeTaskInput[] transcodeTaskInputs1 = new TranscodeTaskInput[1];
            TranscodeTaskInput transcodeTaskInput1 = new TranscodeTaskInput();
            transcodeTaskInput1.setDefinition(1434850L);
            transcodeTaskInputs1[0] = transcodeTaskInput1;

            mediaProcessTaskInput1.setTranscodeTaskSet(transcodeTaskInputs1);

            req.setMediaProcessTask(mediaProcessTaskInput1);

            req.setSessionContext(replay.getId());
            // 返回的resp是一个ProcessMediaResponse的实例，与请求对象对应
            ProcessMediaResponse resp = vodClient.ProcessMedia(req);

            return resp;
        } catch (TencentCloudSDKException e) {
            log.info(e.toString());
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @desc    删除腾讯云点播中存储的视频
     * @param fileId    视频对应fileId
     * @return  腾讯云vod api返回结果
     */
    public DeleteMediaResponse deleteMedia(String fileId) {
        try {
            Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient vodClient = new VodClient(cred, "", clientProfile);

            DeleteMediaRequest req = new DeleteMediaRequest();
            req.setFileId(fileId);
            DeleteMediaResponse resp = vodClient.DeleteMedia(req);
            return resp;
        } catch (TencentCloudSDKException e) {
            log.info(e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @desc    批量删除某订单下所有腾讯云中存储的视频
     * @param orderId   订单id
     */
    public void deleteBatchMediaByOrder(String orderId) {
        List<OrderReplay> replayList = orderReplayService.getFullReplay(orderId);
        log.info("deleteBatchMediaByOrder");
        for (OrderReplay replay : replayList) {
            DeleteMediaResponse resp = this.deleteMedia(replay.getFileId());
            log.info(resp.toString());
        }
    }

}
