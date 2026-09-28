package com.zone01kisumu.backend.repository.specification;

import com.zone01kisumu.backend.dto.CourseCatalogDTOs.CourseCatalogSearchCriteria;
import com.zone01kisumu.backend.model.Course;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** JPA Specifications for dynamic query filtering of courses in the catalog. */
public class CourseSpecifications {

  /**
   * Builds a combined Specification from search and filter criteria.
   *
   * @param criteria The search criteria parameters.
   * @return Composite Spring Data JPA Specification.
   */
  public static Specification<Course> buildSpecification(CourseCatalogSearchCriteria criteria) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (criteria == null) {
        return criteriaBuilder.conjunction();
      }

      // Keyword search across title and description
      if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
        String searchPattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
        Predicate titlePredicate =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
        Predicate descriptionPredicate =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);

        predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
      }

      // Category filter
      if (criteria.getCategory() != null
          && !criteria.getCategory().trim().isEmpty()
          && !"ALL".equalsIgnoreCase(criteria.getCategory().trim())) {
        try {
          Course.Category categoryEnum =
              Course.Category.valueOf(criteria.getCategory().trim().toUpperCase());
          predicates.add(criteriaBuilder.equal(root.get("category"), categoryEnum));
        } catch (IllegalArgumentException ignored) {
          // Ignore invalid category value
        }
      }

      // Mode filter (LIVE, RECORDED)
      if (criteria.getMode() != null
          && !criteria.getMode().trim().isEmpty()
          && !"ALL".equalsIgnoreCase(criteria.getMode().trim())) {
        try {
          Course.Mode modeEnum = Course.Mode.valueOf(criteria.getMode().trim().toUpperCase());
          predicates.add(criteriaBuilder.equal(root.get("mode"), modeEnum));
        } catch (IllegalArgumentException ignored) {
          // Ignore invalid mode value
        }
      }

      // Status filter (OPEN, STARTED, COMPLETED, etc.)
      if (criteria.getStatus() != null
          && !criteria.getStatus().trim().isEmpty()
          && !"ALL".equalsIgnoreCase(criteria.getStatus().trim())) {
        try {
          Course.Status statusEnum =
              Course.Status.valueOf(criteria.getStatus().trim().toUpperCase());
          predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
        } catch (IllegalArgumentException ignored) {
          // Ignore invalid status value
        }
      }

      // Price filters
      if (Boolean.TRUE.equals(criteria.getIsFree())) {
        predicates.add(criteriaBuilder.equal(root.get("price"), BigDecimal.ZERO));
      } else if (Boolean.FALSE.equals(criteria.getIsFree())) {
        predicates.add(criteriaBuilder.gt(root.get("price"), BigDecimal.ZERO));
      } else {
        if (criteria.getMinPrice() != null) {
          predicates.add(
              criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
          predicates.add(
              criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }
      }

      // Teacher ID filter
      if (criteria.getTeacherId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("teacherId"), criteria.getTeacherId()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
