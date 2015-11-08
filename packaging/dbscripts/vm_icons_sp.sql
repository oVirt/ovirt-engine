

-- VmIcon vm_icons
CREATE OR REPLACE FUNCTION GetVmIconByVmIconId (v_id UUID)
RETURNS SETOF vm_icons STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icons
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmIcons ()
RETURNS SETOF vm_icons STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icons;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmIconsFiltered (
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vm_icons STABLE AS $PROCEDURE$
BEGIN
    IF v_is_filtered THEN
        RETURN QUERY

    -- icons associated with owned vms and templates
    SELECT DISTINCT vm_icons.*
    FROM (
        SELECT *
        FROM user_vm_template_permissions_view

        UNION

        SELECT *
        FROM user_vm_permissions_view
        ) AS permissions,
        vm_static,
        vm_icons
    WHERE permissions.entity_id = vm_static.vm_guid
        AND vm_static.entity_type IN (
            'TEMPLATE',
            'VM'
            )
        AND (
            vm_icons.id = vm_static.small_icon_id
            OR vm_icons.id = vm_static.large_icon_id
            )
        AND permissions.user_id = v_user_id

    UNION

    -- predefined icons
    SELECT vm_icons.*
    FROM vm_icons,
        vm_icon_defaults
    WHERE vm_icon_defaults.small_icon_id = vm_icons.id
        OR vm_icon_defaults.large_icon_id = vm_icons.id;ELSE

    RETURN QUERY

    SELECT *
    FROM vm_icons;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmIcon (
    v_id UUID,
    v_data_url TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_icons (
        id,
        data_url
        )
    VALUES (
        v_id,
        v_data_url
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmIcon (
    v_id UUID,
    v_data_url TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_icons
    SET id = v_id,
        data_url = v_data_url
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmIcon (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_icons
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmIconByVmIconDataUrl (v_data_url TEXT)
RETURNS SETOF vm_icons STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icons
    WHERE data_url = v_data_url;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmIconIfUnused (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_icons
    WHERE id = v_id
        AND NOT EXISTS (
            SELECT 1
            FROM vm_icon_defaults
            WHERE vm_icon_defaults.small_icon_id = vm_icons.id
                OR vm_icon_defaults.large_icon_id = vm_icons.id
            )
        AND NOT EXISTS (
            SELECT 1
            FROM vm_static
            WHERE vm_static.small_icon_id = vm_icons.id
                OR vm_static.large_icon_id = vm_icons.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllUnusedVmIcons ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_icons
    WHERE NOT EXISTS (
            SELECT 1
            FROM vm_icon_defaults
            WHERE vm_icon_defaults.small_icon_id = vm_icons.id
                OR vm_icon_defaults.large_icon_id = vm_icons.id
            )
        AND NOT EXISTS (
            SELECT 1
            FROM vm_static
            WHERE vm_static.small_icon_id = vm_icons.id
                OR vm_static.large_icon_id = vm_icons.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IsVmIconExist (v_id UUID)
RETURNS BOOLEAN STABLE AS $PROCEDURE$
BEGIN
    RETURN (
            SELECT EXISTS (
                    SELECT 1
                    FROM vm_icons
                    WHERE id = v_id
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- VmIconDefaults vm_icon_defaults
CREATE OR REPLACE FUNCTION GetVmIconDefaultByVmIconDefaultId (v_id UUID)
RETURNS SETOF vm_icon_defaults STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icon_defaults
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmIconDefaults ()
RETURNS SETOF vm_icon_defaults STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icon_defaults;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmIconDefault (
    v_id UUID,
    v_os_id INT,
    v_small_icon_id UUID,
    v_large_icon_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_icon_defaults (
        id,
        os_id,
        small_icon_id,
        large_icon_id
        )
    VALUES (
        v_id,
        v_os_id,
        v_small_icon_id,
        v_large_icon_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmIconDefault (
    v_id UUID,
    v_os_id INT,
    v_small_icon_id UUID,
    v_large_icon_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_icon_defaults
    SET id = v_id,
        os_id = v_os_id,
        small_icon_id = v_small_icon_id,
        large_icon_id = v_large_icon_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmIconDefault (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_icon_defaults
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmIconDefaultByVmIconDefaultLargeIconId (v_large_icon_id UUID)
RETURNS SETOF vm_icon_defaults STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icon_defaults
    WHERE large_icon_id = v_large_icon_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmIconDefaultByVmIconDefaultOsId (v_os_id INT)
RETURNS SETOF vm_icon_defaults STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_icon_defaults
    WHERE os_id = v_os_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllFromVmIconDefaults ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_icon_defaults;
END;$PROCEDURE$
LANGUAGE plpgsql;


