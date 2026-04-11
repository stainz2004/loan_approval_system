package org.example.backend.repository;

import org.example.backend.entity.LoanConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanConfigRepository extends JpaRepository<LoanConfig, String> {
}
