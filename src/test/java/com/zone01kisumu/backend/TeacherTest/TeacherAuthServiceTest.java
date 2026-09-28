package com.zone01kisumu.backend.TeacherTest;

import com.zone01kisumu.backend.dto.TeacherLogin;
import com.zone01kisumu.backend.dto.TeacherRegistration;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.TeacherRepository;
import com.zone01kisumu.backend.service.TeacherAuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeacherAuthServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherAuthService teacherAuthService;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        teacher = Teacher.builder()
                .id(1L)
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.teacher@example.com")
                .phone("0712345678")
                .password("encodedPassword")
                .professionalLevel("Expert")
                .certification("Certified Educator")
                .profilePicture("profile.jpg")
                .bio("Experienced teacher")
                .yearOfExperience(5)
                .course("Mathematics")
                .language("English")
                .build();
    }

    @Test
    void registerTeacher_Success() {
        TeacherRegistration registration = new TeacherRegistration();
        registration.setFirstName("Jane");
        registration.setLastName("Doe");
        registration.setEmail("jane.teacher@example.com");
        registration.setPhone("0712345678");
        registration.setPassword("rawPass");
        registration.setProfessionalLevel("Expert");
        registration.setCertification("Certified Educator");
        registration.setProfilePicture("profile.jpg");
        registration.setBio("Experienced teacher");
        registration.setYearOfExperience(5);
        registration.setCourse("Mathematics");
        registration.setLanguage("English");

        when(teacherRepository.findByEmail(registration.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPassword");
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        Teacher result = teacherAuthService.registerTeacher(registration);

        assertEquals("jane.teacher@example.com", result.getEmail());
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void registerTeacher_EmailAlreadyExists() {
        when(teacherRepository.findByEmail("jane.teacher@example.com")).thenReturn(Optional.of(teacher));

        TeacherRegistration registration = new TeacherRegistration();
        registration.setEmail("jane.teacher@example.com");

        assertThrows(IllegalArgumentException.class, () -> teacherAuthService.registerTeacher(registration));
    }

    @Test
    void loginTeacher_Success() {
        TeacherLogin login = new TeacherLogin("jane.teacher@example.com", "rawPass");

        when(teacherRepository.findByEmail("jane.teacher@example.com")).thenReturn(Optional.of(teacher));
        when(passwordEncoder.matches("rawPass", "encodedPassword")).thenReturn(true);

        Teacher result = teacherAuthService.loginTeacher(login);

        assertEquals("jane.teacher@example.com", result.getEmail());
    }

    @Test
    void loginTeacher_EmailNotFound() {
        TeacherLogin login = new TeacherLogin("notfound@example.com", "somePass");

        when(teacherRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> teacherAuthService.loginTeacher(login));
    }

    @Test
    void loginTeacher_WrongPassword() {
        TeacherLogin login = new TeacherLogin("jane.teacher@example.com", "wrongPass");

        when(teacherRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(teacher));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> teacherAuthService.loginTeacher(login));
    }

    @Test
    void loadUserByUsername_Success() {
        when(teacherRepository.findByEmail("jane.teacher@example.com")).thenReturn(Optional.of(teacher));

        var userDetails = teacherAuthService.loadUserByUsername("jane.teacher@example.com");

        assertEquals("jane.teacher@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")));
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(teacherRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> teacherAuthService.loadUserByUsername("missing@example.com"));
    }

    @Test
    void findByEmail_Found() {
        when(teacherRepository.findByEmail("jane.teacher@example.com")).thenReturn(Optional.of(teacher));

        Optional<Teacher> result = teacherAuthService.findByEmail("jane.teacher@example.com");

        assertTrue(result.isPresent());
        assertEquals("jane.teacher@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        when(teacherRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherAuthService.findByEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateTeacherProfile_Success() {
        // Request with updated data
        TeacherRegistration request = new TeacherRegistration();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@example.com");
        request.setPhone("0787654321");
        request.setProfessionalLevel("Senior Expert");
        request.setCertification("Advanced Certified Educator");
        request.setProfilePicture("new_profile.jpg");
        request.setBio("Very experienced teacher");
        request.setYearOfExperience(10);
        request.setCourse("Advanced Mathematics");
        request.setLanguage("English, Swahili");

        // Mock repository behavior
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the service method
        Teacher result = teacherAuthService.updateTeacherProfile(1L, request);

        // Assertions
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("jane.doe@example.com", result.getEmail());
        assertEquals("0787654321", result.getPhone());
        assertEquals("Senior Expert", result.getProfessionalLevel());
        assertEquals("Advanced Certified Educator", result.getCertification());
        assertEquals("new_profile.jpg", result.getProfilePicture());
        assertEquals("Very experienced teacher", result.getBio());
        assertEquals(10, result.getYearOfExperience());
        assertEquals("Advanced Mathematics", result.getCourse());
        assertEquals("English, Swahili", result.getLanguage());

        // Verify repository interactions
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacherProfile_NotFound() {
        when(teacherRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> teacherAuthService.updateTeacherProfile(2L, new TeacherRegistration()));
    }

    @Test
    void deleteTeacherProfile_Success() {
        when(teacherRepository.existsById(1L)).thenReturn(true);
        teacherAuthService.deleteTeacherProfile(1L);
        verify(teacherRepository).deleteById(1L);
    }

    @Test
    void deleteTeacherProfile_NotFound() {
        when(teacherRepository.existsById(2L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> teacherAuthService.deleteTeacherProfile(2L));
    }

    // get teacher by id test
    @Test
    void getTeacherById_Found() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        Optional<Teacher> result = teacherAuthService.getTeacherById(1L);
        assertTrue(result.isPresent());
    }

    // get teacher by id not found
    @Test
    void getTeacherById_NotFound() {
        when(teacherRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Teacher> result = teacherAuthService.getTeacherById(2L);
        assertTrue(result.isEmpty());
    }

    // test Email is already in use during update
    @Test
void shouldFailUpdateTeacherProfile_EmailAlreadyInUse() {
    Long teacherId = 1L;

    Teacher existingTeacher = new Teacher();
    existingTeacher.setId(teacherId);
    existingTeacher.setEmail("oldemail@example.com");

    // A different teacher who already has the target email
    Teacher anotherTeacher = new Teacher();
    anotherTeacher.setId(2L);
    anotherTeacher.setEmail("newemail@example.com");

    // Incoming update request
    TeacherRegistration request = new TeacherRegistration();
    request.setEmail("newemail@example.com"); // <-- already in use

    // Mock: teacher being updated exists
    when(teacherRepository.findById(teacherId))
            .thenReturn(Optional.of(existingTeacher));

    // Mock: new email is already used by another teacher
    when(teacherRepository.findByEmail("newemail@example.com"))
            .thenReturn(Optional.of(anotherTeacher));

    // Assert exception
    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> teacherAuthService.updateTeacherProfile(teacherId, request)
    );

    // Verify error message
    assertEquals("Email is already in use", ex.getMessage());
}
//findByPhone test
        @Test
        void shouldFindByPhoneSuccessfully() {
                when(teacherRepository.findByPhone("0712345678")).thenReturn(Optional.of(teacher));
                Optional<Teacher> result = teacherAuthService.findByPhone("0712345678");
                assertTrue(result.isPresent());
                assertEquals("0712345678", result.get().getPhone());
        }

}