package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.CloseFriend;
import com.kuit.chozy.userrelation.dto.response.CloseFriendItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CloseFriendRepository extends JpaRepository<CloseFriend, Long> {

    boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);

    Optional<CloseFriend> findByUserIdAndTargetUserId(Long userId, Long targetUserId);

    void deleteByUserIdAndTargetUserId(Long userId, Long targetUserId);

    @Query("""
            select new com.kuit.chozy.userrelation.dto.response.CloseFriendItemResponse(
                u.id,
                u.loginId,
                u.nickname,
                u.profileImageUrl,
                coalesce(u.isAccountPublic, false),
                cf.createdAt
            )
            from CloseFriend cf
            join User u on u.id = cf.targetUserId
            where cf.userId = :meUserId
            order by cf.createdAt desc
            """)
    Page<CloseFriendItemResponse> findCloseFriendItems(@Param("meUserId") Long meUserId, Pageable pageable);
}