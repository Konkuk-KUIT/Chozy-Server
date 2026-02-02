package com.kuit.chozy.postaction.controller;

import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.postaction.dto.QuoteCreateRequest;
import com.kuit.chozy.postaction.service.QuoteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/quote")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ApiResponse<String> quote(@RequestBody QuoteCreateRequest request) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = quoteService.quote(meId, request);
        return ApiResponse.success(result);
    }
}