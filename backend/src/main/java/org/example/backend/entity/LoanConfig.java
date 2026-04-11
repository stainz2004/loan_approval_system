package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "loan_config")
@Getter
@Setter
public class LoanConfig {

    @Id
    @Column(name = "config_key")
    private String key;

    private String value;
}
