package com.kuit.chozy.auth.service;

import com.kuit.chozy.global.common.config.OAuthConfig;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class NaverApiClient {

    private final RestClient restClient;
    private final OAuthConfig.NaverProperties properties;

    public NaverApiClient(OAuthConfig.NaverProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
    }

    /**
     * authorization code로 네이버 액세스 토큰 발급.
     * state는 프론트에서 인증 요청 시 사용한 값과 동일하게 전달해야 함
     */
    public String getAccessToken(String code, String state) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("code", code);
        form.add("state", StringUtils.hasText(state) ? state : "");
        //form.add("redirect_uri", properties.getRedirectUri());

        log.info("code: {}", code);

        try {
            return restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        String body = new String(res.getBody().readAllBytes());

                        if (status >= 400) {
                            throw new ApiException(ErrorCode.NAVER_TOKEN_INVALID);
                        }
                        if (!body.contains("access_token")) {
                            throw new ApiException(ErrorCode.NAVER_TOKEN_INVALID);
                        }

                        int idx = body.indexOf("\"access_token\"");
                        int start = body.indexOf("\"", body.indexOf(":", idx) + 1) + 1;
                        int end = body.indexOf("\"", start);
                        return body.substring(start, end);
                    });
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.NAVER_COMMUNICATION_FAILED);
        }
    }

    /**
     * 액세스 토큰으로 네이버 회원 식별자(id) 조회.
     */
    public String getNaverId(String accessToken) {
        try {
            return restClient.get()
                    .uri(properties.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        String body = new String(res.getBody().readAllBytes());

                        if (status >= 400) {
                            throw new ApiException(ErrorCode.NAVER_USERINFO_INVALID);
                        }
                        if (!body.contains("\"id\"")) {
                            throw new ApiException(ErrorCode.NAVER_USERINFO_INVALID);
                        }

                        int idx = body.indexOf("\"response\"");
                        int idIdx = body.indexOf("\"id\"", idx);
                        int start = body.indexOf(":", idIdx) + 1;
                        int end = body.indexOf(",", start);
                        if (end == -1) end = body.indexOf("}", start);
                        return body.substring(start, end).trim().replace("\"", "");
                    });
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.NAVER_COMMUNICATION_FAILED);
        }
    }
}
