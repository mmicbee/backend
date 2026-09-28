package com.zone01kisumu.backend.courseTest;

import com.zone01kisumu.backend.dto.CourseDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.service.CourseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test class for CourseService
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private CourseDTO sampleDTO;
    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleDTO = new CourseDTO(
                null,
                "Test Course",
                "This is a test",
                "OTHERS",
                30,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                "LIVE",
                BigDecimal.valueOf(100),
                1L,
                "MPESA",
                "PAYBILL",
                "BuyGoodsPayment",
                "0712345678",
                LocalDateTime.now(),
                "STARTED"
        );

        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setTitle("Test Course");
        sampleCourse.setDescription("This is a test");
        sampleCourse.setCategory(Course.Category.OTHERS);
        sampleCourse.setDuration(30);
        sampleCourse.setStartDate(sampleDTO.getStartDate());
        sampleCourse.setEndDate(sampleDTO.getEndDate());
        sampleCourse.setMode(Course.Mode.LIVE);
        sampleCourse.setPrice(BigDecimal.valueOf(100));
        sampleCourse.setTeacherId(1L);
        sampleCourse.setPaymentMethod(Course.PaymentMethod.MPESA);
        sampleCourse.setPaymentAccount("0712345678");
        sampleCourse.setCreatedAt(LocalDateTime.now());
        sampleCourse.setStatus(Course.Status.STARTED);
    }

    @Test
    void testAddCourse() {
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

        CourseDTO savedDTO = courseService.addCourse(sampleDTO);

        assertNotNull(savedDTO);
        assertEquals("Test Course", savedDTO.getTitle());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testGetCourseById() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

        Optional<CourseDTO> result = courseService.getCourseById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Course", result.get().getTitle());
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(sampleCourse));

        List<CourseDTO> result = courseService.getAllCourses();

        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void testDeleteCourse_WhenExists() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        boolean deleted = courseService.deleteCourse(1L);

        assertTrue(deleted);
        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCourse_WhenNotExists() {
        when(courseRepository.existsById(1L)).thenReturn(false);

        boolean deleted = courseService.deleteCourse(1L);

        assertFalse(deleted);
        verify(courseRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

        Optional<CourseDTO> result = courseService.updateCourse(1L, sampleDTO);

        assertTrue(result.isPresent());
        assertEquals("Test Course", result.get().getTitle());
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateCourse_WithStatus() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus(Course.Status.OPEN);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseDTO updateDTO = new CourseDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        updateDTO.setCategory("BUSINESS");
        updateDTO.setDuration(12);
        updateDTO.setMode("LIVE");
        updateDTO.setPrice(BigDecimal.ZERO);
        updateDTO.setStatus("COMPLETED");

        Optional<CourseDTO> result = courseService.updateCourse(1L, updateDTO);

        assertTrue(result.isPresent());
        assertEquals("COMPLETED", result.get().getStatus());
        assertEquals(Course.Status.COMPLETED, course.getStatus());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void testUpdateCourse_NotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CourseDTO> result = courseService.updateCourse(99L, sampleDTO);

        assertTrue(result.isEmpty());
        verify(courseRepository, times(1)).findById(99L);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void testGetCoursesByTeacherId() {
        when(courseRepository.findByTeacherId(1L)).thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByTeacherId(1L);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByTeacherId(1L);

    }

    @Test
    void testGetCoursesByPriceRange() {
        when(courseRepository.findByPriceRange(Double.valueOf(50), Double.valueOf(150)))
                .thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByPriceRange(50.0, 150.0);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByPriceRange(Double.valueOf(50), Double.valueOf(150));
    }

    @Test
    void testGetCoursesByTitleContaining() {
        when(courseRepository.findByTitleContaining("Test"))
                .thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByTitleContaining("Test");
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByTitleContaining("Test");
    }

    @Test
    void testGetCoursesByTitleOrDescriptionContaining() {
        when(courseRepository.findByTitleOrDescriptionContaining("test"))
                .thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByTitleOrDescriptionContaining("test");
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByTitleOrDescriptionContaining("test");
    }

    @Test
    void testGetCoursesByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(31);
        when(courseRepository.findByDateRange(startDate, endDate))
                .thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByDateRange(startDate, endDate);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByDateRange(startDate, endDate);
    }

    @Test
    void testGetCoursesByModeAndStartDateAfter() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        when(courseRepository.findByModeAndStartDateAfter(Course.Mode.LIVE, date))
                .thenReturn(List.of(sampleCourse));
        List<CourseDTO> result = courseService.getCoursesByModeAndStartDateAfter(Course.Mode.LIVE, date);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByModeAndStartDateAfter(Course.Mode.LIVE, date);
    }

}