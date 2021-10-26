

----------------------------------------------------------------------
--  [vm_backups] Table
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVmBackupByVmBackupId (v_backup_id UUID)
RETURNS SETOF vm_backups STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_backups
    WHERE backup_id = v_backup_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmBackup (
    v_backup_id UUID,
    v_from_checkpoint_id UUID,
    v_to_checkpoint_id UUID,
    v_vm_id UUID,
    v_host_id UUID,
    v_phase TEXT,
    v__create_date TIMESTAMP WITH TIME ZONE,
    v__update_date TIMESTAMP WITH TIME ZONE,
    v_description VARCHAR(1024),
    v_is_live_backup BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_backups (
        backup_id,
        from_checkpoint_id,
        to_checkpoint_id,
        vm_id,
        host_id,
        phase,
        _create_date,
        _update_date,
        description,
        is_live_backup
        )
    VALUES (
        v_backup_id,
        v_from_checkpoint_id,
        v_to_checkpoint_id,
        v_vm_id,
        v_host_id,
        v_phase,
        v__create_date,
        v__update_date,
        v_description,
        v_is_live_backup
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmBackup (
    v_backup_id UUID,
    v_from_checkpoint_id UUID,
    v_to_checkpoint_id UUID,
    v_vm_id UUID,
    v_host_id UUID,
    v_phase TEXT,
    v__update_date TIMESTAMP WITH TIME ZONE,
    v_description VARCHAR(1024),
    v_is_live_backup BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_backups
    SET backup_id = v_backup_id,
        from_checkpoint_id = v_from_checkpoint_id,
        to_checkpoint_id = v_to_checkpoint_id,
        vm_id = v_vm_id,
        host_id = v_host_id,
        phase = v_phase,
        _update_date = v__update_date,
        description = v_description,
        is_live_backup = v_is_live_backup
    WHERE backup_id = v_backup_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmBackup (v_backup_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_backups
    WHERE backup_id = v_backup_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmBackups ()
RETURNS SETOF vm_backups STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_backups;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmBackupsByVmId (v_vm_id UUID)
RETURNS SETOF vm_backups STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_backups
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [vm_backup_disk_map] Table
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVmBackupDiskMap (
    v_backup_id UUID,
    v_disk_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vm_backup_disk_map (
            backup_id,
            disk_id
            )
        VALUES (
            v_backup_id,
            v_disk_id
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmBackupDiskMap (
    v_backup_id UUID,
    v_disk_id UUID,
    v_backup_url TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_backup_disk_map
    SET backup_id = v_backup_id,
        disk_id = v_disk_id,
        backup_url = v_backup_url
    WHERE backup_id = v_backup_id AND disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDisksByVmBackupId (v_backup_id UUID)
RETURNS SETOF images_storage_domain_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT images_storage_domain_view.*
    FROM   images_storage_domain_view
    JOIN   vm_backup_disk_map on vm_backup_disk_map.disk_id = images_storage_domain_view.image_group_id
    WHERE  images_storage_domain_view.active AND vm_backup_disk_map.backup_id = v_backup_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBackupUrlForDiskId (v_backup_id UUID, v_disk_id UUID)
RETURNS TEXT STABLE AS $FUNCTION$
BEGIN
    RETURN
    (
        SELECT backup_url
        FROM vm_backup_disk_map
        WHERE backup_id = v_backup_id AND disk_id = v_disk_id
    );
END;$FUNCTION$
LANGUAGE plpgsql;

-----------------------------------------------------------
-- Cleanup backup entities by create time and phase
-----------------------------------------------------------
CREATE OR REPLACE FUNCTION DeleteCompletedBackupsOlderThanDate (
    v_succeeded_end_time TIMESTAMP WITH TIME ZONE,
    v_failed_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_backups
    WHERE (
            (
                _update_date < v_succeeded_end_time
                AND phase = 'Succeeded'
                )
            OR (
                _update_date < v_failed_end_time
                AND phase = 'Failed'
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;
