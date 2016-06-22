

CREATE OR REPLACE FUNCTION InsertQuota (
    v_id UUID,
    v_storage_pool_id UUID,
    v_quota_name VARCHAR(50),
    v_description VARCHAR(500),
    v_threshold_cluster_percentage INT,
    v_threshold_storage_percentage INT,
    v_grace_cluster_percentage INT,
    v_grace_storage_percentage INT,
    v_is_default BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO quota (
        id,
        storage_pool_id,
        quota_name,
        description,
        threshold_cluster_percentage,
        threshold_storage_percentage,
        grace_cluster_percentage,
        grace_storage_percentage,
        is_default
        )
    VALUES (
        v_id,
        v_storage_pool_id,
        v_quota_name,
        v_description,
        v_threshold_cluster_percentage,
        v_threshold_storage_percentage,
        v_grace_cluster_percentage,
        v_grace_storage_percentage,
        v_is_default
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertQuotaLimitation (
    v_id UUID,
    v_quota_id UUID,
    v_storage_id UUID,
    v_cluster_id UUID,
    v_virtual_cpu INT,
    v_mem_size_mb BIGINT,
    v_storage_size_gb BIGINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO quota_limitation (
        id,
        quota_id,
        storage_id,
        cluster_id,
        virtual_cpu,
        mem_size_mb,
        storage_size_gb
        )
    VALUES (
        v_id,
        v_quota_id,
        v_storage_id,
        v_cluster_id,
        v_virtual_cpu,
        v_mem_size_mb,
        v_storage_size_gb
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all the Quota storages in the storage pool if v_storage_id is null, if v_storage_id is not null then a specific quota storage will be returned.
-- Empty quotas are returned only if v_allow_empty is set to TRUE
CREATE OR REPLACE FUNCTION GetQuotaStorageByStorageGuid (
    v_storage_id UUID,
    v_id UUID,
    v_allow_empty BOOLEAN
    )
RETURNS SETOF quota_storage_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM (
        SELECT COALESCE(q_storage_view.quota_storage_id, q_g_view.quota_id) AS quota_storage_id,
            q_g_view.quota_id AS quota_id,
            q_storage_view.storage_id,
            q_storage_view.storage_name,
            COALESCE(q_storage_view.storage_size_gb, q_g_view.storage_size_gb) AS storage_size_gb,
            COALESCE(q_storage_view.storage_size_gb_usage, q_g_view.storage_size_gb_usage) AS storage_size_gb_usage
        FROM quota_global_view q_g_view
        LEFT JOIN quota_storage_view q_storage_view
            ON q_g_view.quota_id = q_storage_view.quota_id
                AND (
                    v_storage_id = q_storage_view.storage_id
                    OR v_storage_id IS NULL
                    )
        WHERE q_g_view.quota_id = v_id
        ) sub
    WHERE (
            v_allow_empty
            OR storage_size_gb IS NOT NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all the global quotas in the storage pool if v_storage_pool_id is null, if v_storage_id is not null then a specific quota storage will be returned.
CREATE OR REPLACE FUNCTION GetQuotaByAdElementId (
    v_ad_element_id UUID,
    v_storage_pool_id UUID,
    v_recursive BOOLEAN
    )
RETURNS SETOF quota_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_view
    WHERE quota_view.quota_id IN (
            SELECT object_id
            FROM PERMISSIONS
            WHERE object_type_id = 17
                AND role_id IN (
                    SELECT role_id
                    FROM ROLES_groups
                    WHERE action_group_id = 901
                    )
                AND ad_element_id = v_ad_element_id
                OR (
                    v_recursive
                    AND ad_element_id IN (
                        SELECT *
                        FROM getUserAndGroupsById(v_ad_element_id)
                        )
                    )
            )
        AND (
            v_storage_pool_id = quota_view.storage_pool_id
            OR v_storage_pool_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all the quotas in a thin view (only basic quota meta data. no limits or consumption)
CREATE OR REPLACE FUNCTION getAllThinQuota ()
RETURNS SETOF quota_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getQuotaCount ()
RETURNS SETOF BIGINT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT count(*) AS num_quota
    FROM quota;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQuotaStorageByQuotaGuid (v_id UUID)
RETURNS SETOF quota_storage_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_storage_view
    WHERE quota_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQuotaClusterByClusterGuid (
    v_cluster_id UUID,
    v_id UUID,
    v_allow_empty BOOLEAN
    )
RETURNS SETOF quota_cluster_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM (
        SELECT COALESCE(q_vds_view.quota_cluster_id, q_g_view.quota_id) AS quota_cluster_id,
            q_g_view.quota_id AS quota_id,
            q_vds_view.cluster_id AS cluster_id,
            q_vds_view.cluster_name AS cluster_name,
            COALESCE(q_vds_view.virtual_cpu, q_g_view.virtual_cpu) AS virtual_cpu,
            COALESCE(q_vds_view.virtual_cpu_usage, q_g_view.virtual_cpu_usage) AS virtual_cpu_usage,
            COALESCE(q_vds_view.mem_size_mb, q_g_view.mem_size_mb) AS mem_size_mb,
            COALESCE(q_vds_view.mem_size_mb_usage, q_g_view.mem_size_mb_usage) AS mem_size_mb_usage
        FROM quota_global_view q_g_view
        LEFT JOIN quota_cluster_view q_vds_view
            ON q_g_view.quota_id = q_vds_view.quota_id
                AND (
                    v_cluster_id = q_vds_view.cluster_id
                    OR v_cluster_id IS NULL
                    )
        WHERE q_g_view.quota_id = v_id
        ) sub
    WHERE v_allow_empty
        OR virtual_cpu IS NOT NULL
        OR mem_size_mb IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQuotaClusterByQuotaGuid (v_id UUID)
RETURNS SETOF quota_cluster_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT quota_cluster_view.*
    FROM quota_cluster_view
    WHERE quota_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteQuotaByQuotaGuid (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM quota
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteQuotaLimitationByQuotaGuid (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM quota_limitation
    WHERE quota_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateQuotaMetaData (
    v_id UUID,
    v_storage_pool_id UUID,
    v_quota_name VARCHAR(50),
    v_description VARCHAR(500),
    v_threshold_cluster_percentage INT,
    v_threshold_storage_percentage INT,
    v_grace_cluster_percentage INT,
    v_grace_storage_percentage INT,
    v_is_default BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE quota
    SET storage_pool_id = v_storage_pool_id,
        quota_name = v_quota_name,
        description = v_description,
        _update_date = LOCALTIMESTAMP,
        threshold_cluster_percentage = v_threshold_cluster_percentage,
        threshold_storage_percentage = v_threshold_storage_percentage,
        grace_cluster_percentage = v_grace_cluster_percentage,
        grace_storage_percentage = v_grace_storage_percentage,
        is_default = v_is_default
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all the Quota storages in the storage pool if v_storage_id is null, if v_storage_id is not null then a specific quota storage will be returned.
CREATE OR REPLACE FUNCTION GetQuotaByStoragePoolGuid (v_storage_pool_id UUID)
RETURNS SETOF quota_global_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_global_view
    WHERE (
            storage_pool_id = v_storage_pool_id
            OR v_storage_pool_id IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDefaultQuotaForStoragePool (v_storage_pool_id UUID)
RETURNS SETOF quota_global_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_global_view
    WHERE is_default = TRUE
          AND storage_pool_id = v_storage_pool_id;
END;
$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQuotaByQuotaGuid (v_id UUID)
RETURNS SETOF quota_global_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_global_view
    WHERE quota_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQuotaByQuotaName (v_quota_name VARCHAR, v_storage_pool_id UUID)
RETURNS SETOF quota_global_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM quota_global_view
    WHERE quota_name = v_quota_name
        AND storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllThinQuotasByStorageId (
    v_storage_id UUID,
    v_engine_session_seq_id INT,
    v_is_filtered boolean
    )
RETURNS SETOF quota_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT quota_id,
        storage_pool_id,
        storage_pool_name,
        quota_name,
        description,
        threshold_cluster_percentage,
        threshold_storage_percentage,
        grace_cluster_percentage,
        grace_storage_percentage,
        quota_enforcement_type,
        is_default
    FROM quota_limitations_view
    WHERE (
            storage_id = v_storage_id
            OR (
                is_global
                AND NOT is_empty
                AND storage_size_gb IS NOT NULL
                AND storage_pool_id IN (
                    SELECT storage_pool_id
                    FROM storage_pool_iso_map
                    WHERE storage_id = v_storage_id
                    )
                )
            )
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM permissions p
                INNER JOIN engine_session_user_flat_groups u
                    ON u.granted_id = p.ad_element_id
                WHERE u.engine_session_seq_id = v_engine_session_seq_id
                    AND p.object_type_id = 17
                    AND -- quota object
                    p.role_id = 'def0000a-0000-0000-0000-def00000000a'
                    AND -- consume quota
                    quota_id = p.object_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllThinQuotasByClusterId (
    v_cluster_id UUID,
    v_engine_session_seq_id INT,
    v_is_filtered boolean
    )
RETURNS SETOF quota_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT quota_id,
        storage_pool_id,
        storage_pool_name,
        quota_name,
        description,
        threshold_cluster_percentage,
        threshold_storage_percentage,
        grace_cluster_percentage,
        grace_storage_percentage,
        quota_enforcement_type,
        is_default
    FROM quota_limitations_view
    WHERE (
            cluster_id = v_cluster_id
            OR (
                is_global
                AND NOT is_empty
                AND virtual_cpu IS NOT NULL
                AND storage_pool_id IN (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                    )
                )
            )
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM permissions p
                INNER JOIN engine_session_user_flat_groups u
                    ON u.granted_id = p.ad_element_id
                WHERE u.engine_session_seq_id = v_engine_session_seq_id
                    AND p.object_type_id = 17
                    AND -- quota object
                    p.role_id = 'def0000a-0000-0000-0000-def00000000a'
                    AND -- consume quota
                    quota_id = p.object_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IsQuotaInUse (v_quota_id UUID)
RETURNS boolean STABLE AS $BODY$

DECLARE result boolean := FALSE;

BEGIN
    IF EXISTS (
            SELECT quota_id
            FROM image_storage_domain_map
            WHERE quota_id = v_quota_id

            UNION

            SELECT quota_id
            FROM vm_static
            WHERE quota_id = v_quota_id
            ) THEN result := TRUE;
    END IF;
    RETURN result;
END;$BODY$

LANGUAGE plpgsql;


