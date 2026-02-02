package com.kuit.chozy.postaction.repository;

import com.kuit.chozy.postaction.domain.PostAction;
import com.kuit.chozy.postaction.domain.PostActionStatus;
import com.kuit.chozy.postaction.domain.PostActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostActionRepository extends JpaRepository<PostAction, Long> {

    boolean existsByPostIdAndUserIdAndTypeAndStatusNot(
            Long postId,
            Long userId,
            PostActionType type,
            PostActionStatus status
    );

    Optional<PostAction> findByPostIdAndUserIdAndTypeAndStatusNot(
            Long postId,
            Long userId,
            PostActionType type,
            PostActionStatus status
    );
}