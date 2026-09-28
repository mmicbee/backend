-- Migration V4: Seed initial teachers, courses, topics, and lessons for Issue #265

-- 1. Seed Sample Instructors
INSERT INTO teacher (firstname, lastname, email, phone, password, professional_level, certification, bio, year_of_experience, course, language)
SELECT 'Mwangi', 'Kimani', 'mwangi.kimani@example.com', '+254711000001', '$2a$10$wE1M2M8CqJ9K3jF6kG9mbeo8E8hU2G3A5D4F6G7H8J9K0L1M2N3O4', 'PhD in Computer Science', 'AWS Solutions Architect, CKA', 'Senior Software Architect and Cloud Engineer with 12+ years of experience in distributed systems and JVM architecture.', 12, 'Software Engineering', 'English'
WHERE NOT EXISTS (SELECT 1 FROM teacher WHERE email = 'mwangi.kimani@example.com');

INSERT INTO teacher (firstname, lastname, email, phone, password, professional_level, certification, bio, year_of_experience, course, language)
SELECT 'Sarah', 'Cherono', 'sarah.cherono@example.com', '+254711000002', '$2a$10$wE1M2M8CqJ9K3jF6kG9mbeo8E8hU2G3A5D4F6G7H8J9K0L1M2N3O4', 'MSc in Data Science', 'TensorFlow Certified Developer', 'Data Scientist and AI researcher focusing on deep learning, neural networks, and scalable ML pipelines.', 8, 'Data Science', 'English'
WHERE NOT EXISTS (SELECT 1 FROM teacher WHERE email = 'sarah.cherono@example.com');

-- 2. Seed Sample Courses
INSERT INTO course (teacher_id, title, description, duration, category, mode, price, payment_method, mpesa_payment_type, paybill_number, payment_account, start_date, end_date, status)
SELECT 
    t.id,
    'Full Stack Web Development with React & Spring Boot',
    'Comprehensive end-to-end web engineering masterclass covering modern React, Spring Boot 3 REST APIs, JPA, security, and cloud deployment.',
    12,
    'Web Development',
    'LIVE',
    150.00,
    'MPESA',
    'PAYBILL',
    '522522',
    'FullStackWeb2026',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'OPEN'
FROM teacher t WHERE t.email = 'mwangi.kimani@example.com'
AND NOT EXISTS (SELECT 1 FROM course WHERE title = 'Full Stack Web Development with React & Spring Boot');

INSERT INTO course (teacher_id, title, description, duration, category, mode, price, payment_method, mpesa_payment_type, paybill_number, payment_account, start_date, end_date, status)
SELECT 
    t.id,
    'Practical Data Science and Machine Learning with Python',
    'Hands-on data analysis, pandas, scikit-learn, and production machine learning model deployment for real-world enterprise applications.',
    10,
    'Data Science',
    'RECORDED',
    120.00,
    'MPESA',
    'TILL',
    '654321',
    'DataScienceTill',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'OPEN'
FROM teacher t WHERE t.email = 'sarah.cherono@example.com'
AND NOT EXISTS (SELECT 1 FROM course WHERE title = 'Practical Data Science and Machine Learning with Python');

INSERT INTO course (teacher_id, title, description, duration, category, mode, price, payment_method, mpesa_payment_type, paybill_number, payment_account, start_date, end_date, status)
SELECT 
    t.id,
    'Cloud Architecture and DevOps Engineering',
    'Master containerization with Docker, Kubernetes orchestration, CI/CD automation pipelines, and infrastructure as code.',
    8,
    'Cloud Computing',
    'RECORDED',
    0.00,
    'CREDIT_CARD',
    NULL,
    NULL,
    'FreeTierDevOps',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'OPEN'
FROM teacher t WHERE t.email = 'mwangi.kimani@example.com'
AND NOT EXISTS (SELECT 1 FROM course WHERE title = 'Cloud Architecture and DevOps Engineering');

-- 3. Seed Course Topics
INSERT INTO course_topic (course_id, title, description, total_expected_lessons, duration)
SELECT c.id, 'Topic 1: Spring Boot Architecture & RESTful APIs', 'Understanding the Spring container, dependency injection, and REST controllers.', 3, 10800
FROM course c WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_topic WHERE course_id = c.id AND title = 'Topic 1: Spring Boot Architecture & RESTful APIs');

INSERT INTO course_topic (course_id, title, description, total_expected_lessons, duration)
SELECT c.id, 'Topic 2: Frontend Client Integration with React & Tailwind', 'Component state management, responsive UI with Tailwind, and Axios API client integration.', 3, 10800
FROM course c WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_topic WHERE course_id = c.id AND title = 'Topic 2: Frontend Client Integration with React & Tailwind');

INSERT INTO course_topic (course_id, title, description, total_expected_lessons, duration)
SELECT c.id, 'Topic 1: Exploratory Data Analysis with Pandas and NumPy', 'Data cleaning, transformation, and statistical aggregation techniques.', 2, 7200
FROM course c WHERE c.title = 'Practical Data Science and Machine Learning with Python'
AND NOT EXISTS (SELECT 1 FROM course_topic WHERE course_id = c.id AND title = 'Topic 1: Exploratory Data Analysis with Pandas and NumPy');

INSERT INTO course_topic (course_id, title, description, total_expected_lessons, duration)
SELECT c.id, 'Topic 2: Supervised & Unsupervised Machine Learning Models', 'Training regression, classification, and clustering models with scikit-learn.', 2, 7200
FROM course c WHERE c.title = 'Practical Data Science and Machine Learning with Python'
AND NOT EXISTS (SELECT 1 FROM course_topic WHERE course_id = c.id AND title = 'Topic 2: Supervised & Unsupervised Machine Learning Models');

INSERT INTO course_topic (course_id, title, description, total_expected_lessons, duration)
SELECT c.id, 'Topic 1: Docker Containerization & Multi-Stage Builds', 'Creating optimized Dockerfiles, multi-stage builds, and docker-compose environments.', 2, 7200
FROM course c WHERE c.title = 'Cloud Architecture and DevOps Engineering'
AND NOT EXISTS (SELECT 1 FROM course_topic WHERE course_id = c.id AND title = 'Topic 1: Docker Containerization & Multi-Stage Builds');

-- 4. Seed Course Lessons with Videos, PDFs, and Live Links
INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 1.1: Introduction to Spring Framework and Beans', 'VIDEO', 'https://www.youtube.com/watch?v=kqtD5dpn9C8', NULL, 3600, 'seconds', 104857600, 'RECORDED', 'video/mp4', NULL
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 1: Spring Boot Architecture & RESTful APIs'
WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 1.1: Introduction to Spring Framework and Beans');

INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 1.2: Database Persistence with Spring Data JPA & PostgreSQL', 'VIDEO', 'https://www.youtube.com/watch?v=rfscVS0vtbw', NULL, 3600, 'seconds', 157286400, 'RECORDED', 'video/mp4', NULL
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 1: Spring Boot Architecture & RESTful APIs'
WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 1.2: Database Persistence with Spring Data JPA & PostgreSQL');

INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 1.3: Spring Boot Architecture Whitepaper & Specs', 'PDF', 'https://example.com/docs/spring-boot-architecture.pdf', NULL, 45, 'pages', 2097152, 'RECORDED', 'application/pdf', NULL
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 1: Spring Boot Architecture & RESTful APIs'
WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 1.3: Spring Boot Architecture Whitepaper & Specs');

INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 2.1: Live Architecture Review & Q&A Session', 'LIVE', NULL, 'https://meet.google.com/abc-defg-hij', 3600, 'seconds', 0, 'LIVE', 'video/webrtc', 'https://meet.google.com/abc-defg-hij'
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 2: Frontend Client Integration with React & Tailwind'
WHERE c.title = 'Full Stack Web Development with React & Spring Boot'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 2.1: Live Architecture Review & Q&A Session');

INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 1.1: Pandas DataFrames, Series, and Indexing', 'VIDEO', 'https://www.youtube.com/watch?v=vmEHCJofslg', NULL, 3600, 'seconds', 125829120, 'RECORDED', 'video/mp4', NULL
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 1: Exploratory Data Analysis with Pandas and NumPy'
WHERE c.title = 'Practical Data Science and Machine Learning with Python'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 1.1: Pandas DataFrames, Series, and Indexing');

INSERT INTO course_lesson (course_id, topic_id, lesson_name, recorded_media_type, recorded_link, live_url, duration, duration_unit, file_size, dtype, media_type, meeting_url)
SELECT c.id, ct.id, 'Lesson 1.1: Containerizing Spring and React Applications', 'VIDEO', 'https://www.youtube.com/watch?v=3c-iBn73dDE', NULL, 3600, 'seconds', 94371840, 'RECORDED', 'video/mp4', NULL
FROM course c 
JOIN course_topic ct ON ct.course_id = c.id AND ct.title = 'Topic 1: Docker Containerization & Multi-Stage Builds'
WHERE c.title = 'Cloud Architecture and DevOps Engineering'
AND NOT EXISTS (SELECT 1 FROM course_lesson WHERE topic_id = ct.id AND lesson_name = 'Lesson 1.1: Containerizing Spring and React Applications');
