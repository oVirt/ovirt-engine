----------------------------------------------------------------
-- [unregistered_ovf_of_entities] Table
CREATE OR REPLACE FUNCTION InsertUnregisteredDisk (
    v_disk_id UUID,
    v_image_id UUID,
    v_disk_alias VARCHAR(255),
    v_disk_description VARCHAR(255),
    v_storage_domain_id UUID,
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_last_modified TIMESTAMP WITH TIME ZONE,
    v_volume_type INTEGER,
    v_volume_format INTEGER,
    v_actual_size bigint,
    v_size bigint
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    INSERT INTO unregistered_disks (
        disk_id,
        image_id,
        disk_alias,
        disk_description,
        storage_domain_id,
        creation_date,
        last_modified,
        volume_type,
        volume_format,
        actual_size,
        size
        )
    VALUES (
        v_disk_id,
        v_image_id,
        v_disk_alias,
        v_disk_description,
        v_storage_domain_id,
        v_creation_date,
        v_last_modified,
        v_volume_type,
        v_volume_format,
        v_actual_size,
        v_size
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertUnregisteredDisksToVms (
    v_disk_id UUID,
    v_entity_id UUID,
    v_entity_name VARCHAR(255),
    v_storage_domain_id UUID
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    INSERT INTO unregistered_disks_to_vms (
        disk_id,
        entity_id,
        entity_name,
        storage_domain_id
        )
    VALUES (
        v_disk_id,
        v_entity_id,
        v_entity_name,
        v_storage_domain_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION RemoveDiskFromUnregistered (
    v_disk_id UUID,
    v_storage_domain_id UUID
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    DELETE
    FROM unregistered_disks
    WHERE (disk_id = v_disk_id
        OR v_disk_id IS NULL)
        AND (
            storage_domain_id = v_storage_domain_id
            OR v_storage_domain_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveDiskFromUnregisteredRelatedToVM (
    v_vm_id UUID,
    v_storage_domain_id UUID
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    DELETE
    FROM unregistered_disks
    WHERE disk_id IN (SELECT disk_id
                      FROM unregistered_disks_to_vms
                      WHERE entity_id = v_vm_id)
        AND (
            storage_domain_id = v_storage_domain_id
            OR v_storage_domain_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetDiskByDiskIdAndStorageDomainId (
    v_disk_id UUID,
    v_storage_domain_id UUID
    )
RETURNS SETOF unregistered_disks STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM unregistered_disks
    WHERE (disk_id = v_disk_id
        OR v_disk_id IS NULL)
        AND (
            storage_domain_id = v_storage_domain_id
            OR v_storage_domain_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetEntitiesByDiskId (
    v_disk_id UUID)
RETURNS SETOF unregistered_disks_to_vms STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM unregistered_disks_to_vms
    WHERE disk_id = v_disk_id
       OR v_disk_id IS NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;
