package com.zone01kisumu.backend.courseLessonTests;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CourseLessonTest {

    @Test
    void testCourseLessonCreation() {
        Course course = new Course();
        CourseTopic topic = new CourseTopic();
        LocalDateTime now = LocalDateTime.now();

        CourseLesson lesson = new CourseLesson();
        lesson.setId(1L);
        lesson.setCourse(course);
        lesson.setTopic(topic);
        lesson.setLessonName("Introduction to Tika");
        lesson.setLessonDate(now);
        lesson.setDuration(60);
        lesson.setLiveUrl("https://example.zoom.us/j/12345");
        lesson.setRecordedMedia("some content".getBytes());
        lesson.setRecordedMediaType(CourseLesson.RecordedMediaType.VIDEO);
        lesson.setRecordedLink("https://example.com/video.mp4");


        assertAll("CourseLesson properties",
                () -> assertEquals(1L, lesson.getId()),
                () -> assertEquals(course, lesson.getCourse()),
                () -> assertEquals(topic, lesson.getTopic()),
                () -> assertEquals("Introduction to Tika", lesson.getLessonName()),
                () -> assertEquals(now, lesson.getLessonDate()),
                () -> assertEquals(60, lesson.getDuration()),
                () -> assertEquals("https://example.zoom.us/j/12345", lesson.getLiveUrl()),
                () -> assertArrayEquals("some content".getBytes(), lesson.getRecordedMedia()),
                () -> assertEquals(CourseLesson.RecordedMediaType.VIDEO, lesson.getRecordedMediaType()),
                () -> assertEquals("https://example.com/video.mp4", lesson.getRecordedLink())
        );
    }
}