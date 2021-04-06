

----------------------------------------------------------------
-- [vm_device] Table
--
CREATE OR REPLACE FUNCTION InsertVmDevice (
    v_device_id UUID,
    v_vm_id UUID,
    v_device VARCHAR(30),
    v_type VARCHAR(30),
    v_address VARCHAR(255),
    v_spec_params TEXT,
    v_is_managed boolean,
    v_is_plugged boolean,
    v_is_readonly boolean,
    v_alias VARCHAR(255),
    v_custom_properties TEXT,
    v_snapshot_id uuid,
    v_logical_name VARCHAR(255),
    v_host_device VARCHAR(255)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_device (
        device_id,
        vm_id,
        device,
        type,
        address,
        spec_params,
        is_managed,
        is_plugged,
        is_readonly,
        alias,
        custom_properties,
        snapshot_id,
        logical_name,
        host_device
        )
    VALUES (
        v_device_id,
        v_vm_id,
        v_device,
        v_type,
        v_address,
        v_spec_params,
        v_is_managed,
        v_is_plugged,
        v_is_readonly,
        v_alias,
        v_custom_properties,
        v_snapshot_id,
        v_logical_name,
        v_host_device
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmDevice (
    v_device_id UUID,
    v_vm_id UUID,
    v_device VARCHAR(30),
    v_type VARCHAR(30),
    v_address VARCHAR(255),
    v_spec_params TEXT,
    v_is_managed boolean,
    v_is_plugged boolean,
    v_is_readonly boolean,
    v_alias VARCHAR(255),
    v_custom_properties TEXT,
    v_snapshot_id uuid,
    v_logical_name VARCHAR(255),
    v_host_device VARCHAR(255)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_device
    SET device = v_device,
        type = v_type,
        address = v_address,
        spec_params = v_spec_params,
        is_managed = v_is_managed,
        is_plugged = v_is_plugged,
        is_readonly = v_is_readonly,
        alias = v_alias,
        custom_properties = v_custom_properties,
        snapshot_id = v_snapshot_id,
        logical_name = v_logical_name,
        host_device = v_host_device,
        _update_date = current_timestamp
    WHERE device_id = v_device_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmDevice (
    v_device_id UUID,
    v_vm_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_device
    WHERE device_id = v_device_id
        AND (
            v_vm_id IS NULL
            OR vm_id = v_vm_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmDevicesByVmIdAndType (
    v_vm_id UUID,
    v_type VARCHAR(30)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_device
    WHERE vm_id = v_vm_id
        AND type = v_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmDevice ()
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByDeviceId (
    v_device_id UUID,
    v_vm_id UUID
    )
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
    WHERE device_id = v_device_id
        AND (
            v_vm_id IS NULL
            OR vm_id = v_vm_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
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
    ORDER BY device_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByVmIdAndType (
    v_vm_id UUID,
    v_type VARCHAR(30)
    )
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
    WHERE vm_id = v_vm_id
        AND type = v_type
    ORDER BY NULLIF(alias, '') NULLS LAST;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByVmIdTypeAndDevice (
    v_vm_id UUID,
    v_type VARCHAR(30),
    v_device VARCHAR(30),
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
    WHERE vm_id = v_vm_id
        AND type = v_type
        AND device = v_device
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                UNION
                SELECT 1
                FROM user_vm_template_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
            )
        )
    ORDER BY NULLIF(alias, '') NULLS LAST;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns for all VMs
CREATE OR REPLACE FUNCTION GetVmDeviceByTypeAndDevice (
    v_vm_ids UUID[],
    v_type VARCHAR(30),
    v_device VARCHAR(30),
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
    WHERE vm_id = ANY(v_vm_ids)
        AND type = v_type
        AND device = v_device
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vm_id
                )
            )
    ORDER BY NULLIF(alias, '') NULLS LAST;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmUnmanagedDevicesByVmId (v_vm_id UUID)
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_device_view.*
    FROM vm_device_view
    WHERE vm_id = v_vm_id
        AND NOT is_managed;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION isMemBalloonEnabled (v_vm_id UUID)
RETURNS boolean STABLE AS $BODY$

DECLARE result boolean := false;

BEGIN
    IF EXISTS (
            SELECT 1
            FROM vm_device
            WHERE vm_id = v_vm_id
                AND type = 'balloon'
                AND device = 'memballoon'
            ) THEN result := true;
    END IF;
        RETURN result;
END;$BODY$

LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION clearVmDeviceAddress (v_device_id UUID)
RETURNS VOID AS $BODY$

BEGIN
    UPDATE vm_device
    SET address = ''
    WHERE device_id = v_device_id;
END;$BODY$

LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION clearAllDeviceAddressesByVmId (v_vm_id UUID)
RETURNS VOID AS $BODY$

BEGIN
    UPDATE vm_device
    SET address = ''
    WHERE vm_id = v_vm_id;
END;$BODY$

LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION removeAllUnmanagedDevicesByVmId (v_vm_id UUID)
RETURNS VOID AS $BODY$

BEGIN
    DELETE FROM vm_device
    WHERE vm_id = v_vm_id
        AND NOT is_managed;
END;$BODY$

LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION ExistsVmDeviceByVmIdAndType (
    v_vm_id UUID,
    v_type VARCHAR(30)
    )
RETURNS BOOLEAN STABLE AS $PROCEDURE$
BEGIN
    RETURN EXISTS (
            SELECT 1
            FROM vm_device
            WHERE vm_id = v_vm_id
                AND type = v_type
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByType (v_type VARCHAR(30))
RETURNS SETOF vm_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_device_view
    WHERE type = v_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

