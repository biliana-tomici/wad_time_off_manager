-- Users
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (1, 'Alice', 'Smith', NULL, 'ADMIN');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (2, 'Bob', 'Johnson', 1, 'MANAGER');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (3, 'Charlie', 'Williams', 1, 'MANAGER');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (4, 'Diana', 'Brown', 2, 'MANAGER');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (5, 'Eve', 'Davis', 3, 'EMPLOYEE');
INSERT INTO users (id, first_name, last_name, manager_id, role) VALUES (6, 'Frank', 'Miller', 2, 'EMPLOYEE');
ALTER TABLE users ALTER COLUMN id RESTART WITH 7;

-- Projects
INSERT INTO project (id, min_emp, name, manager_id) VALUES (1, 2, 'Project Alpha', 1);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (2, 3, 'Project Beta', 2);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (3, 1, 'Project Gamma', 3);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (4, 2, 'Project Delta', 4);
ALTER TABLE project ALTER COLUMN id RESTART WITH 5;

-- Balances
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (1, 1, 10, 'Vacation', 20, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (2, 2, 5, 'Sick Leave', 10, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (3, 3, 8, 'Vacation', 15, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (4, 4, 12, 'Sick Leave', 12, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (5, 5, 7, 'Vacation', 18, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (6, 6, 9, 'Sick Leave', 14, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (7, 2, 15, 'Vacation', 20, 0);
INSERT INTO balance (id, employee_id, days_left, name, max_days, version) VALUES (8, 4, 6, 'Vacation', 15, 0);
ALTER TABLE balance ALTER COLUMN id RESTART WITH 9;

-- Requests with dates relative to CURRENT_DATE (C10)
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (1, 2, 2, 'PENDING', DATEADD('DAY', 7, CURRENT_DATE), DATEADD('DAY', 11, CURRENT_DATE), 'Family emergency', DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -10, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (2, 3, 3, 'APPROVED', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', 2, CURRENT_DATE), 'Medical appointment', DATEADD('DAY', -15, CURRENT_DATE), DATEADD('DAY', -14, CURRENT_DATE), 1, DATEADD('DAY', -14, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (3, 4, 4, 'REJECTED', DATEADD('DAY', -20, CURRENT_DATE), DATEADD('DAY', -18, CURRENT_DATE), 'Personal reasons', DATEADD('DAY', -25, CURRENT_DATE), DATEADD('DAY', -24, CURRENT_DATE), 2, DATEADD('DAY', -24, CURRENT_DATE), 'Coverage needed');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (4, 5, 5, 'PENDING', DATEADD('DAY', 14, CURRENT_DATE), DATEADD('DAY', 21, CURRENT_DATE), 'Vacation', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -5, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (5, 6, 6, 'APPROVED', DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -6, CURRENT_DATE), 'Sick leave', DATEADD('DAY', -20, CURRENT_DATE), DATEADD('DAY', -19, CURRENT_DATE), 2, DATEADD('DAY', -19, CURRENT_DATE), 'Approved');

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (6, 2, 7, 'PENDING', DATEADD('DAY', 30, CURRENT_DATE), DATEADD('DAY', 35, CURRENT_DATE), 'Summer holiday', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', -2, CURRENT_DATE), NULL, NULL, NULL);

INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date, decided_by, decided_at, decision_comment)
VALUES (7, 4, 8, 'APPROVED', DATEADD('DAY', 45, CURRENT_DATE), DATEADD('DAY', 55, CURRENT_DATE), 'Extended leave', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 2, DATEADD('DAY', -1, CURRENT_DATE), 'Approved');

ALTER TABLE request ALTER COLUMN id RESTART WITH 8;

-- Project Assignments (project_list)
INSERT INTO project_list (project_id, employee_id) VALUES (1, 1);
INSERT INTO project_list (project_id, employee_id) VALUES (1, 2);
INSERT INTO project_list (project_id, employee_id) VALUES (2, 2);
INSERT INTO project_list (project_id, employee_id) VALUES (2, 3);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 3);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 4);
INSERT INTO project_list (project_id, employee_id) VALUES (2, 4);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 4);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 5);
INSERT INTO project_list (project_id, employee_id) VALUES (4, 6);
INSERT INTO project_list (project_id, employee_id) VALUES (1, 5);
INSERT INTO project_list (project_id, employee_id) VALUES (3, 6);

