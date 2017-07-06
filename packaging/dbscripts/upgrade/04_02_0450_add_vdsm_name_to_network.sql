SELECT fn_db_add_column('network', 'vdsm_name', 'character varying(15)');

UPDATE network
SET vdsm_name = name;

ALTER TABLE network ALTER COLUMN name TYPE varchar(256);
ALTER TABLE network ALTER COLUMN vdsm_name SET NOT NULL;
