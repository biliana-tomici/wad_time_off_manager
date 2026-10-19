-- Users
-- 1 Admin
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (1, 'Alice', 'Smith', NULL, 'ADMIN');
-- 3 Managers
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (2, 'Bob', 'Johnson', 1, 'MANAGER');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (3, 'Charlie', 'Williams', 1, 'MANAGER');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (4, 'Diana', 'Brown', 2, 'MANAGER');
-- 6 Employees
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (5, 'Eve', 'Davis', 3, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (6, 'Frank', 'Miller', 2, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (7, 'George', 'Clark', 2, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (8, 'Hannah', 'Abbott', 3, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (9, 'Ian', 'Wright', 4, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (10, 'Julia', 'Roberts', 4, 'EMPLOYEE');
ALTER TABLE users ALTER COLUMN id RESTART WITH 11;

-- Projects
INSERT INTO project (id, min_emp, name, manager_id) VALUES (1, 2, 'Project Alpha', 1);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (2, 3, 'Project Beta', 2);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (3, 1, 'Project Gamma', 3);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (4, 2, 'Project Delta', 4);
ALTER TABLE project ALTER COLUMN id RESTART WITH 5;

-- Balances: Simple employee 22 free days (Vacation), Manager 28 free days (Vacation) + Sick leave
-- Managers / Admin (28 vacation days)
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (1, 1, 28, 'Vacation', 28, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (2, 2, 5, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (3, 3, 25, 'Vacation', 28, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (4, 4, 12, 'Sick Leave', 12, 0);
-- Employees (22 vacation days)
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (5, 5, 17, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (6, 6, 9, 'Sick Leave', 14, 0);
-- Additional balances for full coverage
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (7, 2, 28, 'Vacation', 28, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (8, 4, 20, 'Vacation', 28, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (9, 1, 15, 'Sick Leave', 15, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (10, 3, 10, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (11, 5, 10, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (12, 6, 22, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (13, 7, 22, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (14, 7, 7, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (15, 8, 19, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (16, 8, 10, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (17, 9, 22, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (18, 9, 10, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (19, 10, 17, 'Vacation', 22, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (20, 10, 10, 'Sick Leave', 10, 0);
ALTER TABLE balance ALTER COLUMN id RESTART WITH 21;

-- Requests: rich mix of APPROVED, PENDING, EDITED, REJECTED, and CANCELLED
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (1, 2, 2, 'PENDING', DATEADD('DAY', 7, CURRENT_DATE), DATEADD('DAY', 11, CURRENT_DATE), 'Family emergency', DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -10, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (2, 3, 3, 'APPROVED', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', 2, CURRENT_DATE), 'Medical appointment', DATEADD('DAY', -15, CURRENT_DATE), DATEADD('DAY', -14, CURRENT_DATE), 1, DATEADD('DAY', -14, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (3, 4, 4, 'REJECTED', DATEADD('DAY', -20, CURRENT_DATE), DATEADD('DAY', -18, CURRENT_DATE), 'Personal reasons', DATEADD('DAY', -25, CURRENT_DATE), DATEADD('DAY', -24, CURRENT_DATE), 2, DATEADD('DAY', -24, CURRENT_DATE), 'Coverage needed');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (4, 5, 5, 'PENDING', DATEADD('DAY', 14, CURRENT_DATE), DATEADD('DAY', 21, CURRENT_DATE), 'Autumn vacation', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -5, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (5, 6, 6, 'APPROVED', DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -6, CURRENT_DATE), 'Sick leave', DATEADD('DAY', -20, CURRENT_DATE), DATEADD('DAY', -19, CURRENT_DATE), 2, DATEADD('DAY', -19, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (6, 2, 7, 'PENDING', DATEADD('DAY', 30, CURRENT_DATE), DATEADD('DAY', 35, CURRENT_DATE), 'Summer holiday', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', -2, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (7, 4, 8, 'APPROVED', DATEADD('DAY', 45, CURRENT_DATE), DATEADD('DAY', 55, CURRENT_DATE), 'Extended leave', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 2, DATEADD('DAY', -1, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (8, 7, 13, 'EDITED', DATEADD('DAY', 2, CURRENT_DATE), DATEADD('DAY', 5, CURRENT_DATE), 'Tech conference (shifted schedule)', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (9, 8, 15, 'APPROVED', DATEADD('DAY', 4, CURRENT_DATE), DATEADD('DAY', 8, CURRENT_DATE), 'Family visit', DATEADD('DAY', -8, CURRENT_DATE), DATEADD('DAY', -7, CURRENT_DATE), 3, DATEADD('DAY', -7, CURRENT_DATE), 'Approved. Enjoy!');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (10, 9, 17, 'REJECTED', DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 3, CURRENT_DATE), 'Long weekend getaway', DATEADD('DAY', -4, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), 4, DATEADD('DAY', -3, CURRENT_DATE), 'Critical sprint deadline; please reschedule');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (11, 10, 19, 'APPROVED', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 'Personal family event', DATEADD('DAY', -12, CURRENT_DATE), DATEADD('DAY', -11, CURRENT_DATE), 4, DATEADD('DAY', -11, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (12, 10, 19, 'PENDING', DATEADD('DAY', 18, CURRENT_DATE), DATEADD('DAY', 22, CURRENT_DATE), 'Mountain hiking trip', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', -2, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (13, 6, 12, 'CANCELLED', DATEADD('DAY', -14, CURRENT_DATE), DATEADD('DAY', -12, CURRENT_DATE), 'Music festival weekend', DATEADD('DAY', -22, CURRENT_DATE), DATEADD('DAY', -16, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (14, 5, 5, 'APPROVED', DATEADD('DAY', 5, CURRENT_DATE), DATEADD('DAY', 9, CURRENT_DATE), 'Professional development workshop', DATEADD('DAY', -6, CURRENT_DATE), DATEADD('DAY', -5, CURRENT_DATE), 3, DATEADD('DAY', -5, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (15, 7, 14, 'APPROVED', DATEADD('DAY', -3, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 'Seasonal flu recovery', DATEADD('DAY', -4, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), 2, DATEADD('DAY', -3, CURRENT_DATE), 'Get well soon!');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (16, 8, 15, 'PENDING', DATEADD('DAY', 25, CURRENT_DATE), DATEADD('DAY', 29, CURRENT_DATE), 'Apartment move', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (17, 9, 17, 'PENDING', DATEADD('DAY', 12, CURRENT_DATE), DATEADD('DAY', 16, CURRENT_DATE), 'Annual vacation segment', DATEADD('DAY', -3, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (18, 3, 10, 'REJECTED', DATEADD('DAY', -12, CURRENT_DATE), DATEADD('DAY', -11, CURRENT_DATE), 'Doctor consultation', DATEADD('DAY', -15, CURRENT_DATE), DATEADD('DAY', -14, CURRENT_DATE), 1, DATEADD('DAY', -14, CURRENT_DATE), 'Rescheduled by doctor');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (19, 1, 1, 'APPROVED', DATEADD('DAY', 35, CURRENT_DATE), DATEADD('DAY', 40, CURRENT_DATE), 'Executive strategy retreat', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -4, CURRENT_DATE), 1, DATEADD('DAY', -4, CURRENT_DATE), 'Self-approved');

ALTER TABLE request ALTER COLUMN id RESTART WITH 20;

-- Project Assignments (project_list)
INSERT INTO project_list (project_id, employee_id) VALUES (1, 1);
INSERT INTO project_list (project_id, employee_id) VALUES (1, 2);
INSERT INTO project_list (project_id, employee_id) VALUES (1, 5);
INSERT INTO project_list (project_id, employee_id) VALUES (1, 7);

INSERT INTO project_list (project_id, employee_id) VALUES (2, 2);
INSERT INTO project_list (project_id, employee_id) VALUES (2, 3);
INSERT INTO project_list (project_id, employee_id) VALUES (2, 4);

INSERT INTO project_list (project_id, employee_id) VALUES (3, 3);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 4);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 6);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 8);

INSERT INTO project_list (project_id, employee_id) VALUES (4, 4);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 5);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 6);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 9);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 10);
