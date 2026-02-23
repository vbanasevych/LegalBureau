package com.legalbureau.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "case_services")
@Getter
@Setter
public class CaseService extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "date_added", insertable = false, updatable = false)
    private LocalDate dateAdded;

    @ManyToOne
    @JoinColumn(name = "legal_case_id", nullable = false)
    private LegalCase legalCase;
}
