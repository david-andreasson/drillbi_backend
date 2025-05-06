-- 1. Course table
CREATE TABLE IF NOT EXISTS COURSE (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL UNIQUE,
                                      description TEXT
);

-- 2. Question table
CREATE TABLE IF NOT EXISTS QUESTION (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        course_id BIGINT NOT NULL,
                                        question_number INT NOT NULL,
                                        question_text TEXT NOT NULL,
                                        CONSTRAINT fk_question_course
                                            FOREIGN KEY (course_id)
                                                REFERENCES COURSE(id)
);

-- 3. Question Option table
CREATE TABLE IF NOT EXISTS QUESTION_OPTION (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               question_id BIGINT NOT NULL,
                                               option_label CHAR(1) NOT NULL,
                                               option_text TEXT NOT NULL,
                                               is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                                               CONSTRAINT fk_option_question
                                                   FOREIGN KEY (question_id)
                                                       REFERENCES QUESTION(id)
);

-- 4. User table (renamed to USERS)
CREATE TABLE IF NOT EXISTS USERS (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     first_name VARCHAR(255),
                                     last_name VARCHAR(255),
                                     role VARCHAR(50) NOT NULL,
                                     user_group VARCHAR(50)
);

-- 5. Quiz Session table
CREATE TABLE IF NOT EXISTS QUIZ_SESSION (
                                            id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            course_id BIGINT NOT NULL,
                                            order_type VARCHAR(20) NOT NULL,
                                            start_question INT NOT NULL,
                                            started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            CONSTRAINT fk_session_user
                                                FOREIGN KEY (user_id)
                                                    REFERENCES USERS(id),
                                            CONSTRAINT fk_session_course
                                                FOREIGN KEY (course_id)
                                                    REFERENCES COURSE(id)
);

-- 6. Quiz Statistic table
CREATE TABLE IF NOT EXISTS QUIZ_STAT (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         session_id UUID NOT NULL,
                                         answered_count INT NOT NULL,
                                         correct_count INT NOT NULL,
                                         error_rate DECIMAL(5,2) NOT NULL,
                                         finished_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         CONSTRAINT fk_stat_session
                                             FOREIGN KEY (session_id)
                                                 REFERENCES QUIZ_SESSION(id)
);

-- 7. Daily AI Usage table
CREATE TABLE IF NOT EXISTS DAILY_AI_USAGE (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              usage_date DATE NOT NULL,
                                              request_count INT NOT NULL,
                                              UNIQUE (user_id, usage_date),
                                              CONSTRAINT fk_ai_user
                                                  FOREIGN KEY (user_id)
                                                      REFERENCES USERS(id)
);