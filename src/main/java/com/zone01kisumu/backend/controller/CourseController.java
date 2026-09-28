package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogResponseDTO;
import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogSearchCriteria;
import com.zone01kisumu.backend.dto.CourseDTO;
import com.zone01kisumu.backend.service.CourseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// class CourseController is used to handle course related requests
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

  private final CourseService courseService;

  // Create a new course
  @PostMapping
  public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
    CourseDTO createdCourse = courseService.addCourse(courseDTO);
    return ResponseEntity.status(201).body(createdCourse);
  }

  // Get a course by ID
  @GetMapping("/{id}")
  public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
    Optional<CourseDTO> courseOpt = courseService.getCourseById(id);
    return courseOpt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  // Get all courses
  @GetMapping
  public ResponseEntity<List<CourseDTO>> getAllCourses() {
    List<CourseDTO> courses = courseService.getAllCourses();
    return ResponseEntity.ok(courses);
  }

  // Search and filter courses catalog (supports keywords, categories, mode, status, price range,
  // sorting, pagination)
  @GetMapping("/catalog")
  public ResponseEntity<CourseCatalogResponseDTO<CourseDTO>> searchCourseCatalog(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String mode,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) Boolean isFree,
      @RequestParam(required = false) Long teacherId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "12") int size,
      @RequestParam(required = false, defaultValue = "relevance") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

    CourseCatalogSearchCriteria criteria =
        CourseCatalogSearchCriteria.builder()
            .keyword(keyword)
            .category(category)
            .mode(mode)
            .status(status)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .isFree(isFree)
            .teacherId(teacherId)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

    CourseCatalogResponseDTO<CourseDTO> response = courseService.searchCourseCatalog(criteria);
    return ResponseEntity.ok(response);
  }

  // Get all available course categories
  @GetMapping("/categories")
  public ResponseEntity<List<String>> getCategories() {
    return ResponseEntity.ok(courseService.getAllCategoryNames());
  }

  // get courses by title
  @GetMapping("/search")
  public ResponseEntity<List<CourseDTO>> getCoursesByTitle(@RequestParam String title) {
    List<CourseDTO> courses = courseService.getCoursesByTitleContaining(title);
    return ResponseEntity.ok(courses);
  }

  // get courses by title or description containing
  @GetMapping("/search-any")
  public ResponseEntity<List<CourseDTO>> getCoursesByTitleOrDescriptionContaining(
      @RequestParam String keyword) {
    List<CourseDTO> courses = courseService.getCoursesByTitleOrDescriptionContaining(keyword);
    return ResponseEntity.ok(courses);
  }

  // get courses by teachers id
  @GetMapping("/teacher/{teacherId}")
  public ResponseEntity<List<CourseDTO>> getCoursesByTeacherId(@PathVariable Long teacherId) {
    List<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId);
    return ResponseEntity.ok(courses);
  }

  // get courses within a price range
  @GetMapping("/price-range")
  public ResponseEntity<List<CourseDTO>> getCoursesByPriceRange(
      @RequestParam String minPrice, @RequestParam String maxPrice) {
    List<CourseDTO> courses =
        courseService.getCoursesByPriceRange(Double.valueOf(minPrice), Double.valueOf(maxPrice));
    return ResponseEntity.ok(courses);
  }

  // Update a course
  @PutMapping("/{id}")
  public ResponseEntity<CourseDTO> updateCourse(
      @PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
    Optional<CourseDTO> updated = courseService.updateCourse(id, courseDTO);
    return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  // Delete a course
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
    boolean deleted = courseService.deleteCourse(id);
    return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }
}
