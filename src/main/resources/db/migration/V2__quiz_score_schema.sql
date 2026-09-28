-- Migration V2: Create quiz_score table for Issue #178
CREATE TABLE IF NOT EXISTS quiz_score (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    course_id BIGINT,
    score DOUBLE PRECISION NOT NULL,
    max_score DOUBLE PRECISION DEFAULT 100.0,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_quiz_score_student ON quiz_score (student_id);
CREATE INDEX IF NOT EXISTS idx_quiz_score_quiz ON quiz_score (quiz_id);
CREATE INDEX IF NOT EXISTS idx_quiz_score_course ON quiz_score (course_id);
