CREATE TABLE IF NOT EXISTS student (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    profile_picture VARCHAR(255), -- URL to the student's profile picture
    password VARCHAR(255) NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
--the NOT NULL constraint are for registraition form fields
--the other constrains can be used in the settings of the profile page
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    professional_level VARCHAR(255), -- Education details of the teacher
    certification VARCHAR(255), -- Certifications held by the teacher
    profile_picture VARCHAR(255), -- URL to the teacher's profile picture
    bio TEXT, -- Short biography of the teacher
    year_of_experience INT, -- Years of experience in teaching
    course VARCHAR(255),
    language VARCHAR(50), -- Language the teacher is proficient in
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS institution (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    institution_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(255) NOT NULL, --the registration number of the school
    school_type VARCHAR(50) NOT NULL, -- Type of school
    education_system VARCHAR(50) NOT NULL, -- Education system followed by the school
    location VARCHAR(100) NOT NULL, -- Location of the school
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    -- these details can be used in the settings of the profile page
    postal_address VARCHAR(100), -- Postal address of the school
    website VARCHAR(100), -- Website of the school
    logo VARCHAR(100), -- URL to the school's logo
    description TEXT, -- Description of the school
    principal_name VARCHAR(255), -- Name of the principal
    principal_email VARCHAR(100) UNIQUE, -- Email of the principal
    principal_phone VARCHAR(20) UNIQUE, -- Phone number of the principal
    established_year INT, -- Year the school was established
    accreditation_status VARCHAR(50) DEFAULT 'PENDING', -- Accreditation status of the school
    accreditation_body VARCHAR(255), -- Body that accredited the school
    accreditation_date TIMESTAMP, -- Date of accreditation
    created_by BIGINT, -- ID of the teacher who created the institution
    FOREIGN KEY (created_by) REFERENCES teacher(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    teacher_id BIGINT, -- ID of the teacher who created the course
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT NOT NULL, -- Duration in weeks
    category VARCHAR(50) NOT NULL, -- Category of the course
    mode VARCHAR(50) NOT NULL, -- Mode of delivery
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    Payment_method VARCHAR(50) DEFAULT 'CREDIT_CARD', -- Payment account for the course
    mpesa_payment_type VARCHAR(50), -- PAYBILL or TILL, only used when payment_method is MPESA
    paybill_number VARCHAR(100), -- Only used for paybill (business number)
    payment_account VARCHAR(255), -- Details of the payment account (e.g., MPESA number, credit card details)
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'CLOSED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS course_topic (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    total_expected_lessons INT DEFAULT 0 NOT NULL,
    duration INT DEFAULT 0, -- Duration in seconds for videos or live lessons
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS course_lesson (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    lesson_name VARCHAR(255) NOT NULL,
    recorded_media_type VARCHAR(50), 
    recorded_media BYTEA, -- For storing actual file content
    recorded_link VARCHAR(500), -- For external links
    live_url VARCHAR(255), -- For live session URLs
    lesson_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration INT, -- Duration/size metric (meaning varies by type, in seconds for videos or live lessons)
    duration_unit VARCHAR(20), -- 'seconds', 'pages', 'words', 'slides', etc.
    file_size BIGINT, -- File size in bytes
    dtype VARCHAR(31) NOT NULL, -- Discriminator column for lesson type (RECORDED/LIVE)
    content BYTEA, -- Recorded lesson file content
    media_type VARCHAR(255), -- Recorded lesson media type
    meeting_url VARCHAR(255), -- Live lesson meeting link
    FOREIGN KEY (topic_id) REFERENCES course_topic(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- This table tracks a student's attendance for each *specific scheduled course lesson*.
CREATE TABLE IF NOT EXISTS student_attendance (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_lesson_id BIGINT NOT NULL, -- Refers to a specific scheduled lesson from course_lesson
    attendance_status VARCHAR(50) NOT NULL,
    marked_by VARCHAR(50) NOT NULL, -- Indicates if attendance was marked by a teacher or automatically by the system
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When the attendance was recorded
    UNIQUE (student_id, course_lesson_id), -- Ensures a student has only one attendance record per scheduled lesson
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_lesson_id) REFERENCES course_lesson(id) ON DELETE CASCADE
);

-- This table tracks the overall progress for a student *per course topic*.
-- The progress is calculated based on the student's attendance in the lessons linked to that topic.
CREATE TABLE IF NOT EXISTS course_progress (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL, -- Refers to a specific topic/unit from course_topic
    course_id BIGINT NOT NULL, -- Redundant but useful for direct queries and joins
    completed_lessons_count INT DEFAULT 0 NOT NULL, -- Number of lessons a student has attended for this topic
    progress_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL, -- Calculated percentage of lessons completed for this topic
    status VARCHAR(50) DEFAULT 'NOT_STARTED',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Automatically updates on row modification
    UNIQUE (student_id, topic_id), -- Ensures a student has only one progress entry per topic
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES course_topic(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_enrollment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    payment_method VARCHAR(50) DEFAULT 'CREDIT_CARD',
    amount_due DECIMAL(10, 2) DEFAULT 0.00,
    amount_paid DECIMAL(10, 2) DEFAULT 0.00,
    UNIQUE (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

--payment_status can be used to track the payment status of the course enrollment
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL, -- Amount paid
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL, -- Payment method used
    status VARCHAR(50) DEFAULT 'PENDING', -- Status of the payment
    paystack_reference VARCHAR(100),
    paystack_access_code VARCHAR(100),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
    
);

CREATE TABLE IF NOT EXISTS assesment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT, 
    content_type VARCHAR(50) NOT NULL, -- Type of assessment
    assesment_type VARCHAR(50) NOT NULL, -- Type of assessment 
    content_url VARCHAR(255), 
    duration INT NOT NULL, -- Duration in minutes
    points INT NOT NULL, -- Total points for the quiz
    course_id BIGINT NOT NULL,
    due_date TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL, -- ID of the teacher who created the quiz
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES teacher(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS assesment_submission (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assesment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content TEXT, -- Content of the submission
    grade INT, -- Grade given by the teacher
    feedback TEXT, -- Feedback from the teacher
    UNIQUE (assesment_id, student_id), -- Ensure a student can only submit once per assignment
    FOREIGN KEY (assesment_id) REFERENCES assesment(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS grades (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    submission_id BIGINT NOT NULL, -- ID of the submission this grade is for
    grade INT CHECK (grade >= 0 AND grade <= 100), -- Grade between 0 and 100
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    feedback TEXT, -- Feedback from the teacher
    UNIQUE (student_id, course_id), -- Ensure a student can only have one grade per course
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (submission_id) REFERENCES assesment_submission(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_feedback (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    feedback_text TEXT, -- Feedback text provided by the student
    rating INT CHECK (rating >= 1 AND rating <= 5), -- Rating between 1 and 5
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
);
