package com.zone01kisumu.backend.courseTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.Course;

// This test class is used to validate the Course entity
class CourseTest {

    @Test
    void testBuilderAndDefaults() {
        Course course = Course.builder()
                .title("Java Programming")
                .description("Learn Java from basics to advanced concepts")
                .duration(120)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(120))
                .mode(Course.Mode.LIVE)
                .price(BigDecimal.valueOf(99.99))
                .teacherId(1L)
                .paymentMethod(Course.PaymentMethod.MPESA)
                .paymentAccount("1234567890")
                .build();

        assertNotNull(course);
        assertEquals("Java Programming", course.getTitle());
        assertEquals("Learn Java from basics to advanced concepts", course.getDescription());
        assertEquals(120, course.getDuration());
        assertNotNull(course.getStartDate());
        assertNotNull(course.getEndDate());
        assertEquals(Course.Mode.LIVE, course.getMode());
        assertEquals(BigDecimal.valueOf(99.99), course.getPrice());
        assertEquals(1L, course.getTeacherId().longValue());
        assertEquals(Course.PaymentMethod.MPESA, course.getPaymentMethod());
        assertEquals("1234567890", course.getPaymentAccount());
    }
}
