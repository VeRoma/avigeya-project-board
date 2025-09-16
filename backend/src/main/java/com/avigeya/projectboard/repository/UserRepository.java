package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTgUserId(Long tgUserId);
}
