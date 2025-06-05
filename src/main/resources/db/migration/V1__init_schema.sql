-- Skapa tabeller om de inte finns (kopiera från schema.sql)
CREATE TABLE IF NOT EXISTS COURSE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS QUESTION (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    question_number INT NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(512),
    CONSTRAINT fk_question_course
        FOREIGN KEY (course_id)
            REFERENCES COURSE(id)
);

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

CREATE TABLE IF NOT EXISTS USERS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    user_group VARCHAR(50),
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255)
);

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

CREATE TABLE IF NOT EXISTS QUIZ_STATISTIC (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id UUID NOT NULL,
    question_id BIGINT NOT NULL,
    answered_option CHAR(1),
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stat_session
        FOREIGN KEY (session_id)
            REFERENCES QUIZ_SESSION(id),
    CONSTRAINT fk_stat_question
        FOREIGN KEY (question_id)
            REFERENCES QUESTION(id)
);
