package com.west2.service;

import com.github.kwai.open.KwaiOpenException;
import com.github.kwai.open.api.KwaiOpenLiveApi;
import com.github.kwai.open.api.KwaiOpenOauthApi;
import com.github.kwai.open.request.AccessTokenRequest;
import com.github.kwai.open.request.RefreshTokenRequest;
import com.github.kwai.open.request.UserInfoRequest;
import com.github.kwai.open.response.AccessTokenResponse;
import com.github.kwai.open.response.RefreshTokenResponse;
import com.github.kwai.open.response.UserInfoResponse;
import com.west2.config.RuisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KwaiService {

    @Autowired
    private KwaiOpenOauthApi kwaiOpenOauthApi;

    @Autowired
    private KwaiOpenLiveApi kwaiOpenLiveApi;

    /**
     * @desc    获取快手access-token
     * @param code  临时code
     * @return api返回结果
     * @throws KwaiOpenException
     */
    public AccessTokenResponse getAccessToken(String code) throws KwaiOpenException {
        AccessTokenRequest req = new AccessTokenRequest(code, RuisConfig.KuaishouConfig.appSecret);
        AccessTokenResponse resp = kwaiOpenOauthApi.getAccessToken(req);
        return resp;
    }

    /**
     * @desc    刷新access-token
     * @param refreshToken
     * @return 返回结果
     * @throws KwaiOpenException
     */
    public RefreshTokenResponse refreshAccessToken(String refreshToken) throws KwaiOpenException {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken, RuisConfig.KuaishouConfig.appSecret);
        return kwaiOpenOauthApi.refreshToken(request);
    }


}
