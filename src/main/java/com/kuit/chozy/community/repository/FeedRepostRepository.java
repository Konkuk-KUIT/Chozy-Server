package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedRepost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedRepostRepository extends JpaRepository<FeedRepost, Long> {

    List<FeedRepost> findByUserIdAndSourceFeedIdIn(Long userId, List<Long> sourceFeedIds);

    boolean existsByUserIdAndSourceFeedId(Long userId, Long sourceFeedId);

    List<FeedRepost> findByTargetFeedIdIn(List<Long> targetFeedIds);

    @Query("SELECT fr FROM FeedRepost fr WHERE fr.sourceFeedId = :feedId OR fr.targetFeedId = :feedId")
    List<FeedRepost> findBySourceFeedIdOrTargetFeedId(@Param("feedId") Long feedId);

    Optional<FeedRepost> findByUserIdAndSourceFeedId(Long userId, Long sourceFeedId);
}
