package com.legalbureau.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.legalbureau.entity.enums.CaseStatus;
import java.time.LocalDateTime;
import java.util.List;
import com.legalbureau.entity.enums.CaseResult;

@Entity
@Table(name = "legal_cases")
@Getter
@Setter
public class LegalCase extends BaseEntity {

    @Column(name = "case_number", nullable = false, unique = true)
    private String caseNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "lawyer_id")
    private Lawyer lawyer;

    @ManyToOne
    @JoinColumn(name = "case_category_id")
    private CaseCategory category;

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private List<CaseService> services;

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private List<Hearing> hearings;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private CaseResult result;
}
