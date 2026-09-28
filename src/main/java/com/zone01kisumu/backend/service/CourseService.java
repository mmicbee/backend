package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogResponseDTO;
import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogSearchCriteria;
import com.zone01kisumu.backend.dto.CourseDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;
import com.zone01kisumu.backend.repository.specification.CourseSpecifications;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service class for course-related operations
@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;

  // CREATE
  @Transactional
  public CourseDTO addCourse(CourseDTO courseDTO) {
    Course course = mapToEntity(courseDTO);
    course.setCreatedAt(LocalDateTime.now());
    Course savedCourse = courseRepository.save(course);
    return mapToDTO(savedCourse);
  }

  // READ by ID
  public Optional<CourseDTO> getCourseById(Long id) {
    return courseRepository.findById(id).map(this::mapToDTO);
  }

  // LIST all
  public List<CourseDTO> getAllCourses() {
    return courseRepository.findAll().stream().map(this::mapToDTO).toList();
  }

  // DELETE
  @Transactional
  public boolean deleteCourse(Long id) {
    if (courseRepository.existsById(id)) {
      courseRepository.deleteById(id);
      return true;
    }
    return false;
  }

  // UPDATE
  @Transactional
  public Optional<CourseDTO> updateCourse(Long id, CourseDTO updatedDTO) {
    return courseRepository
        .findById(id)
        .map(
            existing -> {
              existing.setTitle(updatedDTO.getTitle());
              existing.setDescription(updatedDTO.getDescription());
              existing.setCategory(Course.Category.fromString(updatedDTO.getCategory()));
              existing.setDuration(updatedDTO.getDuration());
              existing.setStartDate(updatedDTO.getStartDate());
              existing.setEndDate(updatedDTO.getEndDate());
              existing.setMode(Course.Mode.valueOf(updatedDTO.getMode().toUpperCase()));
              existing.setPrice(updatedDTO.getPrice());
              existing.setTeacherId(updatedDTO.getTeacherId());
              if (updatedDTO.getStatus() != null && !updatedDTO.getStatus().isBlank()) {
                existing.setStatus(Course.Status.valueOf(updatedDTO.getStatus().toUpperCase()));
              }

              // Payment fields are only required for paid courses (price > 0).
              BigDecimal price = updatedDTO.getPrice();
              if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                if (updatedDTO.getPaymentMethod() != null
                    && !updatedDTO.getPaymentMethod().isBlank()) {
                  existing.setPaymentMethod(
                      Course.PaymentMethod.valueOf(updatedDTO.getPaymentMethod().toUpperCase()));
                }
                existing.setPaymentAccount(
                    updatedDTO.getPaymentAccount() != null
                            && !updatedDTO.getPaymentAccount().isEmpty()
                        ? updatedDTO.getPaymentAccount()
                        : existing.getPaymentAccount());
                if (updatedDTO.getMpesaPaymentType() != null
                    && !updatedDTO.getMpesaPaymentType().isBlank()) {
                  existing.setMpesaPaymentType(
                      Course.MpesaPaymentType.valueOf(
                          updatedDTO.getMpesaPaymentType().toUpperCase()));
                }
                existing.setPaybillNumber(updatedDTO.getPaybillNumber());
              } else {
                // Free course - clear payment specific info
                existing.setPaymentAccount(null);
                existing.setMpesaPaymentType(null);
                existing.setPaybillNumber(null);
              }
              return mapToDTO(courseRepository.save(existing));
            });
  }

  // get courses by teacher id
  public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
    return courseRepository.findByTeacherId(teacherId).stream().map(this::mapToDTO).toList();
  }

  // get courses by title containing
  public List<CourseDTO> getCoursesByTitleContaining(String title) {
    return courseRepository.findByTitleContaining(title).stream().map(this::mapToDTO).toList();
  }

  // get courses by mode and start date after
  public List<CourseDTO> getCoursesByModeAndStartDateAfter(Course.Mode mode, LocalDateTime date) {
    return courseRepository.findByModeAndStartDateAfter(mode, date).stream()
        .map(this::mapToDTO)
        .toList();
  }

  // get courses by title or description containing
  public List<CourseDTO> getCoursesByTitleOrDescriptionContaining(String title) {
    return courseRepository.findByTitleOrDescriptionContaining(title).stream()
        .map(this::mapToDTO)
        .toList();
  }

  // get courses by date range
  public List<CourseDTO> getCoursesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return courseRepository.findByDateRange(startDate, endDate).stream()
        .map(this::mapToDTO)
        .toList();
  }

  // get courses by price range
  public List<CourseDTO> getCoursesByPriceRange(Double minPrice, Double maxPrice) {
    return courseRepository.findByPriceRange(minPrice, maxPrice).stream()
        .map(this::mapToDTO)
        .toList();
  }

  // Utility: DTO -> Entity
  private Course mapToEntity(CourseDTO dto) {
    Course course = new Course();
    LocalDateTime now = LocalDateTime.now();
    course.setTitle(dto.getTitle());
    course.setDescription(dto.getDescription());
    course.setCategory(Course.Category.fromString(dto.getCategory()));
    course.setDuration(dto.getDuration());
    course.setMode(Course.Mode.valueOf(dto.getMode().toUpperCase()));
    course.setPrice(dto.getPrice());
    course.setTeacherId(dto.getTeacherId());
    // Only set payment method/account when price > 0 and values are provided. Free courses may omit
    // these.
    if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
      if (dto.getPaymentMethod() != null && !dto.getPaymentMethod().isBlank()) {
        course.setPaymentMethod(Course.PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
      }
      course.setPaymentAccount(
          dto.getPaymentAccount() != null && !dto.getPaymentAccount().isEmpty()
              ? dto.getPaymentAccount()
              : null);
      if (dto.getMpesaPaymentType() != null && !dto.getMpesaPaymentType().isBlank()) {
        course.setMpesaPaymentType(
            Course.MpesaPaymentType.valueOf(dto.getMpesaPaymentType().toUpperCase()));
      }
      course.setPaybillNumber(dto.getPaybillNumber());
    } else {
      // free course - leave paymentMethod as default and clear payment details
      course.setPaymentAccount(null);
      course.setMpesaPaymentType(null);
      course.setPaybillNumber(null);
    }

    if (dto.getMode().equalsIgnoreCase("LIVE") && dto.getStatus().equalsIgnoreCase("STARTED")) {
      course.setStartDate(now);
      // add the duration in months to the start date to get the end date in LocalDateTime
      course.setEndDate(now.plusWeeks(dto.getDuration()));
    } else {
      course.setStartDate(null);
      course.setEndDate(null);
    }
    course.setStatus(Course.Status.valueOf(dto.getStatus().toUpperCase()));
    return course;
  }

  // Utility: Entity -> DTO
  public CourseDTO mapToDTO(Course course) {
    CourseDTO dto = new CourseDTO();
    dto.setId(course.getId());
    dto.setTitle(course.getTitle());
    dto.setDescription(course.getDescription());
    dto.setDuration(course.getDuration());
    dto.setStartDate(course.getStartDate());
    dto.setEndDate(course.getEndDate());
    dto.setMode(course.getMode().toString());
    dto.setPrice(course.getPrice());
    dto.setTeacherId(course.getTeacherId());

    if (teacherRepository != null && course.getTeacherId() != null) {
      teacherRepository
          .findById(course.getTeacherId())
          .ifPresent(
              teacher -> {
                String first = teacher.getFirstName() != null ? teacher.getFirstName() : "";
                String last = teacher.getLastName() != null ? teacher.getLastName() : "";
                String full = (first + " " + last).trim();
                if (!full.isEmpty()) {
                  dto.setInstructorName(full);
                }
              });
    }

    dto.setPaymentMethod(course.getPaymentMethod().toString());
    dto.setPaymentAccount(course.getPaymentAccount());
    dto.setMpesaPaymentType(
        course.getMpesaPaymentType() != null ? course.getMpesaPaymentType().toString() : null);
    dto.setPaybillNumber(course.getPaybillNumber());
    dto.setCreatedAt(course.getCreatedAt());
    dto.setStatus(course.getStatus().toString());
    dto.setCategory(course.getCategory().toString());
    return dto;
  }

  /**
   * Searches and filters the course catalog with support for keywords, category, mode, price
   * ranges, status, pagination, and relevance/attribute sorting.
   *
   * @param criteria The search criteria parameters.
   * @return Paginated catalog envelope with matched courses.
   */
  public CourseCatalogResponseDTO<CourseDTO> searchCourseCatalog(
      CourseCatalogSearchCriteria criteria) {
    if (criteria == null) {
      criteria = CourseCatalogSearchCriteria.builder().build();
    }

    int page = Math.max(0, criteria.getPage());
    int size = criteria.getSize() <= 0 ? 12 : Math.min(100, criteria.getSize());
    String sortBy =
        criteria.getSortBy() != null ? criteria.getSortBy().trim().toLowerCase() : "relevance";
    String sortDir =
        criteria.getSortDirection() != null
            ? criteria.getSortDirection().trim().toLowerCase()
            : "asc";
    boolean isAsc = "asc".equals(sortDir);

    Specification<Course> spec = CourseSpecifications.buildSpecification(criteria);
    boolean hasKeyword = criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty();

    // If relevance sorting is requested with a keyword, rank matched results by relevance
    if ("relevance".equals(sortBy) && hasKeyword) {
      List<Course> allMatches = courseRepository.findAll(spec);
      final String kw = criteria.getKeyword().trim().toLowerCase();

      List<CourseDTO> rankedDTOs =
          allMatches.stream()
              .map(
                  course -> {
                    CourseDTO dto = mapToDTO(course);
                    int relevanceScore = calculateRelevanceScore(course, dto, kw);
                    return Map.entry(dto, relevanceScore);
                  })
              .sorted(
                  (e1, e2) -> {
                    int comp = Integer.compare(e2.getValue(), e1.getValue()); // highest score first
                    if (comp != 0) {
                      return comp;
                    }
                    // tie-breaker: newest first
                    if (e1.getKey().getCreatedAt() != null && e2.getKey().getCreatedAt() != null) {
                      return e2.getKey().getCreatedAt().compareTo(e1.getKey().getCreatedAt());
                    }
                    return 0;
                  })
              .map(Map.Entry::getKey)
              .toList();

      int totalElements = rankedDTOs.size();
      int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
      int fromIndex = Math.min(page * size, totalElements);
      int toIndex = Math.min(fromIndex + size, totalElements);
      List<CourseDTO> pageContent = rankedDTOs.subList(fromIndex, toIndex);

      return CourseCatalogResponseDTO.<CourseDTO>builder()
          .content(pageContent)
          .currentPage(page)
          .pageSize(size)
          .totalElements(totalElements)
          .totalPages(totalPages)
          .isFirst(page == 0)
          .isLast(totalPages == 0 || page >= totalPages - 1)
          .hasNext(totalPages > 0 && page < totalPages - 1)
          .hasPrevious(page > 0)
          .availableCategories(getAllCategoryNames())
          .build();
    }

    // Standard database-level pagination with Sort
    Sort sort;
    switch (sortBy) {
      case "price":
        sort = isAsc ? Sort.by("price").ascending() : Sort.by("price").descending();
        break;
      case "title":
        sort = isAsc ? Sort.by("title").ascending() : Sort.by("title").descending();
        break;
      case "duration":
        sort = isAsc ? Sort.by("duration").ascending() : Sort.by("duration").descending();
        break;
      case "newest":
      case "createdat":
      case "relevance":
      default:
        sort = isAsc ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        break;
    }

    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Course> coursePage = courseRepository.findAll(spec, pageable);

    List<CourseDTO> dtoList = coursePage.getContent().stream().map(this::mapToDTO).toList();

    return CourseCatalogResponseDTO.<CourseDTO>builder()
        .content(dtoList)
        .currentPage(coursePage.getNumber())
        .pageSize(coursePage.getSize())
        .totalElements(coursePage.getTotalElements())
        .totalPages(coursePage.getTotalPages())
        .isFirst(coursePage.isFirst())
        .isLast(coursePage.isLast())
        .hasNext(coursePage.hasNext())
        .hasPrevious(coursePage.hasPrevious())
        .availableCategories(getAllCategoryNames())
        .build();
  }

  private int calculateRelevanceScore(Course course, CourseDTO dto, String keyword) {
    int score = 0;
    String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
    String desc = course.getDescription() != null ? course.getDescription().toLowerCase() : "";
    String instructor =
        dto.getInstructorName() != null ? dto.getInstructorName().toLowerCase() : "";

    if (title.equals(keyword)) {
      score += 100;
    } else if (title.startsWith(keyword)) {
      score += 80;
    } else if (title.contains(keyword)) {
      score += 50;
    }

    if (desc.contains(keyword)) {
      score += 20;
    }

    if (instructor.contains(keyword)) {
      score += 30;
    }

    String[] words = keyword.split("\\s+");
    if (words.length > 1) {
      for (String word : words) {
        if (!word.isBlank()) {
          if (title.contains(word)) score += 15;
          if (desc.contains(word)) score += 5;
        }
      }
    }

    return score;
  }

  public List<String> getAllCategoryNames() {
    return Arrays.stream(Course.Category.values()).map(Enum::name).toList();
  }
}
