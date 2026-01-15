package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    List<FollowRequest> findByRequesterIdAndTargetIdInAndStatus(
            Long requesterId,
            List<Long> targetIds,
            FollowRequestStatus status
    );

    // 이미 PENDING 요청이 있는지 체크(4098)
    boolean existsByRequesterIdAndTargetIdAndStatus(Long requesterId, Long targetId, FollowRequestStatus status);

    // 요청 취소/조회용
    Optional<FollowRequest> findByRequesterIdAndTargetIdAndStatus(Long requesterId, Long targetId, FollowRequestStatus status);

    // status 변경(팔로우 요청 처리에서 사용 예정)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update FollowRequest fr set fr.status = :status where fr.id = :id")
    int changeStatus(@Param("id") Long id, @Param("status") FollowRequestStatus status);

    // 차단 시 펜딩 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from FollowRequest fr
         where fr.requesterId = :requesterId
           and fr.targetId = :targetId
           and fr.status = com.kuit.chozy.userrelation.domain.FollowRequestStatus.PENDING
    """)
    int deletePending(@Param("requesterId") Long requesterId,
                      @Param("targetId") Long targetId);

    // 양방향 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from FollowRequest fr
         where ((fr.requesterId = :meId and fr.targetId = :targetUserId)
             or (fr.requesterId = :targetUserId and fr.targetId = :meId))
           and fr.status = com.kuit.chozy.userrelation.domain.FollowRequestStatus.PENDING
    """)
    int deletePendingBetween(@Param("meId") Long meId,
                             @Param("targetUserId") Long targetUserId);
}
