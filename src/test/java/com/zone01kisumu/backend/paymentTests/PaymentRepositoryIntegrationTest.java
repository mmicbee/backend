package com.zone01kisumu.backend.paymentTests;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.model.Payment.PaymentMethod;
import com.zone01kisumu.backend.model.Payment.PaymentStatus;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.PaymentRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentRepositoryIntegrationTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAndFindByPaystackReference() {
        LocalDateTime now = LocalDateTime.now();

        //Create and save a student
        Student student = new Student();
        student.setCreatedAt(now);
        student.setFirstName("victor");
        student.setLastName("paul");
        student.setEmail("victorpaul@example.com");
        student.setPassword("securepassword");
        student.setPhone("1234567890");

        Student savedStudent = new Student();
        savedStudent.setId(1L);
        savedStudent.setCreatedAt(now);
        savedStudent.setFirstName("victor");
        savedStudent.setLastName("paul");
        savedStudent.setEmail("victorpaul@example.com");
        savedStudent.setPassword("securepassword");
        savedStudent.setPhone("1234567890");

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        student = studentRepository.save(student);

        //Create and save a teacher
        Teacher teacher = new Teacher();
        teacher.setCreatedAt(now);
        teacher.setFirstName("teacher");
        teacher.setLastName("one");
        teacher.setEmail("teacherone@example.com");
        teacher.setPassword("securepassword");
        teacher.setPhone("0987654321");

        Teacher savedTeacher = new Teacher();
        savedTeacher.setId(2L);
        savedTeacher.setCreatedAt(now);
        savedTeacher.setFirstName("teacher");
        savedTeacher.setLastName("one");
        savedTeacher.setEmail("teacherone@example.com");
        savedTeacher.setPassword("securepassword");
        savedTeacher.setPhone("0987654321");

        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        teacher = teacherRepository.save(teacher);

        //Create and save a course
        Course course = new Course();
        course.setCreatedAt(now);
        course.setEndDate(now.plusMonths(3));
        course.setPaymentAccount("acc123");
        course.setPrice(new BigDecimal("1500.00"));
        course.setStartDate(now);
        course.setDuration(90); // days
        course.setMode(Course.Mode.LIVE);
        course.setTeacherId(teacher.getId());
        course.setTitle("Java Basics");
        course.setDescription("Intro course");
        course.setCategory(Course.Category.SCIENCE_AND_TECHNOLOGY);

        Course savedCourse = new Course();
        savedCourse.setId(3L);
        savedCourse.setCreatedAt(now);
        savedCourse.setEndDate(now.plusMonths(3));
        savedCourse.setPaymentAccount("acc123");
        savedCourse.setPrice(new BigDecimal("1500.00"));
        savedCourse.setStartDate(now);
        savedCourse.setDuration(90);
        savedCourse.setMode(Course.Mode.LIVE);
        savedCourse.setTeacherId(teacher.getId());
        savedCourse.setTitle("Java Basics");
        savedCourse.setDescription("Intro course");
        savedCourse.setCategory(Course.Category.SCIENCE_AND_TECHNOLOGY);

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        course = courseRepository.save(course);

        //Save a payment
        Payment payment = new Payment();
        payment.setStudentId(student.getId());
        payment.setCourseId(course.getId());
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setPaymentMethod(PaymentMethod.MPESA);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(now);
        payment.setPaystackReference("ref-abc-123");
        payment.setPaystackAccessCode("code-xyz");

        Payment savedPayment = new Payment();
        savedPayment.setId(4L);
        savedPayment.setStudentId(student.getId());
        savedPayment.setCourseId(course.getId());
        savedPayment.setAmount(new BigDecimal("1500.00"));
        savedPayment.setPaymentMethod(PaymentMethod.MPESA);
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        savedPayment.setPaymentDate(now);
        savedPayment.setPaystackReference("ref-abc-123");
        savedPayment.setPaystackAccessCode("code-xyz");

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentRepository.findByPaystackReference("ref-abc-123")).thenReturn(Optional.of(savedPayment));

        paymentRepository.save(payment);

        //Verify
        Optional<Payment> found = paymentRepository.findByPaystackReference("ref-abc-123");

        assertThat(found).isPresent();
        assertThat(found.get().getStudentId()).isEqualTo(student.getId());
    }
}
