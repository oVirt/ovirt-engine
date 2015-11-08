

----------------------------------------------------------------
-- [repo_file_meta_data] Table
--
CREATE OR REPLACE FUNCTION InsertRepo_domain_file_meta_data (
    v_repo_domain_id UUID,
    v_repo_image_id VARCHAR(256),
    v_repo_image_name VARCHAR(256),
    v_size BIGINT,
    v_date_created TIMESTAMP WITH TIME ZONE,
    v_last_refreshed BIGINT,
    v_file_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO repo_file_meta_data (
        repo_domain_id,
        repo_image_id,
        repo_image_name,
        size,
        date_created,
        last_refreshed,
        file_type
        )
    VALUES (
        v_repo_domain_id,
        v_repo_image_id,
        v_repo_image_name,
        v_size,
        v_date_created,
        v_last_refreshed,
        v_file_type
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteRepo_domain_file_list (
    v_storage_domain_id UUID,
    v_file_type INT DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM repo_file_meta_data
    WHERE repo_domain_id = v_storage_domain_id
        AND (
            v_file_type IS NULL
            OR file_type = v_file_type
            );

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetRepo_files_by_storage_domain (
    v_storage_domain_id UUID,
    v_file_type INT DEFAULT NULL
    )
RETURNS SETOF repo_file_meta_data STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT repo_file_meta_data.*
    FROM repo_file_meta_data
    WHERE repo_domain_id = v_storage_domain_id
        AND (
            v_file_type IS NULL
            OR repo_file_meta_data.file_type = v_file_type
            )
    ORDER BY repo_file_meta_data.last_refreshed;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetRepo_files_in_all_storage_pools_rs CASCADE;
CREATE TYPE GetRepo_files_in_all_storage_pools_rs AS (
        storage_domain_id UUID,
        last_refreshed BIGINT,
        file_type INT
        );

CREATE OR REPLACE FUNCTION GetRepo_files_in_all_storage_pools (
    v_storage_domain_type INT,
    v_storage_pool_status INT,
    v_vds_status INT,
    v_storage_domain_status INT
    )
RETURNS SETOF GetRepo_files_in_all_storage_pools_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT b.storage_domain_id,
        c.last_refreshed,
        b.file_type
    FROM storage_domain_file_repos b
    LEFT JOIN (
        SELECT storage_domain_id,
            file_type,
            min(last_refreshed) AS last_refreshed
        FROM storage_domain_file_repos a
        GROUP BY storage_domain_id,
            file_type
        ) AS c
        ON b.storage_domain_id = c.storage_domain_id
            AND b.file_type = c.file_type
    WHERE b.storage_domain_type = v_storage_domain_type
        AND b.storage_pool_status = v_storage_pool_status
        AND b.storage_domain_status = v_storage_domain_status
        AND b.vds_status = v_vds_status;
END;$PROCEDURE$
LANGUAGE plpgsql;


