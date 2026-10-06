-- Customers
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (1, 'Alice', 'Smith', NULL);
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (2, 'Bob', 'Johnson', 1);
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (3, 'Charlie', 'Williams', 1);
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (4, 'Diana', 'Brown', 2);
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (5, 'Eve', 'Davis', 3);
INSERT INTO users (id, first_name, last_name, manager_id) VALUES (6, 'Frank', 'Miller', 2);
ALTER TABLE users ALTER COLUMN id RESTART WITH 7;

-- Projects
INSERT INTO project (id, min_emp, name, manager_id) VALUES (1, 2, 'Project Alpha', 1);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (2, 3, 'Project Beta', 2);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (3, 1, 'Project Gamma', 3);
INSERT INTO project (id, min_emp, name, manager_id) VALUES (4, 2, 'Project Delta', 4);
ALTER TABLE project ALTER COLUMN id RESTART WITH 5;

-- Balances
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (1, 1, 10, 'Vacation', 20);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (2, 2, 5, 'Sick Leave', 10);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (3, 3, 8, 'Vacation', 15);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (4, 4, 12, 'Sick Leave', 12);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (5, 5, 7, 'Vacation', 18);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (6, 6, 9, 'Sick Leave', 14);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (7, 2, 15, 'Vacation', 20);
INSERT INTO balance (id, employee_id, days_left, name, max_days) VALUES (8, 4, 6, 'Vacation', 15);
ALTER TABLE balance ALTER COLUMN id RESTART WITH 9;

-- Requests
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (1, 2, 2, 'PENDING', '2024-07-01', '2024-07-05', 'Family emergency', '2024-06-01', '2024-06-01');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (2, 3, 3, 'APPROVED', '2024-07-10', '2024-07-12', 'Medical appointment', '2024-06-05', '2024-06-06');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (3, 4, 4, 'REJECTED', '2024-08-01', '2024-08-03', 'Personal reasons', '2024-06-10', '2024-06-11');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (4, 5, 5, 'PENDING', '2024-09-01', '2024-09-10', 'Vacation', '2024-07-01', '2024-07-01');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (5, 6, 6, 'APPROVED', '2024-07-15', '2024-07-20', 'Sick leave', '2024-06-15', '2024-06-16');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (6, 2, 7, 'PENDING', '2024-08-10', '2024-08-15', 'Summer holiday', '2024-07-10', '2024-07-10');
INSERT INTO request (id, employee_id, balance_id, status, start_date, end_date, explanation, creation_date, modified_date)
VALUES (7, 4, 8, 'APPROVED', '2025-08-01', '2025-08-20', 'Extended leave', '2025-07-20', '2025-07-20');
ALTER TABLE request ALTER COLUMN id RESTART WITH 10;

-- Project Assignments (project_list)
INSERT INTO project_list (id, project_id, employee_id) VALUES (1, 1, 1);
INSERT INTO project_list (id, project_id, employee_id) VALUES (2, 1, 2);
INSERT INTO project_list (id, project_id, employee_id) VALUES (3, 2, 2);
INSERT INTO project_list (id, project_id, employee_id) VALUES (4, 2, 3);
INSERT INTO project_list (id, project_id, employee_id) VALUES (5, 3, 3);
INSERT INTO project_list (id, project_id, employee_id) VALUES (6, 3, 4);
INSERT INTO project_list (id, project_id, employee_id) VALUES (7, 2, 4);
INSERT INTO project_list (id, project_id, employee_id) VALUES (8, 4, 4);
INSERT INTO project_list (id, project_id, employee_id) VALUES (9, 4, 5);
INSERT INTO project_list (id, project_id, employee_id) VALUES (10, 4, 6);
INSERT INTO project_list (id, project_id, employee_id) VALUES (11, 1, 5);
INSERT INTO project_list (id, project_id, employee_id) VALUES (12, 3, 6);
ALTER TABLE project_list ALTER COLUMN id RESTART WITH 13;