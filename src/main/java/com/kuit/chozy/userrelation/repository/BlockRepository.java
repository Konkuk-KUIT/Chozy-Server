package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findByBlockerIdAndBlockedIdIn(Long blockerId, List<Long> blockedIds);
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}