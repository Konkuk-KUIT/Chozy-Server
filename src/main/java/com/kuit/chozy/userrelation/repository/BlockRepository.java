package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    /** 나를 차단한 사람(blocker)의 ID 목록. 이 사람들의 게시물은 내 피드에 노출하지 않음. */
    @Query("SELECT b.blockerId FROM Block b WHERE b.blockedId = :blockedId AND b.active = true")
    List<Long> findBlockerIdsByBlockedIdAndActiveTrue(@Param("blockedId") Long blockedId);

    /** 내가 차단한 사람(blocked)의 ID 목록. 이 사람들의 게시물은 내 피드에 노출하지 않음. */
    @Query("SELECT b.blockedId FROM Block b WHERE b.blockerId = :blockerId AND b.active = true")
    List<Long> findBlockedIdsByBlockerIdAndActiveTrue(@Param("blockerId") Long blockerId);

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<Block> findByBlockerIdAndBlockedIdAndActiveTrue(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedIdAndActiveTrue(Long blockerId, Long blockedId);

    List<Block> findByBlockerIdAndBlockedIdInAndActiveTrue(Long blockerId, Collection<Long> blockedIds);

    Page<Block> findByBlockerIdAndActiveTrueOrderByCreatedAtDesc(Long blockerId, Pageable pageable);
}
