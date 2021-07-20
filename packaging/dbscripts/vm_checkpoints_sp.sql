

----------------------------------------------------------------------
--  VM Checkpoints Table
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVmCheckpointByVmCheckpointId (v_checkpoint_id UUID)
RETURNS SETOF vm_checkpoints STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_checkpoints
    WHERE checkpoint_id = v_checkpoint_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmCheckpoint (
    v_checkpoint_id UUID,
    v_vm_id UUID,
    v_parent_id UUID,
    v__create_date TIMESTAMP WITH TIME ZONE,
    v_state TEXT,
    v_description VARCHAR(1024)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_checkpoints (
        checkpoint_id,
        vm_id,
        parent_id,
        _create_date,
        state,
        description
        )
    VALUES (
        v_checkpoint_id,
        v_vm_id,
        v_parent_id,
        v__create_date,
        v_state,
        v_description
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmCheckpoint (
    v_checkpoint_id UUID,
    v_vm_id UUID,
    v_parent_id UUID,
    v_state TEXT,
    v_description VARCHAR(1024)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_checkpoints
    SET checkpoint_id = v_checkpoint_id,
        vm_id = v_vm_id,
        parent_id = v_parent_id,
        state = v_state,
        description = v_description
    WHERE checkpoint_id = v_checkpoint_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmCheckpoint (v_checkpoint_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_checkpoints
    WHERE checkpoint_id = v_checkpoint_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmCheckpoints ()
RETURNS SETOF vm_checkpoints STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_checkpoints;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmCheckpointsByVmId (v_vm_id UUID)
RETURNS SETOF vm_checkpoints STABLE AS $PROCEDURE$
BEGIN
     RETURN QUERY WITH RECURSIVE checkpoint_list AS (
          SELECT *
          FROM   vm_checkpoints
          WHERE  vm_id = v_vm_id AND parent_id is NULL
          UNION ALL
          SELECT vm_checkpoints.*
          FROM   vm_checkpoints
          JOIN   checkpoint_list ON
                 checkpoint_list.checkpoint_id = vm_checkpoints.parent_id AND
                 checkpoint_list.vm_id = vm_checkpoints.vm_id
      )
      SELECT *
      FROM checkpoint_list;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllCheckpointsByVmId (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_checkpoints
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmCheckpointByVmCheckpointParentId (v_checkpoint_id UUID)
RETURNS SETOF vm_checkpoints STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_checkpoints
    WHERE parent_id = v_checkpoint_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InvalidateAllCheckpointsByVmId (
    v_vm_id UUID,
    v_state TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_checkpoints
    SET state = v_state
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_checkpoint_disk_map] Table
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVmCheckpointDiskMap (
    v_checkpoint_id UUID,
    v_disk_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vm_checkpoint_disk_map (
            checkpoint_id,
            disk_id
            )
        VALUES (
            v_checkpoint_id,
            v_disk_id
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDisksByVmCheckpointId (v_checkpoint_id UUID)
RETURNS SETOF images_storage_domain_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT images_storage_domain_view.*
    FROM   images_storage_domain_view
    JOIN   vm_checkpoint_disk_map on vm_checkpoint_disk_map.disk_id = images_storage_domain_view.image_group_id
    WHERE  images_storage_domain_view.active AND vm_checkpoint_disk_map.checkpoint_id = v_checkpoint_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IsDiskIncludedInCheckpoint (v_disk_id UUID)
RETURNS SETOF booleanResultType STABLE
    AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT 1
            FROM vm_checkpoint_disk_map
            WHERE disk_id = v_disk_id
           );
END;$PROCEDURE$
LANGUAGE plpgsql;
