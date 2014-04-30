Create or replace FUNCTION __temp_change_group_ids_03_05_0210()
RETURNS VOID
   AS $procedure$
BEGIN


--If there is no external_id set at users, populate it with content based on the id
update ad_groups set external_id = decode(replace(CAST(id as text),'-',''),'HEX') where encode(external_id,'HEX') = '';


--groups.external_id holds a hex representation of the id of groups at ldap directories.
-- This script sets the guid representation at ad_groups.id, and modifies all relevant references,
--using the following steps:

--1. Adding temp column for mapping from old uuid to new uuid
PERFORM fn_db_add_column('ad_groups', 'temp_id', 'uuid');
--Filling the new column with guid repreentation of external_id
UPDATE ad_groups SET temp_id = CAST(substring(encode(external_id,'hex') FROM 1 FOR 8) || '-' ||
       substring(encode(external_id,'hex') FROM 9 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 13 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 17 FOR 4) || '-' ||
       substring(encode(external_id,'hex') FROM 21 FOR 12) AS uuid);

--2. Changing relevant group_id appearances in other tables
ALTER TABLE tags_user_group_map drop constraint tags_user_map_user_group;

UPDATE tags_user_group_map m set group_id = (
       SELECT temp_id FROM ad_groups WHERE id = m.group_id
);

UPDATE permissions p SET ad_element_id = (
       SELECT temp_id FROM ad_groups g1 WHERE g1.id = p.ad_element_id
) WHERE EXISTS (
       SELECT id from ad_groups where id =  p.ad_element_id
);

--3. Fixing group_ids at users
CREATE temp TABLE tmp_users_groups  ON COMMIT DROP AS
       SELECT fnsplitteruuid(group_ids) AS group_id, user_id FROM users;
UPDATE tmp_users_groups t SET group_id = (
       SELECT temp_id FROM ad_groups WHERE id = t.group_id
);
CREATE temp TABLE tmp_users_group_ids  ON COMMIT DROP AS
       SELECT user_id, array_to_string(array_agg(group_id), ',') group_ids FROM tmp_users_groups GROUP BY user_id;
UPDATE users u SET group_ids = (
        SELECT group_ids FROM tmp_users_group_ids WHERE user_id = u.user_id
);
UPDATE ad_groups SET id = temp_id;
--4. Cleanup
--DROP TABLE tmp_users_group_ids;
--DROP TABLE tmp_users_groups;
ALTER TABLE tags_user_group_map add constraint "tags_user_map_user_group" FOREIGN KEY (group_id) REFERENCES ad_groups(id);

PERFORM fn_db_drop_column('ad_groups','temp_id');
RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT  __temp_change_group_ids_03_05_0210();
DROP FUNCTION __temp_change_group_ids_03_05_0210();

