package com.zone01kisumu.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zone01kisumu.backend.model.Student;
import java.util.Optional;

@Repository // Marks this interface as a Spring data Reposity component
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);
    Optional<Student> findByPhone(String phone);
}