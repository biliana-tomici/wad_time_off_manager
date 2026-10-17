ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);

UPDATE users
SET email = LOWER(first_name) || '.' || LOWER(last_name) || '@timeoff.com'
WHERE email IS NULL;

UPDATE users
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE password_hash IS NULL;

ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
