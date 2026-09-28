package com.zone01kisumu.backend.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CourseCategoryConverter implements AttributeConverter<Course.Category, String> {

  @Override
  public String convertToDatabaseColumn(Course.Category attribute) {
    return attribute != null ? attribute.name() : Course.Category.OTHERS.name();
  }

  @Override
  public Course.Category convertToEntityAttribute(String dbData) {
    return Course.Category.fromString(dbData);
  }
}
