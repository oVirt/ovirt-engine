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


CREATE OR REPLACE FUNCTION GetImageUploadsByCommandId(v_command_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF image_transfers STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.*
    FROM image_transfers
    WHERE image_transfers.command_id = v_command_id AND
    (NOT v_is_filtered OR EXISTS (SELECT    1
                                  FROM      user_disk_permissions_view
                                  WHERE     user_disk_permissions_view.user_id = v_user_id AND
                                            user_disk_permissions_view.entity_id = image_transfers.disk_id ));
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


CREATE OR REPLACE FUNCTION GetImageTransfersByVdsId(v_vds_id UUID)
RETURNS SETOF image_transfers STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT image_transfers.*
    FROM image_transfers
    WHERE image_transfers.vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateImageUploads(
    v_command_id UUID,
    v_command_type INTEGER,
    v_phase INTEGER,
    v_type INTEGER,
    v_active BOOLEAN,
    v_last_updated TIMESTAMP,
    v_message VARCHAR,
    v_vds_id UUID,
    v_disk_id UUID,
    v_imaged_ticket_id UUID,
    v_proxy_uri VARCHAR,
    v_daemon_uri VARCHAR,
    v_signed_ticket VARCHAR,
    v_bytes_sent BIGINT,
    v_bytes_total BIGINT,
    v_client_inactivity_timeout INTEGER
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    UPDATE image_transfers
    SET command_id = v_command_id,
        command_type = v_command_type,
        phase = v_phase,
        type = v_type,
        active = v_active,
        last_updated = v_last_updated,
        message = v_message,
        vds_id = v_vds_id,
        disk_id = v_disk_id,
        imaged_ticket_id = v_imaged_ticket_id,
        proxy_uri = v_proxy_uri,
        daemon_uri = v_daemon_uri,
        signed_ticket = v_signed_ticket,
        bytes_sent = v_bytes_sent,
        bytes_total = v_bytes_total,
        client_inactivity_timeout = v_client_inactivity_timeout
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
    v_type INTEGER,
    v_active BOOLEAN,
    v_last_updated TIMESTAMP,
    v_message VARCHAR,
    v_vds_id UUID,
    v_disk_id UUID,
    v_imaged_ticket_id UUID,
    v_proxy_uri VARCHAR,
    v_daemon_uri VARCHAR,
    v_signed_ticket VARCHAR,
    v_bytes_sent BIGINT,
    v_bytes_total BIGINT,
    v_client_inactivity_timeout INTEGER
    )
RETURNS VOID
AS $PROCEDURE$
BEGIN
    INSERT INTO image_transfers(
        command_id,
        command_type,
        phase,
        type,
        active,
        last_updated,
        message,
        vds_id,
        disk_id,
        imaged_ticket_id,
        proxy_uri,
        daemon_uri,
        signed_ticket,
        bytes_sent,
        bytes_total,
        client_inactivity_timeout
        )
    VALUES (
        v_command_id,
        v_command_type,
        v_phase,
        v_type,
        v_active,
        v_last_updated,
        v_message,
        v_vds_id,
        v_disk_id,
        v_imaged_ticket_id,
        v_proxy_uri,
        v_daemon_uri,
        v_signed_ticket,
        v_bytes_sent,
        v_bytes_total,
        v_client_inactivity_timeout
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

