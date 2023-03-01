package com.west2.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.kwai.open.api.KwaiOpenLiveApi;
import com.github.kwai.open.api.KwaiOpenOauthApi;
import com.west2.common.CommonResult;
import com.west2.common.KuaishouTestResponse;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "${apiPath}/kuaishou")
@Api(value = "KuaishouController", tags = "快手接口")
public class KuaishouController {



    @ApiOperation(value = "快手认证回调")
    @RequestMapping(value = "callback/auth", method = RequestMethod.POST)
    public CommonResult authCallback(@RequestBody JSONObject json) {
        CommonResult result = new CommonResult();
        log.info(json.toJSONString());
        result.success("json", json);
        return (CommonResult) result.end();
    }

    @ApiOperation(value = "快手用户操作回调")
    @RequestMapping(value = "callback/user", method = RequestMethod.POST)
    public KuaishouTestResponse userCallback(@RequestBody JSONObject json) {
        KuaishouTestResponse resp = new KuaishouTestResponse();
        log.info("快手回调: \n{}",json.toJSONString());
        resp.setResult(1).
                setMessage_id(json.getString("message_id"));
        return resp;
    }

    @ApiOperation(value = "快手用户授权登录")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "code",
                    value = "临时授权票据",
                    paramType = "path",
                    dataType ="string",
                    required = true),
    })
    @RequestMapping(value = "user/login", method = RequestMethod.POST)
    public CommonResult userLogin(@PathVariable String code) {
        CommonResult result = new CommonResult().init();


        return (CommonResult) result.end();
    }


}
