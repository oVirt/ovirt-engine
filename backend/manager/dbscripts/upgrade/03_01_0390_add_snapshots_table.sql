CREATE OR REPLACE FUNCTION __temp_Upgrade_AddSnapshotsTable()
RETURNS void
AS $function$
BEGIN
   IF EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'snapshots') THEN
       RETURN;
   END IF;

   -- Add the snapshots table.
   CREATE TABLE snapshots
   (
      snapshot_id UUID NOT NULL CONSTRAINT pk_snapshots PRIMARY KEY,
      vm_id UUID NOT NULL CONSTRAINT fk_snapshot_vm REFERENCES vm_static(vm_guid),
      snapshot_type VARCHAR(32) NOT NULL,
      status VARCHAR(32) NOT NULL,
      description VARCHAR(4000),
      creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
      app_list TEXT,
      vm_configuration TEXT,
      _create_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
      _update_date TIMESTAMP WITH TIME ZONE
   )
   WITH OIDS;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_AddSnapshotsTable();

DROP FUNCTION __temp_Upgrade_AddSnapshotsTable();

