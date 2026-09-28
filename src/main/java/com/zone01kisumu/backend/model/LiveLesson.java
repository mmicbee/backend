package com.zone01kisumu.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// Represents a live-streamed lesson
@Entity
@DiscriminatorValue("LIVE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LiveLesson extends CourseLesson {
    @Column(name = "meeting_url")
    private String meetingUrl;
}
