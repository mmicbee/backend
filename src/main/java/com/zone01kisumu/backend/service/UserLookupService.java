package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.InstitutionRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final StudentAuthService studentAuthService;
    private final TeacherAuthService teacherAuthService;
    private final InstitutionService institutionService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final InstitutionRepository institutionRepository;

    public static class UserLookupResult {
        public final Object user;
        public final String userType;
        public final String userId;

        public UserLookupResult(Object user, String userType, String userId) {
            this.user = user;
            this.userType = userType;
            this.userId = userId;
        }
    }

    public Optional<UserLookupResult> findUserByEmail(String email) {
        Optional<Student> student = studentAuthService.findByEmail(email);
        if (student.isPresent()) {
            return Optional.of(new UserLookupResult(student.get(), "ROLE_STUDENT", student.get().getId().toString()));
        }

        Optional<Teacher> teacher = teacherAuthService.findByEmail(email);
        if (teacher.isPresent()) {
            return Optional.of(new UserLookupResult(teacher.get(), "ROLE_TEACHER", teacher.get().getId().toString()));
        }

        Optional<Institution> institution = institutionService.findByEmail(email);
        if (institution.isPresent()) {
            return Optional.of(new UserLookupResult(institution.get(), "ROLE_INSTITUTION",
                    institution.get().getId().toString()));
        }

        return Optional.empty();
    }

    public Optional<UserLookupResult> findUserByPhone(String phone) {
        Optional<Student> student = studentAuthService.findByPhone(phone);
        if (student.isPresent()) {
            return Optional.of(new UserLookupResult(student.get(), "ROLE_STUDENT", student.get().getId().toString()));
        }

        Optional<Teacher> teacher = teacherAuthService.findByPhone(phone);
        if (teacher.isPresent()) {
            return Optional.of(new UserLookupResult(teacher.get(), "ROLE_TEACHER", teacher.get().getId().toString()));
        }

        Optional<Institution> institution = institutionService.findByPhone(phone);
        if (institution.isPresent()) {
            return Optional.of(new UserLookupResult(institution.get(), "ROLE_INSTITUTION",
                    institution.get().getId().toString()));
        }

        return Optional.empty();
    }

    public boolean isEmail(String identifier) {
        return identifier != null && identifier.contains("@");
    }

    public boolean isPhoneNumber(String identifier) {
        return identifier != null && !isEmail(identifier);
    }

    public boolean isPhone(String identifier) {
        return isPhoneNumber(identifier);
    }

    public Optional<UserLookupResult> findUserByPhoneOrEmail(String phoneOrEmail) {
        if (phoneOrEmail == null) {
            return Optional.empty();
        }
        if (isEmail(phoneOrEmail)) {
            return findUserByEmail(phoneOrEmail);
        } else {
            return findUserByPhone(phoneOrEmail);
        }
    }

    public Optional<UserLookupResult> findUser(String phoneOrEmail) {
        return findUserByPhoneOrEmail(phoneOrEmail);
    }

    public String getUserEmail(Object user) {
        if (user instanceof Student) {
            return ((Student) user).getEmail();
        } else if (user instanceof Teacher) {
            return ((Teacher) user).getEmail();
        } else if (user instanceof Institution) {
            return ((Institution) user).getEmail();
        }
        throw new IllegalArgumentException("Unknown user type");
    }

    public String getUserPhone(Object user) {
        if (user instanceof Student) {
            return ((Student) user).getPhone();
        } else if (user instanceof Teacher) {
            return ((Teacher) user).getPhone();
        } else if (user instanceof Institution) {
            return ((Institution) user).getPhone();
        }
        throw new IllegalArgumentException("Unknown user type");
    }

    public void updateUserPassword(Object user, String encodedPassword) {
        if (user instanceof Student student) {
            student.setPassword(encodedPassword);
            studentRepository.save(student);
        } else if (user instanceof Teacher teacher) {
            teacher.setPassword(encodedPassword);
            teacherRepository.save(teacher);
        } else if (user instanceof Institution institution) {
            institution.setPassword(encodedPassword);
            institutionRepository.save(institution);
        } else {
            throw new IllegalArgumentException("Unknown user type");
        }
    }
}
