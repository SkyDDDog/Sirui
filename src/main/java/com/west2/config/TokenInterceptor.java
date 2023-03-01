package com.west2.config;

import com.alibaba.fastjson.JSONObject;
import com.west2.common.CommonResult;
import com.west2.common.MsgCodeUtil;
import com.west2.utils.JwtUtil;
import com.west2.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //跨域请求会首先发一个option请求，直接返回正常状态并通过拦截器
        if(request.getMethod().equals("OPTIONS")){
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        response.setCharacterEncoding("utf-8");
        String token = request.getHeader("token");
        if (token!=null){
            int result= jwtUtil.verify(token);
            if (result==1){
                log.info("通过拦截器");
                return true;
            }
        }
        response.setContentType("application/json; charset=utf-8");
        try {
            CommonResult result = (new CommonResult()).init();
            result.fail(MsgCodeUtil.MSG_CODE_JWT_SIGNATURE_EXCEPTION);
            CommonResult end = (CommonResult) result.end();
            JSONObject json = new JSONObject();
            json.put("msgCode", end.getMsgCode());
            json.put("errMsg", end.getErrMsg());
            json.put("item", end.getItem());
            json.put("receiptDateTime", end.getReceiptDateTime());
            json.put("returnDateTime", end.getReturnDateTime());
            response.getWriter().append(json.toString());
            log.info(end.toString());
            log.info("认证失败，未通过拦截器");
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}