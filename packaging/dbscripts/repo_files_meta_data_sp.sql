

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
