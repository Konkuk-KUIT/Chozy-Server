package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.RecentViewedProfile;
import com.kuit.chozy.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentViewedProfileRepository extends JpaRepository<RecentViewedProfile, Integer> {
    Optional<RecentViewedProfile> findByViewer_IdAndVisitedUser_Id(Long viewerId, Long visitedUserId);
    List<RecentViewedProfile> findTop8ByViewer_IdAndStatusOrderByUpdatedAtDesc(Long viewerId, UserStatus status);
}
