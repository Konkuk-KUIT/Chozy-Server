package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedRepost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepostRepository extends JpaRepository<FeedRepost, Long> {

    List<FeedRepost> findByUserIdAndSourceFeedIdIn(Long userId, List<Long> sourceFeedIds);

    boolean existsByUserIdAndSourceFeedId(Long userId, Long sourceFeedId);

    List<FeedRepost> findByTargetFeedIdIn(List<Long> targetFeedIds);
}
