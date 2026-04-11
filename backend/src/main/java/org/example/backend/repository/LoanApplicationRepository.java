package org.example.backend.repository;


import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    @Query("SELECT la FROM LoanApplication la " +
           "LEFT JOIN FETCH la.paymentSchedule ps " +
           "LEFT JOIN FETCH ps.items " +
           "WHERE la.loanApplicationStatus = :status")
    List<LoanApplication> findByLoanApplicationStatus(@Param("status") LoanApplicationStatus status);


    boolean existsByPersonalCodeAndLoanApplicationStatus(String personalCode, LoanApplicationStatus loanApplicationStatus);


    @Query("SELECT la FROM LoanApplication la " +
           "LEFT JOIN FETCH la.paymentSchedule ps " +
           "LEFT JOIN FETCH ps.items " +
           "WHERE la.id = :id AND la.loanApplicationStatus = :status")
    Optional<LoanApplication> findByIdAndLoanApplicationStatus(@Param("id") Long id, @Param("status") LoanApplicationStatus status);


    @Query("SELECT la FROM LoanApplication la " +
           "LEFT JOIN FETCH la.paymentSchedule ps " +
           "LEFT JOIN FETCH ps.items " +
           "WHERE la.personalCode = :personalCode AND la.loanApplicationStatus = :status")
    List<LoanApplication> findAllByPersonalCodeAndLoanApplicationStatus(@Param("personalCode") String personalCode, @Param("status") LoanApplicationStatus status);
}