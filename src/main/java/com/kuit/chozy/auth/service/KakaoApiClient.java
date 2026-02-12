package com.kuit.chozy.auth.service;

import com.kuit.chozy.global.common.config.OAuthConfig;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class KakaoApiClient {

    private final RestClient restClient;
    private final OAuthConfig.KakaoProperties properties;

    public KakaoApiClient(OAuthConfig.KakaoProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
    }

    public String getAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("code", code);

        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            form.add("client_secret", properties.getClientSecret());
        }

        try {
            return restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        String body = new String(res.getBody().readAllBytes());
                        System.out.println("KAKAO TOKEN STATUS = " + status);
                        System.out.println("KAKAO TOKEN BODY = " + body);

                        if (status >= 400) {
                            throw new ApiException(ErrorCode.KAKAO_TOKEN_INVALID);
                        }

                        // 간단 파싱: Map으로 다시 읽고 싶으면 ObjectMapper 써도 됨
                        // 일단 body에 access_token이 있는지만 빠르게 확인
                        if (!body.contains("access_token")) {
                            throw new ApiException(ErrorCode.KAKAO_TOKEN_INVALID);
                        }

                        // 빠르게 access_token만 뽑기 (정식은 ObjectMapper 추천)
                        int idx = body.indexOf("\"access_token\"");
                        int start = body.indexOf("\"", body.indexOf(":", idx) + 1) + 1;
                        int end = body.indexOf("\"", start);
                        return body.substring(start, end);
                    });

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorCode.KAKAO_COMMUNICATION_FAILED);
        }
    }


    public String getKakaoId(String accessToken) {
        try {
            return restClient.post()
                    .uri(properties.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        String body = new String(res.getBody().readAllBytes());
                        System.out.println("KAKAO USERINFO STATUS = " + status);
                        System.out.println("KAKAO USERINFO BODY = " + body);

                        if (status >= 400) {
                            throw new ApiException(ErrorCode.KAKAO_USERINFO_INVALID);
                        }

                        if (!body.contains("\"id\"")) {
                            throw new ApiException(ErrorCode.KAKAO_USERINFO_INVALID);
                        }

                        int idx = body.indexOf("\"id\"");
                        int start = body.indexOf(":", idx) + 1;
                        int end = body.indexOf(",", start);
                        if (end == -1) end = body.indexOf("}", start);
                        return body.substring(start, end).trim();
                    });

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorCode.KAKAO_COMMUNICATION_FAILED);
        }
    }

}