package org.example.backend.repository;


import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByLoanApplicationStatus(LoanApplicationStatus status);

    boolean existsByPersonalCodeAndLoanApplicationStatus(String personalCode, LoanApplicationStatus loanApplicationStatus);

    LoanApplication findByIdAndLoanApplicationStatus(Long id, LoanApplicationStatus loanApplicationStatus);

    List<LoanApplication> findAllByPersonalCodeAndLoanApplicationStatus(String personalCode, LoanApplicationStatus loanApplicationStatus);
}