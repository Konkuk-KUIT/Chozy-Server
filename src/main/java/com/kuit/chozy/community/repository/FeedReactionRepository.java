package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedReactionRepository extends JpaRepository<FeedReaction, Long> {

    Optional<FeedReaction> findByUserIdAndFeedId(Long userId, Long feedId);

    List<FeedReaction> findByUserIdAndFeedIdIn(Long userId, List<Long> feedIds);

    boolean existsByUserIdAndFeedId(Long userId, Long feedId);
}
