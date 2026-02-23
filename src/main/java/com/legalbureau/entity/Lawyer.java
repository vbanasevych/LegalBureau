package com.legalbureau.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "lawyers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Lawyer extends User {

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    private String city;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ManyToMany
    @JoinTable(
            name = "lawyer_categories",
            joinColumns = @JoinColumn(name = "lawyer_id"),
            inverseJoinColumns = @JoinColumn(name = "case_category_id")
    )
    private Set<CaseCategory> specializations;

    @OneToMany(mappedBy = "lawyer")
    private List<LegalCase> managedCases;
}
