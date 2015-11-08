

----------------------------------------------------------------
-- [vds_kdump_status] Table
----------------------------------------------------------------
-- UpsertKdumpStatus is used in fence_kdump listener
CREATE OR REPLACE FUNCTION UpsertKdumpStatus (
    v_vds_id UUID,
    v_status VARCHAR(20),
    v_address VARCHAR(255)
    )
RETURNS INT AS $PROCEDURE$
BEGIN
    UPDATE vds_kdump_status
    SET status = v_status,
        address = v_address
    WHERE vds_id = v_vds_id;

    IF NOT found THEN
        INSERT INTO vds_kdump_status (
            vds_id,
            status,
            address
            )
        VALUES (
            v_vds_id,
            v_status,
            v_address
            );
    END IF;

    RETURN 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpsertKdumpStatusForIp (
    v_ip VARCHAR(20),
    v_status VARCHAR(20),
    v_address VARCHAR(255)
    )
RETURNS INT AS $PROCEDURE$
DECLARE v_vds_id UUID;

updated_rows INT;

BEGIN
    updated_rows := 0;

    SELECT vds_id
    INTO v_vds_id
    FROM vds_interface
    WHERE addr = v_ip;

    IF v_vds_id IS NOT NULL THEN
        SELECT UpsertKdumpStatus(v_vds_id, v_status, v_address)
        INTO updated_rows;
    END IF;

    RETURN updated_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveFinishedKdumpStatusForVds (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vds_kdump_status
    WHERE vds_id = v_vds_id
        AND status = 'finished';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetKdumpStatusForVds (v_vds_id UUID)
RETURNS SETOF vds_kdump_status STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_kdump_status
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- GetAllUnfinishedKdumpStatus is used in fence_kdump listener
CREATE OR REPLACE FUNCTION GetAllUnfinishedVdsKdumpStatus ()
RETURNS SETOF vds_kdump_status STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_kdump_status
    WHERE status <> 'finished';
END;$PROCEDURE$
LANGUAGE plpgsql;


