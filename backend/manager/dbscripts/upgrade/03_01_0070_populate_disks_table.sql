CREATE OR REPLACE FUNCTION __temp_Upgrade_PopulateDisksTable()
RETURNS void
AS $function$
DECLARE
   cur RECORD;
BEGIN
   IF (SELECT COUNT(*) FROM disks) > 0 THEN
       RETURN;
   END IF;

   -- Populate Disks table from images table (The active image is what we want)
   -- This is for VM's disks only.

   FOR cur IN (SELECT *
               FROM   images i
               JOIN   image_vm_map ivm ON i.image_guid = ivm.image_id
               WHERE  active = TRUE)
   LOOP
      IF NOT EXISTS (SELECT disk_id from disks WHERE disk_id = cur.image_group_id) THEN
          INSERT
          INTO   disks
             (disk_id,
              status,
              internal_drive_mapping,
              active_image_id,
              disk_type,
              disk_interface,
              wipe_after_delete,
              propagate_errors)
          VALUES
             (cur.image_group_id,
              CASE WHEN cur.imagestatus = 1 THEN 'OK'
                   WHEN cur.imagestatus = 2 THEN 'LOCKED'
                   WHEN cur.imagestatus = 3 THEN 'INVALID'
                   WHEN cur.imagestatus = 4 THEN 'ILLEGAL'
                   ELSE 'Unassigned'
              END,
              CAST(cur.internal_drive_mapping AS INTEGER),
              cur.image_guid,
              CASE WHEN cur.disk_type = 1 THEN 'System'
                   WHEN cur.disk_type = 2 THEN 'Data'
                   WHEN cur.disk_type = 3 THEN 'Shared'
                   WHEN cur.disk_type = 4 THEN 'Swap'
                   WHEN cur.disk_type = 5 THEN 'Temp'
                   ELSE 'Unassigned'
              END,
              CASE WHEN cur.disk_interface = 1 THEN 'SCSI'
                   WHEN cur.disk_interface = 2 THEN 'VirtIO'
                   ELSE 'IDE'
              END,
              cur.wipe_after_delete,
              CASE WHEN cur.propagate_errors = 1 THEN 'On'
                   ELSE 'Off'
              END);
      END IF;
   END LOOP;

   -- Populate Disks table from image_templates table.
   -- This is for Template's disks only.

   FOR cur IN (SELECT *
               FROM   image_templates it
               JOIN   vm_template_image_map vtim USING (it_guid)
               JOIN   images i ON it.it_guid = i.image_guid)
   LOOP
      IF NOT EXISTS (SELECT disk_id from disks WHERE disk_id = cur.image_group_id) THEN
          INSERT
          INTO   disks
             (disk_id,
              status,
              internal_drive_mapping,
              active_image_id,
              disk_type,
              disk_interface,
              wipe_after_delete,
              propagate_errors)
          VALUES
             (cur.image_group_id,
              CASE WHEN cur.imagestatus = 1 THEN 'OK'
                   WHEN cur.imagestatus = 2 THEN 'LOCKED'
                   WHEN cur.imagestatus = 3 THEN 'INVALID'
                   WHEN cur.imagestatus = 4 THEN 'ILLEGAL'
                   ELSE 'Unassigned'
              END,
              CAST(cur.internal_drive_mapping AS INTEGER),
              cur.it_guid,
              CASE WHEN cur.disk_type = 1 THEN 'System'
                   WHEN cur.disk_type = 2 THEN 'Data'
                   WHEN cur.disk_type = 3 THEN 'Shared'
                   WHEN cur.disk_type = 4 THEN 'Swap'
                   WHEN cur.disk_type = 5 THEN 'Temp'
                   ELSE 'Unassigned'
              END,
              CASE WHEN cur.disk_interface = 1 THEN 'SCSI'
                   WHEN cur.disk_interface = 2 THEN 'VirtIO'
                   ELSE 'IDE'
              END,
              cur.wipe_after_delete,
              CASE WHEN cur.propagate_errors = 1 THEN 'On'
                   ELSE 'Off'
              END);
      END IF;
   END LOOP;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_PopulateDisksTable();

DROP FUNCTION __temp_Upgrade_PopulateDisksTable();

