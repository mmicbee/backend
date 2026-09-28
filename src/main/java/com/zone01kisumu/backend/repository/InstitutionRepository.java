package com.zone01kisumu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zone01kisumu.backend.model.Institution;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByEmail(String email);
    Optional<Institution> findByPhone(String phone);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
