package com.kuit.chozy.home.external.coupang;

import com.kuit.chozy.home.dto.request.HomeProductsRequest;
import com.kuit.chozy.home.dto.response.CoupangProductCategoryResponse;
import com.kuit.chozy.home.dto.response.CoupangProductSearchResponse;
import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.Vendor;
import com.kuit.chozy.home.external.ExternalProductClient;
import com.kuit.chozy.home.external.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CoupangPartnersClient implements ExternalProductClient {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${coupang.domain}")
    private String domain; // 예: https://api-gateway.coupang.com/v2/providers/affiliate_open_api/apis/openapi/v1

    @Value("${coupang.access-key}")
    private String accessKey;

    @Value("${coupang.secret-key}")
    private String secretKey;

    @Value("${coupang.search-path}")
    private String searchPath; // /products/search

    @Value("${coupang.best-category-path}")
    private String bestCategoryPath; // /products/bestcategories

    @Override
    public Vendor supports() {
        return Vendor.COUPANG;
    }

    @Override
    public List<ProductSnapshot> fetch(HomeProductsRequest r) {
        boolean hasSearch = r.search() != null && !r.search().isBlank();
        if (hasSearch) return fetchBySearch(r.search(), r.size());
        return fetchByCategory(r.category(), r.size());
    }

    // ✅ 검색 응답은 data가 객체이고 productData가 리스트임 (너가 올린 JSON 그대로)
    private List<ProductSnapshot> fetchBySearch(String keyword, int size) {
        int limit = Math.min(size, 10);

        String pathWithQuery = searchPath
                + "?keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                + "&limit=" + limit;

        CoupangProductSearchResponse res = get(pathWithQuery, CoupangProductSearchResponse.class);
        if (res == null || res.data() == null || res.data().productData() == null) return List.of();

        // ✅ “검색어 기반으로 대충 매핑”
        ProductCategory mappedCategory = guessCategoryByKeyword(keyword);

        return res.data().productData().stream()
                .map(it -> toSnapshot(
                        it.productId(),
                        it.productName(),
                        it.productImage(),
                        it.productPrice(),
                        it.productUrl(),
                        mappedCategory
                ))
                .toList();
    }

    private List<ProductSnapshot> fetchByCategory(ProductCategory category, int size) {
        List<Integer> ids = CoupangCategoryIdMapper.toCoupangCategoryIds(category);

        int maxCalls = 3;
        int limit = Math.min(size, 10);

        Map<Long, ProductSnapshot> dedup = new LinkedHashMap<>();

        for (int i = 0; i < Math.min(ids.size(), maxCalls); i++) {
            Integer categoryId = ids.get(i);

            String pathWithQuery = bestCategoryPath + "/" + categoryId
                    + "?limit=" + limit;

            CoupangProductCategoryResponse res = get(pathWithQuery, CoupangProductCategoryResponse.class);
            if (res == null || res.data() == null) continue;

            res.data().forEach(it -> {
                ProductSnapshot snap = toSnapshot(
                        it.productId(),
                        it.productName(),
                        it.productImage(),
                        it.productPrice(),
                        it.productUrl(),
                        category
                );
                dedup.putIfAbsent(snap.getExternalProductId(), snap);
            });

            if (dedup.size() >= size) break;
        }

        List<ProductSnapshot> merged = new ArrayList<>(dedup.values());
        return merged.subList(0, Math.min(merged.size(), size));
    }

    private ProductSnapshot toSnapshot(long productId, String name, String image, double price, String url, ProductCategory category) {
        return ProductSnapshot.builder()
                .vendor(Vendor.COUPANG)
                .externalProductId(productId)
                .category(category)
                .name(name)
                .listPrice((int) Math.round(price))
                .discountRate(0)
                .productImageUrl(image)
                .productUrl(url)
                .discountedPrice(0)
                .ratingAvg(0.0)
                .build();
    }

    private <T> T get(String pathWithQuery, Class<T> clazz) {
        // ✅ 여기서부터가 401 “갑자기” 터지는 거 방지 핵심:
        // domain 끝 슬래시/ path 시작 슬래시를 정규화해서 "요청 URL 문자열"을 먼저 만든다.
        String requestUrl = joinUrl(domain, pathWithQuery);
        URI requestUri = URI.create(requestUrl);

        // ✅ 서명에 들어갈 uri는 "도메인 뒤 path(+query)"인데,
        // 반드시 requestUri에서 뽑아온 값(=실제 요청과 동일)으로 만든다.
        String rawPath = requestUri.getRawPath();
        String rawQuery = requestUri.getRawQuery();
        String uriForSignature = rawPath + (rawQuery != null ? "?" + rawQuery : "");

        String signedDate = CoupangHmacSigner.signedDateUtcNow();
        String auth = CoupangHmacSigner.authorization("GET", uriForSignature, accessKey, secretKey, signedDate);

        return webClient.get()
                .uri(requestUri)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    // ✅ domain/path 조합 시 // 생기는 거 방지
    private static String joinUrl(String domain, String pathWithQuery) {
        String d = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        String p = pathWithQuery.startsWith("/") ? pathWithQuery : "/" + pathWithQuery;
        return d + p;
    }

    // ✅ 검색어 기반 “대충” 매핑
    private static ProductCategory guessCategoryByKeyword(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT);
        if (k.contains("니트") || k.contains("셔츠") || k.contains("바지") || k.contains("후드") || k.contains("패딩")) return ProductCategory.FASHION;
        if (k.contains("크림") || k.contains("립") || k.contains("향수") || k.contains("선크림")) return ProductCategory.BEAUTY;
        if (k.contains("이어폰") || k.contains("마우스") || k.contains("키보드") || k.contains("모니터")) return ProductCategory.ELECTRONICS;
        if (k.contains("사료") || k.contains("간식") || k.contains("캣") || k.contains("독")) return ProductCategory.PET;
        if (k.contains("장난감") || k.contains("레고") || k.contains("인형")) return ProductCategory.TOYS;
        if (k.contains("세제") || k.contains("휴지") || k.contains("수건") || k.contains("이불")) return ProductCategory.HOME;
        return ProductCategory.HOME; // fallback
    }
}
