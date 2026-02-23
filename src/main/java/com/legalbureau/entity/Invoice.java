package com.legalbureau.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice extends BaseEntity {

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "is_paid")
    private Boolean isPaid;

    @OneToOne
    @JoinColumn(name = "legal_case_id", nullable = false, unique = true)
    private LegalCase legalCase;
}
