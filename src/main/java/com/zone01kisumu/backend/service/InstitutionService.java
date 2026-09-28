package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.dto.InstitutionLoginDTO;
import com.zone01kisumu.backend.dto.InstitutionRegistrationDTO;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.repository.InstitutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Service class for institution-related operations
@Service
@RequiredArgsConstructor
public class InstitutionService implements UserDetailsService {

    /**
     * Repository for Institution persistence operations.
     */
    @Autowired
    private InstitutionRepository institutionRepository;

    // PasswordEncoder for encoding and matching passwords.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register a new institution
    @Transactional
    public Institution register(InstitutionRegistrationDTO dto) {
        if (institutionRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (institutionRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        Institution institution = Institution.builder()
                .id(dto.getId())
                .institutionName(dto.getInstitutionName())
                .registrationNumber(dto.getRegistrationNumber())
                .schoolType(Institution.SchoolType.valueOf(dto.getSchoolType().toUpperCase()))
                .educationSystem(
                        Institution.EducationSystem.valueOf(dto.getEducationSystem().toUpperCase().replace("-", "_")))
                .location(dto.getLocation())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        return institutionRepository.save(institution);
    }

    // Login an institution
    public Institution login(InstitutionLoginDTO dto) {
        Institution institution = institutionRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), institution.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return institution;
    }

    // Find an institution by email
    public Optional<Institution> findByEmail(String email) {
        return institutionRepository.findByEmail(email);
    }

    // Find an institution by phone
    public Optional<Institution> findByPhone(String phone) {
        return institutionRepository.findByPhone(phone);
    }

    // Map an institution to a registration DTO
    public static InstitutionRegistrationDTO mapToRegistrationDTO(Institution institution) {
        return InstitutionRegistrationDTO.builder()
                .id(institution.getId())
                .institutionName(institution.getInstitutionName())
                .registrationNumber(institution.getRegistrationNumber())
                .schoolType(institution.getSchoolType().name())
                .educationSystem(institution.getEducationSystem().name())
                .location(institution.getLocation())
                .email(institution.getEmail())
                .phone(institution.getPhone())
                .build();
    }

    // Map an institution to a login DTO
    public static InstitutionLoginDTO mapToLoginDTO(Institution institution) {
        return InstitutionLoginDTO.builder()
                .id(institution.getId())
                .email(institution.getEmail())
                .build();
    }

    // Load user details by username
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Institution institution = institutionRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Institution not found"));

        return User.builder()
                .username(institution.getEmail())
                .password(institution.getPassword())
                .authorities("ROLE_INSTITUTION")
                .build();
    }

}
