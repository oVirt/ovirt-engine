

----------------------------------------------------------------
-- [disk_image_dynamic] Table
--
CREATE OR REPLACE FUNCTION Insertdisk_image_dynamic (
    v_image_id UUID,
    v_read_rate INT,
    v_read_ops BIGINT,
    v_write_rate INT,
    v_write_ops BIGINT,
    v_actual_size BIGINT,
    v_read_latency_seconds NUMERIC(18, 9),
    v_write_latency_seconds NUMERIC(18, 9),
    v_flush_latency_seconds NUMERIC(18, 9)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO disk_image_dynamic (
        image_id,
        read_rate,
        read_ops,
        write_rate,
        write_ops,
        actual_size,
        read_latency_seconds,
        write_latency_seconds,
        flush_latency_seconds
        )
    VALUES (
        v_image_id,
        v_read_rate,
        v_read_ops,
        v_write_rate,
        v_write_ops,
        v_actual_size,
        v_read_latency_seconds,
        v_write_latency_seconds,
        v_flush_latency_seconds
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatedisk_image_dynamic (
    v_image_id UUID,
    v_read_rate INT,
    v_read_ops BIGINT,
    v_write_rate INT,
    v_write_ops BIGINT,
    v_actual_size BIGINT,
    v_read_latency_seconds NUMERIC(18, 9),
    v_write_latency_seconds NUMERIC(18, 9),
    v_flush_latency_seconds NUMERIC(18, 9)
    )
RETURNS VOID
    --The [disk_image_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE disk_image_dynamic
    SET read_rate = v_read_rate,
        read_ops = v_read_ops,
        write_rate = v_write_rate,
        write_ops = v_write_ops,
        actual_size = v_actual_size,
        read_latency_seconds = v_read_latency_seconds,
        write_latency_seconds = v_write_latency_seconds,
        flush_latency_seconds = v_flush_latency_seconds,
        _update_date = LOCALTIMESTAMP
    WHERE image_id = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatedisk_image_dynamic_by_disk_id_and_vm_id (
    v_image_group_id UUID,
    v_vm_id UUID,
    v_read_rate INT,
    v_read_ops BIGINT,
    v_write_rate INT,
    v_write_ops BIGINT,
    v_actual_size BIGINT,
    v_read_latency_seconds NUMERIC(18, 9),
    v_write_latency_seconds NUMERIC(18, 9),
    v_flush_latency_seconds NUMERIC(18, 9)
    )
RETURNS VOID
    --The [disk_image_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE disk_image_dynamic
    SET read_rate = v_read_rate,
        read_ops = v_read_ops,
        write_rate = v_write_rate,
        write_ops = v_write_ops,
        actual_size = v_actual_size,
        read_latency_seconds = v_read_latency_seconds,
        write_latency_seconds = v_write_latency_seconds,
        flush_latency_seconds = v_flush_latency_seconds,
        _update_date = LOCALTIMESTAMP
    WHERE image_id IN (
            SELECT DISTINCT image_guid
            FROM images
            WHERE image_group_id = v_image_group_id
                AND active = true
            )
        AND EXISTS (
            SELECT 1
            FROM vm_device vmd
            WHERE vmd.vm_id = v_vm_id
                AND vmd.device_id = v_image_group_id
                AND vmd.snapshot_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletedisk_image_dynamic (v_image_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM disk_image_dynamic
    WHERE image_id = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromdisk_image_dynamic ()
RETURNS SETOF disk_image_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_image_dynamic;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getdisk_image_dynamicByimage_id (v_image_id UUID)
RETURNS SETOF disk_image_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_image_dynamic
    WHERE image_id = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TRIGGER

IF EXISTS delete_disk_image_dynamic_for_image
    ON images;
    CREATE
        OR REPLACE FUNCTION fn_image_deleted ()
    RETURNS TRIGGER AS $$

BEGIN
    DELETE
    FROM disk_image_dynamic dim
    WHERE DIM.image_id = OLD.image_guid;

    RETURN OLD;
END;$$

LANGUAGE plpgsql;

CREATE TRIGGER delete_disk_image_dynamic_for_image BEFORE

DELETE
    ON IMAGES
FOR EACH ROW

EXECUTE PROCEDURE fn_image_deleted();


