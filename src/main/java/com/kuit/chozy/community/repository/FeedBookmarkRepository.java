package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedBookmarkRepository extends JpaRepository<FeedBookmark, Long> {

    List<FeedBookmark> findByUserIdAndFeedIdIn(Long userId, List<Long> feedIds);

    List<FeedBookmark> findByFeedId(Long feedId);

    Page<FeedBookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
