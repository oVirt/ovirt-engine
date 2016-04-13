---------------------
-- image_transfers functions
---------------------

CREATE OR REPLACE FUNCTION GetAllFromImageUploads()
RETURNS SETOF image_transfers STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.*
    FROM image_transfers;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllImageUploadIds()
RETURNS SETOF UUID STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.command_id
    FROM image_transfers;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetImageUploadsByCommandId(v_command_id UUID)
RETURNS SETOF image_transfers STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.*
    FROM image_transfers
    WHERE image_transfers.command_id = v_command_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetImageUploadsByDiskId(v_disk_id UUID)
RETURNS SETOF image_transfers STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.*
    FROM image_transfers
    WHERE image_transfers.disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateImageUploads(
    v_command_id UUID,
    v_command_type INTEGER,
    v_phase INTEGER,
    v_last_updated TIMESTAMP,
    v_message VARCHAR,
    v_vds_id UUID,
    v_disk_id UUID,
    v_imaged_ticket_id UUID,
    v_proxy_uri VARCHAR,
    v_signed_ticket VARCHAR,
    v_bytes_sent BIGINT,
    v_bytes_total BIGINT
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    UPDATE image_transfers
    SET command_id = v_command_id,
        command_type = v_command_type,
        phase = v_phase,
        last_updated = v_last_updated,
        message = v_message,
        vds_id = v_vds_id,
        disk_id = v_disk_id,
        imaged_ticket_id = v_imaged_ticket_id,
        proxy_uri = v_proxy_uri,
        signed_ticket = v_signed_ticket,
        bytes_sent = v_bytes_sent,
        bytes_total = v_bytes_total
    WHERE command_id = v_command_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeleteImageUploads(v_command_id UUID)
RETURNS VOID
AS $PROCEDURE$
BEGIN
    DELETE
    FROM image_transfers
    WHERE command_id = v_command_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION InsertImageUploads(
    v_command_id UUID,
    v_command_type INTEGER,
    v_phase INTEGER,
    v_last_updated TIMESTAMP,
    v_message VARCHAR,
    v_vds_id UUID,
    v_disk_id UUID,
    v_imaged_ticket_id UUID,
    v_proxy_uri VARCHAR,
    v_signed_ticket VARCHAR,
    v_bytes_sent BIGINT,
    v_bytes_total BIGINT
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    INSERT INTO image_transfers(
        command_id,
        command_type,
        phase,
        last_updated,
        message,
        vds_id,
        disk_id,
        imaged_ticket_id,
        proxy_uri,
        signed_ticket,
        bytes_sent,
        bytes_total
        )
    VALUES (
        v_command_id,
        v_command_type,
        v_phase,
        v_last_updated,
        v_message,
        v_vds_id,
        v_disk_id,
        v_imaged_ticket_id,
        v_proxy_uri,
        v_signed_ticket,
        v_bytes_sent,
        v_bytes_total
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

