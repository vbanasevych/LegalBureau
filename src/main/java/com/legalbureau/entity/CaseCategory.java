package com.legalbureau.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "case_categories")
@Getter
@Setter
public class CaseCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}