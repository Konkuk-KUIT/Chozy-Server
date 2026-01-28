package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.dto.response.UserLoginIdRecommendProjection;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityUserRepository extends JpaRepository<User, Long> {
    List<UserLoginIdRecommendProjection> findTop10ByStatusAndIsAccountPublicTrueAndLoginIdContainingOrderByLoginIdAsc(
            UserStatus status,
            String loginId
    );
}
