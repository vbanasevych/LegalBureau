package com.legalbureau.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import com.legalbureau.entity.enums.Role;
import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Pattern(regexp = "^\\d{4}-\\d{3}-\\d{3}-\\d{4}$", message = "Не правильний формат номеру")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "client")
    private List<LegalCase> clientCases;

    @Column(name = "is_active")
    private boolean isActive = true;
}
