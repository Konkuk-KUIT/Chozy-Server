package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로워 목록: 나(followingId)를 팔로우하는 사람들
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    // 팔로잉 목록: 내가(followerId)를 팔로우하는 사람들
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    // 내가 ids를 팔로우하는지(내 -> 상대)
    List<Follow> findByFollowerIdAndFollowingIdIn(Long followerId, List<Long> followingIds);

    // ids가 나를 팔로우하는지(상대 -> 나)
    List<Follow> findByFollowerIdInAndFollowingId(List<Long> followerIds, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Follow f
           set f.status = :toStatus
         where f.followerId = :followerId
           and f.followingId = :followingId
           and f.status = :fromStatus
    """)
    int updateStatus(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId,
            @Param("fromStatus") FollowStatus fromStatus,
            @Param("toStatus") FollowStatus toStatus
    );
}