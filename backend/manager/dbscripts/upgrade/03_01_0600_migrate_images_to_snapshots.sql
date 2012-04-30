CREATE OR REPLACE FUNCTION __temp_Upgrade_MigrateSnapshotsFromImages()
RETURNS void
AS $function$
DECLARE
    cur RECORD;
    snap RECORD;
BEGIN
    IF EXISTS (SELECT 1 FROM snapshots) THEN
        RETURN;
    END IF;

    -- Insert snapshots for all images.
    FOR cur IN (
        SELECT DISTINCT vm_snapshot_id
        FROM   images s
        WHERE  EXISTS (
            SELECT 1
            FROM   images d
            JOIN   image_vm_map ivm ON d.image_guid = ivm.image_id
            JOIN   vm_static v ON v.vm_guid = ivm.vm_id
            WHERE  d.image_group_id = s.image_group_id
            AND    v.entity_type = 'VM'))
    LOOP
        SELECT i.*, ivm.vm_id
        INTO   snap
        FROM   images i
        JOIN   images d ON i.image_group_id = d.image_group_id
        JOIN   image_vm_map ivm ON d.image_guid = ivm.image_id
        WHERE  i.vm_snapshot_id = cur.vm_snapshot_id
        LIMIT  1;

        INSERT
        INTO   snapshots (
            snapshot_id,
            vm_id,
            snapshot_type,
            status,
            description,
            creation_date,
            app_list)
        VALUES (
            snap.vm_snapshot_id,
            snap.vm_id,
            CASE WHEN EXISTS (SELECT 1 FROM image_vm_map ivm WHERE snap.image_guid = ivm.image_id AND ivm.active = TRUE) THEN 'ACTIVE'
                 WHEN EXISTS (SELECT 1 FROM image_vm_map ivm WHERE snap.image_guid = ivm.image_id AND ivm.active = FALSE) THEN 'PREVIEW'
                 ELSE
                     CASE WHEN EXISTS (SELECT 1 FROM stateless_vm_image_map svim WHERE snap.image_guid = svim.image_guid) THEN 'STATELESS'
                          ELSE 'REGULAR'
                     END
            END,
            'OK',
            snap.description,
            snap.lastmodified,
            snap.app_list
        );
    END LOOP;

    -- Update all previewed snapshots to status 'IN_PREVIEW'.
    -- This is done to all regular snapshots, that have a 'PREVIEW' type snapshot for the same VM.
    UPDATE snapshots s
    SET    status = 'IN_PREVIEW'
    WHERE  snapshot_type = 'REGULAR'
    AND    EXISTS (
        SELECT 1
        FROM   snapshots preview_snap
        WHERE  preview_snap.vm_id = s.vm_id
        AND    preview_snap.snapshot_type = 'PREVIEW')
    AND    snapshot_id IN (
        SELECT vm_snapshot_id
        FROM   images father
        WHERE  EXISTS (
            SELECT 1
            FROM   images son
            JOIN   image_vm_map ivm ON son.image_guid = ivm.image_id
            WHERE  ivm.active = TRUE
            AND    son.parentId = father.image_guid));

    -- Narrow down double active snapshots for the same VM (if exist).
    -- This is because before 3.0 each disk had a new snapshot_id, so we can get more than one active snapshot.
    FOR cur IN (
        SELECT DISTINCT vm_id
        FROM   snapshots
        WHERE  snapshot_type = 'ACTIVE')
    LOOP
        IF ((SELECT COUNT(*)
             FROM   snapshots s
             WHERE  s.vm_id = cur.vm_id
             AND    snapshot_type = 'ACTIVE') > 1) THEN
            SELECT *
            INTO   snap
            FROM   snapshots s
            WHERE  s.vm_id = cur.vm_id
            AND    snapshot_type = 'ACTIVE'
            LIMIT  1;

            UPDATE images
            SET    vm_snapshot_id = snap.snapshot_id
            WHERE  images.vm_snapshot_id IN (
                SELECT snapshot_id
                FROM   snapshots
                WHERE  vm_id = snap.vm_id
                AND    snapshot_type = 'ACTIVE');
        END IF;
    END LOOP;

    -- Remove all active snapshots that don't have images.
    -- This is because after narrowing down, we can have snapshots left without images.
    DELETE
    FROM   snapshots s
    WHERE  snapshot_type = 'ACTIVE'
    AND    NOT EXISTS(
        SELECT 1
        FROM   images i
        WHERE  i.vm_snapshot_id = s.snapshot_id);

    -- Add active snapshot for all VMs that don't have one yet (since they have no disks).
    FOR cur IN (
        SELECT vm_guid
        FROM   vm_static v
        WHERE  entity_type = 'VM'
        AND    NOT EXISTS(
            SELECT 1
            FROM   snapshots s
            WHERE  s.vm_id = v.vm_guid))
    LOOP
        INSERT
        INTO   snapshots (
            snapshot_id,
            vm_id,
            snapshot_type,
            status,
            creation_date)
        VALUES (
            uuid_generate_v1(),
            cur.vm_guid,
            'ACTIVE',
            'OK',
            NOW()
        );
    END LOOP;

    -- Update active snapshots fields.
    UPDATE snapshots
    SET    description = 'Active VM snapshot',
           app_list = NULL
    WHERE  snapshot_type = 'ACTIVE';

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_MigrateSnapshotsFromImages();

DROP FUNCTION __temp_Upgrade_MigrateSnapshotsFromImages();

