package com.zone01kisumu.backend.courseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.zone01kisumu.backend.controller.CourseController;
import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogResponseDTO;
import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogSearchCriteria;
import com.zone01kisumu.backend.dto.CourseDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;
import com.zone01kisumu.backend.repository.specification.CourseSpecifications;
import com.zone01kisumu.backend.service.CourseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CourseCatalogTest {

  @Mock private CourseRepository courseRepository;

  @Mock private TeacherRepository teacherRepository;

  @InjectMocks private CourseService courseService;

  private Course course1;
  private Course course2;
  private Course course3;
  private Teacher teacher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    teacher =
        Teacher.builder()
            .id(10L)
            .firstName("Ada")
            .lastName("Lovelace")
            .email("ada@example.com")
            .build();

    course1 =
        Course.builder()
            .id(1L)
            .title("Advanced Python Development")
            .description("Master object-oriented and functional Python programming")
            .category(Course.Category.SOFTWARE_AND_DATA)
            .mode(Course.Mode.LIVE)
            .price(new BigDecimal("150.00"))
            .duration(8)
            .teacherId(10L)
            .paymentMethod(Course.PaymentMethod.CREDIT_CARD)
            .status(Course.Status.OPEN)
            .createdAt(LocalDateTime.now().minusDays(5))
            .build();

    course2 =
        Course.builder()
            .id(2L)
            .title("Python for Beginners")
            .description("Introduction to programming fundamentals using Python")
            .category(Course.Category.SOFTWARE_AND_DATA)
            .mode(Course.Mode.RECORDED)
            .price(BigDecimal.ZERO)
            .duration(4)
            .teacherId(10L)
            .paymentMethod(Course.PaymentMethod.CREDIT_CARD)
            .status(Course.Status.OPEN)
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();

    course3 =
        Course.builder()
            .id(3L)
            .title("UI/UX Design Essentials")
            .description("Learn Figma, wireframing, and user testing")
            .category(Course.Category.ARTS_AND_DESIGN)
            .mode(Course.Mode.RECORDED)
            .price(new BigDecimal("49.99"))
            .duration(6)
            .teacherId(null)
            .paymentMethod(Course.PaymentMethod.CREDIT_CARD)
            .status(Course.Status.OPEN)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
  }

  @Test
  void searchCourseCatalog_WithKeyword_ShouldRankByRelevance() {
    when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
    when(courseRepository.findAll(any(Specification.class))).thenReturn(List.of(course1, course2));

    CourseCatalogSearchCriteria criteria =
        CourseCatalogSearchCriteria.builder()
            .keyword("Python for Beginners")
            .sortBy("relevance")
            .page(0)
            .size(10)
            .build();

    CourseCatalogResponseDTO<CourseDTO> result = courseService.searchCourseCatalog(criteria);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    // course2 has exact title match, so should rank first
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Python for Beginners");
    assertThat(result.getContent().get(0).getInstructorName()).isEqualTo("Ada Lovelace");
    assertThat(result.getContent().get(1).getTitle()).isEqualTo("Advanced Python Development");
  }

  @Test
  void searchCourseCatalog_WithPaginationAndSorting_ShouldReturnPageEnvelope() {
    when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
    Page<Course> paged = new PageImpl<>(List.of(course3, course1), Pageable.ofSize(2), 3);
    when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(paged);

    CourseCatalogSearchCriteria criteria =
        CourseCatalogSearchCriteria.builder()
            .sortBy("price")
            .sortDirection("asc")
            .page(0)
            .size(2)
            .build();

    CourseCatalogResponseDTO<CourseDTO> result = courseService.searchCourseCatalog(criteria);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getTotalPages()).isEqualTo(2);
    assertThat(result.isFirst()).isTrue();
    assertThat(result.getAvailableCategories()).isNotEmpty();
  }

  @Test
  void searchCourseCatalog_Specification_ShouldFilterFreeCourses() {
    CourseCatalogSearchCriteria criteria =
        CourseCatalogSearchCriteria.builder()
            .isFree(true)
            .category("SOFTWARE_AND_DATA")
            .mode("RECORDED")
            .status("OPEN")
            .build();

    Specification<Course> spec = CourseSpecifications.buildSpecification(criteria);
    assertThat(spec).isNotNull();
  }

  @Test
  void searchCourseCatalog_Specification_WithNullCriteria_ShouldProduceConjunction() {
    Specification<Course> spec = CourseSpecifications.buildSpecification(null);
    assertThat(spec).isNotNull();
  }

  @Test
  void controller_searchCatalog_ShouldReturn200WithCatalogEnvelope() {
    CourseController controller = new CourseController(courseService);
    Page<Course> paged = new PageImpl<>(List.of(course1));
    when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(paged);

    ResponseEntity<CourseCatalogResponseDTO<CourseDTO>> response =
        controller.searchCourseCatalog(
            "Python",
            "SOFTWARE_AND_DATA",
            "LIVE",
            "OPEN",
            new BigDecimal("50"),
            new BigDecimal("200"),
            false,
            10L,
            0,
            10,
            "price",
            "desc");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isNotEmpty();
  }

  @Test
  void controller_getCategories_ShouldReturnCategoryNames() {
    CourseController controller = new CourseController(courseService);
    ResponseEntity<List<String>> response = controller.getCategories();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("SOFTWARE_AND_DATA", "ARTS_AND_DESIGN", "BUSINESS");
  }
}
