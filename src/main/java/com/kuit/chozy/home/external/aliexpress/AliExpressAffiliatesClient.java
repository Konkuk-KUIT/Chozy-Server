package com.kuit.chozy.home.external.aliexpress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.chozy.home.dto.request.HomeProductsRequest;
import com.kuit.chozy.home.dto.response.AliExpressAffiliateProductQueryResponse;
import com.kuit.chozy.home.dto.response.AliExpressProductDto;
import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.Vendor;
import com.kuit.chozy.home.external.ExternalProductClient;
import com.kuit.chozy.home.external.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AliExpressAffiliatesClient implements ExternalProductClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder().build();

    @Value("${aliexpress.base-url}")
    private String baseUrl;

    @Value("${aliexpress.sync-path:/sync}")
    private String syncPath;

    @Value("${aliexpress.app-key}")
    private String appKey;

    @Value("${aliexpress.app-secret}")
    private String appSecret;

    @Value("${aliexpress.target-currency:KRW}")
    private String targetCurrency;

    @Value("${aliexpress.target-language:KO}")
    private String targetLanguage;

    @Value("${aliexpress.ship-to-country:KR}")
    private String shipToCountry;

    @Value("${aliexpress.sign-method:sha256}")
    private String signMethod;

    @Value("${aliexpress.v:2.0}")
    private String v;

    @Value("${aliexpress.format:json}")
    private String format;

    @Override
    public Vendor supports() {
        return Vendor.ALI;
    }

    @Override
    public List<ProductSnapshot> fetch(HomeProductsRequest r) {

        boolean hasSearch = r.search() != null && !r.search().isBlank();

        if (hasSearch) {
            return fetchByQuery(r.search(), null, r.size());
        }

        ProductCategory c = r.category();
        List<Long> categoryIds = AliExpressCategoryIdMapper.toAliCategoryIds(c);

        String fallbackKeyword = AliExpressCategoryIdMapper.fallbackKeyword(c);

        return fetchByCategoryIdsInChunks(fallbackKeyword, categoryIds, r.size());
    }


    private List<ProductSnapshot> fetchByCategoryIdsInChunks(String keyword, List<Long> categoryIds, int size) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return fetchByQuery(keyword, null, size);
        }

        int pageSize = Math.min(size, 50);
        int chunkSize = 3;
        int maxCalls = 4;

        Map<String, ProductSnapshot> dedup = new LinkedHashMap<>();

        int calls = 0;
        for (int i = 0; i < categoryIds.size() && calls < maxCalls; i += chunkSize) {
            List<Long> chunk = categoryIds.subList(i, Math.min(i + chunkSize, categoryIds.size()));
            calls++;

            List<ProductSnapshot> snaps = fetchByQuery(keyword, chunk, pageSize);
            if (snaps == null || snaps.isEmpty()) continue;

            for (ProductSnapshot s : snaps) {
                String key = s.getVendor() + ":" + s.getExternalProductId();
                dedup.putIfAbsent(key, s);
            }

            if (dedup.size() >= size) break;
        }

        List<ProductSnapshot> merged = new ArrayList<>(dedup.values());
        return merged.subList(0, Math.min(merged.size(), size));
    }

    private List<ProductSnapshot> fetchByQuery(String keywordOrNull, List<Long> categoryIdsOrNull, int size) {
        int pageSize = Math.min(size, 50);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("method", "aliexpress.affiliate.product.query");
        params.put("app_key", appKey);
        params.put("sign_method", signMethod);
        params.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        params.put("v", v);
        params.put("format", format);

        params.put("page_no", "1");
        params.put("page_size", String.valueOf(pageSize));
        params.put("ship_to_country", shipToCountry);
        params.put("target_language", targetLanguage);
        params.put("target_currency", targetCurrency);

        params.put("platform_product_type", "ALL");
        params.put("sort", "SALE_PRICE_ASC");

        if (keywordOrNull != null && !keywordOrNull.isBlank()) {
            params.put("keywords", keywordOrNull);
        }

        if (categoryIdsOrNull != null && !categoryIdsOrNull.isEmpty()) {
            String joined = String.join(",", categoryIdsOrNull.stream().map(String::valueOf).toList());
            params.put("category_ids", joined);
        }

        String sign = AliExpressSigner.signSync(params, appSecret);
        params.put("sign", sign);

        String url = AliExpressUrlBuilder.build(baseUrl, syncPath, params);
        System.out.println("[ALI] request url = " + url);

        String raw = webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> System.out.println("[ALI] raw=" + body))
                .block();

        if (raw == null || raw.isBlank()) return List.of();

        try {
            AliExpressAffiliateProductQueryResponse res =
                    objectMapper.readValue(raw, AliExpressAffiliateProductQueryResponse.class);

            if (res.body() == null || res.body().respResult() == null) return List.of();

            var rr = res.body().respResult();
            System.out.println("[ALI] respCode=" + rr.respCode() + " msg=" + rr.respMsg());

            List<AliExpressProductDto> products =
                    (rr.result() != null && rr.result().products() != null) ? rr.result().products().product() : null;

            System.out.println("[ALI] parsed product size=" + (products == null ? 0 : products.size()));

            if (rr.respCode() == null || rr.respCode() != 200 || products == null || products.isEmpty()) return List.of();

            return products.stream().map(this::toSnapshot).toList();
        } catch (Exception e) {
            System.out.println("[ALI] parse exception=" + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private ProductSnapshot toSnapshot(AliExpressProductDto it) {
        ProductCategory category = AliExpressCategoryToProductCategoryMapper.toProductCategory(
                it.secondLevelCategoryId(),
                it.firstLevelCategoryId()
        );

        Long id = it.productId();
        String name = it.productTitle();
        String image = it.productMainImageUrl();

        String productUrl = (it.promotionLink() != null && !it.promotionLink().isBlank())
                ? it.promotionLink()
                : it.productDetailUrl();

        Integer listPrice = toIntPricePreferTarget(it.targetAppSalePrice(), it.targetSalePrice());
        Integer discountRate = parsePercent(it.discount());
        Integer discountedPrice = toIntPricePreferTarget(
                it.targetAppSalePrice(),
                it.targetSalePrice()
        );

        return ProductSnapshot.builder()
                .vendor(Vendor.ALI)
                .externalProductId(id)
                .category(category)
                .name(name)
                .listPrice(listPrice)
                .discountedPrice(discountedPrice)
                .discountRate(discountRate)
                .productImageUrl(image)
                .productUrl(productUrl)
                .impressionUrl(null)
                .isRocket(false)
                .build();
    }



    private static Integer toIntPricePreferTarget(String targetApp, String targetSale) {
        String s = (targetApp != null && !targetApp.isBlank()) ? targetApp : targetSale;
        if (s == null || s.isBlank()) return 0;
        try {
            BigDecimal bd = new BigDecimal(s.trim());
            return bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private static Integer parsePercent(String s) {
        if (s == null) return 0;
        String t = s.trim().replace("%", "");
        try {
            return (int) Math.round(Double.parseDouble(t));
        } catch (Exception e) {
            return 0;
        }
    }
}
