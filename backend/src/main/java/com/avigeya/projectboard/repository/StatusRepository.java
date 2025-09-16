package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
}