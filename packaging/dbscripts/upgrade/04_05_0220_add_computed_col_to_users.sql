ALTER TABLE users 
ADD COLUMN user_and_domain VARCHAR(512)
GENERATED ALWAYS AS 
(CASE 
    WHEN domain IS NOT NULL THEN name || '@'  || domain 
    ELSE name END)
STORED;
