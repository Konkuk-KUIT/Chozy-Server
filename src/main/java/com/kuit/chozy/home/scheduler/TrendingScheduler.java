package com.kuit.chozy.home.scheduler;

import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.home.service.TrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendingScheduler {

    private final TrendingService trendingService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void updateDailyTrending(){
        trendingService.rebuildDailyTrending();
    }
}
