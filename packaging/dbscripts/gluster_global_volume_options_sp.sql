
CREATE OR REPLACE FUNCTION InsertGlusterGlobalVolumeOption (
    v_id UUID,
    v_cluster_id UUID,
    v_option_key VARCHAR(8192),
    v_option_val VARCHAR(8192)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_global_volume_options (
        id,
        cluster_id,
        option_key,
        option_val
        )
    VALUES (
        v_id,
        v_cluster_id,
        v_option_key,
        v_option_val
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlobalOptionsByGlusterClusterGuid (v_cluster_id UUID)
RETURNS SETOF gluster_global_volume_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_global_volume_options
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterGlobalVolumeOption (
    v_cluster_id UUID,
    v_option_key VARCHAR(8192),
    v_option_val VARCHAR(8192)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_global_volume_options
    SET option_val = v_option_val
    WHERE cluster_id = v_cluster_id
        AND option_key = v_option_key;
END;$PROCEDURE$
LANGUAGE plpgsql;