package com.kuit.chozy.user.repository;

import com.kuit.chozy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}