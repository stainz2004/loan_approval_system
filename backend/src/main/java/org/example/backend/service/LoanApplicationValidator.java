package org.example.backend.service;

import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.ValidationDecision;
import org.example.backend.exception.InvalidPersonalCodeException;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

@Service
public class LoanApplicationValidator {

    public ValidationDecision validateCreateRequest(LoanApplicationRequest request) {
        String personalCode = request.getPersonalCode();

        validateCustomerPersonalCode(personalCode);

        if (calculateAge(parseBirthDate(personalCode)) > 70) {
            return new ValidationDecision(false, "The user is too old!");
        }
        return new ValidationDecision(true, null);
    }

    /**
     * Simple personal code  validator. For the simplicity of the test work, I assume that every month has 31 days.
     * Also for simplicity I assumed that we do not have people older than 120 among us anymore.
     *
     * @param personalCode Customers personal code.
     */
    private void validateCustomerPersonalCode(String personalCode) {
        LocalDate today = LocalDate.now();
        LocalDate minAllowedBirthDate = today.minusYears(120);

        LocalDate birthDate = parseBirthDate(personalCode);

        if (birthDate.isAfter(today) || birthDate.isBefore(minAllowedBirthDate)) {
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

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
