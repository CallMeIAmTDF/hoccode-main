CREATE TABLE IF NOT EXISTS `users`
(
    `id`         varchar(255) NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(255) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `active`     bit(1)       DEFAULT NULL,
    `avatar`     varchar(255) DEFAULT NULL,
    `dob`        date         DEFAULT NULL,
    `email`      varchar(255) DEFAULT NULL,
    `name`       varchar(255) DEFAULT NULL,
    `role`       varchar(255) DEFAULT 'USER',
    `password`   varchar(255) DEFAULT NULL,
    `is_locked`  bit(1)       DEFAULT 0,
    `point`      int          DEFAULT 0,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO users (id, created_at, created_by, updated_at, updated_by, active, avatar, dob, email, name, role, password, is_locked, point)
SELECT 'e692cd89-e09e-4651-afb8-8956d349ff6c',
       NOW(),
       'system',
       NOW(),
       'system',
       1,
       NULL,
       '2000-01-01',
       'user@gmail.com',
       'Default User',
       'USER',
       '$2a$10$EJkL.sXN6Tg.NHzrmTk7DeWJf2lO/QYAJk7x7S41T4iHlgfimeUQu',
       0,
       0 WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'user@gmail.com'
);

INSERT INTO users (id, created_at, created_by, updated_at, updated_by, active, avatar, dob, email, name, role, password, is_locked, point)
SELECT 'e692cd89-e09e-4651-afb8-8956d349ff6t',
       NOW(),
       'system',
       NOW(),
       'system',
       1,
       NULL,
       '2000-01-01',
       'user2@gmail.com',
       'Default User 2',
       'USER',
       '$2a$10$EJkL.sXN6Tg.NHzrmTk7DeWJf2lO/QYAJk7x7S41T4iHlgfimeUQu',
       0,
       0 WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'user2@gmail.com'
);

INSERT INTO users (id, created_at, created_by, updated_at, updated_by, active, avatar, dob, email, name, role, password, is_locked, point)
SELECT 'f7a9b8cd-3f4b-4c6a-8c13-09ec03c00abc',
       NOW(),
       'system',
       NOW(),
       'system',
       1,
       NULL,
       '1995-05-15',
       'admin@gmail.com',
       'Default Admin',
       'ADMIN',
       '$2a$10$EJkL.sXN6Tg.NHzrmTk7DeWJf2lO/QYAJk7x7S41T4iHlgfimeUQu',
       0,
       0
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@gmail.com'
);


CREATE TABLE IF NOT EXISTS `verification_code`
(
    `id`    bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `code`  varchar(255) DEFAULT NULL,
    `email` varchar(255) DEFAULT NULL,
    `exp`   datetime(6) DEFAULT NULL,
    `type`  tinyint(4) DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;


-- =========================================
-- Tạo TABLEs (nếu chưa tồn tại)
-- =========================================

CREATE TABLE IF NOT EXISTS problems (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    constrains TEXT,
    follow_up TEXT,
    time_limit INT,
    memory_limit INT,
    difficulty VARCHAR(20),
    is_contest_problem BOOLEAN,
    point INT,
    deleted BOOLEAN DEFAULT false,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS hints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pid INT NOT NULL,
    description LONGTEXT,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    FOREIGN KEY (pid) REFERENCES problems(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS testcases (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pid INT NOT NULL,
    input LONGTEXT,
    expected_output LONGTEXT,
    is_hidden BOOLEAN,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    FOREIGN KEY (pid) REFERENCES problems(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS problem_categories (
    problem_id INT NOT NULL,
    category VARCHAR(100),
    PRIMARY KEY (problem_id, category),
    FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE
    );


-- =========================================
-- 🧪 Problem 1: Sum of Two Numbers (EASY)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 1, 'Sum of Two Numbers', 'Given two integers, return their sum.', '1 <= a, b <= 10^9', 'Try using bitwise operators.', 2, 256, 'EASY', false, 100, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Sum of Two Numbers');

INSERT INTO hints (id, pid, description, created_at, created_by, updated_at, updated_by)
SELECT 1, 1, 'Use Scanner to read input', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 1 AND description = 'Use Scanner to read input');

INSERT INTO hints (id, pid, description, created_at, created_by, updated_at, updated_by)
SELECT 2, 1, 'Don''t forget to parse integers', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 1 AND description = 'Don''t forget to parse integers');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 1, 1, '1 2', '3', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 1 AND input = '1 2');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 2, 1, '1000 2000', '3000', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 1 AND input = '1000 2000');

INSERT INTO problem_categories (problem_id, category)
SELECT 1, 'MATH'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 1 AND category = 'MATH');

INSERT INTO problem_categories (problem_id, category)
SELECT 1, 'BIT_MANIPULATION'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 1 AND category = 'BIT_MANIPULATION');


-- =========================================
-- 🧪 Problem 2: Maximum of Three Numbers (EASY)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 2, 'Maximum of Three Numbers', 'Find the largest among three integers.', '1 <= a, b, c <= 10^9', 'Try using only two comparisons.', 1, 128, 'EASY', false, 120, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Maximum of Three Numbers');

INSERT INTO hints (id, pid, description, created_at, created_by, updated_at, updated_by)
SELECT 3, 2, 'Use Math.max or nested conditionals', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 2 AND description = 'Use Math.max or nested conditionals');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 3, 2, '1 2 3', '3', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 2 AND input = '1 2 3');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 4, 2, '100 10 1000', '1000', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 2 AND input = '100 10 1000');

INSERT INTO problem_categories (problem_id, category)
SELECT 2, 'MATH'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 2 AND category = 'MATH');

INSERT INTO problem_categories (problem_id, category)
SELECT 2, 'GREEDY'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 2 AND category = 'GREEDY');


-- =========================================
-- 🧪 Problem 3: Reverse a String (MEDIUM)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 3, 'Reverse a String', 'Given a string, return its reverse.', '1 <= |s| <= 10^5', 'Can you reverse in-place?', 1, 128, 'MEDIUM', false, 150, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Reverse a String');

INSERT INTO hints (id, pid, description, created_at, created_by, updated_at, updated_by)
SELECT 4, 3, 'Use two pointers from both ends', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 3 AND description = 'Use two pointers from both ends');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 5, 3, 'hello', 'olleh', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 3 AND input = 'hello');

INSERT INTO testcases (id, pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 6, 3, 'OpenAI', 'IAnepO', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 3 AND input = 'OpenAI');

INSERT INTO problem_categories (problem_id, category)
SELECT 3, 'STRING'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 3 AND category = 'STRING');

INSERT INTO problem_categories (problem_id, category)
SELECT 3, 'TWO_POINTERS'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 3 AND category = 'TWO_POINTERS');


-- =========================================
-- 🧪 Problem 4: Check Prime Number (EASY)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 4, 'Check Prime Number',
       'Given an integer, determine whether it is a prime number.',
       '1 <= n <= 10^6',
       'Can you optimize using sqrt(n)?',
       1, 128, 'EASY', false, 120, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Check Prime Number');

INSERT INTO hints (pid, description, created_at, created_by, updated_at, updated_by)
SELECT 4, 'Try checking divisibility from 2 to sqrt(n)', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 4 AND description LIKE 'Try checking%');

INSERT INTO problem_categories (problem_id, category)
SELECT 4, 'NUMBER_THEORY'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 4 AND category = 'NUMBER_THEORY');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 4, '2', 'true', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 4 AND input = '2');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 4, '9', 'false', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 4 AND input = '9');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 4, '9973', 'true', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 4 AND input = '9973');


-- =========================================
-- 🧪 Problem 5: Factorial Calculation (EASY)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 5, 'Factorial Calculation',
       'Given an integer n, return n factorial (n!).',
       '0 <= n <= 20',
       'Try using loop instead of recursion to avoid stack overflow.',
       1, 128, 'EASY', false, 130, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Factorial Calculation');

INSERT INTO hints (pid, description, created_at, created_by, updated_at, updated_by)
SELECT 5, 'Use a for loop to multiply numbers from 1 to n', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 5);

INSERT INTO problem_categories (problem_id, category)
SELECT 5, 'MATH'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 5 AND category = 'MATH');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 5, '0', '1', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 5 AND input = '0');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 5, '5', '120', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 5 AND input = '5');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 5, '10', '3628800', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 5 AND input = '10');


-- =========================================
-- 🧪 Problem 6: Longest Substring Without Repeating Characters (MEDIUM)
-- =========================================
INSERT INTO problems (id, title, description, constrains, follow_up, time_limit, memory_limit, difficulty, is_contest_problem, point, created_at, created_by, updated_at, updated_by)
SELECT 6, 'Longest Substring Without Repeating Characters',
       'Given a string s, find the length of the longest substring without repeating characters.',
       '0 <= |s| <= 10^5',
       'Try using sliding window and hash set',
       2, 256, 'MEDIUM', false, 200, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Longest Substring Without Repeating Characters');

INSERT INTO hints (pid, description, created_at, created_by, updated_at, updated_by)
SELECT 6, 'Use sliding window with HashSet', NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE pid = 6);

INSERT INTO problem_categories (problem_id, category)
SELECT 6, 'STRING'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 6 AND category = 'STRING');

INSERT INTO problem_categories (problem_id, category)
SELECT 6, 'SLIDING_WINDOW'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 6 AND category = 'SLIDING_WINDOW');

INSERT INTO problem_categories (problem_id, category)
SELECT 6, 'HASH_TABLE'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 6 AND category = 'HASH_TABLE');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 6, 'abcabcbb', '3', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 6 AND input = 'abcabcbb');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 6, 'bbbbb', '1', false, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 6 AND input = 'bbbbb');

INSERT INTO testcases (pid, input, expected_output, is_hidden, created_at, created_by, updated_at, updated_by)
SELECT 6, 'pwwkew', '3', true, NOW(), 'system', NOW(), 'system'
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE pid = 6 AND input = 'pwwkew');