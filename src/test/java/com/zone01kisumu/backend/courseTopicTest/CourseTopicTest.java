package com.zone01kisumu.backend.courseTopicTest;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// This test class is used to validate the CourseTopic entity
class CourseTopicTest {

    @Test
    void testAllArgsConstructor() {
        Course course = new Course();
        course.setId(1L);

        // Constructor arguments should match the entity's AllArgsConstructor
        CourseTopic topic = new CourseTopic(
                1L,
                course,
                "Introduction to Java",
                "Basic overview of Java programming language.",
                1,
                45
                
        );

        assertEquals(1L, topic.getId());
        assertEquals(course, topic.getCourse());
        assertEquals("Introduction to Java", topic.getTitle());
        assertEquals("Basic overview of Java programming language.", topic.getDescription());
        assertEquals(1, topic.getTotalExpectedLessons());
        assertEquals(45, topic.getDuration());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        CourseTopic topic = new CourseTopic();
        Course course = new Course();
        course.setId(2L);

        topic.setId(2L);
        topic.setCourse(course);
        topic.setTitle("Advanced Topics");
        topic.setDescription("In-depth discussion of advanced Java topics.");
        topic.setTotalExpectedLessons(2);
        topic.setDuration(60);

        assertEquals(2L, topic.getId());
        assertEquals(course, topic.getCourse());
        assertEquals("Advanced Topics", topic.getTitle());
        assertEquals("In-depth discussion of advanced Java topics.", topic.getDescription());
        assertEquals(2, topic.getTotalExpectedLessons());
        assertEquals(60, topic.getDuration());
    }
}
