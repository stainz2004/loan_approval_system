package org.example.backend.service;

import org.example.backend.config.LoanProperties;
import org.example.backend.entity.LoanConfig;
import org.example.backend.repository.LoanConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanConfigServiceTest {

    @Mock
    private LoanConfigRepository loanConfigRepository;

    @Mock
    private LoanProperties loanProperties;

    @InjectMocks
    private LoanConfigService loanConfigService;

    private LoanConfig maxAgeConfig;
    private LoanConfig baseInterestConfig;

    @BeforeEach
    void setUp() {
        maxAgeConfig = new LoanConfig();
        maxAgeConfig.setKey("max_age");
        maxAgeConfig.setValue("75");

        baseInterestConfig = new LoanConfig();
        baseInterestConfig.setKey("base_interest");
        baseInterestConfig.setValue("3.5");
    }


    @Test
    void getMaxAge_returnsValueFromDatabase_whenConfigExists() {
        when(loanConfigRepository.findById("max_age")).thenReturn(Optional.of(maxAgeConfig));

        int result = loanConfigService.getMaxAge();

        assertThat(result).isEqualTo(75);
    }

    @Test
    void getMaxAge_returnsFallback_whenConfigNotFound() {
        when(loanConfigRepository.findById("max_age")).thenReturn(Optional.empty());
        when(loanProperties.maxAge()).thenReturn(70);

        int result = loanConfigService.getMaxAge();

        assertThat(result).isEqualTo(70);
    }

    @Test
    void getMaxAge_returnsFallback_whenConfigValueIsNotAValidInteger() {
        maxAgeConfig.setValue("not-a-number");
        when(loanConfigRepository.findById("max_age")).thenReturn(Optional.of(maxAgeConfig));
        when(loanProperties.maxAge()).thenReturn(70);

        int result = loanConfigService.getMaxAge();

        assertThat(result).isEqualTo(70);
    }


    @Test
    void getBaseInterest_returnsValueFromDatabase_whenConfigExists() {
        when(loanConfigRepository.findById("base_interest")).thenReturn(Optional.of(baseInterestConfig));

        BigDecimal result = loanConfigService.getBaseInterest();

        assertThat(result).isEqualByComparingTo(new BigDecimal("3.5"));
    }

    @Test
    void getBaseInterest_returnsFallback_whenConfigNotFound() {
        when(loanConfigRepository.findById("base_interest")).thenReturn(Optional.empty());
        when(loanProperties.baseInterest()).thenReturn(new BigDecimal("2.5"));

        BigDecimal result = loanConfigService.getBaseInterest();

        assertThat(result).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    void getBaseInterest_returnsFallback_whenConfigValueIsNotAValidDecimal() {
        baseInterestConfig.setValue("not-a-number");
        when(loanConfigRepository.findById("base_interest")).thenReturn(Optional.of(baseInterestConfig));
        when(loanProperties.baseInterest()).thenReturn(new BigDecimal("2.5"));

        BigDecimal result = loanConfigService.getBaseInterest();

        assertThat(result).isEqualByComparingTo(new BigDecimal("2.5"));
    }
}
