package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.LoanRejectionReason;
import org.example.backend.dto.LoanApplicationDecisionResponse;
import org.example.backend.exception.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanApplicationValidator {

    private final LoanConfigService loanConfigService;

    /**
     * Age validator.
     *
     * @param personalCode Customers personal code.
     * @return Validation decision based on the age of the customer. If the customer is too old, the decision will be rejected with the reason CUSTOMER_TOO_OLD. Otherwise, the decision will be accepted.
     */
    public LoanApplicationDecisionResponse validateAge(String personalCode) {
        int customersAge = calculateAge(parseBirthDate(personalCode));
        int maxAge = loanConfigService.getMaxAge();

        log.debug("Age validation: customerAge={}, maxAge={}", customersAge, maxAge);

        if (customersAge > maxAge) {
            log.debug("Age validation failed: customer is too old (age={})", customersAge);
            return LoanApplicationDecisionResponse.rejected(LoanRejectionReason.CUSTOMER_TOO_OLD);
        }

        return LoanApplicationDecisionResponse.accepted();
    }

    /**
     * Personal code validator. This method checks if the personal code is valid based on the personal code rules.
     *
     * @param personalCode Customers personal code.
     */
    public void validateCustomerPersonalCode(String personalCode) {
        if (personalCode == null || personalCode.length() != 11 || !personalCode.chars().allMatch(Character::isDigit)) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }

        int firstDigit = Character.getNumericValue(personalCode.charAt(0));
        if (firstDigit < 1 || firstDigit > 6) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }

        LocalDate today = LocalDate.now();
        // For simplicity, I did some research and found that the oldest human being right now is 116 years old, so I assume that there are not people older than 120.
        LocalDate minAllowedBirthDate = today.minusYears(120);

        LocalDate birthDate = parseBirthDate(personalCode);

        if (birthDate.isAfter(today) || birthDate.isBefore(minAllowedBirthDate)) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }

        if (!isValidChecksum(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }
    }

    /**
     * Parser for personal code.
     *
     * @param personalCode Customers personal code.
     * @return Customers birthdate in right format.
     */
    private LocalDate parseBirthDate(String personalCode) {
        try {
            int firstNumber = Character.getNumericValue(personalCode.charAt(0));
            int yy = Integer.parseInt(personalCode.substring(1, 3));
            int mm = Integer.parseInt(personalCode.substring(3, 5));
            int dd = Integer.parseInt(personalCode.substring(5, 7));

            int century;
            if (firstNumber == 1 || firstNumber == 2) century = 1800;
            else if (firstNumber == 3 || firstNumber == 4) century = 1900;
            else if (firstNumber == 5 || firstNumber == 6) century = 2000;
            else throw new InvalidPersonalCodeException("Invalid personal code!");

            return LocalDate.of(century + yy, mm, dd);
        } catch (DateTimeException e) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }
    }

    /**
     * Age calculator based on birthdate.
     *
     * @param birthDate Customers birthdate.
     * @return Customers age in years.
     */
    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Checksum validator for personal code.
     *
     * @param personalCode Customers personal code.
     * @return True if the checksum is valid, false otherwise.
     */
    private boolean isValidChecksum(String personalCode) {
        int[] weights1 = {1,2,3,4,5,6,7,8,9,1};
        int[] weights2 = {3,4,5,6,7,8,9,1,2,3};

        // Every personal code number is times by the corresponding weight and it is all summed up.
        // The sum is divided by 11 and the remainder is the control number for the last digit.
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(personalCode.charAt(i)) * weights1[i];
        }

        int control = sum % 11;
        if (control < 10) {
            return control == Character.getNumericValue(personalCode.charAt(10));
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(personalCode.charAt(i)) * weights2[i];
        }

        control = sum % 11;
        if (control < 10) {
            return control == Character.getNumericValue(personalCode.charAt(10));
        }

        return Character.getNumericValue(personalCode.charAt(10)) == 0;
    }
}
