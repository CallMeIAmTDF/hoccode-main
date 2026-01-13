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
       "2025-09-01 09:10:35.953501",
       'system',
       NOW(),
       'system',
       1,
       NULL,
       '2003-09-19',
       'thaidangfa@gmail.com',
       'Thai Dang',
       'USER',
       '$2a$10$EJkL.sXN6Tg.NHzrmTk7DeWJf2lO/QYAJk7x7S41T4iHlgfimeUQu',
       0,
       0 WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'thaidangfa@gmail.com'
);

INSERT INTO users (id, created_at, created_by, updated_at, updated_by, active, avatar, dob, email, name, role, password, is_locked, point)
SELECT 'f7a9b8cd-3f4b-4c6a-8c13-09ec03c00abc',
       "2025-09-01 09:10:35.953501",
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


CREATE TABLE IF NOT EXISTS documents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    file_name VARCHAR(255),
    status VARCHAR(50),
    size INTEGER,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
    );

-- Chèn dữ liệu có kiểm tra tránh trùng lặp ID
-- Bản ghi 1
INSERT INTO documents (id, created_at, created_by, updated_at, updated_by, file_name, size, status, title)
SELECT 1, '2025-12-15 15:45:52.296272', 'admin@gmail.com', '2025-12-30 16:40:13.511100', 'anonymousUser', '4a7054f9-e0ec-4784-9c6e-3c4224dcf68f.pdf', 19879373, 'UPLOADED', 'Phuong phap lap trinh trong Python v05'
    WHERE NOT EXISTS (SELECT 1 FROM documents WHERE id = 1);

-- Bản ghi 2
INSERT INTO documents (id, created_at, created_by, updated_at, updated_by, file_name, size, status, title)
SELECT 2, '2025-12-15 16:00:58.742483', 'admin@gmail.com', '2025-12-30 16:05:23.031301', 'anonymousUser', 'b1a917cb-e2b4-4cfc-867f-0d6e6e129e37.pdf', 1118908, 'UPLOADED', 'Competitive programmer’s handbook'
    WHERE NOT EXISTS (SELECT 1 FROM documents WHERE id = 2);

-- Bản ghi 3
INSERT INTO documents (id, created_at, created_by, updated_at, updated_by, file_name, size, status, title)
SELECT 3, '2025-12-15 16:45:19.022840', 'admin@gmail.com', NULL, NULL, '8fa98fa5-8fd8-4f49-9d73-daeb53530387.pdf', 1414733, 'UPLOADED', 'Principles of Algorithmic Problem Solving'
    WHERE NOT EXISTS (SELECT 1 FROM documents WHERE id = 3);

CREATE TABLE IF NOT EXISTS contests (
                                        id INT PRIMARY KEY,
                                        created_at TIMESTAMP,
                                        created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description TEXT,
    end_time TIMESTAMP,
    num_attendees INT DEFAULT 0,
    password VARCHAR(255),
    start_time TIMESTAMP,
    title VARCHAR(255),
    deleted TINYINT(1) DEFAULT 0
    );

-- 2. Chèn dữ liệu Contest ID 1 kèm kiểm tra tồn tại
INSERT INTO contests (
    id, created_at, created_by, updated_at, updated_by,
    description, end_time, num_attendees, password,
    start_time, title, deleted
)
SELECT
    1,
    '2025-09-08 09:10:35.953501',
    'thaidangfa@gmail.com',
    NULL,
    NULL,
    'A monthly programming contest with algorithmic and data structure problems.',
    '2025-09-30 12:00:00.000000',
    256,
    'secret123',
    '2025-09-08 12:00:00.000000',
    'September Coding Contest 2025',
    0
    WHERE NOT EXISTS (SELECT 1 FROM contests WHERE id = 1);


INSERT INTO problems (id, created_at, created_by, updated_at, updated_by, constrains, description, difficulty, follow_up, is_contest_problem, memory_limit, point, time_limit, title, deleted)
SELECT 7, '2025-09-08 09:10:35.886654', 'thaidangfa@gmail.com', NULL, NULL, '2 <= n <= 10^5, -10^9 <= nums[i] <= 10^9', 'Given an array of integers and a target, return indices of the two numbers such that they add up to the target.', 'EASY', 'Can you solve it in O(n) time complexity?', 1, 256, 100, 2000, 'Two Sum', 0
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE id = 7);

-- Bản ghi ID 8
INSERT INTO problems (id, created_at, created_by, updated_at, updated_by, constrains, description, difficulty, follow_up, is_contest_problem, memory_limit, point, time_limit, title, deleted)
SELECT 8, '2025-09-08 09:10:35.938713', 'thaidangfa@gmail.com', NULL, NULL, '0 <= |s| <= 10^5', 'Given a string, find the length of the longest substring without repeating characters.', 'MEDIUM', 'Try using sliding window technique.', 1, 256, 200, 2000, 'Longest Substring Without Repeating Characters', 0
    WHERE NOT EXISTS (SELECT 1 FROM problems WHERE id = 8);

INSERT INTO testcases (id, created_at, created_by, updated_at, updated_by, expected_output, input, is_hidden, pid)
SELECT 16, '2025-09-08 09:10:35.914834', 'thaidangfa@gmail.com', NULL, NULL, '0 1', '4\n2 7 11 15\n9', 0, 7
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE id = 16);

INSERT INTO testcases (id, created_at, created_by, updated_at, updated_by, expected_output, input, is_hidden, pid)
SELECT 17, '2025-09-08 09:10:35.917442', 'thaidangfa@gmail.com', NULL, NULL, '1 2', '3\n3 2 4\n6', 1, 7
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE id = 17);

-- Testcase cho bài 8 (Longest Substring)
INSERT INTO testcases (id, created_at, created_by, updated_at, updated_by, expected_output, input, is_hidden, pid)
SELECT 18, '2025-09-08 09:10:35.940813', 'thaidangfa@gmail.com', NULL, NULL, '3', 'abcabcbb', 0, 8
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE id = 18);

INSERT INTO testcases (id, created_at, created_by, updated_at, updated_by, expected_output, input, is_hidden, pid)
SELECT 19, '2025-09-08 09:10:35.942383', 'thaidangfa@gmail.com', NULL, NULL, '1', 'bbbbb', 1, 8
    WHERE NOT EXISTS (SELECT 1 FROM testcases WHERE id = 19);

INSERT INTO hints (id, created_at, created_by, updated_at, updated_by, description, pid)
SELECT 8, '2025-09-08 09:10:35.920083', 'thaidangfa@gmail.com', NULL, NULL, 'Try using a hash map to store complements.', 7
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE id = 8);

-- Hint cho bài 8
INSERT INTO hints (id, created_at, created_by, updated_at, updated_by, description, pid)
SELECT 9, '2025-09-08 09:10:35.943971', 'thaidangfa@gmail.com', NULL, NULL, 'Use a sliding window and a set to track unique characters.', 8
    WHERE NOT EXISTS (SELECT 1 FROM hints WHERE id = 9);

INSERT INTO problem_categories (problem_id, category)
SELECT 7, 'ARRAY'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 7 AND category = 'ARRAY');

INSERT INTO problem_categories (problem_id, category)
SELECT 7, 'HASH_TABLE'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 7 AND category = 'HASH_TABLE');

-- Categories cho Problem 8 (Longest Substring Without Repeating Characters)
INSERT INTO problem_categories (problem_id, category)
SELECT 8, 'STRING'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 8 AND category = 'STRING');

INSERT INTO problem_categories (problem_id, category)
SELECT 8, 'SLIDING_WINDOW'
    WHERE NOT EXISTS (SELECT 1 FROM problem_categories WHERE problem_id = 8 AND category = 'SLIDING_WINDOW');


CREATE TABLE IF NOT EXISTS contest_participant (
    id INT PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    contest_id INT,
    user_id VARCHAR(50)
    );

INSERT INTO contest_participant (id, created_at, created_by, updated_at, updated_by, contest_id, user_id)
SELECT 1, '2025-09-08 09:16:20.358338', 'thaidangfa@gmail.com', NULL, NULL, 1, 'e692cd89-e09e-4651-afb8-8956d349ff6t'
    WHERE NOT EXISTS (SELECT 1 FROM contest_participant WHERE id = 1);

INSERT INTO contest_participant (id, created_at, created_by, updated_at, updated_by, contest_id, user_id)
SELECT 2, '2025-09-23 10:01:20.702968', 'user@gmail.com', NULL, NULL, 1, 'e692cd89-e09e-4651-afb8-8956d349ff6c'
    WHERE NOT EXISTS (SELECT 1 FROM contest_participant WHERE contest_id = 1 AND user_id = 'e692cd89-e09e-4651-afb8-8956d349ff6c');


CREATE TABLE IF NOT EXISTS contest_problems (
    id INT PRIMARY KEY,
    contest_id INT,
    problem_id INT
);

INSERT INTO contest_problems (id, contest_id, problem_id)
SELECT 1, 1, 7 WHERE NOT EXISTS (SELECT 1 FROM contest_problems WHERE id = 1);

INSERT INTO contest_problems (id, contest_id, problem_id)
SELECT 2, 1, 8 WHERE NOT EXISTS (SELECT 1 FROM contest_problems WHERE id = 2);


CREATE TABLE IF NOT EXISTS submissions (
    id VARCHAR(50) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    code TEXT,
    language VARCHAR(20),
    max_time INT,
    message VARCHAR(255),
    passed_test_cases INT,
    pid INT,
    status VARCHAR(50),
    total_test_cases INT
    );

-- Submission 1 (hIK3p7H3j1)
INSERT INTO submissions (id, created_at, created_by, updated_at, updated_by, code, language, max_time, message, passed_test_cases, pid, status, total_test_cases)
SELECT 'hIK3p7H3j1', '2025-09-10 18:04:23.726525', 'thaidangfa@gmail.com', '2025-09-10 18:04:23.847682', 'anonymousUser', 'def two_sum(nums, target):\n    mp = {}\n    for i, num in enumerate(nums):\n        if num in mp:\n            return mp[num], i\n        mp[target - num] = i\n    return None\n\nn = int(input())\nnums = [int(i) for i in input().split()]\ntarget = int(input())\n\nres = two_sum(nums, target)\nif res == None:\n    print("None")\nelse:\n    for i in res:\n        print(i, end=" ")', 'python', 11, 'All testcases passed', 2, 7, 'ACCEPTED', 2
    WHERE NOT EXISTS (SELECT 1 FROM submissions WHERE id = 'hIK3p7H3j1');

-- Submission 2 (newID12345)
INSERT INTO submissions (id, created_at, created_by, updated_at, updated_by, code, language, max_time, message, passed_test_cases, pid, status, total_test_cases)
SELECT 'newID12345', '2025-09-10 18:04:23.726525', 'user@gmail.com', '2025-09-10 18:04:23.847682', 'anonymousUser', 'def two_sum(nums, target):\n    mp = {}\n    for i, num in enumerate(nums):\n        if num in mp:\n            return mp[num], i\n        mp[target - num] = i\n    return None\n\nn = int(input())\nnums = [int(i) for i in input().split()]\ntarget = int(input())\n\nres = two_sum(nums, target)\nif res == None:\n    print("None")\nelse:\n    for i in res:\n        print(i, end=" ")', 'python', 11, 'All testcases passed', 2, 7, 'ACCEPTED', 2
    WHERE NOT EXISTS (SELECT 1 FROM submissions WHERE id = 'newID12345');

-- Submission 3 (A5J4fZJSM3)
INSERT INTO submissions (id, created_at, created_by, updated_at, updated_by, code, language, max_time, message, passed_test_cases, pid, status, total_test_cases)
SELECT 'A5J4fZJSM3', '2025-09-09 17:39:11.620748', 'thaidangfa@gmail.com', '2025-09-09 17:39:11.681059', 'anonymousUser', 'def lengthOfLongestSubstring(s: str) -> int:\n    n = len(s)\n    left = 0\n    seen = set()\n    max_len = 0\n\n    for right in range(n):\n        while s[right] in seen:\n            seen.remove(s[left])\n            left += 1\n        seen.add(s[right])\n        max_len = max(max_len, right - left + 1)\n\n    return max_len\nprint(lengthOfLongestSubstring(input()))', 'python', 10, 'All testcases passed', 2, 8, 'ACCEPTED', 2
    WHERE NOT EXISTS (SELECT 1 FROM submissions WHERE id = 'A5J4fZJSM3');

-- Submission 4 (H92669fWn1)
INSERT INTO submissions (id, created_at, created_by, updated_at, updated_by, code, language, max_time, message, passed_test_cases, pid, status, total_test_cases)
SELECT 'H92669fWn1', '2025-09-09 18:41:01.054629', 'thaidangfa@gmail.com', '2025-09-09 18:41:01.519128', 'anonymousUser', 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Your code here\n    }\n}', 'java', 42, 'Expected: 1, Got:', 0, 8, 'WRONG_ANSWER', 2
    WHERE NOT EXISTS (SELECT 1 FROM submissions WHERE id = 'H92669fWn1');

CREATE TABLE IF NOT EXISTS solutions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(255),
    code TEXT,
    language VARCHAR(50),
    note TEXT,
    pid INT,
    space VARCHAR(100),
    time VARCHAR(100),
    deleted BIT(1) DEFAULT 0
    );
INSERT INTO solutions (pid, language, code, note, space, time, created_at, created_by, updated_at, updated_by, deleted)
SELECT
    5,
    'PYTHON',
    'import sys\n\ndef factorial(n):\n    if n == 0:\n        return 1\n    res = 1\n    for i in range(1, n + 1):\n        res *= i\n    return res\n\n# Đọc dữ liệu từ stdin và in ra stdout để testcase ghi nhận\nfor line in sys.stdin:\n    if line.strip():\n        n = int(line.strip())\n        print(factorial(n))',
    'Sử dụng vòng lặp for để tránh tràn ngăn xếp (stack overflow)',
    'O(1)',
    'O(n)',
    NOW(),
    'admin@gmail.com',
    NOW(),
    'admin@gmail.com',
    0
    WHERE NOT EXISTS (SELECT 1 FROM solutions WHERE pid = 5 AND language = 'PYTHON');


INSERT INTO solutions (pid, language, code, note, space, time, created_at, created_by, updated_at, updated_by, deleted)
SELECT
    5,
    'CPP',
    '#include <iostream>\n\nusing namespace std;\n\nint main() {\n    int n;\n    // Đọc n từ stdin\n    while (cin >> n) {\n        long long res = 1;\n        if (n > 0) {\n            for (int i = 1; i <= n; ++i) {\n                res *= i;\n            }\n        }\n        // In kết quả ra stdout kèm xuống dòng để khớp testcase\n        cout << res << endl;\n    }\n    return 0;\n}',
    'Sử dụng long long để tránh tràn số khi n lên đến 20 và vòng lặp while để xử lý stdin.',
    'O(1)',
    'O(n)',
    NOW(),
    'admin@gmail.com',
    NOW(),
    'admin@gmail.com',
    0
    WHERE NOT EXISTS (SELECT 1 FROM solutions WHERE pid = 5 AND language = 'CPP');