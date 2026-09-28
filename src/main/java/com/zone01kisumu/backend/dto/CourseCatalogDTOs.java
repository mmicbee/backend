package com.zone01kisumu.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Objects for Course Catalog Search, Filtering, and Pagination. */
public class CourseCatalogDTOs {

  /** Search and filtering query criteria for course discovery. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseCatalogSearchCriteria {
    private String keyword;
    private String category;
    private String mode;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isFree;
    private Long teacherId;

    @Builder.Default private int page = 0;

    @Builder.Default private int size = 12;

    @Builder.Default private String sortBy = "relevance";

    @Builder.Default private String sortDirection = "asc";
  }

  /**
   * Paginated response envelope for course catalog results.
   *
   * @param <T> Item type (typically CourseDTO)
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseCatalogResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<String> availableCategories;
  }
}
