package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.FeedCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedCommentReactionRepository extends JpaRepository<FeedCommentReaction, Long> {

    Optional<FeedCommentReaction> findByUserIdAndCommentId(Long userId, Long commentId);

    List<FeedCommentReaction> findByUserIdAndCommentIdIn(Long userId, List<Long> commentIds);

    List<FeedCommentReaction> findByCommentIdIn(List<Long> commentIds);
}
