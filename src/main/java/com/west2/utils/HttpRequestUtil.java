package com.west2.utils;

import com.alibaba.fastjson.JSONObject;
import com.west2.config.WxMappingJackson2HttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @desc http请求封装类
 * @date 2022/11/26
 */
@Slf4j
public class HttpRequestUtil {

    public static JSONObject postJson(String url, String jsonBody) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return  httpRestClient(url, HttpMethod.POST, jsonBody, headers);
    }

    public static JSONObject postFormUnlencoded(String url, MultiValueMap<String, String> params) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        log.info(params.toString());
        return  httpRestClient(url, HttpMethod.POST, params, headers);
    }


    public static JSONObject get(String url, MultiValueMap<String, String> params) throws IOException {
        return  httpRestClient(url, HttpMethod.GET, params, new HttpHeaders());
    }


    private static JSONObject httpRestClient(String url, HttpMethod method, MultiValueMap<String, String> params, HttpHeaders headers) throws IOException {
//        log.info(headers.toString());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        RestTemplate client = new RestTemplate(requestFactory);
        client.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
//        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
//        log.info(requestEntity.toString());
        //  执行HTTP请求
        ResponseEntity<JSONObject> response = null;
        try{
            response = client.exchange(url, method, requestEntity, JSONObject.class);
            return response.getBody();
        } catch (HttpClientErrorException e){
            log.info("------------- 出现异常 HttpClientErrorException -------------");
            log.info(e.getMessage());
            log.info(e.getStatusText());
            log.info("-------------responseBody-------------");
            log.info(e.getResponseBodyAsString());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            log.info("------------- HttpRestUtils.httpRestClient() 出现异常 Exception -------------");
            log.info(e.getMessage());
            return null;
        }
    }

    private static JSONObject httpRestClient(String url, HttpMethod method, String jsonBody, HttpHeaders headers) throws IOException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        RestTemplate client = new RestTemplate(requestFactory);
        client.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
//        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<String>(jsonBody, headers);
        //  执行HTTP请求
        ResponseEntity<JSONObject> response = null;
        try{
            response = client.exchange(url, method, requestEntity, JSONObject.class);
            return response.getBody();
        } catch (HttpClientErrorException e){
            log.info("------------- 出现异常 HttpClientErrorException -------------");
            log.info(e.getMessage());
            log.info(e.getStatusText());
            log.info("-------------responseBody-------------");
            log.info(e.getResponseBodyAsString());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            log.info("------------- HttpRestUtils.httpRestClient() 出现异常 Exception -------------");
            log.info(e.getMessage());
            return null;
        }
    }

}
