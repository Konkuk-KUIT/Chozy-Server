package com.kuit.chozy.user.repository;

import com.kuit.chozy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(List<Long> ids);

    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);

    Optional<User> findByLoginId(String loginId);

}