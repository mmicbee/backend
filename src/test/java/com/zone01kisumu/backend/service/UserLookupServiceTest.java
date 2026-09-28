package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.InstitutionRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLookupServiceTest {

    @Mock
    private StudentAuthService studentAuthService;

    @Mock
    private TeacherAuthService teacherAuthService;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private UserLookupService userLookupService;

    private Student testStudent;
    private Teacher testTeacher;
    private Institution testInstitution;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setEmail("student@example.com");
        testStudent.setPhone("+1234567890");

        testTeacher = new Teacher();
        testTeacher.setId(2L);
        testTeacher.setEmail("teacher@example.com");
        testTeacher.setPhone("+0987654321");

        testInstitution = new Institution();
        testInstitution.setId(3L);
        testInstitution.setEmail("institution@example.com");
        testInstitution.setPhone("+1122334455");
    }

    // ================ findUserByEmail Tests ================

    @Test
    void findUserByEmail_ShouldReturnStudent_WhenStudentExists() {
        when(studentAuthService.findByEmail("student@example.com")).thenReturn(Optional.of(testStudent));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("student@example.com");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        assertEquals("ROLE_STUDENT", result.get().userType);
        assertEquals("1", result.get().userId);

        verify(studentAuthService).findByEmail("student@example.com");
        verify(teacherAuthService, never()).findByEmail(anyString());
        verify(institutionService, never()).findByEmail(anyString());
    }

    @Test
    void findUserByEmail_ShouldReturnTeacher_WhenOnlyTeacherExists() {
        when(studentAuthService.findByEmail("teacher@example.com")).thenReturn(Optional.empty());
        when(teacherAuthService.findByEmail("teacher@example.com")).thenReturn(Optional.of(testTeacher));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("teacher@example.com");

        assertTrue(result.isPresent());
        assertEquals(testTeacher, result.get().user);
        assertEquals("ROLE_TEACHER", result.get().userType);
        assertEquals("2", result.get().userId);

        verify(studentAuthService).findByEmail("teacher@example.com");
        verify(teacherAuthService).findByEmail("teacher@example.com");
        verify(institutionService, never()).findByEmail(anyString());
    }

    @Test
    void findUserByEmail_ShouldReturnInstitution_WhenOnlyInstitutionExists() {
        when(studentAuthService.findByEmail("institution@example.com")).thenReturn(Optional.empty());
        when(teacherAuthService.findByEmail("institution@example.com")).thenReturn(Optional.empty());
        when(institutionService.findByEmail("institution@example.com")).thenReturn(Optional.of(testInstitution));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("institution@example.com");

        assertTrue(result.isPresent());
        assertEquals(testInstitution, result.get().user);
        assertEquals("ROLE_INSTITUTION", result.get().userType);
        assertEquals("3", result.get().userId);

        verify(studentAuthService).findByEmail("institution@example.com");
        verify(teacherAuthService).findByEmail("institution@example.com");
        verify(institutionService).findByEmail("institution@example.com");
    }

    @Test
    void findUserByEmail_ShouldReturnEmpty_WhenNoUserExists() {
        when(studentAuthService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(teacherAuthService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(institutionService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());

        verify(studentAuthService).findByEmail("nonexistent@example.com");
        verify(teacherAuthService).findByEmail("nonexistent@example.com");
        verify(institutionService).findByEmail("nonexistent@example.com");
    }

    @Test
    void findUserByEmail_ShouldReturnStudent_WhenMultipleUsersExist() {
        // Student found first, so should return student (priority order)
        when(studentAuthService.findByEmail("duplicate@example.com")).thenReturn(Optional.of(testStudent));
        // Don't stub teacherAuthService since it won't be called

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("duplicate@example.com");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        assertEquals("ROLE_STUDENT", result.get().userType);

        verify(studentAuthService).findByEmail("duplicate@example.com");
        verify(teacherAuthService, never()).findByEmail(anyString());
        verify(institutionService, never()).findByEmail(anyString());
    }

    @Test
    void findUserByEmail_ShouldHandleNullEmail() {
        when(studentAuthService.findByEmail(null)).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail(null);

        assertFalse(result.isPresent());
    }

    @Test
    void findUserByEmail_ShouldHandleEmptyEmail() {
        when(studentAuthService.findByEmail("")).thenReturn(Optional.empty());
        when(teacherAuthService.findByEmail("")).thenReturn(Optional.empty());
        when(institutionService.findByEmail("")).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByEmail("");

        assertFalse(result.isPresent());
    }

    // ================ findUserByPhone Tests ================

    @Test
    void findUserByPhone_ShouldReturnStudent_WhenStudentExists() {
        when(studentAuthService.findByPhone("+1234567890")).thenReturn(Optional.of(testStudent));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone("+1234567890");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        assertEquals("ROLE_STUDENT", result.get().userType);
        assertEquals("1", result.get().userId);

        verify(studentAuthService).findByPhone("+1234567890");
        verify(teacherAuthService, never()).findByPhone(anyString());
        verify(institutionService, never()).findByPhone(anyString());
    }

    @Test
    void findUserByPhone_ShouldReturnTeacher_WhenOnlyTeacherExists() {
        when(studentAuthService.findByPhone("+0987654321")).thenReturn(Optional.empty());
        when(teacherAuthService.findByPhone("+0987654321")).thenReturn(Optional.of(testTeacher));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone("+0987654321");

        assertTrue(result.isPresent());
        assertEquals(testTeacher, result.get().user);
        assertEquals("ROLE_TEACHER", result.get().userType);
        assertEquals("2", result.get().userId);
    }

    @Test
    void findUserByPhone_ShouldReturnInstitution_WhenOnlyInstitutionExists() {
        when(studentAuthService.findByPhone("+1122334455")).thenReturn(Optional.empty());
        when(teacherAuthService.findByPhone("+1122334455")).thenReturn(Optional.empty());
        when(institutionService.findByPhone("+1122334455")).thenReturn(Optional.of(testInstitution));

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone("+1122334455");

        assertTrue(result.isPresent());
        assertEquals(testInstitution, result.get().user);
        assertEquals("ROLE_INSTITUTION", result.get().userType);
        assertEquals("3", result.get().userId);
    }

    @Test
    void findUserByPhone_ShouldReturnEmpty_WhenNoUserExists() {
        when(studentAuthService.findByPhone("+9999999999")).thenReturn(Optional.empty());
        when(teacherAuthService.findByPhone("+9999999999")).thenReturn(Optional.empty());
        when(institutionService.findByPhone("+9999999999")).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone("+9999999999");

        assertFalse(result.isPresent());
    }

    @Test
    void findUserByPhone_ShouldHandleNullPhone() {
        when(studentAuthService.findByPhone(null)).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone(null);

        assertFalse(result.isPresent());
    }

    @Test
    void findUserByPhone_ShouldHandleEmptyPhone() {
        when(studentAuthService.findByPhone("")).thenReturn(Optional.empty());
        when(teacherAuthService.findByPhone("")).thenReturn(Optional.empty());
        when(institutionService.findByPhone("")).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result = userLookupService.findUserByPhone("");

        assertFalse(result.isPresent());
    }

    // ================ getUserEmail Tests ================

    @Test
    void getUserEmail_ShouldReturnStudentEmail() {
        String email = userLookupService.getUserEmail(testStudent);
        assertEquals("student@example.com", email);
    }

    @Test
    void getUserEmail_ShouldReturnTeacherEmail() {
        String email = userLookupService.getUserEmail(testTeacher);
        assertEquals("teacher@example.com", email);
    }

    @Test
    void getUserEmail_ShouldReturnInstitutionEmail() {
        String email = userLookupService.getUserEmail(testInstitution);
        assertEquals("institution@example.com", email);
    }

    @Test
    void getUserEmail_ShouldThrowException_WhenUnknownUserType() {
        String unknownUser = "unknown";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.getUserEmail(unknownUser)
        );

        assertEquals("Unknown user type", exception.getMessage());
    }

    @Test
    void getUserEmail_ShouldThrowException_WhenNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.getUserEmail(null)
        );

        assertEquals("Unknown user type", exception.getMessage());
    }

    // ================ getUserPhone Tests ================

    @Test
    void getUserPhone_ShouldReturnStudentPhone() {
        String phone = userLookupService.getUserPhone(testStudent);
        assertEquals("+1234567890", phone);
    }

    @Test
    void getUserPhone_ShouldReturnTeacherPhone() {
        String phone = userLookupService.getUserPhone(testTeacher);
        assertEquals("+0987654321", phone);
    }

    @Test
    void getUserPhone_ShouldReturnInstitutionPhone() {
        String phone = userLookupService.getUserPhone(testInstitution);
        assertEquals("+1122334455", phone);
    }

    @Test
    void getUserPhone_ShouldThrowException_WhenUnknownUserType() {
        String unknownUser = "unknown";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.getUserPhone(unknownUser)
        );

        assertEquals("Unknown user type", exception.getMessage());
    }

    @Test
    void getUserPhone_ShouldThrowException_WhenNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.getUserPhone(null)
        );

        assertEquals("Unknown user type", exception.getMessage());
    }

    // ================ UserLookupResult Tests ================

    @Test
    void userLookupResult_ShouldStoreAllFieldsCorrectly() {
        UserLookupService.UserLookupResult result = new UserLookupService.UserLookupResult(
            testStudent, "ROLE_STUDENT", "1"
        );

        assertEquals(testStudent, result.user);
        assertEquals("ROLE_STUDENT", result.userType);
        assertEquals("1", result.userId);
    }

    @Test
    void userLookupResult_ShouldHandleNullValues() {
        UserLookupService.UserLookupResult result = new UserLookupService.UserLookupResult(
            null, null, null
        );

        assertNull(result.user);
        assertNull(result.userType);
        assertNull(result.userId);
    }

    // ================ updateUserPassword Tests ================

    @Test
    void updateUserPassword_ShouldUpdateStudentPasswordAndSave() {
        userLookupService.updateUserPassword(testStudent, "newEncodedPassword");

        assertEquals("newEncodedPassword", testStudent.getPassword());
        verify(studentRepository).save(testStudent);
        verify(teacherRepository, never()).save(any());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void updateUserPassword_ShouldUpdateTeacherPasswordAndSave() {
        userLookupService.updateUserPassword(testTeacher, "newEncodedPassword");

        assertEquals("newEncodedPassword", testTeacher.getPassword());
        verify(teacherRepository).save(testTeacher);
        verify(studentRepository, never()).save(any());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void updateUserPassword_ShouldUpdateInstitutionPasswordAndSave() {
        userLookupService.updateUserPassword(testInstitution, "newEncodedPassword");

        assertEquals("newEncodedPassword", testInstitution.getPassword());
        verify(institutionRepository).save(testInstitution);
        verify(studentRepository, never()).save(any());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateUserPassword_ShouldThrowException_WhenUnknownUserType() {
        String unknownUser = "unknownUserType";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.updateUserPassword(unknownUser, "newEncodedPassword")
        );

        assertEquals("Unknown user type", exception.getMessage());
        verify(studentRepository, never()).save(any());
        verify(teacherRepository, never()).save(any());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void updateUserPassword_ShouldThrowException_WhenNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLookupService.updateUserPassword(null, "newEncodedPassword")
        );

        assertEquals("Unknown user type", exception.getMessage());
        verify(studentRepository, never()).save(any());
        verify(teacherRepository, never()).save(any());
        verify(institutionRepository, never()).save(any());
    }

    // ================ isEmail and isPhoneNumber Tests ================

    @Test
    void isEmail_ShouldReturnTrue_WhenEmailContainsAtSign() {
        assertTrue(userLookupService.isEmail("student@example.com"));
        assertTrue(userLookupService.isEmail("user+test@domain.org"));
    }

    @Test
    void isEmail_ShouldReturnFalse_WhenPhoneOrStringWithoutAtSign() {
        assertFalse(userLookupService.isEmail("+1234567890"));
        assertFalse(userLookupService.isEmail("plainstring"));
    }

    @Test
    void isEmail_ShouldReturnFalse_WhenNull() {
        assertFalse(userLookupService.isEmail(null));
    }

    @Test
    void isPhoneNumber_ShouldReturnTrue_WhenPhoneNumber() {
        assertTrue(userLookupService.isPhoneNumber("+1234567890"));
        assertTrue(userLookupService.isPhoneNumber("0712345678"));
    }

    @Test
    void isPhoneNumber_ShouldReturnFalse_WhenEmail() {
        assertFalse(userLookupService.isPhoneNumber("student@example.com"));
    }

    @Test
    void isPhoneNumber_ShouldReturnFalse_WhenNull() {
        assertFalse(userLookupService.isPhoneNumber(null));
    }

    @Test
    void isPhone_ShouldBehaveSameAsIsPhoneNumber() {
        assertTrue(userLookupService.isPhone("+1234567890"));
        assertFalse(userLookupService.isPhone("student@example.com"));
        assertFalse(userLookupService.isPhone(null));
    }

    // ================ findUserByPhoneOrEmail and findUser Tests ================

    @Test
    void findUserByPhoneOrEmail_ShouldLookupByEmail_WhenEmailProvided() {
        when(studentAuthService.findByEmail("student@example.com")).thenReturn(Optional.of(testStudent));

        Optional<UserLookupService.UserLookupResult> result =
                userLookupService.findUserByPhoneOrEmail("student@example.com");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        assertEquals("ROLE_STUDENT", result.get().userType);
        verify(studentAuthService).findByEmail("student@example.com");
        verify(studentAuthService, never()).findByPhone(anyString());
    }

    @Test
    void findUserByPhoneOrEmail_ShouldLookupByPhone_WhenPhoneProvided() {
        when(studentAuthService.findByPhone("+1234567890")).thenReturn(Optional.of(testStudent));

        Optional<UserLookupService.UserLookupResult> result =
                userLookupService.findUserByPhoneOrEmail("+1234567890");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        assertEquals("ROLE_STUDENT", result.get().userType);
        verify(studentAuthService).findByPhone("+1234567890");
        verify(studentAuthService, never()).findByEmail(anyString());
    }

    @Test
    void findUserByPhoneOrEmail_ShouldReturnEmpty_WhenNullProvided() {
        Optional<UserLookupService.UserLookupResult> result =
                userLookupService.findUserByPhoneOrEmail(null);

        assertFalse(result.isPresent());
        verify(studentAuthService, never()).findByEmail(anyString());
        verify(studentAuthService, never()).findByPhone(anyString());
    }

    @Test
    void findUserByPhoneOrEmail_ShouldReturnEmpty_WhenUserNotFound() {
        when(studentAuthService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(teacherAuthService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(institutionService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<UserLookupService.UserLookupResult> result =
                userLookupService.findUserByPhoneOrEmail("unknown@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findUser_ShouldDelegateToFindUserByPhoneOrEmail() {
        when(studentAuthService.findByEmail("student@example.com")).thenReturn(Optional.of(testStudent));

        Optional<UserLookupService.UserLookupResult> result =
                userLookupService.findUser("student@example.com");

        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get().user);
        verify(studentAuthService).findByEmail("student@example.com");
    }
}