package com.kuit.chozy.user.repository;

import com.kuit.chozy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(List<Long> ids);
}