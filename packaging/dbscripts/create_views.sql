-- noinspection SqlResolveForFile


-- ----------------------------------------------------------------------
-- Views
-- ----------------------------------------------------------------------
CREATE OR REPLACE VIEW domains_with_unregistered_entities_view AS

SELECT storage_domain_id
FROM unregistered_ovf_of_entities
UNION
SELECT storage_domain_id
FROM unregistered_disks;

CREATE OR REPLACE VIEW storage_domain_shared_status AS

SELECT storage_id,
    CASE WHEN COUNT(CASE status WHEN 3 THEN 1 END) > 0
        THEN CASE WHEN COUNT(DISTINCT status) > 1 THEN 3 ELSE 1 END
        ELSE 2
    END AS status
FROM storage_pool_iso_map
GROUP BY storage_id;

CREATE OR REPLACE VIEW vms_for_disk_view AS

SELECT array_agg(vm_name) AS array_vm_names,
    device_id,
    entity_type,
    array_agg(template_version_name) AS array_template_version_name
FROM vm_static
INNER JOIN vm_device
    ON vm_static.vm_guid = vm_device.vm_id
WHERE device = 'disk'
GROUP BY device_id,
    entity_type;


CREATE OR REPLACE VIEW step_progress AS
   SELECT s.step_id, s.job_id, COALESCE(s.progress, CASE s.status WHEN 'FINISHED' THEN 100 END, 0) as progress
   FROM step s;

CREATE OR REPLACE VIEW entity_step_progress AS
SELECT sse.entity_id, SUM(s.progress*sse.step_entity_weight/100) AS progress
FROM step_subject_entity sse
INNER JOIN step_progress s
    ON s.step_id = sse.step_id
INNER JOIN job j
    ON s.job_id = j.job_id
WHERE sse.step_entity_weight IS NOT NULL
    AND j.status = 'STARTED'
    AND s.progress IS NOT NULL
GROUP BY sse.entity_id;

CREATE OR REPLACE VIEW images_storage_domain_view AS

SELECT images.image_guid AS image_guid,
    storage_domain_static.storage_name AS storage_name,
    storage_pool_iso_map.storage_pool_id AS storage_pool_id,
    storage_domain_static.storage_type AS storage_type,
    images.creation_date AS creation_date,
    images.size AS size,
    images.it_guid AS it_guid,
    snap.description AS description,
    snap.creation_date AS snapshot_creation_date,
    images.ParentId AS ParentId,
    images.lastModified AS lastModified,
    snap.app_list AS app_list,
    image_storage_domain_map.storage_domain_id AS storage_id,
    images.vm_snapshot_id AS vm_snapshot_id,
    images.volume_type AS volume_type,
    images.volume_format AS volume_format,
    images.qcow_compat AS qcow_compat,
    images.imageStatus AS imageStatus,
    images.image_group_id AS image_group_id,
    images.active,
    images.volume_classification,
    vms_for_disk_view.entity_type AS entity_type,
    array_to_string(vms_for_disk_view.array_vm_names, ',') AS vm_names,
    COALESCE(array_upper(vms_for_disk_view.array_vm_names, 1), 0) AS number_of_vms,
    array_to_string(vms_for_disk_view.array_template_version_name, ',') AS template_version_names,
    base_disks.disk_id,
    base_disks.disk_alias AS disk_alias,
    base_disks.disk_description AS disk_description,
    base_disks.shareable AS shareable,
    base_disks.wipe_after_delete AS wipe_after_delete,
    base_disks.propagate_errors,
    base_disks.sgio AS sgio,
    base_disks.disk_content_type AS disk_content_type,
    base_disks.backup AS backup,
    base_disks.backup_mode AS backup_mode,
    image_storage_domain_map.quota_id AS quota_id,
    quota.quota_name AS quota_name,
    storage_pool.quota_enforcement_type,
    image_storage_domain_map.disk_profile_id AS disk_profile_id,
    disk_profiles.name AS disk_profile_name,
    disk_image_dynamic.actual_size AS actual_size,
    disk_image_dynamic.read_rate AS read_rate,
    disk_image_dynamic.read_ops AS read_ops,
    disk_image_dynamic.write_rate AS write_rate,
    disk_image_dynamic.write_ops AS write_ops,
    disk_image_dynamic.read_latency_seconds AS read_latency_seconds,
    disk_image_dynamic.write_latency_seconds AS write_latency_seconds,
    disk_image_dynamic.flush_latency_seconds AS flush_latency_seconds,
    base_disks.disk_storage_type AS disk_storage_type,
    base_disks.cinder_volume_type AS cinder_volume_type,
    image_transfers.phase AS image_transfer_phase,
    image_transfers.type AS image_transfer_type,
    image_transfers.bytes_sent AS image_transfer_bytes_sent,
    image_transfers.bytes_total AS image_transfer_bytes_total,
    entity_step_progress.progress AS progress

FROM images
LEFT JOIN disk_image_dynamic
    ON images.image_guid = disk_image_dynamic.image_id
LEFT JOIN base_disks
    ON images.image_group_id = base_disks.disk_id
LEFT JOIN vms_for_disk_view
    ON vms_for_disk_view.device_id = images.image_group_id
LEFT JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = images.image_guid
LEFT JOIN storage_domain_static
    ON image_storage_domain_map.storage_domain_id = storage_domain_static.id
LEFT JOIN snapshots snap
    ON images.vm_snapshot_id = snap.snapshot_id
LEFT JOIN quota
    ON image_storage_domain_map.quota_id = quota.id
LEFT JOIN disk_profiles
    ON image_storage_domain_map.disk_profile_id = disk_profiles.id
LEFT JOIN storage_pool_iso_map
    ON storage_pool_iso_map.storage_id = storage_domain_static.id
LEFT JOIN storage_pool
    ON storage_pool.id = storage_pool_iso_map.storage_pool_id
LEFT JOIN image_transfers
    ON images.image_group_id = image_transfers.disk_id
    AND image_transfers.phase NOT IN (9, 10)
LEFT JOIN entity_step_progress
    ON images.image_group_id = entity_step_progress.entity_id
WHERE images.image_guid != '00000000-0000-0000-0000-000000000000';


CREATE OR REPLACE VIEW storage_domain_file_repos AS

SELECT storage_domain_static.id AS storage_domain_id,
    storage_domain_static.storage_domain_type AS storage_domain_type,
    storage_pool_iso_map.storage_pool_id AS storage_pool_id,
    storage_pool_iso_map.status AS storage_domain_status,
    repo_file_meta_data.repo_image_id AS repo_image_id,
    repo_file_meta_data.size AS size,
    repo_file_meta_data.date_created AS date_created,
    repo_file_meta_data.last_refreshed AS last_refreshed,
    repo_file_meta_data.file_type AS file_type,
    vds_dynamic.status AS vds_status,
    storage_pool.status AS storage_pool_status
FROM storage_domain_static
INNER JOIN storage_pool_iso_map
    ON storage_domain_static.id = storage_pool_iso_map.storage_id
INNER JOIN storage_pool
    ON storage_pool.id = storage_pool_iso_map.storage_pool_id
INNER JOIN vds_dynamic
    ON vds_dynamic.vds_id = storage_pool.spm_vds_id
LEFT JOIN repo_file_meta_data
    ON storage_pool_iso_map.storage_id = repo_file_meta_data.repo_domain_id;

CREATE OR REPLACE VIEW storage_for_image_view AS

SELECT images.image_guid AS image_id,
    array_to_string(array_agg(storage_domain_static.id), ',') storage_id,
    array_to_string(array_agg(storage_domain_static.storage_type), ',') storage_type,
    array_to_string(array_agg(storage_domain_static.storage_name), ',') AS storage_name,
    array_to_string(array_agg(COALESCE(CAST(quota.id AS VARCHAR), '')), ',') AS quota_id,
    array_to_string(array_agg(COALESCE(quota.quota_name, '')), ',') AS quota_name,
    array_to_string(array_agg(COALESCE(CAST(disk_profiles.id AS VARCHAR), '')), ',') AS disk_profile_id,
    array_to_string(array_agg(COALESCE(disk_profiles.name, '')), ',') AS disk_profile_name
FROM images
LEFT JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = images.image_guid
LEFT JOIN storage_domain_static
    ON image_storage_domain_map.storage_domain_id = storage_domain_static.id
LEFT JOIN quota
    ON image_storage_domain_map.quota_id = quota.id
LEFT JOIN disk_profiles
    ON image_storage_domain_map.disk_profile_id = disk_profiles.id
GROUP BY images.image_guid;

CREATE OR REPLACE VIEW vm_images_view AS

SELECT storage_for_image_view.storage_id AS storage_id,
    storage_for_image_view.storage_name AS storage_name,
    storage_for_image_view.storage_type,
    images_storage_domain_view.storage_pool_id AS storage_pool_id,
    images_storage_domain_view.image_guid AS image_guid,
    images_storage_domain_view.creation_date AS creation_date,
    disk_image_dynamic.actual_size AS actual_size,
    disk_image_dynamic.read_rate AS read_rate,
    disk_image_dynamic.read_ops AS read_ops,
    disk_image_dynamic.read_latency_seconds AS read_latency_seconds,
    disk_image_dynamic.write_latency_seconds AS write_latency_seconds,
    disk_image_dynamic.flush_latency_seconds AS flush_latency_seconds,
    disk_image_dynamic.write_rate AS write_rate,
    disk_image_dynamic.write_ops AS write_ops,
    images_storage_domain_view.size AS size,
    images_storage_domain_view.it_guid AS it_guid,
    images_storage_domain_view.description AS description,
    images_storage_domain_view.ParentId AS ParentId,
    images_storage_domain_view.imageStatus AS imageStatus,
    images_storage_domain_view.lastModified AS lastModified,
    images_storage_domain_view.app_list AS app_list,
    images_storage_domain_view.vm_snapshot_id AS vm_snapshot_id,
    images_storage_domain_view.volume_type AS volume_type,
    images_storage_domain_view.image_group_id AS image_group_id,
    images_storage_domain_view.active AS active,
    images_storage_domain_view.volume_classification AS volume_classification,
    images_storage_domain_view.volume_format AS volume_format,
    images_storage_domain_view.qcow_compat AS qcow_compat,
    images_storage_domain_view.wipe_after_delete AS wipe_after_delete,
    images_storage_domain_view.propagate_errors AS propagate_errors,
    images_storage_domain_view.sgio AS sgio,
    images_storage_domain_view.disk_content_type AS disk_content_type,
    images_storage_domain_view.backup AS backup,
    images_storage_domain_view.backup_mode AS backup_mode,
    images_storage_domain_view.entity_type AS entity_type,
    images_storage_domain_view.number_of_vms AS number_of_vms,
    images_storage_domain_view.vm_names AS vm_names,
    images_storage_domain_view.template_version_names AS template_version_names,
    storage_for_image_view.quota_id AS quota_id,
    storage_for_image_view.quota_name AS quota_name,
    images_storage_domain_view.quota_enforcement_type,
    storage_for_image_view.disk_profile_id AS disk_profile_id,
    storage_for_image_view.disk_profile_name AS disk_profile_name,
    images_storage_domain_view.disk_id,
    images_storage_domain_view.disk_alias AS disk_alias,
    images_storage_domain_view.disk_description AS disk_description,
    images_storage_domain_view.shareable AS shareable,
    images_storage_domain_view.image_transfer_phase AS image_transfer_phase,
    images_storage_domain_view.image_transfer_type AS image_transfer_type,
    images_storage_domain_view.image_transfer_bytes_sent AS image_transfer_bytes_sent,
    images_storage_domain_view.image_transfer_bytes_total AS image_transfer_bytes_total,
    images_storage_domain_view.progress AS progress,
    images_storage_domain_view.disk_storage_type AS disk_storage_type,
    images_storage_domain_view.cinder_volume_type AS cinder_volume_type
FROM images_storage_domain_view
INNER JOIN disk_image_dynamic
    ON images_storage_domain_view.image_guid = disk_image_dynamic.image_id
INNER JOIN storage_for_image_view
    ON images_storage_domain_view.image_guid = storage_for_image_view.image_id
WHERE images_storage_domain_view.active = TRUE;

CREATE OR REPLACE VIEW all_disks_including_snapshots AS

SELECT storage_impl.*,
    bd.disk_id,
    -- Disk fields
    bd.wipe_after_delete,
    bd.propagate_errors,
    bd.disk_alias,
    bd.disk_description,
    bd.shareable,
    bd.sgio,
    bd.disk_storage_type,
    bd.cinder_volume_type,
    bd.disk_content_type,
    bd.backup,
    bd.backup_mode
FROM (
    SELECT storage_for_image_view.storage_id AS storage_id,
        -- Storage fields
        storage_for_image_view.storage_name AS storage_name,
        storage_for_image_view.storage_type AS storage_type,
        storage_pool_id,
        image_guid,
        -- Image fields
        creation_date,
        actual_size,
        read_rate,
        read_ops,
        write_rate,
        write_ops,
        read_latency_seconds,
        write_latency_seconds,
        flush_latency_seconds,
        size,
        it_guid,
        imageStatus,
        lastModified,
        volume_type,
        volume_format,
        qcow_compat,
        image_group_id,
        description,
        -- Snapshot fields
        ParentId,
        app_list,
        vm_snapshot_id,
        active,
        volume_classification,
        entity_type,
        number_of_vms,
        vm_names,
        template_version_names,
        storage_for_image_view.quota_id AS quota_id,
        -- Quota fields
        storage_for_image_view.quota_name AS quota_name,
        quota_enforcement_type,
        image_transfer_phase,
        image_transfer_type,
        image_transfer_bytes_sent,
        image_transfer_bytes_total,
        progress,
        storage_for_image_view.disk_profile_id AS disk_profile_id,
        -- disk profile fields
        storage_for_image_view.disk_profile_name AS disk_profile_name,
        NULL AS lun_id,
        -- LUN fields
        NULL AS physical_volume_id,
        NULL AS volume_group_id,
        NULL AS serial,
        NULL AS lun_mapping,
        NULL AS vendor_id,
        NULL AS product_id,
        NULL AS device_size,
        NULL AS discard_max_size
    FROM images_storage_domain_view
    INNER JOIN storage_for_image_view
        ON images_storage_domain_view.image_guid = storage_for_image_view.image_id
    GROUP BY storage_for_image_view.storage_id,
        storage_for_image_view.storage_name,
        storage_for_image_view.storage_type,
        storage_pool_id,
        image_guid,
        -- Image fields
        creation_date,
        actual_size,
        read_rate,
        read_ops,
        write_rate,
        write_ops,
        read_latency_seconds,
        write_latency_seconds,
        flush_latency_seconds,
        size,
        it_guid,
        imageStatus,
        lastModified,
        volume_type,
        volume_format,
        qcow_compat,
        image_group_id,
        description,
        -- Snapshot fields
        ParentId,
        app_list,
        vm_snapshot_id,
        active,
        volume_classification,
        entity_type,
        number_of_vms,
        vm_names,
        template_version_names,
        storage_for_image_view.quota_id,
        storage_for_image_view.quota_name,
        quota_enforcement_type,
        image_transfer_phase,
        image_transfer_type,
        image_transfer_bytes_sent,
        image_transfer_bytes_total,
        progress,
        storage_for_image_view.disk_profile_id,
        storage_for_image_view.disk_profile_name

    UNION ALL

    SELECT NULL AS storage_id,
        -- Storage domain fields
        NULL AS storage_name,
        NULL AS storage_type,
        NULL AS storage_pool_id,
        NULL AS image_guid,
        -- Image fields
        NULL AS creation_date,
        NULL AS actual_size,
        NULL AS read_rate,
        NULL AS read_ops,
        NULL AS write_rate,
        NULL AS write_ops,
        NULL AS read_latency_seconds,
        NULL AS write_latency_seconds,
        NULL AS flush_latency_seconds,
        NULL AS size,
        NULL AS it_guid,
        NULL AS imageStatus,
        NULL AS lastModified,
        NULL AS volume_type,
        NULL AS volume_format,
        NULL AS qcow_compat,
        dlm.disk_id AS image_group_id,
        NULL AS description,
        -- Snapshot fields
        NULL AS ParentId,
        NULL AS app_list,
        NULL AS vm_snapshot_id,
        NULL AS active,
        NULL AS volume_classification,
        vms_for_disk_view.entity_type,
        COALESCE(array_upper(vms_for_disk_view.array_vm_names, 1), 0) AS number_of_vms,
        array_to_string(vms_for_disk_view.array_vm_names, ',') AS vm_names,
        array_to_string(vms_for_disk_view.array_template_version_name, ',') AS template_version_names,
        NULL AS quota_id,
        -- Quota fields
        NULL AS quota_name,
        NULL AS quota_enforcement_type,
        NULL AS image_transfer_phase,
        NULL AS image_transfer_type,
        NULL AS image_transfer_bytes_sent,
        NULL AS image_transfer_bytes_total,
        NULL AS progress,
        NULL AS disk_profile_id,
        -- disk profile fields
        NULL AS disk_profile_name,
        l.lun_id,
        -- LUN fields
        l.physical_volume_id,
        l.volume_group_id,
        l.serial,
        l.lun_mapping,
        l.vendor_id,
        l.product_id,
        l.device_size,
        l.discard_max_size
    FROM disk_lun_map dlm
    INNER JOIN luns l
        ON l.lun_id = dlm.lun_id
    LEFT JOIN vms_for_disk_view
        ON vms_for_disk_view.device_id = dlm.disk_id
    ) AS storage_impl
INNER JOIN base_disks bd
    ON bd.disk_id = storage_impl.image_group_id;


CREATE OR REPLACE VIEW all_disks_including_snapshots_and_memory AS

SELECT storage_impl.*,
    bd.disk_id,
    -- Disk fields
    bd.wipe_after_delete,
    bd.propagate_errors,
    bd.disk_alias,
    bd.disk_description,
    bd.shareable,
    bd.sgio,
    bd.disk_storage_type,
    bd.cinder_volume_type,
    bd.disk_content_type,
    bd.backup,
    bd.backup_mode
FROM (
    SELECT storage_for_image_view.storage_id AS storage_id,
        -- Storage fields
        storage_for_image_view.storage_name AS storage_name,
        storage_for_image_view.storage_type AS storage_type,
        storage_pool_id,
        image_guid,
        -- Image fields
        creation_date,
        actual_size,
        read_rate,
        read_ops,
        write_rate,
        write_ops,
        read_latency_seconds,
        write_latency_seconds,
        flush_latency_seconds,
        size,
        it_guid,
        imageStatus,
        lastModified,
        volume_type,
        volume_format,
        qcow_compat,
        image_group_id,
        description,
        -- Snapshot fields
        ParentId,
        app_list,
        vm_snapshot_id,
        active,
        volume_classification,
        entity_type,
        number_of_vms,
        vm_names,
        template_version_names,
        storage_for_image_view.quota_id AS quota_id,
        -- Quota fields
        storage_for_image_view.quota_name AS quota_name,
        quota_enforcement_type,
        image_transfer_phase,
        image_transfer_type,
        image_transfer_bytes_sent,
        image_transfer_bytes_total,
        progress,
        storage_for_image_view.disk_profile_id AS disk_profile_id,
        -- disk profile fields
        storage_for_image_view.disk_profile_name AS disk_profile_name,
        NULL AS lun_id,
        -- LUN fields
        NULL AS physical_volume_id,
        NULL AS volume_group_id,
        NULL AS serial,
        NULL AS lun_mapping,
        NULL AS vendor_id,
        NULL AS product_id,
        NULL AS device_size,
        NULL AS discard_max_size
    FROM images_storage_domain_view
    INNER JOIN storage_for_image_view
        ON images_storage_domain_view.image_guid = storage_for_image_view.image_id
    GROUP BY storage_for_image_view.storage_id,
        storage_for_image_view.storage_name,
        storage_for_image_view.storage_type,
        storage_pool_id,
        image_guid,
        -- Image fields
        creation_date,
        actual_size,
        read_rate,
        read_ops,
        write_rate,
        write_ops,
        read_latency_seconds,
        write_latency_seconds,
        flush_latency_seconds,
        size,
        it_guid,
        imageStatus,
        lastModified,
        volume_type,
        volume_format,
        qcow_compat,
        image_group_id,
        description,
        -- Snapshot fields
        ParentId,
        app_list,
        vm_snapshot_id,
        active,
        volume_classification,
        entity_type,
        number_of_vms,
        vm_names,
        template_version_names,
        storage_for_image_view.quota_id,
        storage_for_image_view.quota_name,
        quota_enforcement_type,
        image_transfer_phase,
        image_transfer_type,
        image_transfer_bytes_sent,
        image_transfer_bytes_total,
        progress,
        storage_for_image_view.disk_profile_id,
        storage_for_image_view.disk_profile_name

    UNION ALL

    SELECT NULL AS storage_id,
        -- Storage domain fields
        NULL AS storage_name,
        NULL AS storage_type,
        NULL AS storage_pool_id,
        NULL AS image_guid,
        -- Image fields
        NULL AS creation_date,
        NULL AS actual_size,
        NULL AS read_rate,
        NULL AS read_ops,
        NULL AS write_rate,
        NULL AS write_ops,
        NULL AS read_latency_seconds,
        NULL AS write_latency_seconds,
        NULL AS flush_latency_seconds,
        NULL AS size,
        NULL AS it_guid,
        NULL AS imageStatus,
        NULL AS lastModified,
        NULL AS volume_type,
        NULL AS volume_format,
        NULL AS qcow_compat,
        dlm.disk_id AS image_group_id,
        NULL AS description,
        -- Snapshot fields
        NULL AS ParentId,
        NULL AS app_list,
        NULL AS vm_snapshot_id,
        NULL AS active,
        NULL AS volume_classification,
        vms_for_disk_view.entity_type,
        COALESCE(array_upper(vms_for_disk_view.array_vm_names, 1), 0) AS number_of_vms,
        array_to_string(vms_for_disk_view.array_vm_names, ',') AS vm_names,
        array_to_string(vms_for_disk_view.array_template_version_name, ',') AS template_version_names,
        NULL AS quota_id,
        -- Quota fields
        NULL AS quota_name,
        NULL AS quota_enforcement_type,
        NULL AS image_transfer_phase,
        NULL AS image_transfer_type,
        NULL AS image_transfer_bytes_sent,
        NULL AS image_transfer_bytes_total,
        NULL AS progress,
        NULL AS disk_profile_id,
        -- disk profile fields
        NULL AS disk_profile_name,
        l.lun_id,
        -- LUN fields
        l.physical_volume_id,
        l.volume_group_id,
        l.serial,
        l.lun_mapping,
        l.vendor_id,
        l.product_id,
        l.device_size,
        l.discard_max_size
    FROM disk_lun_map dlm
    INNER JOIN luns l
        ON l.lun_id = dlm.lun_id
    LEFT JOIN vms_for_disk_view
        ON vms_for_disk_view.device_id = dlm.disk_id
    ) AS storage_impl
INNER JOIN base_disks bd
    ON bd.disk_id = storage_impl.image_group_id;

CREATE OR REPLACE VIEW all_disks AS

SELECT *
FROM all_disks_including_snapshots_and_memory
WHERE active IS NULL
    OR active = TRUE;

CREATE OR REPLACE VIEW all_disks_for_vms AS

SELECT all_disks_including_snapshots_and_memory.*,
    vm_device.is_plugged,
    vm_device.logical_name,
    vm_device.vm_id
FROM all_disks_including_snapshots_and_memory
INNER JOIN vm_device
    ON vm_device.device_id = all_disks_including_snapshots_and_memory.image_group_id
WHERE (
        (
            vm_device.snapshot_id IS NULL
            AND all_disks_including_snapshots_and_memory.active IS NOT FALSE
            )
        OR vm_device.snapshot_id = all_disks_including_snapshots_and_memory.vm_snapshot_id
        );

CREATE OR REPLACE VIEW storage_domains_image_sizes AS
SELECT storage_domain_id,
    CAST(COALESCE(SUM(
        CASE WHEN active = TRUE
            AND(
                entity_type IS NULL
                OR entity_type <> 'TEMPLATE'
                )
            THEN size
        ELSE actual_size
        END), 0) * 0.000000000931322574615478515625 AS BIGINT)

    AS commited_disk_size,
    CAST(COALESCE(SUM(actual_size), 0)
    * 0.000000000931322574615478515625 AS BIGINT)
    AS actual_images_size
FROM images i
INNER JOIN image_storage_domain_map isdm
    ON i.image_guid = isdm.image_id
LEFT JOIN disk_image_dynamic din
    ON i.image_guid = din.image_id
LEFT JOIN vms_for_disk_view vfdv
    ON i.image_group_id = vfdv.device_id
GROUP BY storage_domain_id;

CREATE OR REPLACE VIEW vg_discard_support_view AS
SELECT volume_group_id,
    BOOL_AND(COALESCE(luns.discard_max_size, 0) > 0) AS supports_discard
FROM luns
WHERE volume_group_id <> ''
GROUP BY volume_group_id;


CREATE OR REPLACE VIEW hosted_engine_storage_domains_ids_view AS

SELECT image_storage_domain_map.storage_domain_id AS id
FROM vm_static
    JOIN vm_device
        ON vm_device.vm_id = vm_static.vm_guid
    JOIN images
        ON images.image_group_id = vm_device.device_id
    JOIN image_storage_domain_map
        ON image_storage_domain_map.image_id = images.image_guid
WHERE vm_static.origin IN (5,6)
    AND images.active;


CREATE OR REPLACE VIEW storage_domains AS

SELECT storage_domain_static.id AS id,
    storage_domain_static.storage AS storage,
    storage_domain_static.storage_name AS storage_name,
    storage_domain_static.storage_description AS storage_description,
    storage_domain_static.storage_comment AS storage_comment,
    storage_pool_iso_map.storage_pool_id AS storage_pool_id,
    storage_domain_dynamic.available_disk_size AS available_disk_size,
    storage_domain_dynamic.confirmed_available_disk_size AS confirmed_available_disk_size,
    storage_domain_dynamic.vdo_savings AS vdo_savings,
    storage_domain_dynamic.used_disk_size AS used_disk_size,
    storage_domains_image_sizes.commited_disk_size,
    storage_domains_image_sizes.actual_images_size,
    storage_pool_iso_map.status AS status,
    storage_pool.name AS storage_pool_name,
    storage_domain_static.storage_type AS storage_type,
    storage_domain_static.storage_domain_type AS storage_domain_type,
    storage_domain_static.storage_domain_format_type AS storage_domain_format_type,
    storage_domain_static.last_time_used_as_master AS last_time_used_as_master,
    storage_domain_static.wipe_after_delete AS wipe_after_delete,
    storage_domain_static.discard_after_delete AS discard_after_delete,
    storage_domain_static.first_metadata_device AS first_metadata_device,
    storage_domain_static.vg_metadata_device AS vg_metadata_device,
    storage_domain_static.backup AS backup,
    storage_domain_static.block_size AS block_size,
    COALESCE(storage_domain_shared_status.status, 0) AS storage_domain_shared_status,
    storage_domain_static.recoverable AS recoverable,
    domains_with_unregistered_entities_view.storage_domain_id IS NOT NULL AS contains_unregistered_entities,
    storage_domain_static.warning_low_space_indicator AS warning_low_space_indicator,
    storage_domain_static.critical_space_action_blocker AS critical_space_action_blocker,
    storage_domain_static.warning_low_confirmed_space_indicator AS warning_low_confirmed_space_indicator,
    storage_domain_dynamic.external_status AS external_status,
    vg_discard_support_view.supports_discard AS supports_discard,
    EXISTS (
        SELECT 1
        FROM hosted_engine_storage_domains_ids_view
        WHERE hosted_engine_storage_domains_ids_view.id = storage_domain_static.id
    ) AS is_hosted_engine_storage

FROM storage_domain_static
INNER JOIN storage_domain_dynamic
    ON storage_domain_static.id = storage_domain_dynamic.id
LEFT JOIN storage_pool_iso_map
    ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT JOIN storage_pool
    ON storage_pool_iso_map.storage_pool_id = storage_pool.id
LEFT JOIN domains_with_unregistered_entities_view
    ON domains_with_unregistered_entities_view.storage_domain_id = storage_domain_static.id
LEFT JOIN storage_domains_image_sizes
    ON storage_domains_image_sizes.storage_domain_id = storage_domain_static.id
LEFT JOIN storage_domain_shared_status
    ON storage_domain_shared_status.storage_id = storage_domain_static.id
LEFT JOIN vg_discard_support_view
    ON storage_domain_static.storage = vg_discard_support_view.volume_group_id;


CREATE OR REPLACE VIEW storage_domains_without_storage_pools AS

SELECT DISTINCT storage_domain_static.id AS id,
    storage_domain_static.storage AS storage,
    storage_domain_static.storage_name AS storage_name,
    storage_domain_static.storage_description AS storage_description,
    storage_domain_static.storage_comment AS storage_comment,
    storage_domain_static.storage_type AS storage_type,
    storage_domain_static.storage_domain_type AS storage_domain_type,
    storage_domain_static.storage_domain_format_type AS storage_domain_format_type,
    storage_domain_static.last_time_used_as_master AS last_time_used_as_master,
    storage_domain_static.wipe_after_delete AS wipe_after_delete,
    storage_domain_static.discard_after_delete AS discard_after_delete,
    storage_domain_static.first_metadata_device AS first_metadata_device,
    storage_domain_static.vg_metadata_device AS vg_metadata_device,
    storage_domain_static.backup AS backup,
    storage_domain_static.block_size as block_size,
    NULL AS storage_pool_id,
    NULL AS storage_pool_name,
    storage_domain_dynamic.available_disk_size AS available_disk_size,
    storage_domain_dynamic.confirmed_available_disk_size AS confirmed_available_disk_size,
    storage_domain_dynamic.vdo_savings AS vdo_savings,
    storage_domain_dynamic.used_disk_size AS used_disk_size,
    storage_domains_image_sizes.commited_disk_size,
    storage_domains_image_sizes.actual_images_size,
    NULL AS status,
    COALESCE(storage_domain_shared_status.status, 0) AS storage_domain_shared_status,
    storage_domain_static.recoverable AS recoverable,
    domains_with_unregistered_entities_view.storage_domain_id IS NOT NULL AS contains_unregistered_entities,
    storage_domain_static.warning_low_space_indicator AS warning_low_space_indicator,
    storage_domain_static.critical_space_action_blocker AS critical_space_action_blocker,
    storage_domain_static.warning_low_confirmed_space_indicator AS warning_low_confirmed_space_indicator,
    storage_domain_dynamic.external_status AS external_status,
    vg_discard_support_view.supports_discard AS supports_discard,
    EXISTS (
        SELECT 1
        FROM hosted_engine_storage_domains_ids_view
        WHERE hosted_engine_storage_domains_ids_view.id = storage_domain_static.id
    ) AS is_hosted_engine_storage

FROM storage_domain_static
INNER JOIN storage_domain_dynamic
    ON storage_domain_static.id = storage_domain_dynamic.id
LEFT JOIN storage_pool_iso_map
    ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT JOIN storage_domain_shared_status
    ON storage_domain_shared_status.storage_id = storage_domain_static.id
LEFT JOIN domains_with_unregistered_entities_view
    ON domains_with_unregistered_entities_view.storage_domain_id = storage_domain_static.id
LEFT JOIN storage_domains_image_sizes
    ON storage_domains_image_sizes.storage_domain_id = storage_domain_static.id
LEFT JOIN vg_discard_support_view
    ON storage_domain_static.storage = vg_discard_support_view.volume_group_id;

CREATE OR REPLACE VIEW storage_domains_for_search AS

SELECT storage_domain_static.id AS id,
    storage_domain_static.storage AS storage,
    storage_domain_static.storage_name AS storage_name,
    storage_domain_static.storage_description AS storage_description,
    storage_domain_static.storage_comment AS storage_comment,
    storage_domain_static.storage_type AS storage_type,
    storage_domain_static.storage_domain_type AS storage_domain_type,
    storage_domain_static.storage_domain_format_type AS storage_domain_format_type,
    storage_domain_static.last_time_used_as_master AS last_time_used_as_master,
    storage_domain_static.wipe_after_delete AS wipe_after_delete,
    storage_domain_static.discard_after_delete AS discard_after_delete,
    storage_domain_static.first_metadata_device AS first_metadata_device,
    storage_domain_static.vg_metadata_device AS vg_metadata_device,
    storage_domain_static.backup AS backup,
    CASE
        WHEN status_table.is_multi_domain
            THEN NULL
        WHEN status_table.status IS NULL
            THEN 2 -- in case domain is unattached
        ELSE status_table.status
        END AS status,
    storage_domain_static.block_size AS block_size,
    status_table.storage_pool_ids [ 1 ] AS storage_pool_id,
    status_table.pool_names AS storage_pool_name,
    storage_domain_dynamic.available_disk_size AS available_disk_size,
    storage_domain_dynamic.confirmed_available_disk_size AS confirmed_available_disk_size,
    storage_domain_dynamic.vdo_savings AS vdo_savings,
    storage_domain_dynamic.used_disk_size AS used_disk_size,
    storage_domain_dynamic.available_disk_size + storage_domain_dynamic.used_disk_size as total_disk_size,
    storage_domains_image_sizes.commited_disk_size,
    storage_domains_image_sizes.actual_images_size,
    COALESCE(storage_domain_shared_status.status, 0) AS storage_domain_shared_status,
    storage_domain_static.recoverable AS recoverable,
    domains_with_unregistered_entities_view.storage_domain_id IS NOT NULL AS contains_unregistered_entities,
    storage_domain_static.warning_low_space_indicator AS warning_low_space_indicator,
    storage_domain_static.critical_space_action_blocker AS critical_space_action_blocker,
    storage_domain_static.warning_low_confirmed_space_indicator AS warning_low_confirmed_space_indicator,
    storage_domain_dynamic.external_status AS external_status,
    vg_discard_support_view.supports_discard AS supports_discard,
    EXISTS (
        SELECT 1
        FROM hosted_engine_storage_domains_ids_view
        WHERE hosted_engine_storage_domains_ids_view.id = storage_domain_static.id
    ) AS is_hosted_engine_storage

FROM storage_domain_static
INNER JOIN storage_domain_dynamic
    ON storage_domain_static.id = storage_domain_dynamic.id
LEFT JOIN (
    SELECT storage_id,
        COUNT(storage_id) > 1 AS is_multi_domain,
        MAX(storage_pool_iso_map.status) AS status,
        array_to_string(array_agg(storage_pool.name), ',') AS pool_names,
        CASE
            WHEN COUNT(DISTINCT storage_pool.id) = 1
                THEN array_agg(storage_pool.id)
            ELSE NULL
            END AS storage_pool_ids
    FROM storage_pool_iso_map
    INNER JOIN storage_pool
        ON storage_pool_iso_map.storage_pool_id = storage_pool.id
    GROUP BY storage_id
    ) AS status_table
    ON storage_domain_static.id = status_table.storage_id
LEFT JOIN storage_domain_shared_status
    ON storage_domain_shared_status.storage_id = storage_domain_static.id
LEFT JOIN domains_with_unregistered_entities_view
    ON domains_with_unregistered_entities_view.storage_domain_id = storage_domain_static.id
LEFT JOIN storage_domains_image_sizes
    ON storage_domains_image_sizes.storage_domain_id = storage_domain_static.id
LEFT JOIN vg_discard_support_view
    ON storage_domain_static.storage = vg_discard_support_view.volume_group_id;

CREATE OR REPLACE VIEW luns_view AS

SELECT luns.*,
    storage_domain_static.id AS storage_id,
    storage_domain_static.storage_name AS storage_name,
    disk_lun_map.disk_id AS disk_id,
    all_disks.disk_alias AS disk_alias
FROM luns
LEFT JOIN storage_domain_static
    ON luns.volume_group_id = storage_domain_static.storage
LEFT JOIN disk_lun_map
    ON luns.lun_id = disk_lun_map.lun_id
LEFT JOIN all_disks
    ON disk_lun_map.disk_id = all_disks.disk_id;

CREATE OR REPLACE VIEW vm_templates_based_view AS

SELECT vm_templates.vm_guid AS vmt_guid,
    vm_templates.vm_name AS name,
    vm_templates.mem_size_mb AS mem_size_mb,
    vm_templates.max_memory_size_mb AS max_memory_size_mb,
    vm_templates.num_of_io_threads AS num_of_io_threads,
    vm_templates.os AS os,
    vm_templates.creation_date AS creation_date,
    vm_templates.child_count AS child_count,
    vm_templates.num_of_sockets AS num_of_sockets,
    vm_templates.cpu_per_socket AS cpu_per_socket,
    vm_templates.threads_per_cpu AS threads_per_cpu,
    vm_templates.fn_get_num_of_vcpus AS num_of_cpus, -- use of function as an attribute
    vm_templates.description AS description,
    vm_templates.free_text_comment AS free_text_comment,
    vm_templates.cluster_id AS cluster_id,
    vm_templates.num_of_monitors AS num_of_monitors,
    vm_templates.allow_console_reconnect AS allow_console_reconnect,
    vm_templates.template_status AS status,
    vm_templates.usb_policy AS usb_policy,
    vm_templates.time_zone AS time_zone,
    cluster.name AS cluster_name,
    cluster.compatibility_version AS cluster_compatibility_version,
    cluster.trusted_service AS trusted_service,
    cluster.bios_type AS cluster_bios_type,
    vm_templates.vm_type AS vm_type,
    vm_templates.nice_level AS nice_level,
    vm_templates.cpu_shares AS cpu_shares,
    storage_pool.id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    storage_pool.quota_enforcement_type AS quota_enforcement_type,
    vm_templates.default_boot_sequence AS default_boot_sequence,
    vm_templates.default_display_type AS default_display_type,
    vm_templates.priority AS priority,
    vm_templates.auto_startup AS auto_startup,
    vm_templates.lease_sd_id AS lease_sd_id,
    vm_templates.is_stateless AS is_stateless,
    vm_templates.is_smartcard_enabled AS is_smartcard_enabled,
    vm_templates.is_delete_protected AS is_delete_protected,
    vm_templates.sso_method AS sso_method,
    vm_templates.iso_path AS iso_path,
    vm_templates.origin AS origin,
    vm_templates.initrd_url AS initrd_url,
    vm_templates.kernel_url AS kernel_url,
    vm_templates.kernel_params AS kernel_params,
    vm_templates.quota_id AS quota_id,
    quota.quota_name AS quota_name,
    vm_templates.db_generation AS db_generation,
    vm_templates.host_cpu_flags AS host_cpu_flags,
    vm_templates.migration_support,
    fn_get_dedicated_hosts_ids_by_vm_id(vm_templates.vm_guid) AS dedicated_vm_for_vds,
    vm_templates.is_disabled,
    vm_templates.tunnel_migration,
    vm_templates.vnc_keyboard_layout AS vnc_keyboard_layout,
    vm_templates.min_allocated_mem AS min_allocated_mem,
    vm_templates.is_run_and_pause AS is_run_and_pause,
    vm_templates.created_by_user_id AS created_by_user_id,
    vm_templates.entity_type,
    vm_templates.migration_downtime AS migration_downtime,
    cluster.architecture AS architecture,
    vm_templates.template_version_number AS template_version_number,
    vm_templates.vmt_guid AS base_template_id,
    vm_templates.template_version_name AS template_version_name,
    vm_templates.serial_number_policy AS serial_number_policy,
    vm_templates.custom_serial_number AS custom_serial_number,
    vm_templates.is_boot_menu_enabled AS is_boot_menu_enabled,
    vm_templates.is_spice_file_transfer_enabled AS is_spice_file_transfer_enabled,
    vm_templates.is_spice_copy_paste_enabled AS is_spice_copy_paste_enabled,
    vm_templates.cpu_profile_id AS cpu_profile_id,
    vm_templates.is_auto_converge AS is_auto_converge,
    vm_templates.is_migrate_compressed AS is_migrate_compressed,
    vm_templates.is_migrate_encrypted AS is_migrate_encrypted,
    vm_templates.predefined_properties AS predefined_properties,
    vm_templates.userdefined_properties AS userdefined_properties,
    vm_templates.custom_emulated_machine AS custom_emulated_machine,
    vm_templates.bios_type AS bios_type,
    vm_templates.custom_cpu_name AS custom_cpu_name,
    vm_templates.small_icon_id AS small_icon_id,
    vm_templates.large_icon_id AS large_icon_id,
    vm_templates.migration_policy_id as migration_policy_id,
    vm_templates.console_disconnect_action AS console_disconnect_action,
    vm_templates.resume_behavior AS resume_behavior,
    vm_templates.custom_compatibility_version as custom_compatibility_version,
    vm_templates.multi_queues_enabled AS multi_queues_enabled,
    vm_templates.virtio_scsi_multi_queues AS virtio_scsi_multi_queues,
    vm_templates.use_tsc_frequency AS use_tsc_frequency,
    vm_templates.is_template_sealed AS is_template_sealed,
    vm_templates.cpu_pinning AS cpu_pinning,
    vm_templates.balloon_enabled AS balloon_enabled,
    vm_templates.console_disconnect_action_delay AS console_disconnect_action_delay,
    vm_templates.cpu_pinning_policy AS cpu_pinning_policy
FROM vm_static AS vm_templates
LEFT JOIN cluster
    ON vm_templates.cluster_id = cluster.cluster_id
LEFT JOIN storage_pool
    ON storage_pool.id = cluster.storage_pool_id
LEFT JOIN quota
    ON vm_templates.quota_id = quota.id
WHERE entity_type = 'TEMPLATE'
    OR entity_type = 'INSTANCE_TYPE'
    OR entity_type = 'IMAGE_TYPE';

CREATE OR REPLACE VIEW vm_templates_based_with_plug_info AS

SELECT vm_templates_based_view.*,
    image_guid,
    image_group_id,
    is_plugged
FROM vm_templates_based_view
INNER JOIN vm_device vd
    ON vd.vm_id = vm_templates_based_view.vmt_guid
INNER JOIN images
    ON images.image_group_id = vd.device_id
        AND images.active = TRUE;

CREATE OR REPLACE VIEW vm_templates_based_storage_domain AS

SELECT vm_templates.vm_guid AS vmt_guid,
    vm_templates.vm_name AS name,
    vm_templates.mem_size_mb,
    vm_templates.max_memory_size_mb,
    vm_templates.num_of_io_threads,
    vm_templates.os,
    vm_templates.creation_date,
    vm_templates.child_count,
    vm_templates.num_of_sockets,
    vm_templates.cpu_per_socket,
    vm_templates.threads_per_cpu,
    vm_templates.fn_get_num_of_vcpus AS num_of_cpus, -- use of function as an attribute
    vm_templates.description,
    vm_templates.free_text_comment,
    vm_templates.cluster_id,
    vm_templates.num_of_monitors,
    vm_templates.allow_console_reconnect,
    vm_templates.template_status AS status,
    vm_templates.usb_policy,
    vm_templates.time_zone,
    cluster.name AS cluster_name,
    vm_templates.vm_type,
    vm_templates.nice_level,
    vm_templates.cpu_shares,
    storage_pool.id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    vm_templates.default_boot_sequence,
    vm_templates.default_display_type,
    vm_templates.priority,
    vm_templates.auto_startup,
    vm_templates.lease_sd_id,
    vm_templates.is_stateless,
    vm_templates.iso_path,
    vm_templates.origin,
    vm_templates.initrd_url,
    vm_templates.kernel_url,
    vm_templates.kernel_params,
    image_storage_domain_map.storage_domain_id AS storage_id,
    quota.quota_name AS quota_name,
    vm_templates.is_disabled,
    vm_templates.min_allocated_mem,
    vm_templates.is_run_and_pause,
    vm_templates.created_by_user_id,
    vm_templates.migration_downtime,
    vm_templates.entity_type,
    cluster.architecture,
    vm_templates.template_version_number AS template_version_number,
    vm_templates.vmt_guid AS base_template_id,
    vm_templates.template_version_name AS template_version_name,
    vm_templates.serial_number_policy AS serial_number_policy,
    vm_templates.custom_serial_number AS custom_serial_number,
    vm_templates.is_boot_menu_enabled AS is_boot_menu_enabled,
    vm_templates.is_spice_file_transfer_enabled AS is_spice_file_transfer_enabled,
    vm_templates.is_spice_copy_paste_enabled AS is_spice_copy_paste_enabled,
    vm_templates.cpu_profile_id AS cpu_profile_id,
    vm_templates.host_cpu_flags AS host_cpu_flags,
    vm_templates.is_auto_converge AS is_auto_converge,
    vm_templates.is_migrate_compressed AS is_migrate_compressed,
    vm_templates.is_migrate_encrypted AS is_migrate_encrypted,
    vm_templates.predefined_properties AS predefined_properties,
    vm_templates.userdefined_properties AS userdefined_properties,
    vm_templates.is_template_sealed AS is_template_sealed,
    vm_templates.console_disconnect_action_delay AS console_disconnect_action_delay
FROM vm_static AS vm_templates
LEFT JOIN cluster
    ON vm_templates.cluster_id = cluster.cluster_id
LEFT JOIN storage_pool
    ON storage_pool.id = cluster.storage_pool_id
INNER JOIN vm_device
    ON vm_device.vm_id = vm_templates.vm_guid
LEFT JOIN images
    ON images.image_group_id = vm_device.device_id
LEFT JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = images.image_guid
LEFT JOIN quota quota
    ON quota.id = vm_templates.quota_id
WHERE entity_type = 'TEMPLATE'
    OR entity_type = 'INSTANCE_TYPE'
    OR entity_type = 'IMAGE_TYPE'

UNION

SELECT vm_templates_1.vm_guid AS vmt_guid,
    vm_templates_1.vm_name AS name,
    vm_templates_1.mem_size_mb,
    vm_templates_1.max_memory_size_mb,
    vm_templates_1.num_of_io_threads,
    vm_templates_1.os,
    vm_templates_1.creation_date,
    vm_templates_1.child_count,
    vm_templates_1.num_of_sockets,
    vm_templates_1.cpu_per_socket,
    vm_templates_1.threads_per_cpu,
    vm_templates_1.fn_get_num_of_vcpus AS num_of_cpus, -- use of function as an attribute
    vm_templates_1.description,
    vm_templates_1.free_text_comment,
    vm_templates_1.cluster_id,
    vm_templates_1.num_of_monitors,
    vm_templates_1.allow_console_reconnect,
    vm_templates_1.template_status AS status,
    vm_templates_1.usb_policy,
    vm_templates_1.time_zone,
    cluster_1.name AS cluster_name,
    vm_templates_1.vm_type,
    vm_templates_1.nice_level,
    vm_templates_1.cpu_shares,
    storage_pool_1.id AS storage_pool_id,
    storage_pool_1.name AS storage_pool_name,
    vm_templates_1.default_boot_sequence,
    vm_templates_1.default_display_type,
    vm_templates_1.priority,
    vm_templates_1.auto_startup,
    vm_templates_1.lease_sd_id,
    vm_templates_1.is_stateless,
    vm_templates_1.iso_path,
    vm_templates_1.origin,
    vm_templates_1.initrd_url,
    vm_templates_1.kernel_url,
    vm_templates_1.kernel_params,
    image_storage_domain_map.storage_domain_id AS storage_id,
    quota.quota_name AS quota_name,
    vm_templates_1.is_disabled,
    vm_templates_1.min_allocated_mem,
    vm_templates_1.is_run_and_pause,
    vm_templates_1.created_by_user_id,
    vm_templates_1.migration_downtime,
    vm_templates_1.entity_type,
    cluster_1.architecture,
    vm_templates_1.template_version_number AS template_version_number,
    vm_templates_1.vmt_guid AS base_template_id,
    vm_templates_1.template_version_name AS template_version_name,
    vm_templates_1.serial_number_policy AS serial_number_policy,
    vm_templates_1.custom_serial_number AS custom_serial_number,
    vm_templates_1.is_boot_menu_enabled AS is_boot_menu_enabled,
    vm_templates_1.is_spice_file_transfer_enabled AS is_spice_file_transfer_enabled,
    vm_templates_1.is_spice_copy_paste_enabled AS is_spice_copy_paste_enabled,
    vm_templates_1.cpu_profile_id AS cpu_profile_id,
    vm_templates_1.host_cpu_flags AS host_cpu_flags,
    vm_templates_1.is_auto_converge AS is_auto_converge,
    vm_templates_1.is_migrate_compressed AS is_migrate_compressed,
    vm_templates_1.is_migrate_encrypted AS is_migrate_encrypted,
    vm_templates_1.predefined_properties AS predefined_properties,
    vm_templates_1.userdefined_properties AS userdefined_properties,
    vm_templates_1.is_template_sealed AS is_template_sealed,
    vm_templates_1.console_disconnect_action_delay AS console_disconnect_action_delay
FROM vm_static AS vm_templates_1
LEFT JOIN cluster AS cluster_1
    ON vm_templates_1.cluster_id = cluster_1.cluster_id
LEFT JOIN storage_pool AS storage_pool_1
    ON storage_pool_1.id = cluster_1.storage_pool_id
INNER JOIN vm_device AS vm_device_1
    ON vm_device_1.vm_id = vm_templates_1.vm_guid
INNER JOIN images AS images_1
    ON images_1.image_group_id = vm_device_1.device_id
INNER JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = images_1.image_guid
LEFT JOIN quota quota
    ON quota.id = vm_templates_1.quota_id
WHERE entity_type = 'TEMPLATE'
    OR entity_type = 'INSTANCE_TYPE'
    OR entity_type = 'IMAGE_TYPE';

CREATE OR REPLACE VIEW vm_templates_view AS

SELECT *
FROM vm_templates_based_view
WHERE entity_type = 'TEMPLATE';

CREATE OR REPLACE VIEW vm_templates_storage_domain AS

SELECT *
FROM vm_templates_based_storage_domain
WHERE entity_type = 'TEMPLATE';

CREATE OR REPLACE VIEW instance_types_view AS

SELECT *
FROM vm_templates_based_view
WHERE entity_type = 'INSTANCE_TYPE';

CREATE OR REPLACE VIEW instance_types_storage_domain AS

SELECT *
FROM vm_templates_based_storage_domain
WHERE entity_type = 'INSTANCE_TYPE';

CREATE OR REPLACE VIEW image_types_view AS

SELECT *
FROM vm_templates_based_view
WHERE entity_type = 'IMAGE_TYPE';

CREATE OR REPLACE VIEW image_types_storage_domain AS

SELECT *
FROM vm_templates_based_storage_domain
WHERE entity_type = 'IMAGE_TYPE';

CREATE OR REPLACE VIEW vm_pool_map_view AS

SELECT vm_pool_map.vm_guid AS vm_guid,
    vm_pool_map.vm_pool_id AS vm_pool_id,
    vm_pools.vm_pool_name AS vm_pool_name,
    vm_pools.spice_proxy AS vm_pool_spice_proxy
FROM vm_pool_map
INNER JOIN vm_pools
    ON vm_pool_map.vm_pool_id = vm_pools.vm_pool_id;

CREATE OR REPLACE VIEW tags_vm_pool_map_view AS

SELECT tags.tag_id AS tag_id,
    tags.tag_name AS tag_name,
    tags.parent_id AS parent_id,
    tags.readonly AS readonly,
    tags.type AS type,
    tags_vm_pool_map.vm_pool_id AS vm_pool_id
FROM tags
INNER JOIN tags_vm_pool_map
    ON tags.tag_id = tags_vm_pool_map.tag_id;

CREATE OR REPLACE VIEW tags_vm_map_view AS

SELECT tags.tag_id AS tag_id,
    tags.tag_name AS tag_name,
    tags.parent_id AS parent_id,
    tags.readonly AS readonly,
    tags.type AS type,
    tags_vm_map.vm_id AS vm_id
FROM tags
INNER JOIN tags_vm_map
    ON tags.tag_id = tags_vm_map.tag_id;

CREATE OR REPLACE VIEW tags_vds_map_view AS

SELECT tags.tag_id AS tag_id,
    tags.tag_name AS tag_name,
    tags.parent_id AS parent_id,
    tags.readonly AS readonly,
    tags.type AS type,
    tags_vds_map.vds_id AS vds_id
FROM tags
INNER JOIN tags_vds_map
    ON tags.tag_id = tags_vds_map.tag_id;

CREATE OR REPLACE VIEW tags_user_map_view AS

SELECT tags.tag_id AS tag_id,
    tags.tag_name AS tag_name,
    tags.parent_id AS parent_id,
    tags.readonly AS readonly,
    tags.type AS type,
    tags_user_map.user_id AS user_id
FROM tags
INNER JOIN tags_user_map
    ON tags.tag_id = tags_user_map.tag_id;

CREATE OR REPLACE VIEW tags_user_group_map_view AS

SELECT tags.tag_id AS tag_id,
    tags.tag_name AS tag_name,
    tags.parent_id AS parent_id,
    tags.readonly AS readonly,
    tags.type AS type,
    tags_user_group_map.group_id AS group_id
FROM tags_user_group_map
INNER JOIN tags
    ON tags_user_group_map.tag_id = tags.tag_id;

CREATE OR REPLACE VIEW vm_static_view AS

SELECT vm_static.*,
    fn_get_dedicated_hosts_ids_by_vm_id(vm_static.vm_guid) AS dedicated_vm_for_vds
FROM vm_static;

CREATE OR REPLACE VIEW vms AS

SELECT vm_static.vm_name AS vm_name,
    vm_static.mem_size_mb AS mem_size_mb,
    vm_static.max_memory_size_mb AS max_memory_size_mb,
    vm_static.num_of_io_threads AS num_of_io_threads,
    vm_static.nice_level AS nice_level,
    vm_static.cpu_shares AS cpu_shares,
    vm_static.vmt_guid AS vmt_guid,
    vm_static.os AS os,
    vm_static.description AS description,
    vm_static.free_text_comment AS free_text_comment,
    vm_static.cluster_id AS cluster_id,
    vm_static.creation_date AS creation_date,
    vm_static.auto_startup AS auto_startup,
    vm_static.lease_sd_id AS lease_sd_id,
    vm_dynamic.lease_info AS lease_info,
    vm_static.is_stateless AS is_stateless,
    vm_static.is_smartcard_enabled AS is_smartcard_enabled,
    vm_static.is_delete_protected AS is_delete_protected,
    vm_static.sso_method AS sso_method,
    fn_get_dedicated_hosts_ids_by_vm_id(vm_static.vm_guid) AS dedicated_vm_for_vds,
    vm_static.default_boot_sequence AS default_boot_sequence,
    vm_static.vm_type AS vm_type,
    vm_pool_map_view.vm_pool_spice_proxy AS vm_pool_spice_proxy,
    cluster.name AS cluster_name,
    cluster.transparent_hugepages AS transparent_hugepages,
    cluster.trusted_service AS trusted_service,
    storage_pool.id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    cluster.spice_proxy AS cluster_spice_proxy,
    vm_templates.vm_name AS vmt_name,
    vm_dynamic.status AS status,
    vm_dynamic.vm_ip AS vm_ip,
    fn_get_comparable_ip_list(vm_ip) AS vm_ip_inet_array,
    vm_dynamic.vm_host AS vm_host,
    vm_dynamic.last_start_time AS last_start_time,
    vm_dynamic.boot_time AS boot_time,
    vm_dynamic.downtime AS downtime,
    vm_dynamic.guest_cur_user_name AS guest_cur_user_name,
    vm_dynamic.console_cur_user_name AS console_cur_user_name,
    vm_dynamic.runtime_name AS runtime_name,
    vm_dynamic.guest_os AS guest_os,
    vm_dynamic.console_user_id AS console_user_id,
    vm_dynamic.guest_agent_nics_hash AS guest_agent_nics_hash,
    vm_dynamic.run_on_vds AS run_on_vds,
    vm_dynamic.migrating_to_vds AS migrating_to_vds,
    vm_dynamic.app_list AS app_list,
    vm_pool_map_view.vm_pool_name AS vm_pool_name,
    vm_pool_map_view.vm_pool_id AS vm_pool_id,
    vm_static.vm_guid AS vm_guid,
    vm_static.num_of_monitors AS num_of_monitors,
    vm_static.allow_console_reconnect AS allow_console_reconnect,
    vm_static.is_initialized AS is_initialized,
    vm_static.num_of_sockets AS num_of_sockets,
    vm_static.cpu_per_socket AS cpu_per_socket,
    vm_static.threads_per_cpu AS threads_per_cpu,
    vm_static.usb_policy AS usb_policy,
    vm_dynamic.acpi_enable AS acpi_enable,
    vm_dynamic.session AS SESSION,
    vm_static.fn_get_num_of_vcpus AS num_of_cpus, -- use of function as an attribute
    vm_static.quota_id AS quota_id,
    quota.quota_name AS quota_name,
    storage_pool.quota_enforcement_type AS quota_enforcement_type,
    vm_dynamic.boot_sequence AS boot_sequence,
    vm_dynamic.utc_diff AS utc_diff,
    vm_dynamic.client_ip AS client_ip,
    vm_dynamic.guest_requested_memory AS guest_requested_memory,
    vm_static.time_zone AS time_zone,
    vm_statistics.cpu_user AS cpu_user,
    vm_statistics.cpu_sys AS cpu_sys,
    vm_statistics.elapsed_time AS elapsed_time,
    vm_statistics.usage_network_percent AS usage_network_percent,
    vm_statistics.disks_usage AS disks_usage,
    vm_statistics.usage_mem_percent AS usage_mem_percent,
    vm_statistics.usage_cpu_percent AS usage_cpu_percent,
    vds_static.vds_name AS run_on_vds_name,
    cluster.cpu_name AS cluster_cpu_name,
    cluster.cpu_flags AS cluster_cpu_flags,
    cluster.cpu_verb AS cluster_cpu_verb,
    vm_static.default_display_type AS default_display_type,
    vm_static.priority AS priority,
    vm_static.iso_path AS iso_path,
    vm_static.origin AS origin,
    cluster.compatibility_version AS cluster_compatibility_version,
    vm_static.initrd_url AS initrd_url,
    vm_static.kernel_url AS kernel_url,
    vm_static.kernel_params AS kernel_params,
    vm_dynamic.pause_status AS pause_status,
    vm_dynamic.exit_message AS exit_message,
    vm_dynamic.exit_status AS exit_status,
    vm_static.migration_support AS migration_support,
    vm_static.predefined_properties AS predefined_properties,
    vm_static.userdefined_properties AS userdefined_properties,
    vm_static.min_allocated_mem AS min_allocated_mem,
    vm_dynamic.HASH AS HASH,
    vm_static.cpu_pinning AS cpu_pinning,
    vm_static.db_generation AS db_generation,
    vm_static.host_cpu_flags AS host_cpu_flags,
    vm_static.tunnel_migration AS tunnel_migration,
    vm_static.vnc_keyboard_layout AS vnc_keyboard_layout,
    vm_static.is_run_and_pause AS is_run_and_pause,
    vm_static.created_by_user_id AS created_by_user_id,
    vm_dynamic.last_watchdog_event AS last_watchdog_event,
    vm_dynamic.last_watchdog_action AS last_watchdog_action,
    vm_dynamic.is_run_once AS is_run_once,
    vm_dynamic.volatile_run AS volatile_run,
    vm_dynamic.vm_fqdn AS vm_fqdn,
    vm_dynamic.cpu_name AS cpu_name,
    vm_dynamic.emulated_machine AS emulated_machine,
    vm_dynamic.current_cd AS current_cd,
    vm_dynamic.reason AS reason,
    vm_dynamic.exit_reason AS exit_reason,
    vm_static.instance_type_id AS instance_type_id,
    vm_static.image_type_id AS image_type_id,
    cluster.architecture AS architecture,
    vm_static.original_template_id AS original_template_id,
    vm_static.original_template_name AS original_template_name,
    vm_dynamic.last_stop_time AS last_stop_time,
    vm_static.migration_downtime AS migration_downtime,
    vm_static.template_version_number AS template_version_number,
    vm_static.serial_number_policy AS serial_number_policy,
    vm_static.custom_serial_number AS custom_serial_number,
    vm_static.is_boot_menu_enabled AS is_boot_menu_enabled,
    vm_dynamic.guest_cpu_count AS guest_cpu_count,
    vm_snapshots.next_run_config_exists AS next_run_config_exists,
    vm_snapshots.is_previewing_snapshot AS is_previewing_snapshot,
    vm_snapshots.changed_fields AS changed_fields,
    vm_static.is_spice_file_transfer_enabled AS is_spice_file_transfer_enabled,
    vm_static.is_spice_copy_paste_enabled AS is_spice_copy_paste_enabled,
    vm_static.cpu_profile_id AS cpu_profile_id,
    vm_static.is_auto_converge AS is_auto_converge,
    vm_static.is_migrate_compressed AS is_migrate_compressed,
    vm_static.is_migrate_encrypted AS is_migrate_encrypted,
    vm_static.custom_emulated_machine AS custom_emulated_machine,
    vm_static.bios_type AS bios_type,
    cluster.bios_type AS cluster_bios_type,
    vm_static.custom_cpu_name AS custom_cpu_name,
    vm_dynamic.spice_port AS spice_port,
    vm_dynamic.spice_tls_port AS spice_tls_port,
    vm_dynamic.spice_ip AS spice_ip,
    vm_dynamic.vnc_port AS vnc_port,
    vm_dynamic.vnc_ip AS vnc_ip,
    vm_dynamic.ovirt_guest_agent_status AS ovirt_guest_agent_status,
    vm_dynamic.qemu_guest_agent_status AS qemu_guest_agent_status,
    vm_statistics.guest_mem_buffered AS guest_mem_buffered,
    vm_statistics.guest_mem_cached AS guest_mem_cached,
    vm_static.small_icon_id AS small_icon_id,
    vm_static.large_icon_id AS large_icon_id,
    vm_static.migration_policy_id as migration_policy_id,
    vm_static.provider_id AS provider_id,
    vm_static.console_disconnect_action AS console_disconnect_action,
    vm_static.resume_behavior AS resume_behavior,
    vm_dynamic.guest_timezone_offset AS guest_timezone_offset,
    vm_dynamic.guest_timezone_name AS guest_timezone_name,
    vm_dynamic.guestos_arch AS guestos_arch,
    vm_dynamic.guestos_codename AS guestos_codename,
    vm_dynamic.guestos_distribution AS guestos_distribution,
    vm_dynamic.guestos_kernel_version AS guestos_kernel_version,
    vm_dynamic.guestos_type AS guestos_type,
    vm_dynamic.guestos_version AS guestos_version,
    vm_static.custom_compatibility_version as custom_compatibility_version,
    vm_dynamic.guest_containers AS guest_containers,
    image_details.has_illegal_images,
    vm_static.multi_queues_enabled AS multi_queues_enabled,
    vm_static.virtio_scsi_multi_queues AS virtio_scsi_multi_queues,
    vm_static.use_tsc_frequency AS use_tsc_frequency,
    vm_static.namespace AS namespace,
    vm_static.balloon_enabled AS balloon_enabled,
    vm_static.console_disconnect_action_delay AS console_disconnect_action_delay,
    vm_static.cpu_pinning_policy AS cpu_pinning_policy,
    vm_dynamic.current_cpu_pinning AS current_cpu_pinning,
    vm_dynamic.current_sockets AS current_sockets,
    vm_dynamic.current_cores AS current_cores,
    vm_dynamic.current_threads AS current_threads
FROM vm_static
INNER JOIN vm_dynamic
    ON vm_static.vm_guid = vm_dynamic.vm_guid
INNER JOIN vm_static AS vm_templates
    ON vm_static.vmt_guid = vm_templates.vm_guid
INNER JOIN vm_statistics
    ON vm_static.vm_guid = vm_statistics.vm_guid
INNER JOIN cluster
    ON vm_static.cluster_id = cluster.cluster_id
LEFT JOIN storage_pool
    ON vm_static.cluster_id = cluster.cluster_id
        AND cluster.storage_pool_id = storage_pool.id
LEFT JOIN quota
    ON vm_static.quota_id = quota.id
LEFT JOIN vds_static
    ON vm_dynamic.run_on_vds = vds_static.vds_id
LEFT JOIN vm_pool_map_view
    ON vm_static.vm_guid = vm_pool_map_view.vm_guid
LEFT JOIN (
    SELECT vm_id,
        COUNT(CASE snapshot_type
                WHEN 'NEXT_RUN'
                    THEN 1
                END) > 0 AS next_run_config_exists,
        COUNT(CASE snapshot_type
                WHEN 'PREVIEW'
                    THEN 1
                END) > 0 AS is_previewing_snapshot,
        array_to_string(array_agg(changed_fields), '') AS changed_fields
    FROM snapshots
    WHERE snapshot_type = 'NEXT_RUN' OR snapshot_type = 'PREVIEW'
    GROUP BY vm_id
    ) vm_snapshots
    ON vm_static.vm_guid = vm_snapshots.vm_id
LEFT JOIN (
  SELECT vm_id, COUNT(CASE imagestatus WHEN 4 THEN 1 END) > 0 AS has_illegal_images
  FROM disk_vm_element
    JOIN images
      ON images.image_group_id = disk_vm_element.disk_id
  GROUP BY vm_id
  HAVING BOOL_OR(images.active) = TRUE
  ) image_details ON image_details.vm_id = vm_static.vm_guid
WHERE vm_static.entity_type = 'VM';


CREATE OR REPLACE VIEW vms_monitoring_view AS

SELECT vm_dynamic.*,
    vm_static.vm_name,
    vm_static.origin,
    vm_static.auto_startup,
    vm_static.mem_size_mb,
    vm_static.min_allocated_mem,
    vm_static.num_of_sockets,
    vm_static.cpu_per_socket,
    vm_static.threads_per_cpu,
    vm_static.priority,
    vm_static.lease_sd_id,
    vm_static.fn_get_num_of_vcpus AS num_of_cpus -- use of function as an attribute
FROM vm_dynamic
INNER JOIN vm_static
    ON vm_static.vm_guid = vm_dynamic.vm_guid;

CREATE OR REPLACE VIEW vms_with_tags AS

SELECT DISTINCT vms.vm_name,
    vms.lease_sd_id,
    vms.lease_info,
    vms.mem_size_mb,
    vms.max_memory_size_mb,
    vms.num_of_io_threads,
    vms.nice_level,
    vms.cpu_shares,
    vms.vmt_guid,
    vms.os,
    vms.description,
    vms.free_text_comment,
    vms.cluster_id,
    vms.creation_date,
    vms.auto_startup,
    vms.is_stateless,
    vms.is_smartcard_enabled,
    vms.is_delete_protected,
    vms.sso_method,
    fn_get_dedicated_hosts_ids_by_vm_id(vms.vm_guid) AS dedicated_vm_for_vds,
    vms.default_boot_sequence,
    vms.vm_type,
    vms.cluster_name,
    vms.storage_pool_id,
    vms.storage_pool_name,
    vms.vmt_name,
    vms.status,
    vms.vm_ip,
    vms.vm_ip_inet_array,
    vms.vm_host,
    vms.last_start_time,
    vms.boot_time,
    vms.downtime,
    vms.last_stop_time,
    vms.guest_cur_user_name,
    vms.console_cur_user_name,
    vms.runtime_name,
    vms.console_user_id,
    vms.guest_agent_nics_hash,
    vms.guest_os,
    vms.run_on_vds,
    vms.migrating_to_vds,
    vms.app_list,
    vms.vm_pool_name,
    vms.vm_pool_id,
    vms.vm_guid,
    vms.num_of_monitors,
    vms.allow_console_reconnect,
    vms.is_initialized,
    vms.num_of_sockets,
    vms.cpu_per_socket,
    vms.threads_per_cpu,
    vms.usb_policy,
    vms.acpi_enable,
    vms.session,
    vms.num_of_cpus AS num_of_cpus,
    vms.boot_sequence,
    vms.utc_diff,
    vms.client_ip,
    vms.guest_requested_memory,
    vms.time_zone,
    vms.cpu_user,
    vms.cpu_sys,
    vms.elapsed_time,
    vms.usage_network_percent,
    vms.disks_usage,
    vms.usage_mem_percent,
    vms.usage_cpu_percent,
    vms.run_on_vds_name,
    vms.cluster_cpu_name,
    vms.cluster_cpu_flags,
    vms.cluster_cpu_verb,
    tags_vm_map_view.tag_name,
    tags_vm_map_view.tag_id,
    vms.default_display_type,
    vms.iso_path,
    vms.origin,
    vms.priority,
    vms.cluster_compatibility_version,
    vms.initrd_url,
    vms.kernel_url,
    vms.kernel_params,
    vms.pause_status,
    vms.exit_status,
    vms.migration_support,
    vms.predefined_properties,
    vms.userdefined_properties,
    vms.exit_message,
    vms.min_allocated_mem,
    vms.hash,
    vms.cpu_pinning,
    vms.db_generation,
    vms.host_cpu_flags,
    image_storage_domain_map.storage_domain_id AS storage_id,
    vms.quota_id AS quota_id,
    vms.quota_name AS quota_name,
    vms.quota_enforcement_type,
    vms.tunnel_migration AS tunnel_migration,
    vms.vnc_keyboard_layout AS vnc_keyboard_layout,
    vms.is_run_and_pause AS is_run_and_pause,
    vms.created_by_user_id AS created_by_user_id,
    vms.last_watchdog_event,
    vms.last_watchdog_action,
    vms.is_run_once,
    vms.volatile_run,
    vms.vm_fqdn,
    vms.cpu_name AS cpu_name,
    vms.emulated_machine AS emulated_machine,
    vms.custom_emulated_machine AS custom_emulated_machine,
    vms.bios_type AS bios_type,
    vms.cluster_bios_type AS cluster_bios_type,
    vms.custom_cpu_name AS custom_cpu_name,
    vms.vm_pool_spice_proxy AS vm_pool_spice_proxy,
    vms.trusted_service,
    vms.transparent_hugepages,
    vms.cluster_spice_proxy AS cluster_spice_proxy,
    vms.instance_type_id AS instance_type_id,
    vms.image_type_id AS image_type_id,
    vms.architecture AS architecture,
    vms.original_template_id AS original_template_id,
    vms.original_template_name AS original_template_name,
    vms.migration_downtime AS migration_downtime,
    vms.template_version_number AS template_version_number,
    vms.current_cd AS current_cd,
    vms.reason AS reason,
    vms.serial_number_policy AS serial_number_policy,
    vms.custom_serial_number AS custom_serial_number,
    vms.exit_reason AS exit_reason,
    vms.is_boot_menu_enabled AS is_boot_menu_enabled,
    vms.guest_cpu_count AS guest_cpu_count,
    vms.next_run_config_exists,
    vms.is_previewing_snapshot,
    vms.changed_fields,
    vms.is_spice_file_transfer_enabled,
    vms.is_spice_copy_paste_enabled,
    vms.cpu_profile_id,
    vms.is_auto_converge,
    vms.is_migrate_compressed,
    vms.is_migrate_encrypted,
    vms.spice_port,
    vms.spice_tls_port,
    vms.spice_ip,
    vms.vnc_port,
    vms.vnc_ip,
    vms.ovirt_guest_agent_status,
    vms.qemu_guest_agent_status,
    vms.guest_mem_buffered AS guest_mem_buffered,
    vms.guest_mem_cached AS guest_mem_cached,
    vms.small_icon_id AS small_icon_id,
    vms.large_icon_id AS large_icon_id,
    vms.migration_policy_id as migration_policy_id,
    vms.provider_id,
    vms.console_disconnect_action,
    vms.resume_behavior AS resume_behavior,
    vms.guest_timezone_offset AS guest_timezone_offset,
    vms.guest_timezone_name AS guest_timezone_name,
    vms.guestos_arch AS guestos_arch,
    vms.guestos_codename AS guestos_codename,
    vms.guestos_distribution AS guestos_distribution,
    vms.guestos_kernel_version AS guestos_kernel_version,
    vms.guestos_type AS guestos_type,
    vms.guestos_version AS guestos_version,
    vms.custom_compatibility_version as custom_compatibility_version,
    vms.guest_containers as guest_containers,
    vms.has_illegal_images,
    vms.multi_queues_enabled,
    vms.virtio_scsi_multi_queues,
    vms.use_tsc_frequency,
    vms.namespace,
    vms.balloon_enabled,
    vms.console_disconnect_action_delay,
    vms.cpu_pinning_policy,
    vms.current_cpu_pinning,
    vms.current_sockets,
    vms.current_cores,
    vms.current_threads
FROM vms
LEFT JOIN tags_vm_map_view
    ON vms.vm_guid = tags_vm_map_view.vm_id
LEFT JOIN vm_device
    ON vm_device.vm_id = vms.vm_guid
LEFT JOIN images
    ON images.image_group_id = vm_device.device_id
LEFT JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = images.image_guid
WHERE (vm_device.device='disk' AND
    (images.active IS NULL OR images.active = TRUE)) OR
    vms.vm_guid NOT IN (SELECT DISTINCT vm_id FROM vm_device WHERE device='disk');

CREATE OR REPLACE VIEW server_vms AS

SELECT *
FROM vms
WHERE vm_type = '1';

CREATE OR REPLACE VIEW vms_with_plug_info AS

SELECT *
FROM vms
INNER JOIN vm_device vd
    ON vd.vm_id = vms.vm_guid;

CREATE OR REPLACE VIEW desktop_vms AS

SELECT *
FROM vms
WHERE vm_type = '0';

CREATE OR REPLACE VIEW vds AS

SELECT cluster.cluster_id AS cluster_id,
    cluster.name AS cluster_name,
    cluster.description AS cluster_description,
    cluster.architecture AS architecture,
    cluster.enable_balloon AS enable_balloon,
    vds_static.vds_id AS vds_id,
    vds_static.vds_name AS vds_name,
    vds_static.vds_unique_id AS vds_unique_id,
    vds_static.host_name AS host_name,
    vds_static.free_text_comment AS free_text_comment,
    vds_static.port AS port,
    vds_static.server_SSL_enabled AS server_SSL_enabled,
    vds_static.vds_type AS vds_type,
    vds_static.pm_enabled AS pm_enabled,
    vds_static.pm_proxy_preferences AS pm_proxy_preferences,
    vds_static.pm_detect_kdump AS pm_detect_kdump,
    vds_static.vds_spm_priority AS vds_spm_priority,
    vds_dynamic.hooks AS hooks,
    vds_dynamic.status AS status,
    vds_dynamic.external_status AS external_status,
    vds_dynamic.cpu_cores AS cpu_cores,
    vds_dynamic.cpu_threads AS cpu_threads,
    vds_dynamic.cpu_model AS cpu_model,
    vds_dynamic.cpu_speed_mh AS cpu_speed_mh,
    vds_dynamic.if_total_speed AS if_total_speed,
    vds_dynamic.kvm_enabled AS kvm_enabled,
    vds_dynamic.physical_mem_mb AS physical_mem_mb,
    vds_dynamic.pending_vcpus_count AS pending_vcpus_count,
    vds_dynamic.pending_vmem_size AS pending_vmem_size,
    vds_dynamic.mem_commited AS mem_commited,
    vds_dynamic.vm_active AS vm_active,
    vds_dynamic.vm_count AS vm_count,
    vds_dynamic.vm_migrating AS vm_migrating,
    vds_dynamic.incoming_migrations AS incoming_migrations,
    vds_dynamic.outgoing_migrations AS outgoing_migrations,
    vds_dynamic.kernel_args AS kernel_args,
    vds_dynamic.pretty_name AS pretty_name,
    vds_dynamic.hosted_engine_configured AS hosted_engine_configured,
    vds_dynamic.vms_cores_count AS vms_cores_count,
    vds_statistics.cpu_over_commit_time_stamp AS cpu_over_commit_time_stamp,
    cluster.max_vds_memory_over_commit AS max_vds_memory_over_commit,
    vds_dynamic.net_config_dirty AS net_config_dirty,
    cluster.count_threads_as_cores AS count_threads_as_cores,
    storage_pool.id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    vds_dynamic.reserved_mem AS reserved_mem,
    vds_dynamic.guest_overhead AS guest_overhead,
    vds_dynamic.rpm_version AS rpm_version,
    vds_dynamic.software_version AS software_version,
    vds_dynamic.version_name AS version_name,
    vds_dynamic.build_name AS build_name,
    vds_dynamic.previous_status AS previous_status,
    vds_statistics.cpu_idle AS cpu_idle,
    vds_statistics.cpu_load AS cpu_load,
    vds_statistics.cpu_sys AS cpu_sys,
    vds_statistics.cpu_user AS cpu_user,
    vds_statistics.usage_mem_percent AS usage_mem_percent,
    vds_statistics.usage_cpu_percent AS usage_cpu_percent,
    vds_statistics.usage_network_percent AS usage_network_percent,
    vds_statistics.mem_free AS mem_free,
    vds_statistics.mem_shared AS mem_shared,
    vds_statistics.swap_free AS swap_free,
    vds_statistics.swap_total AS swap_total,
    vds_statistics.ksm_cpu_percent AS ksm_cpu_percent,
    vds_statistics.ksm_pages AS ksm_pages,
    vds_statistics.ksm_state AS ksm_state,
    vds_dynamic.cpu_flags AS cpu_flags,
    cluster.cpu_name AS cluster_cpu_name,
    cluster.cpu_flags AS cluster_cpu_flags,
    cluster.cpu_verb AS cluster_cpu_verb,
    vds_dynamic.cpu_sockets AS cpu_sockets,
    vds_spm_id_map.vds_spm_id AS vds_spm_id,
    vds_static.otp_validity AS otp_validity,
    CASE
        WHEN storage_pool.spm_vds_id = vds_static.vds_id
            THEN CASE
                    WHEN storage_pool.status = 5
                        THEN 1
                    ELSE 2
                    END
        ELSE 0
        END AS spm_status,
    vds_dynamic.supported_cluster_levels AS supported_cluster_levels,
    vds_dynamic.supported_engines AS supported_engines,
    cluster.compatibility_version AS cluster_compatibility_version,
    cluster.virt_service AS cluster_virt_service,
    cluster.gluster_service AS cluster_gluster_service,
    vds_dynamic.host_os AS host_os,
    vds_dynamic.kvm_version AS kvm_version,
    vds_dynamic.libvirt_version AS libvirt_version,
    vds_dynamic.spice_version AS spice_version,
    vds_dynamic.gluster_version AS gluster_version,
    vds_dynamic.librbd1_version AS librbd1_version,
    vds_dynamic.glusterfs_cli_version AS glusterfs_cli_version,
    vds_dynamic.openvswitch_version AS openvswitch_version,
    vds_dynamic.nmstate_version AS nmstate_version,
    vds_dynamic.kernel_version AS kernel_version,
    vds_dynamic.iscsi_initiator_name AS iscsi_initiator_name,
    vds_dynamic.transparent_hugepages_state AS transparent_hugepages_state,
    vds_statistics.anonymous_hugepages AS anonymous_hugepages,
    vds_statistics.hugepages AS hugepages,
    vds_dynamic.non_operational_reason AS non_operational_reason,
    vds_static.recoverable AS recoverable,
    vds_static.sshKeyFingerprint AS sshKeyFingerprint,
    vds_static.host_provider_id AS host_provider_id,
    vds_dynamic.hw_manufacturer AS hw_manufacturer,
    vds_dynamic.hw_product_name AS hw_product_name,
    vds_dynamic.hw_version AS hw_version,
    vds_dynamic.hw_serial_number AS hw_serial_number,
    vds_dynamic.hw_uuid AS hw_uuid,
    vds_dynamic.hw_family AS hw_family,
    vds_static.console_address AS console_address,
    vds_dynamic.hbas AS hbas,
    vds_dynamic.supported_emulated_machines AS supported_emulated_machines,
    vds_dynamic.supported_rng_sources AS supported_rng_sources,
    vds_static.ssh_port AS ssh_port,
    vds_static.ssh_username AS ssh_username,
    vds_statistics.ha_score AS ha_score,
    vds_statistics.ha_configured AS ha_configured,
    vds_statistics.ha_active AS ha_active,
    vds_statistics.ha_global_maintenance AS ha_global_maintenance,
    vds_statistics.ha_local_maintenance AS ha_local_maintenance,
    vds_static.disable_auto_pm AS disable_auto_pm,
    vds_dynamic.controlled_by_pm_policy AS controlled_by_pm_policy,
    vds_statistics.boot_time AS boot_time,
    vds_dynamic.kdump_status AS kdump_status,
    vds_dynamic.selinux_enforce_mode AS selinux_enforce_mode,
    vds_dynamic.auto_numa_balancing AS auto_numa_balancing,
    vds_dynamic.is_numa_supported AS is_numa_supported,
    vds_dynamic.online_cpus AS online_cpus,
    vds_dynamic.maintenance_reason AS maintenance_reason,
    vds_dynamic.is_update_available AS is_update_available,
    vds_dynamic.is_hostdev_enabled AS is_hostdev_enabled,
    -- optimization for following subquery
    vds_dynamic.vm_count > 0
        AND vds_statistics.ha_score IS NOT NULL
        AND EXISTS (
            SELECT 1
            FROM vm_dynamic
            LEFT JOIN vm_static
                ON vm_dynamic.vm_guid = vm_static.vm_guid
            WHERE vds_static.vds_id = vm_dynamic.run_on_vds
            -- Values (5 & 6) correspond to OriginTypes of HOSTED_ENGINE & MANAGED_HOSTED_ENGINE respectively
            AND vm_static.origin IN (5,6)) AS is_hosted_engine_host,
    vds_static.kernel_cmdline AS kernel_cmdline,
    vds_static.last_stored_kernel_cmdline AS last_stored_kernel_cmdline,
    cluster.fencing_enabled AS fencing_enabled,
    gluster_server.peer_status AS gluster_peer_status,
    vds_dynamic.in_fence_flow AS in_fence_flow,
    vds_static.reinstall_required AS reinstall_required,
    vds_dynamic.kernel_features AS kernel_features,
    vds_dynamic.vnc_encryption_enabled AS vnc_encryption_enabled,
    vds_static.vgpu_placement AS vgpu_placement,
    vds_dynamic.connector_info AS connector_info,
    vds_dynamic.backup_enabled AS backup_enabled,
    vds_dynamic.cold_backup_enabled AS cold_backup_enabled,
    vds_dynamic.clear_bitmaps_enabled AS clear_bitmaps_enabled,
    vds_dynamic.supported_domain_versions AS supported_domain_versions,
    vds_dynamic.supported_block_size AS supported_block_size,
    cluster.smt_disabled AS cluster_smt_disabled,
    vds_dynamic.tsc_frequency AS tsc_frequency,
    vds_dynamic.tsc_scaling AS tsc_scaling,
    vds_dynamic.fips_enabled AS fips_enabled,
    vds_dynamic.boot_uuid AS boot_uuid,
    vds_dynamic.cd_change_pdiv AS cd_change_pdiv,
    vds_dynamic.ovn_configured AS ovn_configured,
    vds_static.ssh_public_key AS ssh_public_key
FROM cluster
INNER JOIN vds_static
    ON cluster.cluster_id = vds_static.cluster_id
INNER JOIN vds_dynamic
    ON vds_static.vds_id = vds_dynamic.vds_id
INNER JOIN vds_statistics
    ON vds_static.vds_id = vds_statistics.vds_id
LEFT JOIN storage_pool
    ON cluster.storage_pool_id = storage_pool.id
LEFT JOIN vds_spm_id_map
    ON vds_static.vds_id = vds_spm_id_map.vds_id
LEFT JOIN gluster_server
    ON vds_static.vds_id = gluster_server.server_id;

CREATE OR REPLACE VIEW vds_with_tags AS

SELECT cluster.cluster_id,
    cluster.name AS cluster_name,
    cluster.description AS cluster_description,
    cluster.architecture AS architecture,
    cluster.enable_balloon,
    vds_static.vds_id,
    vds_static.vds_name,
    vds_static.vds_unique_id,
    vds_static.host_name,
    vds_static.free_text_comment,
    vds_static.port,
    vds_static.server_SSL_enabled,
    vds_static.vds_type,
    vds_dynamic.hw_product_name,
    vds_dynamic.hw_version,
    vds_dynamic.hw_serial_number,
    vds_dynamic.hw_uuid,
    vds_dynamic.hw_family,
    vds_static.pm_enabled,
    vds_static.pm_proxy_preferences AS pm_proxy_preferences,
    vds_static.pm_detect_kdump AS pm_detect_kdump,
    vds_static.vds_spm_priority,
    vds_dynamic.hooks,
    vds_dynamic.status,
    vds_dynamic.external_status,
    vds_dynamic.cpu_cores,
    vds_dynamic.cpu_threads,
    vds_dynamic.kernel_args,
    vds_dynamic.pretty_name,
    vds_dynamic.hosted_engine_configured,
    vds_dynamic.cpu_model,
    vds_dynamic.cpu_speed_mh,
    vds_dynamic.if_total_speed,
    vds_dynamic.kvm_enabled,
    vds_dynamic.physical_mem_mb,
    vds_dynamic.pending_vcpus_count,
    vds_dynamic.pending_vmem_size,
    vds_dynamic.mem_commited,
    vds_dynamic.vm_active,
    vds_dynamic.vm_count,
    vds_dynamic.vm_migrating,
    vds_dynamic.incoming_migrations,
    vds_dynamic.outgoing_migrations,
    vds_dynamic.vms_cores_count,
    vds_statistics.cpu_over_commit_time_stamp,
    vds_dynamic.net_config_dirty,
    cluster.max_vds_memory_over_commit,
    cluster.count_threads_as_cores,
    storage_pool.id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    tags_vds_map_view.tag_name,
    tags_vds_map_view.tag_id,
    vds_dynamic.reserved_mem,
    vds_dynamic.guest_overhead,
    vds_dynamic.rpm_version,
    vds_dynamic.software_version,
    vds_dynamic.version_name,
    vds_dynamic.build_name,
    vds_dynamic.previous_status,
    vds_statistics.cpu_idle,
    vds_statistics.cpu_load,
    vds_statistics.cpu_sys,
    vds_statistics.cpu_user,
    vds_statistics.usage_mem_percent,
    vds_statistics.usage_cpu_percent,
    vds_statistics.usage_network_percent,
    vds_statistics.mem_free,
    vds_statistics.mem_shared,
    vds_statistics.swap_free,
    vds_statistics.swap_total,
    vds_statistics.ksm_cpu_percent,
    vds_statistics.ksm_pages,
    vds_statistics.ksm_state,
    vds_dynamic.cpu_flags,
    cluster.cpu_name AS cluster_cpu_name,
    cluster.cpu_flags AS cluster_cpu_flags,
    cluster.cpu_verb AS cluster_cpu_verb,
    vds_dynamic.cpu_sockets,
    vds_spm_id_map.vds_spm_id,
    vds_static.otp_validity AS otp_validity,
    vds_static.console_address AS console_address,
    CASE
        WHEN storage_pool.spm_vds_id = vds_static.vds_id
            THEN CASE
                    WHEN storage_pool.status = 5
                        THEN 1
                    ELSE 2
                    END
        ELSE 0
        END AS spm_status,
    vds_dynamic.supported_cluster_levels,
    vds_dynamic.supported_engines,
    cluster.compatibility_version AS cluster_compatibility_version,
    cluster.virt_service AS cluster_virt_service,
    cluster.gluster_service AS cluster_gluster_service,
    vds_dynamic.host_os,
    vds_dynamic.kvm_version,
    vds_dynamic.libvirt_version,
    vds_dynamic.spice_version,
    vds_dynamic.gluster_version,
    vds_dynamic.librbd1_version,
    vds_dynamic.glusterfs_cli_version,
    vds_dynamic.openvswitch_version,
    vds_dynamic.nmstate_version,
    vds_dynamic.kernel_version,
    vds_dynamic.iscsi_initiator_name,
    vds_dynamic.transparent_hugepages_state,
    vds_statistics.anonymous_hugepages,
    vds_statistics.hugepages AS hugepages,
    vds_dynamic.non_operational_reason,
    vds_static.recoverable,
    vds_static.sshkeyfingerprint,
    vds_static.host_provider_id,
    vds_dynamic.hw_manufacturer,
    storage_pool_iso_map.storage_id,
    vds_static.ssh_port,
    vds_static.ssh_username,
    vds_statistics.ha_score,
    vds_statistics.ha_configured,
    vds_statistics.ha_active,
    vds_statistics.ha_global_maintenance,
    vds_statistics.ha_local_maintenance,
    vds_static.disable_auto_pm AS disable_auto_pm,
    vds_dynamic.controlled_by_pm_policy AS controlled_by_pm_policy,
    vds_statistics.boot_time,
    vds_dynamic.kdump_status AS kdump_status,
    vds_dynamic.selinux_enforce_mode AS selinux_enforce_mode,
    vds_dynamic.auto_numa_balancing AS auto_numa_balancing,
    vds_dynamic.is_numa_supported AS is_numa_supported,
    vds_dynamic.hbas,
    vds_dynamic.supported_emulated_machines,
    vds_dynamic.supported_rng_sources AS supported_rng_sources,
    vds_dynamic.online_cpus AS online_cpus,
    vds_dynamic.maintenance_reason AS maintenance_reason,
    vds_dynamic.is_update_available AS is_update_available,
    vds_dynamic.is_hostdev_enabled AS is_hostdev_enabled,
    vds_static.kernel_cmdline,
    vds_static.last_stored_kernel_cmdline,
    cluster.fencing_enabled AS fencing_enabled,
    vds_dynamic.in_fence_flow AS in_fence_flow,
    vds_static.reinstall_required,
    vds_dynamic.kernel_features,
    vds_dynamic.vnc_encryption_enabled AS vnc_encryption_enabled,
    vds_static.vgpu_placement AS vgpu_placement,
    vds_dynamic.connector_info AS connector_info,
    vds_dynamic.backup_enabled AS backup_enabled,
    vds_dynamic.cold_backup_enabled AS cold_backup_enabled,
    vds_dynamic.clear_bitmaps_enabled AS clear_bitmaps_enabled,
    vds_dynamic.supported_domain_versions AS supported_domain_versions,
    vds_dynamic.supported_block_size AS supported_block_size,
    cluster.smt_disabled AS cluster_smt_disabled,
    vds_dynamic.tsc_frequency AS tsc_frequency,
    vds_dynamic.tsc_scaling AS tsc_scaling,
    vds_dynamic.fips_enabled AS fips_enabled,
    vds_dynamic.boot_uuid AS boot_uuid,
    vds_dynamic.cd_change_pdiv AS cd_change_pdiv,
    vds_dynamic.ovn_configured AS ovn_configured,
    vds_static.ssh_public_key AS ssh_public_key
FROM cluster
INNER JOIN vds_static
    ON cluster.cluster_id = vds_static.cluster_id
INNER JOIN vds_dynamic
    ON vds_static.vds_id = vds_dynamic.vds_id
INNER JOIN vds_statistics
    ON vds_static.vds_id = vds_statistics.vds_id
LEFT JOIN storage_pool
    ON cluster.storage_pool_id = storage_pool.id
LEFT JOIN tags_vds_map_view
    ON vds_static.vds_id = tags_vds_map_view.vds_id
LEFT JOIN vds_spm_id_map
    ON vds_static.vds_id = vds_spm_id_map.vds_id
LEFT JOIN storage_pool_iso_map
    ON storage_pool_iso_map.storage_pool_id = storage_pool.id;

CREATE OR REPLACE VIEW users_and_groups_to_vm_pool_map_view AS

SELECT p.vm_pool_id AS vm_pool_id,
    p.vm_pool_name AS vm_pool_name,
    per.ad_element_id AS user_id
FROM vm_pools AS p
INNER JOIN permissions AS per
    ON per.object_id = p.vm_pool_id;

CREATE OR REPLACE VIEW vdc_users AS

SELECT 'user' AS user_group,
    users_1.name AS name,
    users_1.user_id AS user_id,
    users_1.surname AS surname,
    users_1.domain AS DOMAIN,
    users_1.username AS username,
    users_1.department AS department,
    users_1.email AS email,
    users_1.note AS note,
    0 AS vm_admin,
    users_1.last_admin_check_status AS last_admin_check_status,
    users_1.external_id AS external_id,
    users_1.namespace AS namespace
FROM users AS users_1

UNION

SELECT 'group' AS user_group,
    ad_groups.name AS name,
    ad_groups.id AS id,
    '' AS surname,
    ad_groups.domain AS DOMAIN,
    '' AS username,
    '' AS department,
    '' AS email,
    '' AS note,
    1 AS vm_admin,
    NULL AS last_admin_check_status,
    ad_groups.external_id AS external_id,
    ad_groups.namespace AS namespace
FROM ad_groups;

-- create the new vdc_users_with_tags view with no use of the tag_permission_map
CREATE OR REPLACE VIEW vdc_users_with_tags AS

SELECT users_1.user_group AS user_group,
    users_1.name AS name,
    permissions.object_id AS vm_guid,
    users_1.user_id AS user_id,
    users_1.surname AS surname,
    users_1.domain AS DOMAIN,
    users_1.username AS username,
    users_1.department AS department,
    roles1.name AS mla_role,
    users_1.email AS email,
    users_1.note AS note,
    users_1.vm_admin AS vm_admin,
    tags_user_map_view_1.tag_name AS tag_name,
    tags_user_map_view_1.tag_id AS tag_id,
    users_1.last_admin_check_status AS last_admin_check_status,
    users_1.external_id AS external_id,
    users_1.namespace AS namespace,
    pools.vm_pool_name AS vm_pool_name
FROM vdc_users AS users_1
LEFT JOIN users_and_groups_to_vm_pool_map_view AS pools
    ON users_1.user_id = pools.user_id
LEFT JOIN permissions
    ON users_1.user_id = permissions.ad_element_id
LEFT JOIN tags
    ON tags.type = 1
LEFT JOIN tags_user_map_view AS tags_user_map_view_1
    ON users_1.user_id = tags_user_map_view_1.user_id
LEFT JOIN roles AS roles1
    ON roles1.id = permissions.role_id
WHERE (users_1.user_group = 'user')

UNION

SELECT users_2.user_group AS user_group,
    users_2.name AS name,
    permissions_1.object_id AS vm_guid,
    users_2.user_id AS user_id,
    users_2.surname AS surname,
    users_2.domain AS DOMAIN,
    users_2.username AS username,
    users_2.department AS department,
    roles2.name AS mla_role,
    users_2.email AS email,
    users_2.note AS note,
    users_2.vm_admin AS vm_admin,
    tags_user_group_map_view.tag_name AS tag_name,
    tags_user_group_map_view.tag_id AS tag_id,
    users_2.last_admin_check_status AS last_admin_check_status,
    users_2.external_id AS external_id,
    users_2.namespace AS namespace,
    pools1.vm_pool_name AS vm_pool_name
FROM vdc_users AS users_2
LEFT JOIN users_and_groups_to_vm_pool_map_view AS pools1
    ON users_2.user_id = pools1.user_id
LEFT JOIN permissions AS permissions_1
    ON users_2.user_id = permissions_1.ad_element_id
LEFT JOIN tags AS tags_1
    ON tags_1.type = 1
LEFT JOIN tags_user_group_map_view
    ON users_2.user_id = tags_user_group_map_view.group_id
LEFT JOIN roles AS roles2
    ON roles2.id = permissions_1.role_id
WHERE (users_2.user_group = 'group');

CREATE OR REPLACE VIEW vm_pools_view AS

SELECT vm_pools.vm_pool_id,
    vm_pools.vm_pool_name,
    vm_pools.vm_pool_description,
    vm_pools.vm_pool_comment,
    vm_pools.vm_pool_type,
    vm_pools.stateful,
    vm_pools.parameters,
    vm_pools.prestarted_vms,
    vm_pools.cluster_id,
    cluster.name AS cluster_name,
    cluster.architecture AS architecture,
    storage_pool.name AS storage_pool_name,
    storage_pool.id AS storage_pool_id,
    vm_pools.max_assigned_vms_per_user AS max_assigned_vms_per_user,
    vm_pools.spice_proxy AS spice_proxy,
    vm_pools.is_being_destroyed AS is_being_destroyed,
    vm_pools.is_auto_storage_select AS is_auto_storage_select
FROM vm_pools
INNER JOIN cluster
    ON vm_pools.cluster_id = cluster.cluster_id
LEFT JOIN storage_pool
    ON storage_pool.id = cluster.storage_pool_id;

CREATE OR REPLACE VIEW vm_pools_full_view AS

SELECT vmp.vm_pool_id,
    vmp.vm_pool_name,
    vmp.vm_pool_description,
    vmp.vm_pool_comment,
    vmp.vm_pool_type,
    vmp.stateful,
    vmp.parameters,
    vmp.prestarted_vms,
    vmp.cluster_id,
    vmp.cluster_name,
    vmp.architecture,
    vmp.max_assigned_vms_per_user,
    vmp.spice_proxy AS spice_proxy,
    (
        SELECT COUNT(vm_pool_map.vm_pool_id) AS expr1
        FROM vm_pools_view v1
        LEFT JOIN vm_pool_map
            ON v1.vm_pool_id = vm_pool_map.vm_pool_id
                AND v1.vm_pool_id = vmp.vm_pool_id
        ) AS assigned_vm_count,
    (
        SELECT COUNT(v2.vm_pool_id) AS expr1
        FROM vm_pools v2
        LEFT JOIN vm_pool_map vm_pool_map_1
            ON v2.vm_pool_id = vm_pool_map_1.vm_pool_id
                AND v2.vm_pool_id = vmp.vm_pool_id
        LEFT JOIN vm_dynamic
            ON vm_pool_map_1.vm_guid = vm_dynamic.vm_guid
        WHERE vm_dynamic.status <> ALL (
                ARRAY [ 0,
                15 ]
                )
        GROUP BY v2.vm_pool_id
        ) AS vm_running_count,
    vmp.storage_pool_name,
    vmp.storage_pool_id,
    vmp.is_being_destroyed,
    vmp.is_auto_storage_select
FROM vm_pools_view vmp;

CREATE OR REPLACE VIEW permissions_view AS

SELECT permissions.id AS id,
    permissions.role_id AS role_id,
    permissions.ad_element_id AS ad_element_id,
    permissions.object_id AS object_id,
    permissions.object_type_id AS object_type_id,
    roles.name AS role_name,
    roles.role_type AS role_type,
    roles.allows_viewing_children AS allows_viewing_children,
    roles.app_mode AS app_mode,
    fn_get_entity_name(permissions.object_id, permissions.object_type_id) AS object_name,
    (fn_authz_entry_info(permissions.ad_element_id)).name AS owner_name,
    (fn_authz_entry_info(permissions.ad_element_id)).namespace AS namespace,
    (fn_authz_entry_info(permissions.ad_element_id)).authz AS authz,
    permissions.creation_date AS creation_date
FROM permissions
INNER JOIN roles
    ON permissions.role_id = roles.id;

CREATE OR REPLACE VIEW internal_permissions_view AS

SELECT permissions.id AS id,
    permissions.role_id AS role_id,
    permissions.ad_element_id AS ad_element_id,
    permissions.object_id AS object_id,
    permissions.object_type_id AS object_type_id,
    roles.name AS role_name,
    roles.role_type AS role_type,
    roles.allows_viewing_children AS allows_viewing_children
FROM permissions
INNER JOIN roles
    ON permissions.role_id = roles.id;

--
--SELECT     storages.id, storages.storage, storages.storage_pool_id, storages.storage_type, storage_pool.name,
--                      storage_pool.storage_pool_type
--FROM         storage_pool INNER JOIN
--                      storages ON storage_pool.id = storages.storage_pool_id
--
/*************************************************
        vds/vm/ interface view
*************************************************/
CREATE OR REPLACE VIEW vds_interface_view AS

SELECT vds_interface_statistics.rx_rate,
    vds_interface_statistics.tx_rate,
    vds_interface_statistics.rx_drop,
    vds_interface_statistics.tx_drop,
    vds_interface_statistics.rx_total,
    vds_interface_statistics.tx_total,
    vds_interface_statistics.rx_offset,
    vds_interface_statistics.tx_offset,
    vds_interface_statistics.iface_status,
    vds_interface_statistics.sample_time,
    vds_interface.type,
    vds_interface.gateway,
    vds_interface.ipv4_default_route,
    vds_interface.ipv6_gateway,
    vds_interface.subnet,
    vds_interface.ipv6_prefix,
    vds_interface.addr,
    vds_interface.ipv6_address,
    vds_interface.speed,
    vds_interface.base_interface,
    vds_interface.vlan_id,
    vds_interface.bond_type,
    vds_interface.bond_name,
    vds_interface.is_bond,
    vds_interface.bond_opts,
    vds_interface.mac_addr,
    vds_interface.network_name,
    vds_interface.name,
    vds_static.vds_id,
    vds_static.vds_name,
    vds_interface.id,
    vds_interface.boot_protocol,
    vds_interface.ipv6_boot_protocol,
    vds_interface.mtu AS mtu,
    vds_interface.bridged,
    vds_interface.reported_switch_type,
    1 AS is_vds,
    vds_interface.qos_overridden AS qos_overridden,
    vds_interface.labels AS labels,
    vds_static.cluster_id AS cluster_id,
    vds_interface.ad_partner_mac,
    vds_interface.ad_aggregator_id,
    vds_interface.bond_active_slave
FROM vds_interface_statistics
INNER JOIN vds_interface
    ON vds_interface_statistics.id = vds_interface.id
INNER JOIN vds_static
    ON vds_interface.vds_id = vds_static.vds_id;

CREATE OR REPLACE VIEW vm_interface_view AS

SELECT vm_interface_statistics.rx_rate,
    vm_interface_statistics.tx_rate,
    vm_interface_statistics.rx_drop,
    vm_interface_statistics.tx_drop,
    vm_interface_statistics.rx_total,
    vm_interface_statistics.tx_total,
    vm_interface_statistics.rx_offset,
    vm_interface_statistics.tx_offset,
    vm_interface_statistics.iface_status,
    vm_interface_statistics.sample_time,
    vm_interface.type,
    vm_interface.speed,
    vm_interface.mac_addr,
    vm_interface.synced,
    network.name AS network_name,
    vm_interface.name,
    vm_interface.vnic_profile_id,
    vm_static.vm_guid,
    vm_static.vm_name,
    vm_interface.id,
    0 AS boot_protocol,
    0 AS is_vds,
    vm_device.is_plugged,
    vm_device.custom_properties,
    vnic_profiles.port_mirroring AS port_mirroring,
    vm_interface.linked,
    vm_static.cluster_id AS cluster_id,
    vm_static.entity_type AS vm_entity_type,
    vnic_profiles.name AS vnic_profile_name,
    qos.name AS qos_name,
    failover_vnic_profile.name AS failover_vnic_profile_name
FROM vm_interface_statistics
INNER JOIN vm_interface
    ON vm_interface_statistics.id = vm_interface.id
INNER JOIN vm_static
    ON vm_interface.vm_guid = vm_static.vm_guid
INNER JOIN vm_device
    ON vm_interface.vm_guid = vm_device.vm_id
        AND vm_interface.id = vm_device.device_id
LEFT JOIN (
    (
        vnic_profiles INNER JOIN network
            ON network.id = vnic_profiles.network_id
        ) LEFT JOIN qos
        ON vnic_profiles.network_qos_id = qos.id
        LEFT JOIN vnic_profiles as failover_vnic_profile
        ON vnic_profiles.failover_vnic_profile_id = failover_vnic_profile.id
    )
    ON vnic_profiles.id = vm_interface.vnic_profile_id
WHERE entity_type = 'VM'

UNION

SELECT vm_interface_statistics.rx_rate,
    vm_interface_statistics.tx_rate,
    vm_interface_statistics.rx_drop,
    vm_interface_statistics.tx_drop,
    vm_interface_statistics.rx_total,
    vm_interface_statistics.tx_total,
    vm_interface_statistics.rx_offset,
    vm_interface_statistics.tx_offset,
    vm_interface_statistics.iface_status,
    vm_interface_statistics.sample_time,
    vm_interface.type,
    vm_interface.speed,
    vm_interface.mac_addr,
    vm_interface.synced,
    network.name AS network_name,
    vm_interface.name,
    vm_interface.vnic_profile_id,
    vm_templates.vm_guid,
    vm_templates.vm_name AS vm_name,
    vm_interface.id,
    0 AS boot_protocol,
    0 AS is_vds,
    vm_device.is_plugged AS is_plugged,
    vm_device.custom_properties AS custom_properties,
    vnic_profiles.port_mirroring AS port_mirroring,
    vm_interface.linked,
    vm_templates.cluster_id AS cluster_id,
    vm_templates.entity_type AS vm_entity_type,
    vnic_profiles.name AS vnic_profile_name,
    qos.name AS qos_name,
    failover_vnic_profile.name AS failover_vnic_profile_name
FROM vm_interface_statistics
RIGHT JOIN vm_interface
    ON vm_interface_statistics.id = vm_interface.id
INNER JOIN vm_static AS vm_templates
    ON vm_interface.vm_guid = vm_templates.vm_guid
INNER JOIN vm_device
    ON vm_interface.vm_guid = vm_device.vm_id
        AND vm_interface.id = vm_device.device_id
LEFT JOIN (
    (
        vnic_profiles INNER JOIN network
            ON network.id = vnic_profiles.network_id
        ) LEFT JOIN qos
        ON vnic_profiles.network_qos_id = qos.id
        LEFT JOIN vnic_profiles as failover_vnic_profile
        ON vnic_profiles.failover_vnic_profile_id = failover_vnic_profile.id
    )
    ON vnic_profiles.id = vm_interface.vnic_profile_id
WHERE vm_templates.entity_type = 'TEMPLATE';

CREATE OR REPLACE VIEW vm_interface_monitoring_view AS

SELECT vm_interface_statistics.rx_rate,
    vm_interface_statistics.tx_rate,
    vm_interface_statistics.rx_drop,
    vm_interface_statistics.tx_drop,
    vm_interface_statistics.rx_total,
    vm_interface_statistics.tx_total,
    vm_interface_statistics.rx_offset,
    vm_interface_statistics.tx_offset,
    vm_interface_statistics.iface_status,
    vm_interface_statistics.sample_time,
    vm_interface.mac_addr,
    vm_interface.vnic_profile_id,
    vm_interface.speed,
    vm_interface.vm_guid,
    vm_interface.id
FROM vm_interface_statistics
INNER JOIN vm_interface
    ON vm_interface_statistics.id = vm_interface.id;


----------------------------------------------
-- Storage Pool
----------------------------------------------
CREATE OR REPLACE VIEW storage_pool_with_storage_domain AS

SELECT storage_pool.id AS id,
    storage_pool.name AS name,
    storage_pool.description AS description,
    storage_pool.free_text_comment AS free_text_comment,
    storage_pool.status AS status,
    storage_pool.is_local AS is_local,
    storage_pool.master_domain_version AS master_domain_version,
    storage_pool.spm_vds_id AS spm_vds_id,
    storage_pool.compatibility_version AS compatibility_version,
    storage_pool._create_date AS _create_date,
    storage_pool._update_date AS _update_date,
    storage_pool_iso_map.storage_id AS storage_id,
    storage_pool_iso_map.storage_pool_id AS storage_pool_id,
    storage_domain_static.storage_type AS storage_type,
    storage_domain_static.storage_domain_type AS storage_domain_type,
    storage_domain_static.storage_domain_format_type AS storage_domain_format_type,
    storage_domain_static.storage_name AS storage_name,
    storage_domain_static.storage AS storage,
    storage_domain_static.last_time_used_as_master AS last_time_used_as_master
FROM storage_pool
LEFT JOIN storage_pool_iso_map
    ON storage_pool.id = storage_pool_iso_map.storage_pool_id
LEFT JOIN storage_domain_static
    ON storage_pool_iso_map.storage_id = storage_domain_static.id;

----------------------------------------------
-- Clusters
----------------------------------------------
CREATE OR REPLACE VIEW cluster_storage_domain AS

SELECT cluster.cluster_id,
    cluster.name,
    cluster.description,
    cluster.free_text_comment,
    cluster.cpu_name,
    cluster.cpu_flags,
    cluster.cpu_verb,
    cluster._create_date,
    cluster._update_date,
    cluster.storage_pool_id,
    cluster.max_vds_memory_over_commit,
    cluster.count_threads_as_cores,
    cluster.compatibility_version,
    cluster.transparent_hugepages,
    cluster.migrate_on_error,
    cluster.architecture,
    storage_pool_iso_map.storage_id,
    storage_pool.name AS storage_pool_name
FROM cluster
LEFT JOIN storage_pool_iso_map
    ON cluster.storage_pool_id = storage_pool_iso_map.storage_pool_id
LEFT JOIN storage_pool
    ON cluster.storage_pool_id = storage_pool.id;

CREATE OR REPLACE VIEW cluster_view AS

SELECT cluster.*,
    storage_pool.name AS storage_pool_name,
    cluster_policies.name AS cluster_policy_name
FROM cluster
LEFT JOIN storage_pool
    ON cluster.storage_pool_id = storage_pool.id
LEFT JOIN cluster_policies
    ON cluster.cluster_policy_id = cluster_policies.id;

CREATE OR REPLACE VIEW storage_domains_with_hosts_view AS

SELECT storage_domain_static.id,
    storage_domain_static.storage,
    storage_domain_static.storage_name,
    storage_domain_static.storage_description AS storage_description,
    storage_domain_static.storage_comment AS storage_comment,
    storage_domain_dynamic.available_disk_size,
    storage_domain_dynamic.confirmed_available_disk_size,
    storage_domain_dynamic.vdo_savings,
    storage_domain_dynamic.used_disk_size,
    storage_domains_image_sizes.commited_disk_size,
    storage_domains_image_sizes.actual_images_size,
    storage_pool.name AS storage_pool_name,
    storage_domain_static.storage_type,
    storage_domain_static.storage_domain_type,
    storage_domain_static.storage_domain_format_type,
    storage_domain_static.last_time_used_as_master AS last_time_used_as_master,
    storage_domain_static.wipe_after_delete AS wipe_after_delete,
    storage_domain_static.discard_after_delete AS discard_after_delete,
    storage_domain_static.first_metadata_device AS first_metadata_device,
    storage_domain_static.vg_metadata_device AS vg_metadata_device,
    storage_domain_static.backup AS backup,
    storage_domain_static.block_size as block_size,
    COALESCE(storage_domain_shared_status.status, 0) AS storage_domain_shared_status,
    cluster.cluster_id,
    vds_static.vds_id,
    storage_pool_iso_map.storage_pool_id,
    vds_static.recoverable
FROM storage_domain_static
INNER JOIN storage_domain_dynamic
    ON storage_domain_static.id = storage_domain_dynamic.id
LEFT JOIN storage_pool_iso_map
    ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT JOIN storage_domain_shared_status
    ON storage_domain_shared_status.storage_id = storage_domain_static.id
LEFT JOIN storage_domains_image_sizes
    ON storage_domains_image_sizes.storage_domain_id = storage_domain_static.id
LEFT JOIN storage_pool
    ON storage_pool_iso_map.storage_pool_id = storage_pool.id
LEFT JOIN cluster
    ON storage_pool_iso_map.storage_pool_id = cluster.storage_pool_id
LEFT JOIN vds_static
    ON cluster.cluster_id = vds_static.cluster_id;

CREATE OR REPLACE VIEW vm_images_storage_domains_view AS

SELECT vm_images_view.storage_id,
    vm_images_view.storage_pool_id,
    vm_images_view.image_guid,
    vm_images_view.creation_date,
    vm_images_view.actual_size,
    vm_images_view.read_rate,
    vm_images_view.read_ops,
    vm_images_view.write_rate,
    vm_images_view.write_ops,
    vm_images_view.size,
    vm_images_view.it_guid,
    vm_images_view.description,
    vm_images_view.parentid,
    vm_images_view.imagestatus,
    vm_images_view.lastmodified,
    vm_images_view.app_list,
    vm_images_view.vm_snapshot_id,
    vm_images_view.volume_type,
    vm_images_view.image_group_id,
    vm_images_view.active,
    vm_images_view.volume_format,
    vm_images_view.qcow_compat,
    vm_images_view.wipe_after_delete,
    vm_images_view.propagate_errors,
    vm_images_view.disk_content_type,
    vm_images_view.backup,
    vm_images_view.entity_type,
    vm_images_view.number_of_vms,
    vm_images_view.vm_names,
    vm_images_view.template_version_names,
    vm_images_view.quota_id,
    vm_images_view.quota_name,
    vm_images_view.disk_profile_id,
    vm_images_view.disk_profile_name,
    vm_images_view.disk_alias,
    vm_images_view.disk_description,
    vm_images_view.sgio,
    storage_domains_with_hosts_view.id,
    storage_domains_with_hosts_view.storage,
    storage_domains_with_hosts_view.storage_name,
    storage_domains_with_hosts_view.available_disk_size,
    storage_domains_with_hosts_view.used_disk_size,
    storage_domains_with_hosts_view.commited_disk_size,
    storage_domains_with_hosts_view.actual_images_size,
    storage_domains_with_hosts_view.storage_type,
    storage_domains_with_hosts_view.storage_domain_type,
    storage_domains_with_hosts_view.storage_domain_format_type,
    storage_domains_with_hosts_view.storage_domain_shared_status,
    storage_domains_with_hosts_view.cluster_id,
    storage_domains_with_hosts_view.vds_id,
    storage_domains_with_hosts_view.recoverable,
    storage_domains_with_hosts_view.storage_pool_name,
    storage_domains_with_hosts_view.storage_name AS name
FROM vm_images_view
INNER JOIN images_storage_domain_view
    ON vm_images_view.image_guid = images_storage_domain_view.image_guid
INNER JOIN storage_domains_with_hosts_view
    ON storage_domains_with_hosts_view.id = images_storage_domain_view.storage_id;

----------------------------------------------
-- Quota
----------------------------------------------
CREATE OR REPLACE VIEW quota_view AS

SELECT q.id AS quota_id,
    q.storage_pool_id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    q.quota_name AS quota_name,
    q.description AS description,
    q.threshold_cluster_percentage AS threshold_cluster_percentage,
    q.threshold_storage_percentage AS threshold_storage_percentage,
    q.grace_cluster_percentage AS grace_cluster_percentage,
    q.grace_storage_percentage AS grace_storage_percentage,
    storage_pool.quota_enforcement_type AS quota_enforcement_type,
    q.is_default AS is_default
FROM storage_pool,
    quota q
WHERE storage_pool.id = q.storage_pool_id;

CREATE OR REPLACE VIEW quota_global_view AS

SELECT q_limit.quota_id AS quota_id,
    q.storage_pool_id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    q.quota_name AS quota_name,
    q.description AS description,
    q.threshold_cluster_percentage AS threshold_cluster_percentage,
    q.threshold_storage_percentage AS threshold_storage_percentage,
    q.grace_cluster_percentage AS grace_cluster_percentage,
    q.grace_storage_percentage AS grace_storage_percentage,
    virtual_cpu,
    (CalculateClusterUsage(quota_id, NULL)).virtual_cpu_usage,
    mem_size_mb,
    (CalculateClusterUsage(quota_id, NULL)).mem_size_mb_usage,
    storage_size_gb,
    CalculateStorageUsage(quota_id, NULL) AS storage_size_gb_usage,
    storage_pool.quota_enforcement_type AS quota_enforcement_type,
    q.is_default AS is_default
FROM storage_pool,
    quota q
LEFT JOIN quota_limitation q_limit
    ON q_limit.quota_id = q.id
WHERE storage_pool.id = q.storage_pool_id
    AND q_limit.cluster_id IS NULL
    AND q_limit.storage_id IS NULL;

CREATE OR REPLACE VIEW quota_limitations_view AS

SELECT q_limit.quota_id AS quota_id,
    q.storage_pool_id AS storage_pool_id,
    storage_pool.name AS storage_pool_name,
    q.quota_name AS quota_name,
    q.description AS description,
    q.threshold_cluster_percentage AS threshold_cluster_percentage,
    q.threshold_storage_percentage AS threshold_storage_percentage,
    q.grace_cluster_percentage AS grace_cluster_percentage,
    q.grace_storage_percentage AS grace_storage_percentage,
    virtual_cpu,
    mem_size_mb,
    storage_size_gb,
    storage_pool.quota_enforcement_type AS quota_enforcement_type,
    q.is_default AS is_default,
    cluster_id,
    storage_id,
    (COALESCE(cluster_id, storage_id) IS NULL) AS is_global,
    (COALESCE(virtual_cpu, mem_size_mb, storage_size_gb) IS NULL) AS is_empty
FROM quota q
INNER JOIN storage_pool
    ON storage_pool.id = q.storage_pool_id
LEFT JOIN quota_limitation q_limit
    ON q_limit.quota_id = q.id;

CREATE OR REPLACE VIEW quota_storage_view AS

SELECT q_limit.id AS quota_storage_id,
    q_limit.quota_id AS quota_id,
    storage_id,
    storage_domain_static.storage_name AS storage_name,
    storage_size_gb,
    CalculateStorageUsage(quota_id, storage_id) AS storage_size_gb_usage
FROM quota_limitation q_limit,
    quota q,
    storage_domain_static
WHERE q_limit.quota_id = q.id
    AND q_limit.cluster_id IS NULL
    AND q_limit.storage_id IS NOT NULL
    AND storage_domain_static.id = q_limit.storage_id;

CREATE OR REPLACE VIEW quota_cluster_view AS

SELECT q_limit.id AS quota_cluster_id,
    q_limit.quota_id AS quota_id,
    q_limit.cluster_id,
    cluster.name AS cluster_name,
    virtual_cpu,
    (CalculateClusterUsage(quota_id, q_limit.cluster_id)).virtual_cpu_usage AS virtual_cpu_usage,
    mem_size_mb,
    (CalculateClusterUsage(quota_id, q_limit.cluster_id)).mem_size_mb_usage AS mem_size_mb_usage
FROM quota_limitation q_limit,
    quota q,
    cluster
WHERE q_limit.quota_id = q.id
    AND q_limit.cluster_id IS NOT NULL
    AND q_limit.storage_id IS NULL
    AND cluster.cluster_id = q_limit.cluster_id;

----------------------------------------------
-- Network
----------------------------------------------
CREATE OR REPLACE VIEW network_cluster_view AS

SELECT network_cluster.cluster_id AS cluster_id,
    network_cluster.network_id AS network_id,
    network.name AS network_name,
    network_cluster.status AS status,
    network_cluster.required AS required,
    network_cluster.is_display AS is_display,
    network_cluster.migration AS migration,
    cluster.name AS cluster_name
FROM network_cluster
INNER JOIN network
    ON network_cluster.network_id = network.id
INNER JOIN cluster
    ON network_cluster.cluster_id = cluster.cluster_id;

CREATE OR REPLACE VIEW network_vds_view AS

SELECT network.id AS network_id,
    network.name AS network_name,
    vds_static.vds_name AS vds_name
FROM vds_interface
INNER JOIN vds_static
    ON vds_interface.vds_id = vds_static.vds_id
INNER JOIN network
    ON vds_interface.network_name = network.name
INNER JOIN network_cluster
    ON network_cluster.network_id = network.id
WHERE network_cluster.cluster_id = vds_static.cluster_id;

CREATE OR REPLACE VIEW network_view AS

SELECT network.id AS id,
    network.name AS name,
    network.vdsm_name AS vdsm_name,
    network.description AS description,
    network.free_text_comment AS free_text_comment,
    network.type AS type,
    network.addr AS addr,
    network.subnet AS subnet,
    network.gateway AS gateway,
    network.vlan_id AS vlan_id,
    network.stp AS stp,
    network.mtu AS mtu,
    network.vm_network AS vm_network,
    network.storage_pool_id AS storage_pool_id,
    network.provider_network_provider_id AS provider_network_provider_id,
    network.provider_network_external_id AS provider_network_external_id,
    network.provider_physical_network_id AS provider_physical_network_id,
    network.qos_id AS qos_id,
    network.dns_resolver_configuration_id,
    network.label AS label,
    network.port_isolation AS port_isolation,
    storage_pool.name AS storage_pool_name,
    storage_pool.compatibility_version AS compatibility_version,
    providers.name AS provider_name,
    qos.name AS qos_name
FROM network
INNER JOIN storage_pool
    ON network.storage_pool_id = storage_pool.id
LEFT JOIN providers
    ON network.provider_network_provider_id = providers.id
LEFT JOIN qos
    ON qos.id = network.qos_id;

CREATE OR REPLACE VIEW vnic_profiles_view AS

SELECT vnic_profiles.id AS id,
    vnic_profiles.name AS name,
    vnic_profiles.network_id AS network_id,
    vnic_profiles.network_qos_id AS network_qos_id,
    vnic_profiles.port_mirroring AS port_mirroring,
    vnic_profiles.passthrough AS passthrough,
    vnic_profiles.migratable AS migratable,
    vnic_profiles.custom_properties AS custom_properties,
    vnic_profiles.description AS description,
    vnic_profiles.network_filter_id AS network_filter_id,
    vnic_profiles.failover_vnic_profile_id as failover_vnic_profile_id,
    network_filter.filter_name AS network_filter_name,
    network.name AS network_name,
    qos.name AS network_qos_name,
    storage_pool.name AS data_center_name,
    storage_pool.compatibility_version AS compatibility_version,
    storage_pool.id AS data_center_id,
    failover_vnic_profiles.name AS failover_vnic_profile_name

FROM vnic_profiles
INNER JOIN network
    ON vnic_profiles.network_id = network.id
LEFT JOIN qos
    ON vnic_profiles.network_qos_id = qos.id
INNER JOIN storage_pool
    ON network.storage_pool_id = storage_pool.id
LEFT JOIN network_filter
    ON vnic_profiles.network_filter_id = network_filter.filter_id
LEFT JOIN vnic_profiles AS failover_vnic_profiles
    ON vnic_profiles.failover_vnic_profile_id = failover_vnic_profiles.id;

----------------------------------------------
-- Query Permissions
----------------------------------------------
-- Flatten all the objects a user can get permissions on them
CREATE OR REPLACE VIEW engine_session_user_flat_groups AS

SELECT id AS engine_session_seq_id,
    user_id AS user_id,
    fnSplitterUuid(engine_sessions.group_ids) AS granted_id
FROM engine_sessions

UNION ALL -- The user itself

SELECT id,
    user_id,
    user_id
FROM engine_sessions

UNION ALL -- user is also member of 'Everyone'

SELECT id,
    user_id,
    'EEE00000-0000-0000-0000-123456789EEE'
FROM engine_sessions;

-- Permissions view for Clusters
-- The user has permissions on a cluster
CREATE OR REPLACE VIEW user_cluster_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 9
    AND role_type = 2 -- Or the object is a VM or Template in the cluster

UNION ALL

SELECT DISTINCT cluster_id,
    ad_element_id
FROM vm_static
INNER JOIN internal_permissions_view
    ON object_id = vm_guid
        AND (
            object_type_id = 2
            OR object_type_id = 4
            )
        AND role_type = 2
        AND cluster_id IS NOT NULL -- Or the object is the Data Center containing the Cluster

UNION ALL

SELECT cluster_id,
    ad_element_id
FROM cluster
INNER JOIN internal_permissions_view
    ON object_id = cluster.storage_pool_id
        AND object_type_id = 14
        AND role_type = 2 -- Or the user has permissions on system;

UNION ALL

SELECT cluster_id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN cluster
WHERE object_type_id = 1
    AND role_type = 2;

CREATE OR REPLACE VIEW user_cluster_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_cluster_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions view for Data Center
-- The user has permissions on a data center
CREATE OR REPLACE VIEW user_storage_pool_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 14
    AND role_type = 2 -- Or the object is a cluster in the data center

UNION ALL

SELECT storage_pool_id,
    ad_element_id
FROM cluster
INNER JOIN internal_permissions_view
    ON object_id = cluster.cluster_id
        AND object_type_id = 9
        AND role_type = 2 -- Or the object is vm pool in the data center

UNION ALL

SELECT storage_pool_id,
    ad_element_id
FROM cluster
INNER JOIN vm_pools
    ON cluster.cluster_id = vm_pools.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = vm_pools.vm_pool_id
        AND object_type_id = 5
        AND role_type = 2 -- Or the object is a VM in the data center

UNION ALL

SELECT storage_pool_id,
    ad_element_id
FROM vm_static
INNER JOIN cluster
    ON cluster.cluster_id = vm_static.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = vm_guid
        AND object_type_id = 2
        AND role_type = 2 -- Or the user has permission on system

UNION ALL

SELECT storage_pool.id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN storage_pool
WHERE object_type_id = 1
    AND role_type = 2;

CREATE OR REPLACE VIEW user_storage_pool_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_storage_pool_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions for Storage Domains
-- The user has permissions on a storage domain
CREATE OR REPLACE VIEW user_storage_domain_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 11
    AND role_type = 2 -- Or the user has permissions on a VM in the storage domain

UNION ALL

SELECT storage_domains.id,
    ad_element_id
FROM storage_domains
INNER JOIN cluster
    ON cluster.storage_pool_id = storage_domains.storage_pool_id
INNER JOIN vm_static
    ON cluster.cluster_id = vm_static.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = vm_static.vm_guid
        AND object_type_id = 2
        AND role_type = 2 -- Or the user has permissions on a template in the storage domain

UNION ALL

SELECT storage_id,
    ad_element_id
FROM vm_templates_based_storage_domain
INNER JOIN internal_permissions_view
    ON vmt_guid = internal_permissions_view.object_id
        AND object_type_id = 4
        AND role_type = 2 -- Or the user has permissions on a VM created from a template in the storage domain

UNION ALL

SELECT storage_id,
    ad_element_id
FROM vm_static
INNER JOIN vm_templates_based_storage_domain
    ON vm_static.vmt_guid = vm_templates_based_storage_domain.vmt_guid
INNER JOIN internal_permissions_view
    ON vm_static.vm_guid = object_id
        AND objecT_type_id = 2
        AND role_type = 2 -- Or the user has permissions on the Data Center containing the storage domain

UNION ALL

SELECT storage_domains.id,
    ad_element_id
FROM storage_domains
INNER JOIN internal_permissions_view
    ON object_id = storage_domains.storage_pool_id
        AND object_type_id = 14
        AND role_type = 2 -- Or the user has permissions on System

UNION ALL

SELECT storage_domains.id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN storage_domains
WHERE object_type_id = 1
    AND role_type = 2;

CREATE OR REPLACE VIEW user_storage_domain_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_storage_domain_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on Hosts
-- The user has permissions on a host
CREATE OR REPLACE VIEW user_vds_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 3
    AND role_type = 2 -- Or the user has permissions on a VM in the cluster or Data Center that contains the host

UNION ALL

SELECT vds_id,
    ad_element_id
FROM vds
INNER JOIN internal_permissions_view
    ON (
            object_id = cluster_id
            AND object_type_id = 9
            )
        OR (
            object_id = storage_pool_id
            AND object_type_id = 14
            )
        AND role_type = 2 -- Or the user has permissions on System

UNION ALL

SELECT vds_id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN vds
WHERE object_type_id = 1
    AND role_type = 2;

CREATE OR REPLACE VIEW user_vds_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_vds_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on VM Pools
-- The user has permissions on the pool
CREATE OR REPLACE VIEW user_vm_pool_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 5
    AND role_type = 2 -- Or the user has permissions on a VM from the pool

UNION ALL

SELECT vm_pool_id,
    ad_element_id
FROM vm_pool_map
INNER JOIN internal_permissions_view
    ON object_id = vm_guid
        AND object_type_id = 2
        AND role_type = 2 -- Or the user has permissions on the cluster containing the pool

UNION ALL

SELECT vm_pool_id,
    ad_element_id
FROM vm_pools
INNER JOIN internal_permissions_view
    ON object_id = cluster_id
        AND object_type_id = 9
        AND allows_viewing_children
        AND role_type = 2 -- Or the user has permission on the data center containing the VM pool

UNION ALL

SELECT vm_pool_id,
    ad_element_id
FROM vm_pools
INNER JOIN cluster
    ON vm_pools.cluster_id = cluster.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = storage_pool_id
        AND object_type_id = 14
        AND allows_viewing_children
        AND role_type = 2 -- Or the user has permissions on System

UNION ALL

SELECT vm_pool_id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN vm_pools
WHERE object_type_id = 1
    AND allows_viewing_children
    AND role_type = 2;

CREATE OR REPLACE VIEW user_vm_pool_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_vm_pool_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on Templates
-- The user has permissions on the template
CREATE OR REPLACE VIEW user_vm_template_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 4
    AND role_type = 2 -- Or the user has permissions on a VM created from the tempalate

UNION ALL

SELECT vmt_guid,
    ad_element_id
FROM vm_static
INNER JOIN internal_permissions_view
    ON object_id = vm_static.vm_guid
        AND object_type_id = 2
        AND role_type = 2 -- Or the user has permissions on the data center containing the template

UNION ALL

SELECT vm_guid,
    ad_element_id
FROM vm_static
INNER JOIN cluster
    ON cluster.cluster_id = vm_static.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = storage_pool_id
        AND object_type_id = 14
        AND allows_viewing_children
        AND role_type = 2
        AND vm_static.entity_type::TEXT = 'TEMPLATE'::TEXT -- Or the user has permissions on system

UNION ALL

SELECT vm_guid,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN vm_static
WHERE object_type_id = 1
    AND allows_viewing_children
    AND role_type = 2
    AND (
        vm_static.entity_type::TEXT = 'TEMPLATE'::TEXT
        OR vm_static.entity_type::TEXT = 'INSTANCE_TYPE'::TEXT
        OR vm_static.entity_type::TEXT = 'IMAGE_TYPE'::TEXT
        );

CREATE OR REPLACE VIEW user_vm_template_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_vm_template_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on VMs
-- The user and admin has permission on the VM
CREATE OR REPLACE VIEW vm_permissions_view_base (
    entity_id,
    granted_id,
    role_type
    ) AS

SELECT object_id,
    ad_element_id,
    role_type
FROM internal_permissions_view
WHERE object_type_id = 2

UNION ALL

SELECT vm_guid,
    ad_element_id,
    role_type
FROM vm_static
INNER JOIN internal_permissions_view
    ON object_id = cluster_id
        AND object_type_id = 9
        AND allows_viewing_children

UNION ALL

SELECT vm_guid,
    ad_element_id,
    role_type
FROM vm_static
INNER JOIN cluster
    ON cluster.cluster_id = vm_static.cluster_id
INNER JOIN internal_permissions_view
    ON object_id = storage_pool_id
        AND object_type_id = 14
        AND allows_viewing_children

UNION ALL

SELECT vm_guid,
    ad_element_id,
    role_type
FROM internal_permissions_view
CROSS JOIN vm_static
WHERE object_type_id = 1
    AND allows_viewing_children;

-- only user permissions
CREATE OR REPLACE VIEW user_vm_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM vm_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups
 WHERE vm_permissions_view_base.role_type = 2;

-- both admin and user permissions
CREATE OR REPLACE VIEW vm_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM vm_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on disk
-- The user has permissions on the disk directly
CREATE OR REPLACE VIEW user_disk_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 19
    AND role_type = 2 -- Or the user has permissions on the VM the disk is attached to

UNION ALL

SELECT device_id,
    user_vm_permissions_view.user_id AS ad_element_id
FROM vm_device
INNER JOIN user_vm_permissions_view
    ON user_vm_permissions_view.entity_id = vm_device.vm_id
WHERE vm_device.type = 'disk'
    AND vm_device.device = 'disk' -- Or the user has permissions on the template the disk is attached to

UNION ALL

SELECT device_id,
    user_vm_template_permissions_view.user_id AS ad_element_id
FROM vm_device
INNER JOIN user_vm_template_permissions_view
    ON user_vm_template_permissions_view.entity_id = vm_device.vm_id
WHERE type = 'disk'
    AND device = 'disk' -- Or the user has permissions on the storage domain containing the disk

UNION ALL

SELECT images.image_group_id,
    ad_element_id
FROM image_storage_domain_map
INNER JOIN images
    ON images.image_guid = image_storage_domain_map.image_id
INNER JOIN internal_permissions_view
    ON object_id = storage_domain_id
        AND object_type_id = 11
        AND allows_viewing_children
        AND role_type = 2 -- Or the user has permissions on the data center containing the storage pool constaining the disk

UNION ALL

SELECT images.image_group_id,
    ad_element_id
FROM image_storage_domain_map
INNER JOIN storage_pool_iso_map
    ON image_storage_domain_map.storage_domain_id = storage_pool_iso_map.storage_id
INNER JOIN images
    ON images.image_guid = image_storage_domain_map.image_id
INNER JOIN internal_permissions_view
    ON object_id = storage_pool_id
        AND object_type_id = 14
        AND allows_viewing_children
        AND role_type = 2 -- Or the user has permissions on system

UNION ALL

SELECT device_id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN vm_device
WHERE object_type_id = 1
    AND allows_viewing_children
    AND role_type = 2;

CREATE OR REPLACE VIEW user_disk_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_disk_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on permissions
CREATE OR REPLACE VIEW user_permissions_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT id,
    user_id
FROM internal_permissions_view
INNER JOIN engine_session_user_flat_groups
    ON granted_id = ad_element_id;

-- Direct permissions assigned to user
CREATE OR REPLACE VIEW user_object_permissions_view AS

SELECT DISTINCT permissions.object_id AS entity_id,
    engine_session_user_flat_groups.user_id
FROM permissions
INNER JOIN roles
    ON permissions.role_id = roles.id
INNER JOIN engine_session_user_flat_groups
    ON engine_session_user_flat_groups.granted_id = permissions.ad_element_id
WHERE permissions.ad_element_id != getGlobalIds('everyone');

-- Permissions to view users in db
CREATE OR REPLACE VIEW user_db_users_permissions_view AS

SELECT DISTINCT permissions.ad_element_id,
    roles_groups.role_id,
    roles_groups.action_group_id
FROM permissions
INNER JOIN roles_groups
    ON permissions.role_id = roles_groups.role_id
WHERE roles_groups.action_group_id = 502;

CREATE OR REPLACE VIEW vm_device_view AS

SELECT device_id,
    vm_id,
    type,
    device,
    address,
    spec_params,
    is_managed,
    is_plugged,
    is_readonly,
    alias,
    custom_properties,
    snapshot_id,
    logical_name,
    host_device
FROM vm_device;

-- Permissions on VNIC Profiles
-- The user has permissions on the Profile directly
CREATE OR REPLACE VIEW user_vnic_profile_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 27
    AND role_type = 2 -- Or the user has permissions on the Network in which the profile belongs to

UNION ALL

SELECT vnic_profiles.id,
    ad_element_id
FROM vnic_profiles
INNER JOIN internal_permissions_view
    ON object_id = network_id
WHERE object_type_id = 20
    AND allows_viewing_children
    AND role_type = 2 -- Or the user has permissions on the Profile-Network's Data-Center directly

UNION ALL

SELECT vnic_profiles.id,
    ad_element_id
FROM vnic_profiles
INNER JOIN network
    ON network.id = network_id
INNER JOIN internal_permissions_view
    ON object_id = network.storage_pool_id
WHERE object_type_id = 14
    AND role_type = 2
    AND allows_viewing_children -- Or the user has permissions on the Cluster the networks are assigned to

UNION ALL

SELECT vnic_profiles.id,
    ad_element_id
FROM vnic_profiles
INNER JOIN network_cluster
    ON network_cluster.network_id = vnic_profiles.network_id
INNER JOIN internal_permissions_view
    ON object_id = network_cluster.cluster_id
WHERE object_type_id = 9
    AND role_type = 2
    AND allows_viewing_children --Or the user has permissions on the VM with this profile

UNION ALL

SELECT DISTINCT vnic_profile_id,
    ad_element_id
FROM vm_interface
INNER JOIN internal_permissions_view
    ON object_id = vm_guid
INNER JOIN vm_static
    ON vm_interface.vm_guid = vm_static.vm_guid
WHERE entity_type = 'TEMPLATE'
    AND object_type_id = 2
    AND role_type = 2 -- Or the user has permissions on the Template with the profile

UNION ALL

SELECT DISTINCT vnic_profile_id,
    ad_element_id
FROM vm_interface
INNER JOIN internal_permissions_view
    ON object_id = vm_guid
INNER JOIN vm_static
    ON vm_interface.vm_guid = vm_static.vm_guid
WHERE entity_type = 'VM'
    AND  object_type_id = 4
    AND role_type = 2 -- Or the user has permissions on system

UNION ALL

SELECT vnic_profiles.id,
    ad_element_id
FROM internal_permissions_view
CROSS JOIN vnic_profiles
WHERE object_type_id = 1
    AND allows_viewing_children
    AND role_type = 2;

CREATE OR REPLACE VIEW user_vnic_profile_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_vnic_profile_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on Networks
CREATE OR REPLACE VIEW user_network_permissions_view_base (
    entity_id,
    granted_id
    ) AS -- Or the user has permissions on one of the Network's VNIC Profiles

SELECT network.id,
    user_id
FROM network
INNER JOIN vnic_profiles
    ON network_id = network.id
INNER JOIN user_vnic_profile_permissions_view
    ON entity_id = vnic_profiles.id;

CREATE OR REPLACE VIEW user_network_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_network_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

-- Permissions on disk profiles
-- The user has permissions on the disk profile directly
CREATE OR REPLACE VIEW user_disk_profile_permissions_view_base (
    entity_id,
    granted_id
    ) AS

SELECT object_id,
    ad_element_id
FROM internal_permissions_view
WHERE object_type_id = 29
    AND role_type = 2;

CREATE OR REPLACE VIEW user_disk_profile_permissions_view (
    entity_id,
    user_id
    ) AS

SELECT DISTINCT entity_id,
    user_id
FROM user_disk_profile_permissions_view_base NATURAL
 JOIN engine_session_user_flat_groups;

CREATE OR REPLACE VIEW gluster_volumes_view AS
SELECT gluster_volumes.*,
    cluster.name AS cluster_name,
    CASE
        WHEN EXISTS (
                SELECT session_id
                FROM gluster_georep_session
                WHERE master_volume_id = gluster_volumes.id
                )
            THEN true
        ELSE false
        END AS is_master,
    (
        SELECT vol.vol_name || '|' || cluster.name
        FROM gluster_georep_session
        INNER JOIN gluster_volumes vol
            ON master_volume_id = vol.id
        INNER JOIN cluster
            ON cluster.cluster_id = vol.cluster_id
        WHERE slave_volume_id = gluster_volumes.id
        ) AS master_vol_cluster
FROM gluster_volumes
INNER JOIN cluster
    ON gluster_volumes.cluster_id = cluster.cluster_id;

CREATE OR REPLACE VIEW gluster_volume_snapshots_view AS

SELECT gluster_volume_snapshots.*,
    gluster_volumes.cluster_id AS cluster_id,
    gluster_volumes.vol_name AS volume_name
FROM gluster_volume_snapshots
INNER JOIN gluster_volumes
    ON gluster_volume_snapshots.volume_id = gluster_volumes.id;

CREATE OR REPLACE VIEW gluster_volume_snapshot_schedules_view AS

SELECT gluster_volume_snapshot_schedules.*,
    gluster_volumes.cluster_id AS cluster_id
FROM gluster_volume_snapshot_schedules
INNER JOIN gluster_volumes
    ON gluster_volume_snapshot_schedules.volume_id = gluster_volumes.id;

CREATE OR REPLACE VIEW gluster_volume_bricks_view AS

SELECT gluster_volume_bricks.*,
    vds_static.host_name AS vds_name,
    gluster_volumes.vol_name AS volume_name,
    vds_interface.addr AS interface_address,
    gluster_volumes.cluster_id AS cluster_id
FROM gluster_volume_bricks
INNER JOIN vds_static
    ON vds_static.vds_id = gluster_volume_bricks.server_id
INNER JOIN gluster_volumes
    ON gluster_volumes.id = gluster_volume_bricks.volume_id
LEFT JOIN network
    ON network.id = gluster_volume_bricks.network_id
LEFT JOIN vds_interface
    ON vds_interface.vds_id = gluster_volume_bricks.server_id
        AND vds_interface.network_name = network.name;

CREATE OR REPLACE VIEW gluster_volume_task_steps AS

SELECT step.*,
    gluster_volumes.id AS volume_id,
    job.job_id AS job_job_id,
    job.action_type,
    job.description AS job_description,
    job.status AS job_status,
    job.start_time AS job_start_time,
    job.end_time AS job_end_time
FROM gluster_volumes
INNER JOIN job_subject_entity js
    ON js.entity_id = gluster_volumes.id
INNER JOIN job
    ON job.job_id = js.job_id
        AND job.action_type IN (
            'StartRebalanceGlusterVolume',
            'StartRemoveGlusterVolumeBricks'
            )
LEFT JOIN step
    ON step.external_id = gluster_volumes.task_id
        AND step.external_system_type = 'GLUSTER'
        AND step.job_id = js.job_id;

CREATE OR REPLACE VIEW gluster_server_services_view AS

SELECT gluster_server_services.*,
    gluster_services.service_name,
    gluster_services.service_type,
    vds_static.vds_name
FROM gluster_server_services
INNER JOIN gluster_services
    ON gluster_server_services.service_id = gluster_services.id
INNER JOIN vds_static
    ON gluster_server_services.server_id = vds_static.vds_id;

CREATE OR REPLACE VIEW gluster_server_hooks_view AS

SELECT gluster_server_hooks.*,
    vds_static.vds_name AS server_name
FROM gluster_server_hooks
INNER JOIN vds_static
    ON gluster_server_hooks.server_id = vds_static.vds_id;

CREATE OR REPLACE VIEW gluster_georep_sessions_view AS

SELECT session_id,
    master_volume_id,
    session_key,
    slave_host_uuid,
    slave_host_name,
    slave_volume_id,
    slave_volume_name,
    georep.status,
    georep._create_date,
    georep._update_date,
    gluster_volumes.vol_name AS master_volume_name,
    gluster_volumes.cluster_id AS cluster_id,
    georep.user_name
FROM gluster_georep_session georep
INNER JOIN gluster_volumes
    ON gluster_volumes.id = georep.master_volume_id;

CREATE OR REPLACE VIEW gluster_geo_rep_config_view AS

SELECT session_id,
    georepConfig.config_key,
    config_value,
    config_description,
    config_possible_values,
    _update_date
FROM gluster_georep_config georepConfig
LEFT JOIN gluster_config_master
    ON gluster_config_master.config_key = georepConfig.config_key
        AND gluster_config_master.config_feature = 'geo_replication';

CREATE OR REPLACE VIEW supported_cluster_features_view AS

SELECT cluster_features.*,
    supported_cluster_features.cluster_id,
    supported_cluster_features.is_enabled
FROM cluster_features
INNER JOIN supported_cluster_features
    ON supported_cluster_features.feature_id = cluster_features.feature_id;

-- Affinity Groups view, including members
CREATE OR REPLACE VIEW affinity_groups_view AS

SELECT affinity_groups.*,
    array_agg(affinity_group_members.vm_id::text) AS vm_ids,
    array_agg(vm_static.vm_name::text) AS vm_names,
    array_agg(affinity_group_members.vds_id::text) AS vds_ids,
    array_agg(vds_static.vds_name::text) AS vds_names,
    array_agg(affinity_group_members.vm_label_id::text) as vm_label_ids,
    array_agg(vm_labels.label_name::text) AS vm_label_names,
    array_agg(affinity_group_members.host_label_id::text) as host_label_ids,
    array_agg(vds_labels.label_name::text) AS host_label_names
FROM affinity_groups
LEFT JOIN affinity_group_members
    ON affinity_group_members.affinity_group_id = affinity_groups.id
LEFT JOIN vm_static
    ON vm_static.vm_guid = affinity_group_members.vm_id -- postgres 8.X issue, need to group by all fields.
LEFT JOIN vds_static
    ON vds_static.vds_id = affinity_group_members.vds_id
LEFT JOIN labels AS vm_labels
    ON vm_labels.label_id = affinity_group_members.vm_label_id
LEFT JOIN labels AS vds_labels
    ON vds_labels.label_id = affinity_group_members.host_label_id
GROUP BY affinity_groups.id,
    affinity_groups.name,
    affinity_groups.description,
    affinity_groups.cluster_id,
    affinity_groups.vm_positive,
    affinity_groups.vm_enforcing,
    affinity_groups.vds_positive,
    affinity_groups.vds_enforcing,
    affinity_groups.vms_affinity_enabled,
    affinity_groups.vds_affinity_enabled,
    affinity_groups.priority,
    affinity_groups._create_date,
    affinity_groups._update_date;


-- Affinity group members view, where labels are substituted by the VMs and hosts they contain
CREATE OR REPLACE VIEW affinity_groups_members_flat_labels_view AS

SELECT affinity_group_id as affinity_group_id,
    vm_id as vm_id,
    vds_id as vds_id
FROM affinity_group_members
WHERE vm_id IS NOT NULL OR vds_id IS NOT NULL

UNION

SELECT affinity_group_members.affinity_group_id as affinity_group_id,
    labels_map.vm_id as vm_id,
    null as vds_id
FROM affinity_group_members
JOIN labels_map ON affinity_group_members.vm_label_id = labels_map.label_id
WHERE labels_map.vm_id IS NOT NULL

UNION

SELECT affinity_group_members.affinity_group_id as group_id,
    null as vm_id,
    labels_map.vds_id as vds_id
FROM affinity_group_members
JOIN labels_map ON affinity_group_members.host_label_id = labels_map.label_id
WHERE labels_map.vds_id IS NOT NULL;


-- Affinity Groups view, including members from labels
CREATE OR REPLACE VIEW affinity_groups_with_members_from_labels_view AS

SELECT affinity_groups.*,
    array_agg(members.vm_id::text) AS vm_ids,
    array_agg(vm_static.vm_name::text) AS vm_names,
    array_agg(members.vds_id::text) AS vds_ids,
    array_agg(vds_static.vds_name::text) AS vds_names,
    '{}'::TEXT[] as vm_label_ids,
    '{}'::TEXT[] as vm_label_names,
    '{}'::TEXT[] as host_label_ids,
    '{}'::TEXT[] as host_label_names
FROM affinity_groups
LEFT JOIN affinity_groups_members_flat_labels_view AS members
    ON members.affinity_group_id = affinity_groups.id
LEFT JOIN vm_static
    ON vm_static.vm_guid = members.vm_id -- postgres 8.X issue, need to group by all fields.
LEFT JOIN vds_static
    ON vds_static.vds_id = members.vds_id
GROUP BY affinity_groups.id,
    affinity_groups.name,
    affinity_groups.description,
    affinity_groups.cluster_id,
    affinity_groups.vm_positive,
    affinity_groups.vm_enforcing,
    affinity_groups.vds_positive,
    affinity_groups.vds_enforcing,
    affinity_groups.vms_affinity_enabled,
    affinity_groups.vds_affinity_enabled,
    affinity_groups._create_date,
    affinity_groups._update_date;


-- Numa node cpus view
CREATE OR REPLACE VIEW numa_node_cpus_view AS

SELECT *
FROM numa_node
INNER JOIN
    (SELECT numa_node_id AS numa_node_id2,
        ARRAY_AGG(cpu_core_id ORDER BY cpu_core_id ASC) AS cpu_core_ids
    FROM numa_node_cpu_map
    GROUP BY numa_node_id
    ) numa_node_to_cpu_core_ids
    ON numa_node.numa_node_id = numa_node_to_cpu_core_ids.numa_node_id2;

-- Numa node assignment view
CREATE OR REPLACE VIEW numa_node_assignment_view AS

SELECT vm_vds_numa_node_map.vm_numa_node_id AS assigned_vm_numa_node_id,
    vm_vds_numa_node_map.vds_numa_node_index AS run_in_vds_numa_node_index,
    vm_numa_node.vm_id AS vm_numa_node_vm_id,
    vm_numa_node.numa_node_index AS vm_numa_node_index,
    vm_numa_node.mem_total AS vm_numa_node_mem_total,
    vm_numa_node.cpu_count AS vm_numa_node_cpu_count,
    vm_numa_node.mem_free AS vm_numa_node_mem_free,
    vm_numa_node.usage_mem_percent AS vm_numa_node_usage_mem_percent,
    vm_numa_node.cpu_sys AS vm_numa_node_cpu_sys,
    vm_numa_node.cpu_user AS vm_numa_node_cpu_user,
    vm_numa_node.cpu_idle AS vm_numa_node_cpu_idle,
    vm_numa_node.usage_cpu_percent AS vm_numa_node_usage_cpu_percent,
    vm_numa_node.distance AS vm_numa_node_distance,
    vm_numa_node.hugepages AS vm_numa_node_hugepages,
    vm_numa_node.numa_tune_mode AS vm_numa_node_numa_tune_mode
FROM vm_vds_numa_node_map
LEFT JOIN numa_node AS vm_numa_node
    ON vm_vds_numa_node_map.vm_numa_node_id = vm_numa_node.numa_node_id;

-- Numa node with vds group view
CREATE OR REPLACE VIEW numa_node_with_cluster_view AS

SELECT vm_numa_node.numa_node_id AS vm_numa_node_id,
    vm_numa_node.vm_id AS vm_numa_node_vm_id,
    vm_numa_node.numa_node_index AS vm_numa_node_index,
    vm_numa_node.mem_total AS vm_numa_node_mem_total,
    vm_numa_node.cpu_count AS vm_numa_node_cpu_count,
    vm_numa_node.mem_free AS vm_numa_node_mem_free,
    vm_numa_node.usage_mem_percent AS vm_numa_node_usage_mem_percent,
    vm_numa_node.cpu_sys AS vm_numa_node_cpu_sys,
    vm_numa_node.cpu_user AS vm_numa_node_cpu_user,
    vm_numa_node.cpu_idle AS vm_numa_node_cpu_idle,
    vm_numa_node.usage_cpu_percent AS vm_numa_node_usage_cpu_percent,
    vm_numa_node.distance AS vm_numa_node_distance,
    vm_numa_node.hugepages AS vm_numa_node_hugepages,
    vm_numa_node.numa_tune_mode AS vm_numa_node_numa_tune_mode,
    vm_static.cluster_id
FROM numa_node AS vm_numa_node
LEFT JOIN vm_static
    ON vm_numa_node.vm_id = vm_static.vm_guid;

CREATE OR REPLACE VIEW vm_host_pinning_view AS

SELECT host.vds_name,
    vm.vm_name,
    vm_vds.*
FROM vm_host_pinning_map AS vm_vds
INNER JOIN vm_static AS vm
    ON vm_vds.vm_id = vm.vm_guid
INNER JOIN vds_static AS host
    ON vm_vds.vds_id = host.vds_id;

CREATE OR REPLACE VIEW host_device_view AS

SELECT host_device.*,
    NULL::UUID AS configured_vm_id,
    NULL::VARCHAR AS spec_params,
    (
        SELECT array_to_string(array_agg(vm_host_pinning_view.vm_name), ',')
        FROM vm_device
        INNER JOIN vm_host_pinning_view
            ON vm_device.vm_id = vm_host_pinning_view.vm_id
        WHERE vm_device.device = host_device.device_name
            AND vm_host_pinning_view.vds_id = host_device.host_id
        ) AS attached_vm_names,
    (
        SELECT vm_name
        FROM vm_static
        WHERE vm_static.vm_guid = host_device.vm_id
        ) AS running_vm_name
FROM host_device;

CREATE OR REPLACE VIEW host_network_qos_of_migration_network_by_cluster AS

SELECT network_cluster.cluster_id, qos.*
FROM qos, network_cluster, network
WHERE network_cluster.network_id = network.id
      AND network_cluster.migration
      AND network.qos_id = qos.id;

CREATE OR REPLACE VIEW active_migration_network_interfaces AS

SELECT vds_interface_view.*
FROM vds_interface_view,
    vds_dynamic,
    network_attachments,
    network,
    network_cluster
WHERE vds_interface_view.id = network_attachments.nic_id
      AND network_attachments.network_id = network.id
      AND network.id = network_cluster.network_id
      AND network_cluster.migration
      AND network_cluster.cluster_id = vds_interface_view.cluster_id
      AND vds_dynamic.vds_id = vds_interface_view.vds_id
      AND vds_dynamic.status IN (3, 9)         -- Up, PreparingForMaintenance
      AND vds_interface_view.iface_status = 1; -- Up


CREATE OR REPLACE VIEW vm_host_device_view AS

SELECT host_device.*,
    vm_device.vm_id AS configured_vm_id,
    vm_device.spec_params AS spec_params,
    array_to_string(array_agg(vm_host_pinning_view.vm_name) OVER (
            PARTITION BY host_id,
            device_name
            ), ',') AS attached_vm_names,
    (
        SELECT vm_name
        FROM vm_static
        WHERE vm_static.vm_guid = host_device.vm_id
        ) AS running_vm_name
FROM vm_device
INNER JOIN vm_host_pinning_view
    ON vm_device.vm_id = vm_host_pinning_view.vm_id
INNER JOIN host_device
    ON host_device.device_name = vm_device.device
        AND vm_host_pinning_view.vds_id = host_device.host_id
WHERE vm_device.type = 'hostdev';

CREATE OR REPLACE VIEW user_profiles_view AS

SELECT user_profiles.*,
    users.username || '@' || users.domain AS login_name
FROM user_profiles
INNER JOIN users
    ON user_profiles.user_id = users.user_id;

CREATE OR REPLACE VIEW qos_for_vm_view AS

SELECT qos.*,
    vm_static.vm_guid AS vm_id

FROM qos
    INNER JOIN cpu_profiles
        ON qos.id = cpu_profiles.qos_id
    INNER JOIN vm_static
        ON vm_static.cpu_profile_id = cpu_profiles.id;

CREATE OR REPLACE VIEW qos_for_disk_profile_view AS

SELECT qos.*,
    disk_profiles.id AS disk_profile_id

FROM qos
    INNER JOIN disk_profiles
        ON qos.id = disk_profiles.qos_id;

CREATE OR REPLACE VIEW labels_map_view AS

-- the aggregated arrays have to be type-casted to strings, because of a missing functionality in pgsql JDBC driver
-- reading UUID arrays is not supported
-- see https://github.com/pgjdbc/pgjdbc/pull/50 and https://github.com/pgjdbc/pgjdbc/pull/81 for details

SELECT labels.label_id,
    labels.label_name,
    labels.read_only,
    labels.has_implicit_affinity_group,
    array_agg(labels_map.vm_id::text) as vm_ids,
    array_agg(labels_map.vds_id::text) as vds_ids
FROM labels
LEFT JOIN labels_map
    ON labels_map.label_id = labels.label_id
GROUP BY labels.label_id, labels.label_name, labels.read_only;



CREATE OR REPLACE VIEW disk_vm_element_extended AS
    SELECT dve.*, vd.is_plugged, vd.logical_name, vd.is_readonly
    FROM disk_vm_element dve
    JOIN vm_device vd
        ON dve.disk_id = vd.device_id
            AND dve.vm_id = vd.vm_id;



CREATE OR REPLACE VIEW iso_disks_as_repo_images AS
   SELECT sds.id AS repo_domain_id,
       bd.disk_id::VARCHAR(256) AS repo_image_id,
       img.size AS size,
       img.creation_date AS date_created,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)*1000)::BIGINT AS last_refreshed,
       1 AS file_type,
       bd.disk_alias::VARCHAR(256) AS repo_image_name,
       spim.storage_pool_id,
       spim.status
   FROM base_disks bd
   JOIN images img ON bd.disk_id = img.image_group_id
   JOIN image_storage_domain_map isdm ON isdm.image_id = img.image_guid
   JOIN storage_domain_static sds ON sds.id = isdm.storage_domain_id
   JOIN storage_pool_iso_map spim ON spim.storage_id = sds.id

   WHERE bd.disk_content_type=4;


CREATE OR REPLACE VIEW vm_interfaces_plugged_on_vm_not_down_view AS
    SELECT vm_interface.*
    FROM vm_interface
    INNER JOIN vm_dynamic
        ON vm_interface.vm_guid = vm_dynamic.vm_guid
    INNER JOIN vm_device
        ON vm_interface.id = vm_device.device_id
    WHERE vm_dynamic.status <> 0
        AND vm_device.is_plugged = true;
