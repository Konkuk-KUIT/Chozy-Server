package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {

    List<FeedImage> findByFeed_IdIn(List<Long> feedIds);

    List<FeedImage> findByFeed_Id(Long feedId);
}
