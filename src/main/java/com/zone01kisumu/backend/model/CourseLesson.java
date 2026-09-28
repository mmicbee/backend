package com.zone01kisumu.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_lesson")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseLesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Course course;
    
    @Column(name = "topic_id", nullable = false)
    private Long topicId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, insertable = false, updatable = false)
    private CourseTopic topic;
    
    @Column(name = "lesson_name", nullable = false)
    private String lessonName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recorded_media_type")
    private RecordedMediaType recordedMediaType;
    
    @Column(name = "recorded_media", columnDefinition = "BYTEA")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] recordedMedia;
    
    @Column(name = "recorded_link", length = 500)
    private String recordedLink;
    
    @Column(name = "live_url", length = 255)
    private String liveUrl;
    
    @UpdateTimestamp
    @Column(name = "lesson_date")
    private LocalDateTime lessonDate;
    
    @Column(name = "duration")
    private Integer duration;
    
    @Column(name = "duration_unit", length = 20)
    private String durationUnit;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    public enum RecordedMediaType {
        PDF,
        VIDEO,
        LIVE,
        SLIDES,
        OTHER
    }
}