

----------------------------------------------------------------
-- [vds_spm_id_map] Table
--
CREATE OR REPLACE FUNCTION Insertvds_spm_id_map (
    v_storage_pool_id UUID,
    v_vds_id UUID,
    v_vds_spm_id INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_spm_id_map (
        storage_pool_id,
        vds_id,
        vds_spm_id
        )
    VALUES (
        v_storage_pool_id,
        v_vds_id,
        v_vds_spm_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevds_spm_id_map (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vds_spm_id_map
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteByPoolvds_spm_id_map (
    v_vds_id UUID,
    v_storage_pool_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vds_spm_id_map
    WHERE vds_id = v_vds_id
        AND storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromvds_spm_id_map ()
RETURNS SETOF vds_spm_id_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_spm_id_map.*
    FROM vds_spm_id_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getvds_spm_id_mapBystorage_pool_idAndByvds_spm_id (
    v_storage_pool_id UUID,
    v_vds_spm_id INT
    )
RETURNS SETOF vds_spm_id_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_spm_id_map.*
    FROM vds_spm_id_map
    WHERE storage_pool_id = v_storage_pool_id
        AND vds_spm_id = v_vds_spm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getvds_spm_id_mapBystorage_pool_id (v_storage_pool_id UUID)
RETURNS SETOF vds_spm_id_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_spm_id_map.*
    FROM vds_spm_id_map
    WHERE storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getvds_spm_id_mapByvds_id (v_vds_id UUID)
RETURNS SETOF vds_spm_id_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_spm_id_map.*
    FROM vds_spm_id_map
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


