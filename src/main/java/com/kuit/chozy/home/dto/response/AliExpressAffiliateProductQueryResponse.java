package com.kuit.chozy.home.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kuit.chozy.home.dto.response.AliExpressProductDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AliExpressAffiliateProductQueryResponse(
        @JsonProperty("aliexpress_affiliate_product_query_response")
        Body body
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonProperty("resp_result") RespResult respResult
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespResult(
            @JsonProperty("resp_code") Integer respCode,
            @JsonProperty("resp_msg") String respMsg,
            @JsonProperty("result") Result result
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("current_page_no") Integer currentPageNo,
            @JsonProperty("current_record_count") Integer currentRecordCount,
            @JsonProperty("products") Products products
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Products(
            @JsonProperty("product") List<AliExpressProductDto> product
    ) {}
}
