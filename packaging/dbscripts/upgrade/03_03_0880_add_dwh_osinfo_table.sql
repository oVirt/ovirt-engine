 -- ----------------------------------------------------------------------
 --  table dwh_osinfo is a translation table used by DWH
 -- ----------------------------------------------------------------------

Insert into dwh_history_timekeeping  VALUES('lastOsinfoUpdate',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY'));

CREATE TABLE dwh_osinfo (
  os_id INTEGER NOT NULL,
  os_name VARCHAR(255),
  CONSTRAINT pk_os_id PRIMARY KEY(os_id)
) WITH OIDS;


