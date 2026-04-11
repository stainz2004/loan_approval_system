package org.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "loan")
public record LoanProperties(int maxAge, BigDecimal baseInterest) {}