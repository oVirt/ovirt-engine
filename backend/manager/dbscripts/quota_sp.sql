Create or replace FUNCTION InsertQuota(v_id UUID, v_storage_pool_id UUID, v_quota_name VARCHAR(50), v_description VARCHAR(500), v_threshold_vds_group_percentage INTEGER, v_threshold_storage_percentage INTEGER, v_grace_vds_group_percentage INTEGER, v_grace_storage_percentage INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO quota(id, storage_pool_id, quota_name, description, threshold_vds_group_percentage, threshold_storage_percentage, grace_vds_group_percentage, grace_storage_percentage)
   VALUES(v_id,  v_storage_pool_id, v_quota_name,  v_description,  v_threshold_vds_group_percentage ,  v_threshold_storage_percentage,  v_grace_vds_group_percentage,  v_grace_storage_percentage);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertQuotaLimitation(v_id UUID, v_quota_id UUID, v_storage_id UUID, v_vds_group_id UUID, v_virtual_cpu INTEGER, v_mem_size_mb BIGINT, v_storage_size_gb BIGINT)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO quota_limitation(id, quota_id, storage_id, vds_group_id, virtual_cpu, mem_size_mb, storage_size_gb)
   VALUES(v_id, v_quota_id, v_storage_id, v_vds_group_id, v_virtual_cpu, v_mem_size_mb, v_storage_size_gb);

END; $procedure$
LANGUAGE plpgsql;


-- Returns all the Quota storages in the storage pool if v_storage_id is null, if v_storage_id is not null then a specific quota storage will be returned.
Create or replace FUNCTION GetQuotaStorageByStorageGuid(v_storage_id UUID, v_id UUID)
RETURNS SETOF quota_storage_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT quota_storage_id,
   q_g_view.quota_id as quota_id,
   q_storage_view.storage_id,
   q_storage_view.storage_name,
   COALESCE(q_storage_view.storage_size_gb, q_g_view.storage_size_gb) as storage_size_gb,
   COALESCE(q_storage_view.storage_size_gb_usage, q_g_view.storage_size_gb_usage) as storage_size_gb_usage
   FROM  quota_global_view q_g_view LEFT OUTER JOIN
    quota_storage_view q_storage_view on q_g_view.quota_id = q_storage_view.quota_id
    AND (v_storage_id = q_storage_view.storage_id OR v_storage_id IS NULL)
   WHERE q_g_view.quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetQuotaStorageByQuotaGuid(v_id UUID)
RETURNS SETOF quota_storage_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM  quota_storage_view
   WHERE quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetQuotaVdsGroupByVdsGroupGuid(v_vds_group_id UUID, v_id UUID)
RETURNS SETOF quota_vds_group_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT quota_vds_group_id,
   q_g_view.quota_id as quota_id,
   q_vds_view.vds_group_id as vds_group_id,
   q_vds_view.vds_group_name as vds_group_name,
   COALESCE(q_vds_view.virtual_cpu,q_g_view.virtual_cpu) as virtual_cpu,
   COALESCE(q_vds_view.virtual_cpu_usage, q_g_view.virtual_cpu_usage) as virtual_cpu_usage,
   COALESCE(q_vds_view.mem_size_mb,q_g_view.mem_size_mb) as mem_size_mb,
   COALESCE(q_vds_view.mem_size_mb_usage, q_g_view.mem_size_mb_usage) as mem_size_mb_usage
   FROM quota_global_view q_g_view LEFT OUTER JOIN
    quota_vds_group_view q_vds_view on q_g_view.quota_id = q_vds_view.quota_id
    AND (v_vds_group_id = q_vds_view.vds_group_id or v_vds_group_id IS NULL)
   WHERE q_g_view.quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetQuotaVdsGroupByQuotaGuid(v_id UUID)
RETURNS SETOF quota_vds_group_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT quota_vds_group_view.*
   FROM  quota_vds_group_view
   WHERE quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteQuotaByQuotaGuid(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   DELETE FROM quota
   WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteQuotaLimitationByQuotaGuid(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   DELETE FROM quota_limitation
   WHERE quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateQuotaMetaData(v_id UUID, v_storage_pool_id UUID, v_quota_name VARCHAR(50), v_description VARCHAR(500), v_threshold_vds_group_percentage INTEGER, v_threshold_storage_percentage INTEGER, v_grace_vds_group_percentage INTEGER, v_grace_storage_percentage INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
UPDATE quota
   SET storage_pool_id = v_storage_pool_id,
   quota_name = v_quota_name,
   description = v_description,
   _update_date = LOCALTIMESTAMP,
   threshold_vds_group_percentage = v_threshold_vds_group_percentage,
   threshold_storage_percentage = v_threshold_storage_percentage,
   grace_vds_group_percentage = v_grace_vds_group_percentage,
   grace_storage_percentage = v_grace_storage_percentage
   WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Returns all the Quota storages in the storage pool if v_storage_id is null, if v_storage_id is not null then a specific quota storage will be returned.
Create or replace FUNCTION GetQuotaByStoragePoolGuid(v_storage_pool_id UUID)
RETURNS SETOF quota_global_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM  quota_global_view
   WHERE (storage_pool_id = v_storage_pool_id or v_storage_pool_id IS NULL);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetQuotaByQuotaGuid(v_id UUID)
RETURNS SETOF quota_global_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM quota_global_view
   WHERE quota_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetQuotaByQuotaName(v_quota_name VARCHAR, v_storage_pool_id UUID)
RETURNS SETOF quota_global_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM quota_global_view
   WHERE quota_name = v_quota_name and storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;