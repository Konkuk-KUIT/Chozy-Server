package com.kuit.chozy.home.external.coupang;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class CoupangHmacSigner {

    private static final String ALGORITHM = "HmacSHA256";

    private CoupangHmacSigner() {}

    /** 쿠팡 가이드 포맷: yyMMdd'T'HHmmss'Z' (GMT) */
    public static String signedDateUtcNow() {
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    /**
     * @param method GET/POST...
     * @param fullPathWithQuery 도메인 뒤 전체 경로(+쿼리). 예: /v2/providers/.../products/bestcategories/1001?limit=10
     */
    public static String authorization(
            String method,
            String fullPathWithQuery,
            String accessKey,
            String secretKey,
            String signedDate
    ) {
        String[] parts = fullPathWithQuery.split("\\?", 2);
        String path = parts[0];
        String query = (parts.length == 2) ? parts[1] : "";

        String message = signedDate + method + path + query;

        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = Hex.encodeHexString(rawHmac);

            return String.format(
                    "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
                    ALGORITHM, accessKey, signedDate, signature
            );
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("HMAC 생성 실패: " + e.getMessage(), e);
        }
    }
}
