package com.kuit.chozy.home.external.aliexpress;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class AliExpressUrlBuilder {

    private AliExpressUrlBuilder() {}

    public static String build(String baseUrl, String path, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(trimTrailingSlash(baseUrl));
        if (path == null || path.isBlank()) path = "/sync";
        if (!path.startsWith("/")) sb.append("/");
        sb.append(path);

        if (params == null || params.isEmpty()) return sb.toString();

        sb.append("?");
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append("&");
            first = false;

            String key = e.getKey();
            String val = e.getValue();

            sb.append(urlEncode(key)).append("=").append(urlEncode(val));
        }
        return sb.toString();
    }

    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        if (s.endsWith("/")) return s.substring(0, s.length() - 1);
        return s;
    }

    private static String urlEncode(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
        // 공백 -> + 로 변환됨. query param에서 정상.
    }
}
