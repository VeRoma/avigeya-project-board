package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}