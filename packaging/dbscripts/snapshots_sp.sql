

----------------------------------------------------------------
-- [snapshots] Table
--
CREATE OR REPLACE FUNCTION InsertSnapshot (
    v_snapshot_id UUID,
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32),
    v_status VARCHAR(32),
    v_description VARCHAR(4000),
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_app_list TEXT,
    v_vm_configuration TEXT,
    v_memory_dump_disk_id UUID,
    v_memory_metadata_disk_id UUID,
    v_changed_fields TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO snapshots (
        snapshot_id,
        status,
        vm_id,
        snapshot_type,
        description,
        creation_date,
        app_list,
        vm_configuration,
        memory_dump_disk_id,
        memory_metadata_disk_id,
        changed_fields
        )
    VALUES(
        v_snapshot_id,
        v_status,
        v_vm_id,
        v_snapshot_type,
        v_description,
        v_creation_date,
        v_app_list,
        v_vm_configuration,
        v_memory_dump_disk_id,
        v_memory_metadata_disk_id,
        v_changed_fields
        );
END; $PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSnapshot (
    v_snapshot_id UUID,
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32),
    v_status VARCHAR(32),
    v_description VARCHAR(4000),
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_app_list TEXT,
    v_vm_configuration TEXT,
    v_memory_dump_disk_id UUID,
    v_memory_metadata_disk_id UUID,
    v_vm_configuration_broken BOOLEAN,
    v_changed_fields TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET status = v_status,
        vm_id = v_vm_id,
        snapshot_type = v_snapshot_type,
        description = v_description,
        creation_date = v_creation_date,
        app_list = v_app_list,
        vm_configuration = v_vm_configuration,
        memory_dump_disk_id = v_memory_dump_disk_id,
        memory_metadata_disk_id = v_memory_metadata_disk_id,
        vm_configuration_broken = v_vm_configuration_broken,
        changed_fields = v_changed_fields,
        _update_date = NOW()
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSnapshotStatus (
    v_snapshot_id UUID,
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET status = v_status
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSnapshotId (
    v_snapshot_id UUID,
    v_new_snapshot_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET snapshot_id = v_new_snapshot_id
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteSnapshot (v_snapshot_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM snapshots
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromSnapshots ()
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllSnapshotsByStorageDomainId (v_storage_id UUID)
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT snapshots.*
    FROM snapshots
    INNER JOIN images
        ON snapshots.snapshot_id = images.vm_snapshot_id
    INNER JOIN image_storage_domain_map
        ON image_storage_domain_map.storage_domain_id = v_storage_id
            AND image_storage_domain_map.image_id = images.image_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotByVmIdAndType (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32),
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            )
    ORDER BY creation_date ASC LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotByVmIdAndTypeAndStatus (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32),
    v_status VARCHAR(32)
    )
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type
        AND status = v_status
    ORDER BY creation_date ASC LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotByVmIdAndStatus (
    v_vm_id UUID,
    v_status VARCHAR(32)
    )
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND status = v_status
    ORDER BY creation_date ASC LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetAllFromSnapshotsByVmId_rs CASCADE;
CREATE TYPE GetAllFromSnapshotsByVmId_rs AS (
        snapshot_id UUID,
        vm_id UUID,
        snapshot_type VARCHAR(32),
        status VARCHAR(32),
        description VARCHAR(4000),
        creation_date TIMESTAMP WITH TIME ZONE,
        app_list TEXT,
        memory_dump_disk_id UUID,
        memory_metadata_disk_id UUID,
        vm_configuration TEXT,
        vm_configuration_available BOOLEAN,
        vm_configuration_broken BOOLEAN,
        changed_fields TEXT
        );

CREATE OR REPLACE FUNCTION GetAllFromSnapshotsByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN,
    v_fill_configuration BOOLEAN
    )
RETURNS SETOF GetAllFromSnapshotsByVmId_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT snapshot_id,
        vm_id,
        snapshot_type,
        status,
        description,
        creation_date,
        app_list,
        memory_dump_disk_id,
        memory_metadata_disk_id,
        CASE
            WHEN v_fill_configuration = TRUE
                THEN vm_configuration
            ELSE NULL
            END,
        vm_configuration IS NOT NULL
        AND LENGTH(vm_configuration) > 0,
        vm_configuration_broken,
        changed_fields
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            )
    ORDER BY creation_date ASC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotBySnapshotId (
    v_snapshot_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots
    WHERE snapshot_id = v_snapshot_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = (
                        SELECT vm_id
                        FROM snapshots
                        WHERE snapshot_id = v_snapshot_id
                        )
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotIdsByVmIdAndType (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32)
    )
RETURNS SETOF idUuidType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT snapshot_id
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type
    ORDER BY creation_date ASC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSnapshotIdsByVmIdAndTypeAndStatus (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32),
    v_status VARCHAR(32)
    )
RETURNS SETOF idUuidType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT snapshot_id
    FROM snapshots
    WHERE vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type
        AND status = v_status
    ORDER BY creation_date ASC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CheckIfSnapshotExistsByVmIdAndType (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32)
    )
RETURNS SETOF booleanResultType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT *
            FROM snapshots
            WHERE vm_id = v_vm_id
                AND snapshot_type = v_snapshot_type
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CheckIfSnapshotExistsByVmIdAndStatus (
    v_vm_id UUID,
    v_status VARCHAR(32)
    )
RETURNS SETOF booleanResultType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT *
            FROM snapshots
            WHERE vm_id = v_vm_id
                AND status = v_status
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CheckIfSnapshotExistsByVmIdAndSnapshotId (
    v_vm_id UUID,
    v_snapshot_id UUID
    )
RETURNS SETOF booleanResultType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT *
            FROM snapshots
            WHERE vm_id = v_vm_id
                AND snapshot_id = v_snapshot_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNumOfSnapshotsByMemoryVolume(v_memory_disk_ids UUID[])
RETURNS SETOF BIGINT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT COUNT(*)
    FROM snapshots
    WHERE memory_dump_disk_id  = ANY(v_memory_disk_ids)
       OR memory_metadata_disk_id  = ANY(v_memory_disk_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateMemory (
    v_memory_dump_disk_id UUID,
    v_memory_metadata_disk_id UUID,
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET    memory_dump_disk_id = v_memory_dump_disk_id,
           memory_metadata_disk_id = v_memory_metadata_disk_id
    WHERE vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveMemoryFromSnapshotByVmIdAndType (
    v_vm_id UUID,
    v_snapshot_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET    memory_dump_disk_id = NULL,
           memory_metadata_disk_id = NULL
    WHERE  vm_id = v_vm_id
        AND snapshot_type = v_snapshot_type;
END; $PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveMemoryFromSnapshotBySnapshotId (v_snapshot_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE snapshots
    SET    memory_dump_disk_id = NULL,
           memory_metadata_disk_id = NULL
    WHERE  snapshot_id = v_snapshot_id;
END; $PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllSnapshotsByMemoryDisk(v_memory_disk_id UUID)
RETURNS SETOF snapshots STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM snapshots
    WHERE v_memory_disk_id IN (memory_dump_disk_id, memory_metadata_disk_id);
END; $PROCEDURE$
LANGUAGE plpgsql;
