package com.kuit.chozy.home.external.aliexpress;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

public final class AliExpressSigner {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private AliExpressSigner() {}

    /**
     * ✅ /sync(OpenService) 전용 서명 (Postman 성공 규칙과 동일)
     * - sign 제외
     * - null/blank value 제외
     * - key ASCII 정렬
     * - base = key + value concat (prefix 절대 없음)
     * - HMAC-SHA256(base, appSecret) -> HEX 대문자
     */
    public static String signSync(Map<String, String> params, String appSecret) {
        Map<String, String> filtered = new HashMap<>();

        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();

            if (k == null || k.isBlank()) continue;
            if ("sign".equalsIgnoreCase(k)) continue;
            if (v == null || v.isBlank()) continue;

            filtered.put(k, v);
        }

        String[] keys = filtered.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        StringBuilder base = new StringBuilder();
        for (String k : keys) {
            base.append(k).append(filtered.get(k));
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] raw = mac.doFinal(base.toString().getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(raw).toUpperCase(Locale.ROOT);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AliExpress signSync 실패", e);
        }
    }
}
