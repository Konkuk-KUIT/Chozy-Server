package com.kuit.chozy.userrelation.repository;

import com.kuit.chozy.userrelation.domain.Mute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MuteRepository extends JpaRepository<Mute, Long> {

    Optional<Mute> findByMuterIdAndMutedId(Long muterId, Long mutedId);

    Optional<Mute> findByMuterIdAndMutedIdAndActiveTrue(Long muterId, Long mutedId);

    boolean existsByMuterIdAndMutedIdAndActiveTrue(Long muterId, Long mutedId);

    @Query("SELECT m.mutedId FROM Mute m WHERE m.muterId = :muterId AND m.active = true")
    List<Long> findMutedIdsByMuterIdAndActiveTrue(@Param("muterId") Long muterId);

    Page<Mute> findByMuterIdAndActiveTrueOrderByMutedAtDesc(Long muterId, Pageable pageable);
}
