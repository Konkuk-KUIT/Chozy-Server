package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<Block> findByBlockerIdAndBlockedIdAndActiveTrue(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedIdAndActiveTrue(Long blockerId, Long blockedId);

    List<Block> findByBlockerIdAndBlockedIdInAndActiveTrue(Long blockerId, Collection<Long> blockedIds);

    Page<Block> findByBlockerIdAndActiveTrueOrderByBlockedAtDesc(Long blockerId, Pageable pageable);
}
