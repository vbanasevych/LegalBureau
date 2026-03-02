package com.legalbureau.repository;


import com.legalbureau.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'CLIENT' AND " +
            "(:search IS NULL OR LOWER(u.fullName) LIKE :search OR LOWER(u.email) LIKE :search)")
    org.springframework.data.domain.Page<User> findFilteredClients(@org.springframework.data.repository.query.Param("search") String search,
                                                                   org.springframework.data.domain.Pageable pageable);
}