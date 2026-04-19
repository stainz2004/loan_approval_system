package org.example.backend.service;

import org.example.backend.dto.LoanApplicationDecisionResponse;
import org.example.backend.entity.LoanRejectionReason;
import org.example.backend.exception.InvalidPersonalCodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationValidatorTest {

    @Mock
    private LoanConfigService loanConfigService;

    @InjectMocks
    private LoanApplicationValidator validator;


    private static final String VALID_CODE_BORN_1980 = "38001085718";
    private static final String VALID_CODE_BORN_1945 = "34501234215";


    @Test
    void validateCustomerPersonalCode_doesNotThrow_whenPersonalCodeIsValid() {
        validator.validateCustomerPersonalCode(VALID_CODE_BORN_1980);
        // no exception expected
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenPersonalCodeIsNull() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode(null))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenPersonalCodeIsTooShort() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("3800108571"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenPersonalCodeIsTooLong() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("380010857180"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenPersonalCodeContainsNonDigits() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("3800108571A"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenFirstDigitIsZero() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("08001085718"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenFirstDigitIsNine() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("98001085718"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenBirthDateIsInFuture() {
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode("59901010000"))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }

    @Test
    void validateCustomerPersonalCode_throwsInvalidPersonalCodeException_whenChecksumIsInvalid() {
        String invalidChecksum = VALID_CODE_BORN_1980.substring(0, 10) + "0";
        assertThatThrownBy(() -> validator.validateCustomerPersonalCode(invalidChecksum))
                .isInstanceOf(InvalidPersonalCodeException.class);
    }


    @Test
    void validateAge_returnsAccepted_whenCustomerAgeIsWithinLimit() {
        when(loanConfigService.getMaxAge()).thenReturn(75);

        LoanApplicationDecisionResponse response = validator.validateAge(VALID_CODE_BORN_1980);

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void validateAge_returnsRejected_whenCustomerIsTooOld() {
        when(loanConfigService.getMaxAge()).thenReturn(75);

        LoanApplicationDecisionResponse response = validator.validateAge(VALID_CODE_BORN_1945);

        assertThat(response.isAccepted()).isFalse();
        assertThat(response.rejectionReason()).isEqualTo(LoanRejectionReason.CUSTOMER_TOO_OLD);
    }

    @Test
    void validateAge_returnsAccepted_whenCustomerAgeEqualsMaxAge() {
        when(loanConfigService.getMaxAge()).thenReturn(81);

        LoanApplicationDecisionResponse response = validator.validateAge(VALID_CODE_BORN_1945);

        // The validator uses strictly-greater-than (age > maxAge), so age == maxAge is accepted.
        assertThat(response.isAccepted()).isTrue();
    }

    @Test
    void validateAge_returnsAccepted_whenMaxAgeIsVeryHigh() {
        when(loanConfigService.getMaxAge()).thenReturn(200);

        LoanApplicationDecisionResponse response = validator.validateAge(VALID_CODE_BORN_1945);

        assertThat(response.isAccepted()).isTrue();
    }
}
