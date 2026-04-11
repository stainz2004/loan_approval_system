package org.example.backend.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.LoanProperties;
import org.example.backend.repository.LoanConfigRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanConfigService {

    private static final String MAX_AGE_KEY = "max_age";
    private static final String BASE_INTEREST_KEY = "base_interest";

    private final LoanConfigRepository loanConfigRepository;
    private final LoanProperties loanProperties;

    /**
     * Returns the maximum allowed customer age for a loan application.
     * falls back to the value configured application.properties.
     *
     * @return the maximum allowed age in years
     */
    @Transactional(readOnly = true)
    public int getMaxAge() {
        return loanConfigRepository.findById(MAX_AGE_KEY)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getValue());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid '{}' config value '{}', falling back to default", MAX_AGE_KEY, config.getValue());
                        return loanProperties.maxAge();
                    }
                })
                .orElse(loanProperties.maxAge());
    }

    /**
     * Returns the base interest rate for loan calculations. If the value in the database is invalid or not found, it falls back to the value configured in application.properties.
     *
     * @return the base interest rate as a BigDecimal
     */
    @Transactional(readOnly = true)
    public BigDecimal getBaseInterest() {
        return loanConfigRepository.findById(BASE_INTEREST_KEY)
                .map(config -> {
                    try {
                        return new BigDecimal(config.getValue());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid '{}' config value '{}', falling back to default",
                                BASE_INTEREST_KEY, config.getValue());
                        return loanProperties.baseInterest(); // fallback in application.properties
                    }
                })
                .orElse(loanProperties.baseInterest());
    }
}
