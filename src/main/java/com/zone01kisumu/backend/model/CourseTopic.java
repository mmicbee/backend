package com.zone01kisumu.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entity class for Course Topic
@Entity
@Table(name = "course_topic")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_expected_lessons", nullable = false)
    private Integer totalExpectedLessons = 1;

    @Column(name ="duration")
    private Integer duration = 0; // Duration in minutes for videos or live lessons

}
