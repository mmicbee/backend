package com.zone01kisumu.backend.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// Represents a lesson with pre-recorded content (video, PDF, etc.)
@Entity
@DiscriminatorValue("RECORDED")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RecordedLesson extends CourseLesson {
    @Column(name = "content", columnDefinition = "BYTEA")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] content;

    @Column(name = "media_type")
    private String mediaType;
}