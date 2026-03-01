package com.legalbureau.repository;


import com.legalbureau.entity.Lawyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
    @Query("SELECT DISTINCT l FROM Lawyer l LEFT JOIN l.specializations s " +
            "WHERE (:name IS NULL OR :name = '' OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR s.id = :categoryId)")
    Page<Lawyer> searchAndFilter(@Param("name") String name,
                                 @Param("categoryId") Long categoryId,
                                 Pageable pageable);
}
