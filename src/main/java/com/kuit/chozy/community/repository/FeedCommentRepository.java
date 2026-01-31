package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    List<FeedComment> findByFeedId(Long feedId);

    List<FeedComment> findByFeedIdAndParentCommentIdIsNullOrderByCreatedAtAsc(Long feedId);

    List<FeedComment> findByFeedIdAndParentCommentIdOrderByCreatedAtAsc(Long feedId, Long parentCommentId);

    List<FeedComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    long countByFeedId(Long feedId);

    long countByParentCommentId(Long parentCommentId);
}
