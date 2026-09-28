package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.dto.TeacherLogin;
import com.zone01kisumu.backend.dto.TeacherRegistration;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for handling teacher authentication and registration.
 */

@Service
@RequiredArgsConstructor
public class TeacherAuthService implements UserDetailsService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Teacher registerTeacher(TeacherRegistration request) {
        if (teacherRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }

        final String encodedPassword = passwordEncoder.encode(request.getPassword());
        final Teacher teacher = Teacher.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(encodedPassword)
                .professionalLevel(request.getProfessionalLevel())
                .certification(request.getCertification())
                .profilePicture(request.getProfilePicture())
                .bio(request.getBio())
                .yearOfExperience(request.getYearOfExperience())
                .course(request.getCourse())
                .language(request.getLanguage())
                .build();

        return teacherRepository.save(teacher);
    }

    public Teacher loginTeacher(TeacherLogin request) {
        Teacher teacher = teacherRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return teacher;
    }

    //update teacher profile
    @Transactional
    public Teacher updateTeacherProfile(Long id, TeacherRegistration request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + id));

        // Check if email is being updated and if the new email is already taken
        if (request.getEmail() != null && !request.getEmail().equals(teacher.getEmail())) {
            if (teacherRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email is already in use");
            }
            teacher.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            teacher.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            teacher.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }
        if (request.getProfessionalLevel() != null) {
            teacher.setProfessionalLevel(request.getProfessionalLevel());
        }
        if (request.getCertification() != null) {
            teacher.setCertification(request.getCertification());
        }
        if (request.getProfilePicture() != null) {
            teacher.setProfilePicture(request.getProfilePicture());
        }
        if (request.getBio() != null) {
            teacher.setBio(request.getBio());
        }
        if (request.getYearOfExperience() != null) {
            teacher.setYearOfExperience(request.getYearOfExperience());
        }
        if (request.getCourse() != null) {
            teacher.setCourse(request.getCourse());
        }
        if (request.getLanguage() != null) {
            teacher.setLanguage(request.getLanguage());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return teacherRepository.save(teacher);
    }

    //delete teacher profile
    @Transactional
    public void deleteTeacherProfile(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    //get teacher by id
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found with email: " + email));

        return User.builder()
                .username(teacher.getEmail())
                .password(teacher.getPassword())
                .authorities("ROLE_TEACHER")
                .build();
    }

    public Optional<Teacher> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    public Optional<Teacher> findByPhone(String phone) {
        return teacherRepository.findByPhone(phone);
    }
}