package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entity class for Institutions
@Entity
@Table(name = "institution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    // ID of the institution
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_name", nullable = false, length = 255)
    private String institutionName;
    @Column(name = "registration_number", length = 255)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_type")
    private SchoolType schoolType;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_system")
    private EducationSystem educationSystem;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(length = 255)
    private String password;

    @Column(name = "postal_address", length = 255)
    private String postalAddress;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String logo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "principal_name", length = 255)
    private String principalName;

    @Column(name = "principal_email", unique = true, length = 255)
    private String principalEmail;

    @Column(name = "principal_phone", unique = true, length = 20)
    private String principalPhone;

    @Column(name = "established_year")
    private Integer establishedYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "accreditation_status")
    @Builder.Default
    private AccreditationStatus accreditationStatus = AccreditationStatus.PENDING;

    @Column(name = "accreditation_body", length = 255)
    private String accreditationBody;

    @Column(name = "accreditation_date")
    private LocalDateTime accreditationDate;

    // Link to Teacher entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Teacher createdBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Enums
    public enum SchoolType {
        PRIMARY, SECONDARY, TERTIARY, COLLAGE, UNIVERSITY
    }

    public enum EducationSystem {
        CBC, EIGHT_FOUR_FOUR, IGCSE, IB, OTHER
    }

    public enum AccreditationStatus {
        ACCREDITED, PENDING, NOT_ACCREDITED
    }

}
