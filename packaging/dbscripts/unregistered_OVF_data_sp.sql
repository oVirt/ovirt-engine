

----------------------------------------------------------------
-- [unregistered_ovf_of_entities] Table
CREATE OR REPLACE FUNCTION InsertOVFDataForEntities (
    v_entity_guid UUID,
    v_entity_name VARCHAR(255),
    v_entity_type VARCHAR(32),
    v_architecture INT,
    v_lowest_comp_version VARCHAR(40),
    v_storage_domain_id UUID,
    v_ovf_data TEXT,
    v_ovf_extra_data TEXT,
    v_status INTEGER
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO unregistered_ovf_of_entities (
        entity_guid,
        entity_name,
        entity_type,
        architecture,
        lowest_comp_version,
        storage_domain_id,
        ovf_extra_data,
        ovf_data,
        status
        )
    VALUES (
        v_entity_guid,
        v_entity_name,
        v_entity_type,
        v_architecture,
        v_lowest_comp_version,
        v_storage_domain_id,
        v_ovf_extra_data,
        v_ovf_data,
        v_status
        );

    UPDATE unregistered_ovf_of_entities u
    SET ovf_data = vog.ovf_data
    FROM vm_ovf_generations vog
    WHERE vog.vm_guid = u.entity_guid
        AND u.entity_guid = v_entity_guid
        AND v_ovf_data IS NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveEntityFromUnregistered (
    v_entity_guid UUID,
    v_storage_domain_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM unregistered_ovf_of_entities
    WHERE entity_guid = v_entity_guid
        AND (
            storage_domain_id = v_storage_domain_id
            OR v_storage_domain_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllOVFEntitiesForStorageDomainByEntityType (
    v_storage_domain_id UUID,
    v_entity_type VARCHAR(20)
    )
RETURNS SETOF unregistered_ovf_of_entities STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM unregistered_ovf_of_entities
    WHERE storage_domain_id = v_storage_domain_id
        AND (
            entity_type = v_entity_type
            OR v_entity_type IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetOVFDataByEntityIdAndStorageDomain (
    v_entity_guid UUID,
    v_storage_domain_id UUID
    )
RETURNS SETOF unregistered_ovf_of_entities STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM unregistered_ovf_of_entities
    WHERE entity_guid = v_entity_guid
        AND (
            storage_domain_id = v_storage_domain_id
            OR v_storage_domain_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


