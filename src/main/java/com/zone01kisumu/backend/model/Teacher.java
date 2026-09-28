package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Teacher entity for storing teacher profile details in the database.
 */

@Entity
@Table(name = "teacher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstname", nullable = false, length = 255)
    private String firstName;

    @Column(name = "lastname", nullable = false, length = 255)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "professional_level", length = 255)
    private String professionalLevel;

    @Column(length = 255)
    private String certification;

    @Column(name = "profile_picture", length = 255)
    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "year_of_experience")
    private Integer yearOfExperience;

    @Column(length = 255)
    private String course;

    @Column(length = 50)
    private String language;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
