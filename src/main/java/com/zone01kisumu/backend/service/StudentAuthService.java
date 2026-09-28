package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.dto.StudentLogin;
import com.zone01kisumu.backend.dto.StudentRegistration;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;

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

@Service
@RequiredArgsConstructor
public class StudentAuthService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Student registerStudent(StudentRegistration request) {
        if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        final String encodedPassword = passwordEncoder.encode(request.getPassword());
        final Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .profilePicture(request.getProfilePicture())
                .profilePicture(request.getProfilePicture())
                .password(encodedPassword)
                .build();
        return studentRepository.save(student);
    }

    public Student loginStudent(StudentLogin request) {
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return student;
    }

    //update student profile
    @Transactional
    public Student updateStudentProfile(Long id, StudentRegistration request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        // Check if email is being updated and if the new email is already taken
        if (request.getEmail() != null && !request.getEmail().equals(student.getEmail())) {
            if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email is already in use");
            }
            student.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getProfilePicture() != null) {
            student.setProfilePicture(request.getProfilePicture());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            student.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return studentRepository.save(student);
    }

    // Delete student profile
    @Transactional
    public void deleteStudentProfile(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    //get student by id
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with email: " + email));

        return User.builder()
                .username(student.getEmail())
                .password(student.getPassword())
                .authorities("ROLE_STUDENT")
                .build();
    }

    public Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public Optional<Student> findByPhone(String phone) {
        return studentRepository.findByPhone(phone);
    }
}