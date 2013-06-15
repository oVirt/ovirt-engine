CREATE OR REPLACE FUNCTION __temp_Upgrade_AddDisksTable()
RETURNS void
AS $function$
BEGIN
   IF EXISTS (SELECT * FROM pg_tables WHERE tablename ILIKE 'disks') THEN
       RETURN;
   END IF;

   -- Add the disks table.
   CREATE TABLE disks
   (
      disk_id UUID NOT NULL,
      status VARCHAR(32) NOT NULL,
      internal_drive_mapping INTEGER NOT NULL,
      active_image_id UUID CONSTRAINT fk_disk_active_image REFERENCES images(image_guid) ON DELETE SET NULL,
      disk_type VARCHAR(32) NOT NULL,
      disk_interface VARCHAR(32) NOT NULL,
      wipe_after_delete BOOLEAN NOT NULL DEFAULT false,
      propagate_errors VARCHAR(32) NOT NULL DEFAULT 'Off',
      CONSTRAINT pk_disks PRIMARY KEY(disk_id)
   )
   WITH OIDS;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_AddDisksTable();

DROP FUNCTION __temp_Upgrade_AddDisksTable();

