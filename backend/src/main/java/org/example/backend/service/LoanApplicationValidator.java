package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.config.LoanProperties;
import org.example.backend.dto.LoanRejectionReason;
import org.example.backend.dto.ValidationDecision;
import org.example.backend.exception.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class LoanApplicationValidator {

    private final LoanProperties loanProperties;

    /**
     * Age validator.
     *
     * @param personalCode Customers personal code.
     * @return Validation decision based on the age of the customer. If the customer is too old, the decision will be rejected with the reason CUSTOMER_TOO_OLD. Otherwise, the decision will be accepted.
     */
    public ValidationDecision validateAge(String personalCode) {
        int customersAge = calculateAge(parseBirthDate(personalCode));

        if (customersAge > loanProperties.maxAge()) {
            return ValidationDecision.rejected(LoanRejectionReason.CUSTOMER_TOO_OLD);
        }

        return ValidationDecision.accepted();
    }

    /**
     * Simple personal code  validator. For the simplicity of the test work, I assume that every month has 31 days.
     * Also for simplicity I assumed that we do not have people older than 120 among us anymore.
     *
     * @param personalCode Customers personal code.
     */
    public void validateCustomerPersonalCode(String personalCode) {
        if (personalCode == null || personalCode.length() != 11 || !personalCode.chars().allMatch(Character::isDigit)) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }

        int firstDigit = Character.getNumericValue(personalCode.charAt(0));
        if (firstDigit < 1 || firstDigit > 8) {
            throw new InvalidPersonalCodeException("Invalid personal code!");
        }

        LocalDate today = LocalDate.now();
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
        } catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException e) {
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
