Create or replace FUNCTION __temp_update_user_ids_03_05_0220()
RETURNS VOID
   AS $procedure$
BEGIN

--If there is no external_id set at users, populate it with content based on the user_id
update users set external_id = decode(replace(CAST(user_id as text),'-',''),'HEX') where encode(external_id,'HEX') = '';

--users.external_id holds a hex representation of the id of users at ldap directories.
-- This script sets the guid representation at users.id, and modifies all relevant references,
--using the following steps:

--1. Adding temp column for mapping from old uuid to new uuid
PERFORM fn_db_add_column('users', 'temp_id', 'uuid');
--Filling the new column with guid repreentation of external_id
UPDATE users SET temp_id = CAST(substring(encode(external_id,'hex') FROM 1 FOR 8) || '-' ||
       substring(encode(external_id,'hex') FROM 9 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 13 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 17 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 21 FOR 12) AS uuid);

--2. Changing relevant group_id appearances in other tables
ALTER TABLE tags_user_map drop constraint "tags_user_map_user";
UPDATE tags_user_map m set user_id = (
       SELECT temp_id FROM users WHERE user_id = m.user_id);

UPDATE permissions p SET ad_element_id = (
       SELECT temp_id FROM users u1 WHERE u1.user_id = p.ad_element_id
) WHERE EXISTS (
       SELECT user_id from users where user_id =  p.ad_element_id
);

UPDATE users SET user_id = temp_id;
--3. Cleanup
ALTER TABLE tags_user_map add constraint "tags_user_map_user" FOREIGN KEY (user_id) REFERENCES users(user_id);
PERFORM fn_db_drop_column('users','temp_id');
RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT  __temp_update_user_ids_03_05_0220();
DROP FUNCTION __temp_update_user_ids_03_05_0220();

