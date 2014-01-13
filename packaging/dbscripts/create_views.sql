-- ----------------------------------------------------------------------
-- Views
-- ----------------------------------------------------------------------

CREATE OR REPLACE VIEW storage_domain_static_view
AS

SELECT     storage_domain_static.id as id,
			storage_domain_static.storage as storage,
			storage_domain_static.storage_name as storage_name,
			storage_domain_static.storage_description as storage_description,
			storage_domain_static.storage_comment as storage_comment,
			storage_pool_iso_map.storage_pool_id as storage_pool_id,
                        storage_pool_iso_map.status as status,
			storage_domain_static.storage_domain_type as storage_domain_type,
			storage_domain_static.storage_type as storage_type,
                        storage_domain_static.storage_domain_format_type as storage_domain_format_type,
            storage_domain_static.last_time_used_as_master as last_time_used_as_master,
            storage_domain_static.wipe_after_delete as wipe_after_delete,
			storage_pool.name as storage_pool_name,
			unregistered_entities.storage_domain_id IS NOT NULL AS contains_unregistered_entities
FROM        storage_domain_static LEFT OUTER JOIN
storage_pool_iso_map on storage_pool_iso_map.storage_id = storage_domain_static.id
LEFT OUTER JOIN storage_pool ON storage_pool.id = storage_pool_iso_map.storage_pool_id
LEFT OUTER JOIN (SELECT DISTINCT storage_domain_id
                 FROM unregistered_ovf_of_entities) AS unregistered_entities ON unregistered_entities.storage_domain_id = storage_domain_static.id;

CREATE OR REPLACE VIEW vms_for_disk_view
AS
SELECT array_agg(vm_name) as array_vm_names,device_id,entity_type
FROM vm_static
JOIN vm_device ON vm_static.vm_guid = vm_device.vm_id
WHERE device = 'disk'
GROUP BY device_id, entity_type;


CREATE OR REPLACE VIEW images_storage_domain_view
AS

-- TODO: Change code to treat disks values directly instead of through this view.
SELECT images.image_guid as image_guid,
    storage_domain_static_view.storage_name as storage_name,
    storage_domain_static_view.storage as storage_path,
	storage_domain_static_view.storage_pool_id as storage_pool_id,
	storage_domain_static_view.storage_type as storage_type,
	images.creation_date as creation_date,
    images.size as size,
    images.it_guid as it_guid,
    snapshots.description as description,
    images.ParentId as ParentId,
    images.lastModified as lastModified,
    snapshots.app_list as app_list,
    image_storage_domain_map.storage_domain_id as storage_id,
    images.vm_snapshot_id as vm_snapshot_id,
    images.volume_type as volume_type,
    images.volume_format as volume_format,
    images.imageStatus as imageStatus,
    images.image_group_id as image_group_id,
    images.active,
    vms_for_disk_view.entity_type as entity_type,
    array_to_string(vms_for_disk_view.array_vm_names, ',') as vm_names,
    COALESCE(array_upper(vms_for_disk_view.array_vm_names,1),0) as number_of_vms,
    base_disks.disk_id,
    base_disks.disk_alias as disk_alias,
    base_disks.disk_description as disk_description,
    base_disks.shareable as shareable,
    base_disks.disk_interface,
    base_disks.wipe_after_delete as wipe_after_delete,
    base_disks.propagate_errors,
    base_disks.boot as boot,
    base_disks.sgio as sgio,
    image_storage_domain_map.quota_id as quota_id,
    quota.quota_name as quota_name,
    storage_pool.quota_enforcement_type,
    image_storage_domain_map.disk_profile_id as disk_profile_id,
    disk_profiles.name as disk_profile_name,
    disk_image_dynamic.actual_size as actual_size,
    disk_image_dynamic.read_rate as read_rate,
    disk_image_dynamic.write_rate as write_rate,
    disk_image_dynamic.read_latency_seconds as read_latency_seconds,
    disk_image_dynamic.write_latency_seconds as write_latency_seconds,
    disk_image_dynamic.flush_latency_seconds as flush_latency_seconds,
    base_disks.alignment as alignment,
    base_disks.last_alignment_scan as last_alignment_scan,
    EXISTS (SELECT 1 FROM storage_domains_ovf_info WHERE images.image_group_id = storage_domains_ovf_info.ovf_disk_id) as ovf_store,
    storage_domain_static_view.contains_unregistered_entities as contains_unregistered_entities
FROM
images
left outer join disk_image_dynamic on images.image_guid = disk_image_dynamic.image_id
LEFT OUTER JOIN base_disks ON images.image_group_id = base_disks.disk_id
LEFT OUTER JOIN vms_for_disk_view on vms_for_disk_view.device_id = images.image_group_id
LEFT JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
LEFT OUTER JOIN storage_domain_static_view ON image_storage_domain_map.storage_domain_id = storage_domain_static_view.id
LEFT OUTER JOIN snapshots ON images.vm_snapshot_id = snapshots.snapshot_id
LEFT OUTER JOIN quota ON image_storage_domain_map.quota_id = quota.id
LEFT OUTER JOIN disk_profiles ON image_storage_domain_map.disk_profile_id = disk_profiles.id
LEFT OUTER JOIN storage_pool ON storage_pool.id = storage_domain_static_view.storage_pool_id
WHERE images.image_guid != '00000000-0000-0000-0000-000000000000';


CREATE OR REPLACE VIEW storage_domain_file_repos
AS
SELECT
storage_domain_static.id as storage_domain_id,
			storage_domain_static.storage_domain_type as storage_domain_type,
	        storage_pool_iso_map.storage_pool_id as storage_pool_id,
	       	storage_pool_iso_map.status as storage_domain_status,
	repo_file_meta_data.repo_image_id as repo_image_id,
		repo_file_meta_data.size as size,
		repo_file_meta_data.date_created as date_created,
		repo_file_meta_data.last_refreshed as last_refreshed,
		repo_file_meta_data.file_type as file_type,
	vds_dynamic.status as vds_status,
    storage_pool.status as storage_pool_status
FROM storage_domain_static
INNER JOIN storage_pool_iso_map ON storage_domain_static.id = storage_pool_iso_map.storage_id
INNER JOIN storage_pool ON storage_pool.id = storage_pool_iso_map.storage_pool_id
INNER JOIN vds_dynamic ON vds_dynamic.vds_id = storage_pool.spm_vds_id
LEFT OUTER JOIN repo_file_meta_data ON storage_pool_iso_map.storage_id = repo_file_meta_data.repo_domain_id;

CREATE OR REPLACE VIEW storage_for_image_view
AS
SELECT images.image_guid as image_id,
	   array_to_string(array_agg(storage_domain_static.storage), ',') as storage_path,
	   array_to_string(array_agg(storage_domain_static.id), ',') storage_id,
	   array_to_string(array_agg(storage_domain_static.storage_type), ',') storage_type,
	   array_to_string(array_agg(storage_domain_static.storage_name), ',') as storage_name,
	   array_to_string(array_agg(COALESCE(CAST(quota.id as varchar), '')), ',') as quota_id,
	   array_to_string(array_agg(COALESCE(quota.quota_name, '')), ',') as quota_name,
           array_to_string(array_agg(COALESCE(CAST(disk_profiles.id as varchar), '')), ',') as disk_profile_id,
	   array_to_string(array_agg(COALESCE(disk_profiles.name, '')), ',') as disk_profile_name
FROM images
LEFT JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
LEFT OUTER JOIN storage_domain_static ON image_storage_domain_map.storage_domain_id = storage_domain_static.id
LEFT OUTER JOIN quota ON image_storage_domain_map.quota_id = quota.id
LEFT OUTER JOIN disk_profiles ON image_storage_domain_map.disk_profile_id = disk_profiles.id
GROUP BY images.image_guid;

CREATE OR REPLACE VIEW vm_images_view
AS
SELECT                storage_for_image_view.storage_id as storage_id, storage_for_image_view.storage_path as storage_path, storage_for_image_view.storage_name as storage_name,
					  storage_for_image_view.storage_type, images_storage_domain_view.storage_pool_id as storage_pool_id, images_storage_domain_view.image_guid as image_guid,
                      images_storage_domain_view.creation_date as creation_date, disk_image_dynamic.actual_size as actual_size, disk_image_dynamic.read_rate as read_rate,
                      disk_image_dynamic.read_latency_seconds as read_latency_seconds, disk_image_dynamic.write_latency_seconds as write_latency_seconds,
                      disk_image_dynamic.flush_latency_seconds as flush_latency_seconds, disk_image_dynamic.write_rate as write_rate,
                      images_storage_domain_view.size as size, images_storage_domain_view.it_guid as it_guid,
                      images_storage_domain_view.description as description,
                      images_storage_domain_view.ParentId as ParentId, images_storage_domain_view.imageStatus as imageStatus, images_storage_domain_view.lastModified as lastModified,
                      images_storage_domain_view.app_list as app_list, images_storage_domain_view.vm_snapshot_id as vm_snapshot_id,
                      images_storage_domain_view.volume_type as volume_type, images_storage_domain_view.image_group_id as image_group_id,
                      images_storage_domain_view.active as active, images_storage_domain_view.volume_format as volume_format,
                      images_storage_domain_view.disk_interface as disk_interface, images_storage_domain_view.boot as boot, images_storage_domain_view.wipe_after_delete as wipe_after_delete, images_storage_domain_view.propagate_errors as propagate_errors, images_storage_domain_view.sgio as sgio,
                      images_storage_domain_view.entity_type as entity_type,images_storage_domain_view.number_of_vms as number_of_vms,images_storage_domain_view.vm_names as vm_names,
                      storage_for_image_view.quota_id as quota_id, storage_for_image_view.quota_name as quota_name, images_storage_domain_view.quota_enforcement_type,
                      storage_for_image_view.disk_profile_id as disk_profile_id, storage_for_image_view.disk_profile_name as disk_profile_name,
                      images_storage_domain_view.disk_id, images_storage_domain_view.disk_alias as disk_alias, images_storage_domain_view.disk_description as disk_description,images_storage_domain_view.shareable as shareable,
                      images_storage_domain_view.alignment as alignment, images_storage_domain_view.last_alignment_scan as last_alignment_scan, images_storage_domain_view.ovf_store as ovf_store
FROM         images_storage_domain_view
INNER JOIN disk_image_dynamic ON images_storage_domain_view.image_guid = disk_image_dynamic.image_id
INNER JOIN storage_for_image_view ON images_storage_domain_view.image_guid = storage_for_image_view.image_id
WHERE images_storage_domain_view.active = TRUE;


CREATE OR REPLACE VIEW all_disks_including_snapshots
AS
SELECT storage_impl.*,
       bd.disk_id, -- Disk fields
       bd.disk_interface,
       bd.wipe_after_delete,
       bd.propagate_errors,
       bd.disk_alias,
       bd.disk_description,
       bd.shareable,
       bd.boot,
       bd.sgio,
       bd.alignment,
       bd.last_alignment_scan
FROM
(
    SELECT 0 AS disk_storage_type,
           storage_for_image_view.storage_id as storage_id, -- Storage fields
           storage_for_image_view.storage_path as storage_path,
           storage_for_image_view.storage_name as storage_name,
           storage_for_image_view.storage_type as storage_type,
           storage_pool_id,
           image_guid, -- Image fields
           creation_date,
           actual_size,
           read_rate,
           write_rate,
           read_latency_seconds,
           write_latency_seconds,
           flush_latency_seconds,
           size,
           it_guid,
           imageStatus,
           lastModified,
           volume_type,
           volume_format,
           image_group_id,
           description, -- Snapshot fields
           ParentId,
           app_list,
           vm_snapshot_id,
           active,
           entity_type,
           number_of_vms,
           vm_names,
           storage_for_image_view.quota_id as quota_id, -- Quota fields
           storage_for_image_view.quota_name as quota_name,
           quota_enforcement_type,
           ovf_store,
           storage_for_image_view.disk_profile_id as disk_profile_id, -- disk profile fields
           storage_for_image_view.disk_profile_name as disk_profile_name,
           null AS lun_id, -- LUN fields
           null AS physical_volume_id,
           null AS volume_group_id,
           null AS serial,
           null AS lun_mapping,
           null AS vendor_id,
           null AS product_id,
           null AS device_size
    FROM images_storage_domain_view
    INNER JOIN storage_for_image_view ON images_storage_domain_view.image_guid = storage_for_image_view.image_id
    GROUP BY storage_for_image_view.storage_id,
           storage_for_image_view.storage_path,
           storage_for_image_view.storage_name,
           storage_for_image_view.storage_type,
           storage_pool_id,
           image_guid, -- Image fields
           creation_date,
           actual_size,
           read_rate,
           write_rate,
           read_latency_seconds,
           write_latency_seconds,
           flush_latency_seconds,
           size,
           it_guid,
           imageStatus,
           lastModified,
           volume_type,
           volume_format,
           image_group_id,
           description, -- Snapshot fields
           ParentId,
           app_list,
           vm_snapshot_id,
           active,
           entity_type,
           number_of_vms,
           vm_names,
           storage_for_image_view.quota_id,
           storage_for_image_view.quota_name,
           quota_enforcement_type,
           ovf_store,
           storage_for_image_view.disk_profile_id,
           storage_for_image_view.disk_profile_name
    UNION ALL
    SELECT 1 AS disk_storage_type,
           null AS storage_id, -- Storage domain fields
           null AS storage_path,
           null AS storage_name,
           null AS storage_type,
           null AS storage_pool_id,
           null AS image_guid, -- Image fields
           null AS creation_date,
           null AS actual_size,
           null AS read_rate,
           null AS write_rate,
           null AS read_latency_seconds,
           null AS write_latency_seconds,
           null AS flush_latency_seconds,
           null AS size,
           null AS it_guid,
           null AS imageStatus,
           null AS lastModified,
           null AS volume_type,
           null AS volume_format,
           dlm.disk_id AS image_group_id,
           null AS description, -- Snapshot fields
           null AS ParentId,
           null AS app_list,
           null AS vm_snapshot_id,
           null AS active,
           vms_for_disk_view.entity_type,
           COALESCE(array_upper(vms_for_disk_view.array_vm_names,1),0) as number_of_vms,
           array_to_string(vms_for_disk_view.array_vm_names, ',') as vm_names,
           null AS quota_id, -- Quota fields
           null AS quota_name,
           null AS quota_enforcement_type,
           false as ovf_store,
           null AS disk_profile_id, -- disk profile fields
           null AS disk_profile_name,
           l.lun_id, -- LUN fields
           l.physical_volume_id,
           l.volume_group_id,
           l.serial,
           l.lun_mapping,
           l.vendor_id,
           l.product_id,
           l.device_size
    FROM disk_lun_map dlm
    JOIN luns l ON l.lun_id = dlm.lun_id
    LEFT JOIN vms_for_disk_view on vms_for_disk_view.device_id = dlm.disk_id
) AS storage_impl
JOIN base_disks bd ON bd.disk_id = storage_impl.image_group_id;

CREATE OR REPLACE VIEW all_disks
AS
SELECT *
FROM all_disks_including_snapshots
WHERE active IS NULL OR active = TRUE;


CREATE OR REPLACE VIEW all_disks_for_vms
AS
SELECT all_disks_including_snapshots.*, vm_device.is_plugged, vm_device.is_readonly, vm_device.logical_name, vm_device.vm_id
FROM all_disks_including_snapshots
JOIN vm_device ON vm_device.device_id = all_disks_including_snapshots.image_group_id
WHERE ((vm_device.snapshot_id IS NULL AND all_disks_including_snapshots.active IS NOT FALSE)
OR vm_device.snapshot_id = all_disks_including_snapshots.vm_snapshot_id);


CREATE OR REPLACE VIEW storage_domains
AS
SELECT
storage_domain_static.id as id,
		storage_domain_static.storage as storage,
		storage_domain_static.storage_name as storage_name,
		storage_domain_static.storage_description as storage_description,
		storage_domain_static.storage_comment as storage_comment,
        storage_pool_iso_map.storage_pool_id as storage_pool_id,
		storage_domain_dynamic.available_disk_size as available_disk_size,
		storage_domain_dynamic.used_disk_size as used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
		fn_get_actual_images_size_by_storage(storage_domain_static.id) as actual_images_size,
        storage_pool_iso_map.status as status,
		storage_pool.name as storage_pool_name,
		storage_domain_static.storage_type as storage_type,
		storage_domain_static.storage_domain_type as storage_domain_type,
                storage_domain_static.storage_domain_format_type as storage_domain_format_type,
        storage_domain_static.last_time_used_as_master as last_time_used_as_master,
    storage_domain_static.wipe_after_delete as wipe_after_delete,
        fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_pool_iso_map.status,storage_domain_static.storage_domain_type) as storage_domain_shared_status,
	storage_domain_static.recoverable as recoverable,
	unregistered_entities.storage_domain_id IS NOT NULL AS contains_unregistered_entities
FROM    storage_domain_static
INNER JOIN storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id
LEFT OUTER JOIN storage_pool_iso_map ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT OUTER JOIN storage_pool ON storage_pool_iso_map.storage_pool_id = storage_pool.id
LEFT OUTER JOIN (SELECT DISTINCT storage_domain_id
                 FROM unregistered_ovf_of_entities) AS unregistered_entities ON unregistered_entities.storage_domain_id = storage_domain_static.id;




CREATE OR REPLACE VIEW storage_domains_without_storage_pools
AS
SELECT DISTINCT
storage_domain_static.id as id, storage_domain_static.storage as storage, storage_domain_static.storage_name as storage_name, storage_domain_static.storage_description as storage_description,
		storage_domain_static.storage_comment as storage_comment, storage_domain_static.storage_type as storage_type, storage_domain_static.storage_domain_type as storage_domain_type,
                storage_domain_static.storage_domain_format_type as storage_domain_format_type,
        storage_domain_static.last_time_used_as_master as last_time_used_as_master,
        storage_domain_static.wipe_after_delete as wipe_after_delete,
		null as storage_pool_id, null as storage_pool_name,
		storage_domain_dynamic.available_disk_size as available_disk_size,
		storage_domain_dynamic.used_disk_size as used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
		fn_get_actual_images_size_by_storage(storage_domain_static.id) as actual_images_size,
	        null as status,
        fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_pool_iso_map.status,storage_domain_static.storage_domain_type) as storage_domain_shared_status,
		storage_domain_static.recoverable as recoverable,
		unregistered_entities.storage_domain_id IS NOT NULL AS contains_unregistered_entities
FROM
storage_domain_static
INNER JOIN storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id
LEFT OUTER JOIN storage_pool_iso_map ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT OUTER JOIN (SELECT DISTINCT storage_domain_id
                 FROM unregistered_ovf_of_entities) AS unregistered_entities ON unregistered_entities.storage_domain_id = storage_domain_static.id;


CREATE OR REPLACE VIEW storage_domains_for_search
AS
SELECT
                storage_domain_static.id as id, storage_domain_static.storage as storage, storage_domain_static.storage_name as storage_name, storage_domain_static.storage_description as storage_description,
                storage_domain_static.storage_comment as storage_comment, storage_domain_static.storage_type as storage_type, storage_domain_static.storage_domain_type as storage_domain_type,
                storage_domain_static.storage_domain_format_type as storage_domain_format_type,
                storage_domain_static.last_time_used_as_master as last_time_used_as_master,
                storage_domain_static.wipe_after_delete as wipe_after_delete,
                CASE
                          WHEN status_table.is_multi_domain THEN NULL
                          WHEN status_table.status IS NULL THEN 2 -- in case domain is unattached
                          ELSE status_table.status END as status,
                status_table.storage_pool_ids[1] as storage_pool_id,
                status_table.pool_names AS storage_pool_name,
                storage_domain_dynamic.available_disk_size as available_disk_size,
                storage_domain_dynamic.used_disk_size as used_disk_size,
                fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
		fn_get_actual_images_size_by_storage(storage_domain_static.id) as actual_images_size,
                fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,status_table.status,storage_domain_static.storage_domain_type) as storage_domain_shared_status,
                storage_domain_static.recoverable as recoverable,
                unregistered_entities.storage_domain_id IS NOT NULL AS contains_unregistered_entities
FROM
                storage_domain_static
INNER JOIN
                storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id
LEFT OUTER JOIN
                (SELECT storage_id,
                        count(storage_id) > 1 AS is_multi_domain,
                        max(storage_pool_iso_map.status) AS status,
                        array_to_string(array_agg(storage_pool.name), ',') AS pool_names,
                        CASE WHEN COUNT(distinct storage_pool.id) = 1 THEN array_agg(storage_pool.id) ELSE NULL END as storage_pool_ids
                 FROM storage_pool_iso_map
                 JOIN storage_pool ON storage_pool_iso_map.storage_pool_id = storage_pool.id
                 GROUP BY storage_id) AS status_table ON storage_domain_static.id=status_table.storage_id
LEFT OUTER JOIN (SELECT DISTINCT storage_domain_id
                 FROM unregistered_ovf_of_entities) AS unregistered_entities ON unregistered_entities.storage_domain_id = storage_domain_static.id;



CREATE OR REPLACE VIEW luns_view
AS
SELECT
luns.*, storage_domain_static.id as storage_id, storage_domain_static.storage_name as storage_name,
        disk_lun_map.disk_id as disk_id, all_disks.disk_alias as disk_alias
FROM luns
LEFT OUTER JOIN storage_domain_static ON luns.volume_group_id = storage_domain_static.storage
LEFT OUTER JOIN disk_lun_map ON luns.lun_id = disk_lun_map.lun_id
LEFT OUTER JOIN all_disks ON disk_lun_map.disk_id = all_disks.disk_id;



CREATE OR REPLACE VIEW vm_templates_view
AS

SELECT
vm_templates.vm_guid as vmt_guid,
       vm_templates.vm_name as name,
       vm_templates.mem_size_mb as mem_size_mb,
       vm_templates.os as os,
       vm_templates.creation_date as creation_date,
       vm_templates.child_count as child_count,
       vm_templates.num_of_sockets as num_of_sockets,
       vm_templates.cpu_per_socket as cpu_per_socket,
       vm_templates.num_of_sockets*vm_templates.cpu_per_socket as num_of_cpus,
       vm_templates.description as description,
       vm_templates.free_text_comment as free_text_comment,
       vm_templates.vds_group_id as vds_group_id,
       vm_templates.num_of_monitors as num_of_monitors,
       vm_templates.single_qxl_pci as single_qxl_pci,
       vm_templates.allow_console_reconnect as allow_console_reconnect,
       vm_templates.template_status as status,
       vm_templates.usb_policy as usb_policy,
       vm_templates.time_zone as time_zone,
       vm_templates.fail_back as fail_back,
       vds_groups.name as vds_group_name,
       vds_groups.trusted_service as trusted_service,
       vm_templates.vm_type as vm_type,
       vm_templates.nice_level as nice_level,
       vm_templates.cpu_shares as cpu_shares,
       storage_pool.id as storage_pool_id,
       storage_pool.name as storage_pool_name,
       storage_pool.quota_enforcement_type as quota_enforcement_type,
       vm_templates.default_boot_sequence as default_boot_sequence,
       vm_templates.default_display_type as default_display_type,
       vm_templates.priority as priority,
       vm_templates.auto_startup as auto_startup,
       vm_templates.is_stateless as is_stateless,
       vm_templates.is_smartcard_enabled as is_smartcard_enabled,
       vm_templates.is_delete_protected as is_delete_protected,
       vm_templates.sso_method as sso_method,
       vm_templates.iso_path as iso_path,
       vm_templates.origin as origin,
       vm_templates.initrd_url as initrd_url,
       vm_templates.kernel_url as kernel_url,
       vm_templates.kernel_params as kernel_params,
       vm_templates.quota_id as quota_id,
       quota.quota_name as quota_name,
       vm_templates.db_generation as db_generation,
       vm_templates.migration_support,
       vm_templates.dedicated_vm_for_vds,
       vm_templates.is_disabled,
       vm_templates.tunnel_migration,
       vm_templates.vnc_keyboard_layout as vnc_keyboard_layout,
       vm_templates.min_allocated_mem as min_allocated_mem,
       vm_templates.is_run_and_pause as is_run_and_pause,
       vm_templates.created_by_user_id as created_by_user_id,
       vm_templates.entity_type,
       vm_templates.migration_downtime as migration_downtime,
       vds_groups.architecture as architecture,
       vm_templates.template_version_number as template_version_number,
       vm_templates.vmt_guid as base_template_id,
       vm_templates.template_version_name as template_version_name,
       vm_templates.serial_number_policy as serial_number_policy,
       vm_templates.custom_serial_number as custom_serial_number,
       vm_templates.is_boot_menu_enabled as is_boot_menu_enabled,
       vm_templates.is_spice_file_transfer_enabled as is_spice_file_transfer_enabled,
       vm_templates.is_spice_copy_paste_enabled as is_spice_copy_paste_enabled,
       vm_templates.cpu_profile_id as cpu_profile_id,
       vm_templates.numatune_mode as numatune_mode,
       vm_templates.is_auto_converge as is_auto_converge, vm_templates.is_migrate_compressed as is_migrate_compressed,
       vm_templates.predefined_properties as predefined_properties,
       vm_templates.userdefined_properties as userdefined_properties,
       vm_templates.custom_emulated_machine as custom_emulated_machine,
       vm_templates.custom_cpu_name as custom_cpu_name
FROM       vm_static AS vm_templates  LEFT OUTER JOIN
vds_groups ON vm_templates.vds_group_id = vds_groups.vds_group_id
left outer JOIN
storage_pool ON storage_pool.id = vds_groups.storage_pool_id
left outer JOIN
quota ON vm_templates.quota_id = quota.id
WHERE entity_type = 'TEMPLATE' OR entity_type = 'INSTANCE_TYPE' OR entity_type = 'IMAGE_TYPE';



CREATE OR REPLACE VIEW vm_templates_with_plug_info
as
SELECT vm_templates_view.*, image_guid, image_group_id, is_plugged
FROM vm_templates_view
INNER JOIN vm_device vd ON vd.vm_id = vm_templates_view.vmt_guid
INNER JOIN images ON images.image_group_id = vd.device_id AND images.active = TRUE;

CREATE OR REPLACE VIEW vm_templates_storage_domain
AS
	SELECT            vm_templates.vm_guid AS vmt_guid, vm_templates.vm_name AS name, vm_templates.mem_size_mb,
                      vm_templates.os, vm_templates.creation_date,
                      vm_templates.child_count, vm_templates.num_of_sockets, vm_templates.cpu_per_socket,
	                  vm_templates.num_of_sockets*vm_templates.cpu_per_socket AS num_of_cpus, vm_templates.description, vm_templates.free_text_comment,
	                  vm_templates.vds_group_id, vm_templates.num_of_monitors, vm_templates.single_qxl_pci, vm_templates.allow_console_reconnect, vm_templates.template_status AS status,
	                  vm_templates.usb_policy, vm_templates.time_zone, vm_templates.fail_back,
	                  vds_groups.name AS vds_group_name, vm_templates.vm_type, vm_templates.nice_level, vm_templates.cpu_shares, storage_pool.id AS storage_pool_id, storage_pool.name
                      AS storage_pool_name,
	                  vm_templates.default_boot_sequence, vm_templates.default_display_type, vm_templates.priority, vm_templates.auto_startup,
	                  vm_templates.is_stateless, vm_templates.iso_path, vm_templates.origin, vm_templates.initrd_url, vm_templates.kernel_url,
	                  vm_templates.kernel_params, image_storage_domain_map.storage_domain_id AS storage_id,
                    quota.quota_name as quota_name, vm_templates.is_disabled, vm_templates.min_allocated_mem, vm_templates.is_run_and_pause, vm_templates.created_by_user_id,
                    vm_templates.migration_downtime,
                    vm_templates.entity_type, vds_groups.architecture,
                    vm_templates.template_version_number as template_version_number, vm_templates.vmt_guid as base_template_id,
                    vm_templates.template_version_name as template_version_name,
                    vm_templates.serial_number_policy as serial_number_policy, vm_templates.custom_serial_number as custom_serial_number,
                    vm_templates.is_boot_menu_enabled as is_boot_menu_enabled,
                    vm_templates.is_spice_file_transfer_enabled as is_spice_file_transfer_enabled, vm_templates.is_spice_copy_paste_enabled as is_spice_copy_paste_enabled,
                    vm_templates.cpu_profile_id as cpu_profile_id, vm_templates.numatune_mode as numatune_mode,
                    vm_templates.is_auto_converge as is_auto_converge, vm_templates.is_migrate_compressed as is_migrate_compressed,
                    vm_templates.predefined_properties as predefined_properties, vm_templates.userdefined_properties as userdefined_properties

FROM                  vm_static AS vm_templates LEFT OUTER JOIN
	                  vds_groups ON vm_templates.vds_group_id = vds_groups.vds_group_id LEFT OUTER JOIN
                      storage_pool ON storage_pool.id = vds_groups.storage_pool_id INNER JOIN
                      vm_device ON vm_device.vm_id = vm_templates.vm_guid LEFT JOIN
	                  images ON images.image_group_id = vm_device.device_id
	                  LEFT JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
                          LEFT OUTER JOIN quota quota on quota.id = vm_templates.quota_id
WHERE      entity_type = 'TEMPLATE' OR entity_type = 'INSTANCE_TYPE' OR entity_type = 'IMAGE_TYPE'
UNION
SELECT                vm_templates_1.vm_guid AS vmt_guid, vm_templates_1.vm_name AS name, vm_templates_1.mem_size_mb, vm_templates_1.os, vm_templates_1.creation_date,
                      vm_templates_1.child_count, vm_templates_1.num_of_sockets, vm_templates_1.cpu_per_socket,
                      vm_templates_1.num_of_sockets*vm_templates_1.cpu_per_socket AS num_of_cpus, vm_templates_1.description, vm_templates_1.free_text_comment, vm_templates_1.vds_group_id,
                      vm_templates_1.num_of_monitors, vm_templates_1.single_qxl_pci, vm_templates_1.allow_console_reconnect, vm_templates_1.template_status AS status, vm_templates_1.usb_policy, vm_templates_1.time_zone,
                      vm_templates_1.fail_back, vds_groups_1.name AS vds_group_name, vm_templates_1.vm_type,
                      vm_templates_1.nice_level, vm_templates_1.cpu_shares, storage_pool_1.id AS storage_pool_id,
                      storage_pool_1.name AS storage_pool_name, vm_templates_1.default_boot_sequence, vm_templates_1.default_display_type,
                      vm_templates_1.priority, vm_templates_1.auto_startup, vm_templates_1.is_stateless, vm_templates_1.iso_path, vm_templates_1.origin,
                      vm_templates_1.initrd_url, vm_templates_1.kernel_url, vm_templates_1.kernel_params,
                      image_storage_domain_map.storage_domain_id AS storage_id,
                      quota.quota_name as quota_name, vm_templates_1.is_disabled, vm_templates_1.min_allocated_mem, vm_templates_1.is_run_and_pause, vm_templates_1.created_by_user_id,
                      vm_templates_1.migration_downtime,
                      vm_templates_1.entity_type, vds_groups_1.architecture,
                      vm_templates_1.template_version_number as template_version_number, vm_templates_1.vmt_guid as base_template_id,
                      vm_templates_1.template_version_name as template_version_name,
                      vm_templates_1.serial_number_policy as serial_number_policy,
                      vm_templates_1.custom_serial_number as custom_serial_number,
                      vm_templates_1.is_boot_menu_enabled as is_boot_menu_enabled,
                      vm_templates_1.is_spice_file_transfer_enabled as is_spice_file_transfer_enabled,
                      vm_templates_1.is_spice_copy_paste_enabled as is_spice_copy_paste_enabled,
                      vm_templates_1.cpu_profile_id as cpu_profile_id,
                      vm_templates_1.numatune_mode as numatune_mode,
                      vm_templates_1.is_auto_converge as is_auto_converge, vm_templates_1.is_migrate_compressed as is_migrate_compressed,
                      vm_templates_1.predefined_properties as predefined_properties, vm_templates_1.userdefined_properties as userdefined_properties
FROM                  vm_static AS vm_templates_1 LEFT OUTER JOIN
                      vds_groups AS vds_groups_1 ON vm_templates_1.vds_group_id = vds_groups_1.vds_group_id LEFT OUTER JOIN
                      storage_pool AS storage_pool_1 ON storage_pool_1.id = vds_groups_1.storage_pool_id INNER JOIN
                      vm_device AS vm_device_1 ON vm_device_1.vm_id = vm_templates_1.vm_guid INNER JOIN
                      images AS images_1 ON images_1.image_group_id = vm_device_1.device_id INNER JOIN
                      image_storage_domain_map ON image_storage_domain_map.image_id = images_1.image_guid
                      LEFT OUTER JOIN quota quota on quota.id = vm_templates_1.quota_id
WHERE                 entity_type = 'TEMPLATE' OR entity_type = 'INSTANCE_TYPE' OR entity_type = 'IMAGE_TYPE';


CREATE OR REPLACE VIEW instance_types_view
AS
	SELECT * from vm_templates_view where entity_type = 'INSTANCE_TYPE';

CREATE OR REPLACE VIEW instance_types_storage_domain
AS
	SELECT * from vm_templates_storage_domain where entity_type = 'INSTANCE_TYPE';


CREATE OR REPLACE VIEW image_types_view
AS
	SELECT * from vm_templates_view where entity_type = 'IMAGE_TYPE';

CREATE OR REPLACE VIEW image_types_storage_domain
AS
	SELECT * from vm_templates_storage_domain where entity_type = 'IMAGE_TYPE';


CREATE OR REPLACE VIEW vm_pool_map_view
AS
SELECT
vm_pool_map.vm_guid as vm_guid,
vm_pool_map.vm_pool_id as vm_pool_id,
vm_pools.vm_pool_name as vm_pool_name,
vm_pools.spice_proxy as vm_pool_spice_proxy
from vm_pool_map
INNER JOIN vm_pools
ON vm_pool_map.vm_pool_id = vm_pools.vm_pool_id;




CREATE OR REPLACE VIEW tags_vm_pool_map_view
AS
SELECT		tags.tag_id as tag_id,
			tags.tag_name as tag_name,
			tags.parent_id as parent_id,
			tags.readonly as readonly, tags.type as type,
			tags_vm_pool_map.vm_pool_id as vm_pool_id
FROM        tags INNER JOIN
tags_vm_pool_map ON tags.tag_id = tags_vm_pool_map.tag_id;




CREATE OR REPLACE VIEW tags_vm_map_view
AS
SELECT     tags.tag_id as tag_id,
		   tags.tag_name as tag_name,
		   tags.parent_id as parent_id,
		   tags.readonly as readonly,
		   tags.type as type,
           tags_vm_map.vm_id as vm_id
FROM       tags INNER JOIN
tags_vm_map ON tags.tag_id = tags_vm_map.tag_id;




CREATE OR REPLACE VIEW tags_vds_map_view
AS
SELECT     tags.tag_id as tag_id,
		   tags.tag_name as tag_name,
		   tags.parent_id as parent_id,
		   tags.readonly as readonly,
		   tags.type as type,
           tags_vds_map.vds_id as vds_id
FROM       tags INNER JOIN
tags_vds_map ON tags.tag_id = tags_vds_map.tag_id;




CREATE OR REPLACE VIEW tags_user_map_view
AS
SELECT     tags.tag_id as tag_id,
           tags.tag_name as tag_name,
		   tags.parent_id as parent_id,
		   tags.readonly as readonly,
		   tags.type as type,
           tags_user_map.user_id as user_id
FROM       tags INNER JOIN
tags_user_map ON tags.tag_id = tags_user_map.tag_id;



CREATE OR REPLACE VIEW tags_user_group_map_view
AS
SELECT     tags.tag_id as tag_id,
			tags.tag_name as tag_name,
			tags.parent_id as parent_id,
			tags.readonly as readonly,
		   tags.type as type,
			 tags_user_group_map.group_id as group_id
FROM         tags_user_group_map INNER JOIN
tags ON tags_user_group_map.tag_id = tags.tag_id;



CREATE OR REPLACE VIEW vms
AS
SELECT     vm_static.vm_name as vm_name, vm_static.mem_size_mb as mem_size_mb, vm_static.nice_level as nice_level, vm_static.cpu_shares as cpu_shares,
                      vm_static.vmt_guid as vmt_guid, vm_static.os as os, vm_static.description as description, vm_static.free_text_comment as free_text_comment, vm_static.vds_group_id as vds_group_id,
                      vm_static.creation_date as creation_date, vm_static.auto_startup as auto_startup, vm_static.is_stateless as is_stateless,
                      vm_static.is_smartcard_enabled as is_smartcard_enabled, vm_static.is_delete_protected as is_delete_protected, vm_static.sso_method as sso_method, vm_static.dedicated_vm_for_vds as dedicated_vm_for_vds,
                      vm_static.fail_back as fail_back, vm_static.default_boot_sequence as default_boot_sequence, vm_static.vm_type as vm_type, vm_pool_map_view.vm_pool_spice_proxy as vm_pool_spice_proxy,
                      vds_groups.name as vds_group_name, vds_groups.transparent_hugepages as transparent_hugepages, vds_groups.trusted_service as trusted_service,
                      storage_pool.id as storage_pool_id, storage_pool.name as storage_pool_name,
                      vds_groups.description as vds_group_description, vds_groups.spice_proxy as vds_group_spice_proxy, vm_templates.vm_name as vmt_name,
                      vm_templates.mem_size_mb as vmt_mem_size_mb, vm_templates.os as vmt_os, vm_templates.creation_date as vmt_creation_date,
                      vm_templates.child_count as vmt_child_count, vm_templates.num_of_sockets as vmt_num_of_sockets,
                      vm_templates.cpu_per_socket as vmt_cpu_per_socket, vm_templates.num_of_sockets*vm_templates.cpu_per_socket as vmt_num_of_cpus,
                      vm_templates.description as vmt_description, vm_dynamic.status as status, vm_dynamic.vm_ip as vm_ip, vm_dynamic.vm_host as vm_host,
                      vm_dynamic.vm_pid as vm_pid, vm_dynamic.last_start_time as last_start_time, vm_dynamic.guest_cur_user_name as guest_cur_user_name, vm_dynamic.console_cur_user_name as console_cur_user_name,
                      vm_dynamic.guest_last_login_time as guest_last_login_time, vm_dynamic.guest_last_logout_time as guest_last_logout_time, vm_dynamic.guest_os as guest_os,
                      vm_dynamic.console_user_id as console_user_id, vm_dynamic.guest_agent_nics_hash as guest_agent_nics_hash,
                      vm_dynamic.run_on_vds as run_on_vds, vm_dynamic.migrating_to_vds as migrating_to_vds, vm_dynamic.app_list as app_list,
                      vm_pool_map_view.vm_pool_name as vm_pool_name, vm_pool_map_view.vm_pool_id as vm_pool_id, vm_static.vm_guid as vm_guid, vm_static.num_of_monitors as num_of_monitors, vm_static.single_qxl_pci as single_qxl_pci, vm_static.allow_console_reconnect as allow_console_reconnect, vm_static.is_initialized as is_initialized,
                      vm_static.num_of_sockets as num_of_sockets, vm_static.cpu_per_socket as cpu_per_socket, vm_static.usb_policy as usb_policy, vm_dynamic.acpi_enable as acpi_enable, vm_dynamic.session as session,
                      vm_static.num_of_sockets*vm_static.cpu_per_socket as num_of_cpus,
                      vm_static.quota_id as quota_id, quota.quota_name as quota_name, storage_pool.quota_enforcement_type as quota_enforcement_type,
                      vm_dynamic.kvm_enable as kvm_enable, vm_dynamic.boot_sequence as boot_sequence,
                      vm_dynamic.utc_diff as utc_diff, vm_dynamic.last_vds_run_on as last_vds_run_on,
					  vm_dynamic.client_ip as client_ip,vm_dynamic.guest_requested_memory as guest_requested_memory, vm_static.time_zone as time_zone, vm_statistics.cpu_user as cpu_user, vm_statistics.cpu_sys as cpu_sys,
                      vm_statistics.memory_usage_history as memory_usage_history, vm_statistics.cpu_usage_history as cpu_usage_history,
                      vm_statistics.network_usage_history as network_usage_history,
                      vm_statistics.elapsed_time as elapsed_time, vm_statistics.usage_network_percent as usage_network_percent, vm_statistics.disks_usage as disks_usage,
                      vm_statistics.usage_mem_percent as usage_mem_percent, vm_statistics.migration_progress_percent as migration_progress_percent, vm_statistics.usage_cpu_percent as usage_cpu_percent, vds_static.vds_name as run_on_vds_name, vds_groups.cpu_name as vds_group_cpu_name,
                      vm_static.default_display_type as default_display_type, vm_static.priority as priority,vm_static.iso_path as iso_path, vm_static.origin as origin, vds_groups.compatibility_version as vds_group_compatibility_version,
                      vm_static.initrd_url as initrd_url, vm_static.kernel_url as kernel_url, vm_static.kernel_params as kernel_params, vm_dynamic.pause_status as pause_status, vm_dynamic.exit_message as exit_message, vm_dynamic.exit_status as exit_status,vm_static.migration_support as migration_support,vm_static.predefined_properties as predefined_properties,vm_static.userdefined_properties as userdefined_properties,vm_static.min_allocated_mem as min_allocated_mem,  vm_dynamic.hash as hash, vm_static.cpu_pinning as cpu_pinning, vm_static.db_generation as db_generation, vm_static.host_cpu_flags as host_cpu_flags,
                      vm_static.tunnel_migration as tunnel_migration, vm_static.vnc_keyboard_layout as vnc_keyboard_layout, vm_static.is_run_and_pause as is_run_and_pause, vm_static.created_by_user_id as created_by_user_id,
                      vm_dynamic.last_watchdog_event as last_watchdog_event, vm_dynamic.last_watchdog_action as last_watchdog_action, vm_dynamic.is_run_once as is_run_once, vm_dynamic.vm_fqdn as vm_fqdn, vm_dynamic.cpu_name as cpu_name, vm_dynamic.emulated_machine as emulated_machine, vm_dynamic.current_cd as current_cd, vm_dynamic.reason as reason, vm_dynamic.exit_reason as exit_reason,
                      vm_static.instance_type_id as instance_type_id, vm_static.image_type_id as image_type_id, vds_groups.architecture as architecture, vm_static.original_template_id as original_template_id, vm_static.original_template_name as original_template_name, vm_dynamic.last_stop_time as last_stop_time,
                      vm_static.migration_downtime as migration_downtime, vm_static.template_version_number as template_version_number,
                      vm_static.serial_number_policy as serial_number_policy, vm_static.custom_serial_number as custom_serial_number,
                      vm_static.is_boot_menu_enabled as is_boot_menu_enabled, vm_dynamic.guest_cpu_count as guest_cpu_count,
                      (snapshots.snapshot_id is not null) as next_run_config_exists,
                      vm_static.numatune_mode as numatune_mode,
                      vm_static.is_spice_file_transfer_enabled as is_spice_file_transfer_enabled, vm_static.is_spice_copy_paste_enabled as is_spice_copy_paste_enabled,
                      vm_static.cpu_profile_id as cpu_profile_id,
                      vm_static.is_auto_converge as is_auto_converge, vm_static.is_migrate_compressed as is_migrate_compressed,
                      vm_static.custom_emulated_machine as custom_emulated_machine,
                      vm_static.custom_cpu_name as custom_cpu_name,
                      vm_dynamic.spice_port as spice_port,
                      vm_dynamic.spice_tls_port as spice_tls_port,
                      vm_dynamic.spice_ip as spice_ip,
                      vm_dynamic.vnc_port as vnc_port,
                      vm_dynamic.vnc_ip as vnc_ip,
                      vm_dynamic.guest_agent_status as guest_agent_status
FROM         vm_static INNER JOIN
vm_dynamic ON vm_static.vm_guid = vm_dynamic.vm_guid INNER JOIN
vm_static AS vm_templates ON vm_static.vmt_guid = vm_templates.vm_guid INNER JOIN
vm_statistics ON vm_static.vm_guid = vm_statistics.vm_guid INNER JOIN
vds_groups ON vm_static.vds_group_id = vds_groups.vds_group_id LEFT OUTER JOIN
storage_pool ON vm_static.vds_group_id = vds_groups.vds_group_id
and vds_groups.storage_pool_id = storage_pool.id LEFT OUTER JOIN
quota ON vm_static.quota_id = quota.id LEFT OUTER JOIN
vds_static ON vm_dynamic.run_on_vds = vds_static.vds_id LEFT OUTER JOIN
vm_pool_map_view ON vm_static.vm_guid = vm_pool_map_view.vm_guid
left outer join snapshots on vm_static.vm_guid = snapshots.vm_id and snapshot_type='NEXT_RUN'
WHERE vm_static.entity_type = 'VM';



CREATE OR REPLACE VIEW vms_with_tags
AS
SELECT      vms.vm_name, vms.mem_size_mb, vms.nice_level, vms.cpu_shares, vms.vmt_guid, vms.os, vms.description, vms.free_text_comment,
            vms.vds_group_id, vms.creation_date, vms.auto_startup, vms.is_stateless, vms.is_smartcard_enabled, vms.is_delete_protected,
            vms.sso_method, vms.dedicated_vm_for_vds, vms.fail_back, vms.default_boot_sequence, vms.vm_type,
            vms.vds_group_name, vms.storage_pool_id, vms.storage_pool_name,
            vms.vds_group_description, vms.vmt_name, vms.vmt_mem_size_mb, vms.vmt_os, vms.vmt_creation_date,
            vms.vmt_child_count, vms.vmt_num_of_sockets, vms.vmt_cpu_per_socket, vms.vmt_description, vms.status, vms.vm_ip,
            vms.vm_host, vms.vmt_num_of_sockets * vms.vmt_cpu_per_socket AS vmt_num_of_cpus, vms.vm_pid,
            vms.last_start_time, vms.last_stop_time, vms.guest_cur_user_name, vms.console_cur_user_name, vms.guest_last_login_time, vms.console_user_id,
            vms.guest_last_logout_time, vms.guest_os, vms.run_on_vds, vms.migrating_to_vds, vms.app_list,
            vms.vm_pool_name, vms.vm_pool_id, vms.vm_guid, vms.num_of_monitors, vms.single_qxl_pci,
            vms.allow_console_reconnect,
            vms.is_initialized, vms.num_of_sockets, vms.cpu_per_socket, vms.usb_policy, vms.acpi_enable,
            vms.session, vms.num_of_sockets * vms.cpu_per_socket AS num_of_cpus,
            vms.kvm_enable, vms.boot_sequence, vms.utc_diff, vms.last_vds_run_on, vms.client_ip,
            vms.guest_requested_memory, vms.time_zone, vms.cpu_user, vms.cpu_sys, vms.elapsed_time,
            vms.usage_network_percent, vms.disks_usage, vms.usage_mem_percent, vms.migration_progress_percent, vms.usage_cpu_percent, vms.run_on_vds_name,
            vms.vds_group_cpu_name, tags_vm_map_view.tag_name, tags_vm_map_view.tag_id, vms.default_display_type, vms.priority,
            vms.vds_group_compatibility_version, vms.initrd_url, vms.kernel_url, vms.kernel_params, vms.pause_status,
            vms.exit_status, vms.exit_message, vms.min_allocated_mem, storage_domain_static.id AS storage_id,
            vms.quota_id as quota_id, vms.quota_name as quota_name, vms.tunnel_migration as tunnel_migration,
            vms.vnc_keyboard_layout as vnc_keyboard_layout, vms.is_run_and_pause as is_run_and_pause, vms.created_by_user_id as created_by_user_id, vms.vm_fqdn, vms.cpu_name as cpu_name, vms.emulated_machine as emulated_machine,
            vms.custom_emulated_machine as custom_emulated_machine, vms.custom_cpu_name as custom_cpu_name,
            vms.vm_pool_spice_proxy as vm_pool_spice_proxy, vms.vds_group_spice_proxy as vds_group_spice_proxy,
            vms.instance_type_id as instance_type_id, vms.image_type_id as image_type_id, vms.architecture as architecture, vms.original_template_id as original_template_id, vms.original_template_name as original_template_name,
            vms.migration_downtime as migration_downtime, vms.template_version_number as template_version_number,
            vms.current_cd as current_cd, vms.reason as reason,
            vms.serial_number_policy as serial_number_policy, vms.custom_serial_number as custom_serial_number, vms.exit_reason as exit_reason,
            vms.is_boot_menu_enabled as is_boot_menu_enabled, vms.guest_cpu_count as guest_cpu_count,
            (snapshots.snapshot_id is not null) as next_run_config_exists,
            vms.numatune_mode,
            vms.is_spice_file_transfer_enabled, vms.is_spice_copy_paste_enabled,
            vms.cpu_profile_id,
            vms.is_auto_converge, vms.is_migrate_compressed,
            vms.spice_port,
            vms.spice_tls_port,
            vms.spice_ip,
            vms.vnc_port,
            vms.vnc_ip,
            vms.guest_agent_status
FROM        vms LEFT OUTER JOIN
            tags_vm_map_view ON vms.vm_guid = tags_vm_map_view.vm_id LEFT OUTER JOIN
            vm_device ON vm_device.vm_id = vms.vm_guid LEFT OUTER JOIN
            images ON images.image_group_id = vm_device.device_id LEFT OUTER JOIN
            image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid LEFT OUTER JOIN
            storage_domain_static ON storage_domain_static.id = image_storage_domain_map.storage_domain_id
            left outer join snapshots on vms.vm_guid = snapshots.vm_id and snapshot_type='NEXT_RUN'
WHERE       images.active IS NULL OR images.active = TRUE;

CREATE OR REPLACE VIEW server_vms
as
SELECT * FROM vms
WHERE vm_type = '1';





CREATE OR REPLACE VIEW vms_with_plug_info
as
SELECT *
FROM vms
INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid;



CREATE OR REPLACE VIEW desktop_vms
as
SELECT * FROM vms
WHERE vm_type = '0';





CREATE OR REPLACE VIEW vds
as
SELECT     vds_groups.vds_group_id as vds_group_id, vds_groups.name as vds_group_name, vds_groups.description as vds_group_description, vds_groups.architecture as architecture, vds_groups.enable_balloon as enable_balloon,
                      vds_static.vds_id as vds_id, vds_static.vds_name as vds_name, vds_static.vds_unique_id as vds_unique_id,
                      vds_static.host_name as host_name, vds_static.free_text_comment as free_text_comment,
                      vds_static.port as port, vds_static.vds_strength as vds_strength, vds_static.server_SSL_enabled as server_SSL_enabled, vds_static.vds_type as vds_type,
                      vds_static.pm_enabled as pm_enabled,
                      vds_static.pm_proxy_preferences as pm_proxy_preferences,
                      vds_static.pm_detect_kdump as pm_detect_kdump,
                      vds_static.vds_spm_priority as vds_spm_priority, vds_dynamic.hooks as hooks,vds_dynamic.status as status,
                      vds_dynamic.cpu_cores as cpu_cores, vds_dynamic.cpu_threads as cpu_threads, vds_dynamic.cpu_model as cpu_model, vds_dynamic.cpu_speed_mh as cpu_speed_mh,
                      vds_dynamic.if_total_speed as if_total_speed, vds_dynamic.kvm_enabled as kvm_enabled, vds_dynamic.physical_mem_mb as physical_mem_mb,
                      vds_dynamic.pending_vcpus_count as pending_vcpus_count, vds_dynamic.pending_vmem_size as pending_vmem_size,vds_dynamic.mem_commited as mem_commited, vds_dynamic.vm_active as vm_active, vds_dynamic.vm_count as vm_count,
                      vds_dynamic.vm_migrating as vm_migrating, vds_dynamic.vms_cores_count as vms_cores_count, vds_statistics.cpu_over_commit_time_stamp as cpu_over_commit_time_stamp,
                      vds_groups.max_vds_memory_over_commit as max_vds_memory_over_commit, vds_dynamic.net_config_dirty as net_config_dirty, vds_groups.count_threads_as_cores as count_threads_as_cores,
                      storage_pool.id as storage_pool_id, storage_pool.name as storage_pool_name, vds_dynamic.reserved_mem as reserved_mem,
                      vds_dynamic.guest_overhead as guest_overhead, vds_dynamic.rpm_version as rpm_version, vds_dynamic.software_version as software_version, vds_dynamic.version_name as version_name, vds_dynamic.build_name as build_name,
                      vds_dynamic.previous_status as previous_status, vds_statistics.cpu_idle as cpu_idle, vds_statistics.cpu_load as cpu_load, vds_statistics.cpu_sys as cpu_sys, vds_statistics.cpu_user as cpu_user,
                      vds_statistics.usage_mem_percent as usage_mem_percent, vds_statistics.usage_cpu_percent as usage_cpu_percent, vds_statistics.usage_network_percent as usage_network_percent,
                      vds_statistics.mem_available as mem_available, vds_statistics.mem_free as mem_free, vds_statistics.mem_shared as mem_shared, vds_statistics.swap_free as swap_free,
					  vds_statistics.swap_total as swap_total,  vds_statistics.ksm_cpu_percent as ksm_cpu_percent,  vds_statistics.ksm_pages as ksm_pages,  vds_statistics.ksm_state as ksm_state,
                      vds_dynamic.cpu_flags as cpu_flags,vds_groups.cpu_name as vds_group_cpu_name, vds_dynamic.cpu_sockets as cpu_sockets, vds_spm_id_map.vds_spm_id as vds_spm_id, vds_static.otp_validity as otp_validity,
                      CASE WHEN storage_pool.spm_vds_id = vds_static.vds_id THEN CASE WHEN storage_pool.status = 5 THEN 1 ELSE 2 END ELSE 0 END as spm_status, vds_dynamic.supported_cluster_levels as supported_cluster_levels, vds_dynamic.supported_engines as supported_engines, vds_groups.compatibility_version as vds_group_compatibility_version,
                      vds_groups.virt_service as vds_group_virt_service, vds_groups.gluster_service as vds_group_gluster_service,
                      vds_dynamic.host_os as host_os, vds_dynamic.kvm_version as kvm_version, vds_dynamic.libvirt_version as libvirt_version, vds_dynamic.spice_version as spice_version, vds_dynamic.gluster_version as gluster_version, vds_dynamic.kernel_version as kernel_version, vds_dynamic.iscsi_initiator_name as iscsi_initiator_name,
                      vds_dynamic.transparent_hugepages_state as transparent_hugepages_state, vds_statistics.anonymous_hugepages as anonymous_hugepages, vds_dynamic.non_operational_reason as non_operational_reason,
			vds_static.recoverable as recoverable, vds_static.sshKeyFingerprint as sshKeyFingerprint, vds_static.host_provider_id as host_provider_id, vds_dynamic.hw_manufacturer as hw_manufacturer, vds_dynamic.hw_product_name as hw_product_name, vds_dynamic.hw_version as hw_version,
                      vds_dynamic.hw_serial_number as hw_serial_number, vds_dynamic.hw_uuid as hw_uuid, vds_dynamic.hw_family as hw_family, vds_static.console_address as console_address,
                      vds_dynamic.hbas as hbas, vds_dynamic.supported_emulated_machines as supported_emulated_machines, vds_dynamic.supported_rng_sources as supported_rng_sources, vds_static.ssh_port as ssh_port, vds_static.ssh_username as ssh_username, vds_statistics.ha_score as ha_score,
                      vds_statistics.ha_configured as ha_configured, vds_statistics.ha_active as ha_active, vds_statistics.ha_global_maintenance as ha_global_maintenance,
                      vds_statistics.ha_local_maintenance as ha_local_maintenance, vds_static.disable_auto_pm as disable_auto_pm, vds_dynamic.controlled_by_pm_policy as controlled_by_pm_policy, vds_statistics.boot_time as boot_time,
                      vds_dynamic.kdump_status as kdump_status, vds_dynamic.selinux_enforce_mode as selinux_enforce_mode,
                      vds_dynamic.auto_numa_balancing as auto_numa_balancing, vds_dynamic.is_numa_supported as is_numa_supported, vds_dynamic.is_live_snapshot_supported as is_live_snapshot_supported, vds_static.protocol as protocol, vds_dynamic.is_live_merge_supported as is_live_merge_supported,
                      vds_dynamic.online_cpus as online_cpus, fence_agents.id as agent_id, fence_agents.agent_order as agent_order, fence_agents.ip as agent_ip, fence_agents.type as agent_type, fence_agents.agent_user as agent_user, fence_agents.agent_password as agent_password, fence_agents.port as agent_port, fence_agents.options as agent_options
FROM         vds_groups INNER JOIN
vds_static ON vds_groups.vds_group_id = vds_static.vds_group_id INNER JOIN
vds_dynamic ON vds_static.vds_id = vds_dynamic.vds_id INNER JOIN
vds_statistics ON vds_static.vds_id = vds_statistics.vds_id LEFT OUTER JOIN
storage_pool ON vds_groups.storage_pool_id = storage_pool.id LEFT OUTER JOIN
fence_agents ON vds_static.vds_id = fence_agents.vds_id LEFT OUTER JOIN
vds_spm_id_map ON vds_static.vds_id = vds_spm_id_map.vds_id;



CREATE OR REPLACE VIEW vds_with_tags
as
SELECT     vds_groups.vds_group_id, vds_groups.name AS vds_group_name, vds_groups.description AS vds_group_description, vds_groups.architecture as architecture,
                      vds_static.vds_id, vds_static.vds_name, vds_static.vds_unique_id,
                      vds_static.host_name, vds_static.free_text_comment, vds_static.port, vds_static.vds_strength, vds_static.server_SSL_enabled, vds_static.vds_type,
                      vds_dynamic.hw_product_name, vds_dynamic.hw_version, vds_dynamic.hw_serial_number, vds_dynamic.hw_uuid, vds_dynamic.hw_family,
                      vds_static.pm_enabled, vds_static.pm_proxy_preferences as pm_proxy_preferences,
                      vds_static.pm_detect_kdump as pm_detect_kdump,
                      vds_dynamic.hooks, vds_dynamic.status, vds_dynamic.cpu_cores,
                      vds_dynamic.cpu_threads, vds_dynamic.cpu_model, vds_dynamic.cpu_speed_mh, vds_dynamic.if_total_speed, vds_dynamic.kvm_enabled,
                      vds_dynamic.physical_mem_mb, vds_dynamic.pending_vcpus_count, vds_dynamic.pending_vmem_size,
                      vds_dynamic.mem_commited, vds_dynamic.vm_active, vds_dynamic.vm_count, vds_dynamic.vm_migrating,
                      vds_dynamic.vms_cores_count, vds_statistics.cpu_over_commit_time_stamp,
                      vds_dynamic.net_config_dirty, vds_groups.max_vds_memory_over_commit, vds_groups.count_threads_as_cores,
                      storage_pool.id AS storage_pool_id, storage_pool.name AS storage_pool_name, tags_vds_map_view.tag_name,
                      tags_vds_map_view.tag_id, vds_dynamic.reserved_mem, vds_dynamic.guest_overhead, vds_dynamic.rpm_version, vds_dynamic.software_version,
                      vds_dynamic.version_name, vds_dynamic.build_name, vds_dynamic.previous_status, vds_statistics.cpu_idle,
                      vds_statistics.cpu_load, vds_statistics.cpu_sys, vds_statistics.cpu_user, vds_statistics.usage_mem_percent,
                      vds_statistics.usage_cpu_percent, vds_statistics.usage_network_percent, vds_statistics.mem_available, vds_statistics.mem_free,
                      vds_statistics.mem_shared, vds_statistics.swap_free, vds_statistics.swap_total, vds_statistics.ksm_cpu_percent,
                      vds_statistics.ksm_pages, vds_statistics.ksm_state, vds_dynamic.cpu_flags, vds_groups.cpu_name AS vds_group_cpu_name,
                      vds_dynamic.cpu_sockets, vds_spm_id_map.vds_spm_id, vds_static.otp_validity as otp_validity, vds_static.console_address as console_address,
                      CASE WHEN storage_pool.spm_vds_id = vds_static.vds_id THEN CASE WHEN storage_pool.status = 5 THEN 1 ELSE 2 END ELSE 0 END AS
spm_status, vds_dynamic.supported_cluster_levels, vds_dynamic.supported_engines,
                      vds_groups.compatibility_version AS vds_group_compatibility_version, vds_dynamic.host_os, vds_dynamic.kvm_version, vds_dynamic.libvirt_version,
                      vds_dynamic.spice_version, vds_dynamic.gluster_version, vds_dynamic.kernel_version, vds_dynamic.iscsi_initiator_name,
                      vds_dynamic.transparent_hugepages_state, vds_statistics.anonymous_hugepages, vds_dynamic.non_operational_reason,
                      storage_pool_iso_map.storage_id, vds_static.ssh_port, vds_static.ssh_username, vds_statistics.ha_score,
                      vds_statistics.ha_configured, vds_statistics.ha_active, vds_statistics.ha_global_maintenance, vds_statistics.ha_local_maintenance,
                      vds_static.disable_auto_pm as disable_auto_pm, vds_dynamic.controlled_by_pm_policy as controlled_by_pm_policy,
                      vds_statistics.boot_time, vds_dynamic.kdump_status as kdump_status, vds_dynamic.selinux_enforce_mode as selinux_enforce_mode,
                      vds_dynamic.auto_numa_balancing as auto_numa_balancing, vds_dynamic.is_numa_supported as is_numa_supported,
                      vds_dynamic.supported_rng_sources as supported_rng_sources,
                      vds_dynamic.is_live_snapshot_supported as is_live_snapshot_supported, vds_static.protocol as protocol,
                      vds_dynamic.is_live_merge_supported as is_live_merge_supported,
                      vds_dynamic.online_cpus as online_cpus,
		      fence_agents.id as agent_id,
		      fence_agents.agent_order as agent_order,
		      fence_agents.ip as agent_ip,
		      fence_agents.type as agent_type,
		      fence_agents.agent_user as agent_user,
		      fence_agents.agent_password as agent_password,
		      fence_agents.port as agent_port,
		      fence_agents.options as agent_options
FROM         vds_groups INNER JOIN
vds_static ON vds_groups.vds_group_id = vds_static.vds_group_id INNER JOIN
vds_dynamic ON vds_static.vds_id = vds_dynamic.vds_id INNER JOIN
vds_statistics ON vds_static.vds_id = vds_statistics.vds_id LEFT OUTER JOIN
storage_pool ON vds_groups.storage_pool_id = storage_pool.id LEFT OUTER JOIN
tags_vds_map_view ON vds_static.vds_id = tags_vds_map_view.vds_id LEFT OUTER JOIN
fence_agents ON vds_static.vds_id = fence_agents.vds_id LEFT OUTER JOIN
vds_spm_id_map ON vds_static.vds_id = vds_spm_id_map.vds_id LEFT OUTER JOIN
storage_pool_iso_map ON storage_pool_iso_map.storage_pool_id = storage_pool.id;



CREATE OR REPLACE VIEW users_and_groups_to_vm_pool_map_view
AS
select	p.vm_pool_id as vm_pool_id, p.vm_pool_name as vm_pool_name, per.ad_element_id as user_id
FROM    vm_pools AS p
INNER JOIN permissions as per on per.object_id = p.vm_pool_id;




CREATE OR REPLACE VIEW vdc_users
AS

SELECT     'user' as user_group, users_1.name as name, users_1.user_id as user_id, users_1.surname as surname, users_1.domain as domain, users_1.username as username, users_1.department as department,
                      users_1.email as email, users_1.note as note, 0 as vm_admin,
                      users_1.last_admin_check_status as last_admin_check_status,
                      users_1.external_id as external_id, users_1.namespace as namespace
FROM         users AS users_1
UNION
SELECT     'group' as user_group, ad_groups.name as name, ad_groups.id as id, '' as surname, ad_groups.domain as domain, '' as username, '' as department,
                      '' as email, '' as note, 1 as vm_admin, null as last_admin_check_status,
                      ad_groups.external_id as external_id, ad_groups.namespace as namespace
FROM         ad_groups;


-- create the new vdc_users_with_tags view with no use of the tag_permission_map

CREATE OR REPLACE VIEW vdc_users_with_tags
AS

SELECT     users_1.user_group as user_group, users_1.name as name, permissions.object_id as vm_guid, users_1.user_id as user_id, users_1.surname as surname, users_1.domain as domain,
                      users_1.username as username, users_1.department as department, roles1.name as mla_role, users_1.email as email,
                      users_1.note as note, users_1.vm_admin as vm_admin, tags_user_map_view_1.tag_name as tag_name, tags_user_map_view_1.tag_id as tag_id, users_1.last_admin_check_status as last_admin_check_status,
                      pools.vm_pool_name as vm_pool_name
FROM         vdc_users AS users_1 LEFT OUTER JOIN
users_and_groups_to_vm_pool_map_view AS pools ON users_1.user_id = pools.user_id LEFT OUTER JOIN
permissions ON users_1.user_id = permissions.ad_element_id LEFT OUTER JOIN
tags ON tags.type = 1 LEFT OUTER JOIN
tags_user_map_view AS tags_user_map_view_1 ON users_1.user_id = tags_user_map_view_1.user_id LEFT OUTER JOIN
roles AS roles1 ON roles1.id = permissions.role_id
WHERE     (users_1.user_group = 'user')
UNION
SELECT     users_2.user_group as user_group, users_2.name as name, permissions_1.object_id as vm_guid, users_2.user_id as user_id, users_2.surname as surname, users_2.domain as domain,
                      users_2.username as username, users_2.department as department, roles2.name as mla_role, users_2.email as email,
                      users_2.note as note, users_2.vm_admin as vm_admin, tags_user_group_map_view.tag_name as tag_name, tags_user_group_map_view.tag_id as tag_id,
                      users_2.last_admin_check_status as last_admin_check_status, pools1.vm_pool_name as vm_pool_name
FROM         vdc_users AS users_2 LEFT OUTER JOIN
users_and_groups_to_vm_pool_map_view AS pools1 ON users_2.user_id = pools1.user_id LEFT OUTER JOIN
permissions AS permissions_1 ON users_2.user_id = permissions_1.ad_element_id LEFT OUTER JOIN
tags AS tags_1 ON tags_1.type = 1 LEFT OUTER JOIN
tags_user_group_map_view ON users_2.user_id = tags_user_group_map_view.group_id LEFT OUTER JOIN
roles AS roles2 ON roles2.id = permissions_1.role_id
WHERE     (users_2.user_group = 'group');



CREATE OR REPLACE VIEW vm_pools_view AS
 SELECT vm_pools.vm_pool_id, vm_pools.vm_pool_name, vm_pools.vm_pool_description, vm_pools.vm_pool_comment, vm_pools.vm_pool_type, vm_pools.parameters, vm_pools.prestarted_vms, vm_pools.vds_group_id, vds_groups.name AS vds_group_name, vds_groups.architecture AS architecture, storage_pool.name as storage_pool_name, storage_pool.id as storage_pool_id, vm_pools.max_assigned_vms_per_user as max_assigned_vms_per_user,
 vm_pools.spice_proxy as spice_proxy
   FROM vm_pools
   JOIN vds_groups ON vm_pools.vds_group_id = vds_groups.vds_group_id
   LEFT JOIN storage_pool ON storage_pool.id = vds_groups.storage_pool_id;



CREATE OR REPLACE VIEW vm_pools_full_view AS
 SELECT vmp.vm_pool_id, vmp.vm_pool_name, vmp.vm_pool_description, vmp.vm_pool_comment, vmp.vm_pool_type, vmp.parameters, vmp.prestarted_vms, vmp.vds_group_id, vmp.vds_group_name, vmp.architecture, vmp.max_assigned_vms_per_user, vmp.spice_proxy as spice_proxy, ( SELECT count(vm_pool_map.vm_pool_id) AS expr1
           FROM vm_pools_view v1
      LEFT JOIN vm_pool_map ON v1.vm_pool_id = vm_pool_map.vm_pool_id AND v1.vm_pool_id = vmp.vm_pool_id) AS assigned_vm_count, ( SELECT count(v2.vm_pool_id) AS expr1
           FROM vm_pools v2
      LEFT JOIN vm_pool_map vm_pool_map_1 ON v2.vm_pool_id = vm_pool_map_1.vm_pool_id AND v2.vm_pool_id = vmp.vm_pool_id
   LEFT JOIN vm_dynamic ON vm_pool_map_1.vm_guid = vm_dynamic.vm_guid
  WHERE vm_dynamic.status <> ALL (ARRAY[0, 15])
  GROUP BY v2.vm_pool_id) AS vm_running_count, vmp.storage_pool_name, vmp.storage_pool_id
   FROM vm_pools_view vmp;



CREATE OR REPLACE VIEW permissions_view
AS

SELECT     permissions.id as id, permissions.role_id as role_id, permissions.ad_element_id as ad_element_id, permissions.object_id as object_id, permissions.object_type_id as object_type_id,
	       roles.name as role_name, roles.role_type as role_type, roles.allows_viewing_children as allows_viewing_children, roles.app_mode as app_mode, fn_get_entity_name(permissions.object_id,permissions.object_type_id) as object_name,
			(fn_authz_entry_info(permissions.ad_element_id)).name as owner_name, (fn_authz_entry_info(permissions.ad_element_id)).namespace as namespace, (fn_authz_entry_info(permissions.ad_element_id)).authz as authz
FROM         permissions INNER JOIN
roles ON permissions.role_id = roles.id;

CREATE OR REPLACE VIEW internal_permissions_view
AS

SELECT     permissions.id as id, permissions.role_id as role_id, permissions.ad_element_id as ad_element_id, permissions.object_id as object_id, permissions.object_type_id as object_type_id,
	       roles.name as role_name, roles.role_type as role_type, roles.allows_viewing_children as allows_viewing_children
FROM         permissions
INNER JOIN roles ON permissions.role_id = roles.id;


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
  SELECT vds_interface_statistics.rx_rate, vds_interface_statistics.tx_rate, vds_interface_statistics.rx_drop,
      vds_interface_statistics.tx_drop, vds_interface_statistics.iface_status, vds_interface.type, vds_interface.gateway,
      vds_interface.subnet, vds_interface.addr, vds_interface.speed, vds_interface.base_interface, vds_interface.vlan_id, vds_interface.bond_type,
      vds_interface.bond_name, vds_interface.is_bond, vds_interface.bond_opts, vds_interface.mac_addr,
      vds_interface.network_name, vds_interface.name, vds_static.vds_id, vds_static.vds_name,  vds_interface.id,
      vds_interface.boot_protocol, vds_interface.mtu as mtu, vds_interface.bridged, 1 AS is_vds, vds_interface.qos_overridden AS qos_overridden,
      vds_interface.labels as labels, vds_interface.custom_properties AS custom_properties, vds_static.vds_group_id as vds_group_id
  FROM vds_interface_statistics
  JOIN vds_interface ON vds_interface_statistics.id = vds_interface.id
  JOIN vds_static ON vds_interface.vds_id = vds_static.vds_id;

CREATE OR REPLACE VIEW vm_interface_view AS
  SELECT vm_interface_statistics.rx_rate, vm_interface_statistics.tx_rate, vm_interface_statistics.rx_drop,
      vm_interface_statistics.tx_drop, vm_interface_statistics.iface_status, vm_interface.type, vm_interface.speed,
      vm_interface.mac_addr, network.name AS network_name, vm_interface.name, vm_interface.vnic_profile_id, vm_static.vm_guid, vm_interface.vmt_guid,
      vm_static.vm_name, vm_interface.id, 0 AS boot_protocol, 0 AS is_vds, vm_device.is_plugged,
      vm_device.custom_properties, vnic_profiles.port_mirroring AS port_mirroring, vm_interface.linked,
      vm_static.vds_group_id AS vds_group_id, vm_static.entity_type AS vm_entity_type, vnic_profiles.name AS vnic_profile_name, qos.name AS qos_name
  FROM vm_interface_statistics
  JOIN vm_interface ON vm_interface_statistics.id = vm_interface.id
  JOIN vm_static ON vm_interface.vm_guid = vm_static.vm_guid
  JOIN vm_device ON vm_interface.vm_guid = vm_device.vm_id AND vm_interface.id = vm_device.device_id
  LEFT JOIN ((vnic_profiles JOIN network ON network.id = vnic_profiles.network_id)LEFT JOIN qos ON vnic_profiles.network_qos_id = qos.id) ON vnic_profiles.id = vm_interface.vnic_profile_id
  UNION
  SELECT vm_interface_statistics.rx_rate, vm_interface_statistics.tx_rate, vm_interface_statistics.rx_drop,
      vm_interface_statistics.tx_drop, vm_interface_statistics.iface_status, vm_interface.type, vm_interface.speed,
      vm_interface.mac_addr, network.name AS network_name, vm_interface.name, vm_interface.vnic_profile_id, NULL::uuid as vm_guid,
      vm_interface.vmt_guid, vm_templates.vm_name AS vm_name, vm_interface.id, 0 AS boot_protocol, 0 AS is_vds,
      vm_device.is_plugged as is_plugged, vm_device.custom_properties as custom_properties, vnic_profiles.port_mirroring AS port_mirroring,
      vm_interface.linked, vm_templates.vds_group_id AS vds_group_id, vm_templates.entity_type AS vm_entity_type, vnic_profiles.name AS vnic_profile_name , qos.name AS qos_name
  FROM vm_interface_statistics
  RIGHT JOIN vm_interface ON vm_interface_statistics.id = vm_interface.id
  JOIN vm_static AS vm_templates ON vm_interface.vmt_guid = vm_templates.vm_guid
  JOIN vm_device ON vm_interface.vmt_guid = vm_device.vm_id AND vm_interface.id = vm_device.device_id
  LEFT JOIN ((vnic_profiles JOIN network ON network.id = vnic_profiles.network_id)LEFT JOIN qos ON vnic_profiles.network_qos_id = qos.id) ON vnic_profiles.id = vm_interface.vnic_profile_id;



----------------------------------------------
-- Storage Pool
----------------------------------------------
CREATE OR REPLACE VIEW storage_pool_with_storage_domain

AS
SELECT     storage_pool.id as id, storage_pool.name as name, storage_pool.description as description, storage_pool.free_text_comment as free_text_comment, storage_pool.status as status, storage_pool.is_local as is_local,
		   storage_pool.master_domain_version as master_domain_version, storage_pool.spm_vds_id as spm_vds_id, storage_pool.compatibility_version as compatibility_version, storage_pool._create_date as _create_date,
		   storage_pool._update_date as _update_date, storage_pool_iso_map.storage_id as storage_id, storage_pool_iso_map.storage_pool_id as storage_pool_id,
		   storage_domain_static.storage_type as storage_type, storage_domain_static.storage_domain_type as storage_domain_type,
                   storage_domain_static.storage_domain_format_type as storage_domain_format_type,
		   storage_domain_static.storage_name as storage_name, storage_domain_static.storage as storage,
		   storage_domain_static.last_time_used_as_master as last_time_used_as_master
FROM         storage_pool LEFT OUTER JOIN
		   storage_pool_iso_map ON storage_pool.id = storage_pool_iso_map.storage_pool_id LEFT OUTER JOIN
		   storage_domain_static ON storage_pool_iso_map.storage_id = storage_domain_static.id;


----------------------------------------------
-- Clusters
----------------------------------------------
CREATE OR REPLACE VIEW vds_groups_storage_domain

AS
SELECT     vds_groups.vds_group_id, vds_groups.name, vds_groups.description, vds_groups.free_text_comment, vds_groups.cpu_name, vds_groups._create_date,
                      vds_groups._update_date, vds_groups.storage_pool_id,
                      vds_groups.max_vds_memory_over_commit, vds_groups.count_threads_as_cores, vds_groups.compatibility_version,
                      vds_groups.transparent_hugepages, vds_groups.migrate_on_error, vds_groups.architecture,
                      storage_pool_iso_map.storage_id, storage_pool.name AS storage_pool_name
FROM vds_groups
LEFT JOIN storage_pool_iso_map ON vds_groups.storage_pool_id = storage_pool_iso_map.storage_pool_id
LEFT JOIN storage_pool ON vds_groups.storage_pool_id = storage_pool.id;

CREATE OR REPLACE VIEW vds_groups_view
AS
SELECT vds_groups.*,
       storage_pool.name AS storage_pool_name,
       cluster_policies.name AS cluster_policy_name
FROM vds_groups
LEFT JOIN storage_pool ON vds_groups.storage_pool_id = storage_pool.id
LEFT JOIN cluster_policies ON vds_groups.cluster_policy_id = cluster_policies.id;

CREATE OR REPLACE VIEW storage_domains_with_hosts_view

AS
SELECT
storage_domain_static.id,
		storage_domain_static.storage,
		storage_domain_static.storage_name,
		storage_domain_static.storage_description as storage_description,
		storage_domain_static.storage_comment as storage_comment,
		storage_domain_dynamic.available_disk_size,
		storage_domain_dynamic.used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
		fn_get_actual_images_size_by_storage(storage_domain_static.id) as actual_images_size,
		storage_pool.name as storage_pool_name,
		storage_domain_static.storage_type,
		storage_domain_static.storage_domain_type,
                storage_domain_static.storage_domain_format_type,
        storage_domain_static.last_time_used_as_master as last_time_used_as_master,
        storage_domain_static.wipe_after_delete as wipe_after_delete,
		fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_pool_iso_map.status,storage_domain_static.storage_domain_type) AS
		storage_domain_shared_status,
		vds_groups.vds_group_id,
		vds_static.vds_id,
		storage_pool_iso_map.storage_pool_id,
		vds_static.recoverable
FROM storage_domain_static
	INNER JOIN storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id
	LEFT OUTER JOIN storage_pool_iso_map ON storage_domain_static.id = storage_pool_iso_map.storage_id
	LEFT OUTER JOIN storage_pool ON storage_pool_iso_map.storage_pool_id = storage_pool.id
	LEFT OUTER JOIN vds_groups ON storage_pool_iso_map.storage_pool_id = vds_groups.storage_pool_id
	LEFT OUTER JOIN vds_static ON vds_groups.vds_group_id = vds_static.vds_group_id;


CREATE OR REPLACE VIEW vm_images_storage_domains_view
AS
SELECT vm_images_view.storage_id, vm_images_view.storage_path, vm_images_view.storage_pool_id,
       vm_images_view.image_guid, vm_images_view.creation_date, vm_images_view.actual_size, vm_images_view.read_rate, vm_images_view.write_rate,
       vm_images_view.size, vm_images_view.it_guid, vm_images_view.description, vm_images_view.parentid,
       vm_images_view.imagestatus, vm_images_view.lastmodified, vm_images_view.app_list, vm_images_view.vm_snapshot_id, vm_images_view.volume_type,
       vm_images_view.image_group_id, vm_images_view.active, vm_images_view.volume_format, vm_images_view.disk_interface,
       vm_images_view.boot, vm_images_view.wipe_after_delete, vm_images_view.propagate_errors, vm_images_view.entity_type, vm_images_view.number_of_vms, vm_images_view.vm_names, vm_images_view.quota_id,
       vm_images_view.quota_name, vm_images_view.disk_profile_id, vm_images_view.disk_profile_name, vm_images_view.disk_alias, vm_images_view.disk_description, vm_images_view.sgio,
       storage_domains_with_hosts_view.id, storage_domains_with_hosts_view.storage, storage_domains_with_hosts_view.storage_name,
       storage_domains_with_hosts_view.available_disk_size, storage_domains_with_hosts_view.used_disk_size,
       storage_domains_with_hosts_view.commited_disk_size, storage_domains_with_hosts_view.actual_images_size, storage_domains_with_hosts_view.storage_type,
       storage_domains_with_hosts_view.storage_domain_type, storage_domains_with_hosts_view.storage_domain_format_type,
       storage_domains_with_hosts_view.storage_domain_shared_status, storage_domains_with_hosts_view.vds_group_id,
       storage_domains_with_hosts_view.vds_id, storage_domains_with_hosts_view.recoverable, storage_domains_with_hosts_view.storage_pool_name,
       storage_domains_with_hosts_view.storage_name as name

FROM vm_images_view
INNER JOIN images_storage_domain_view ON vm_images_view.image_guid = images_storage_domain_view.image_guid
INNER JOIN storage_domains_with_hosts_view ON storage_domains_with_hosts_view.id = images_storage_domain_view.storage_id;


----------------------------------------------
-- Quota
----------------------------------------------
CREATE OR REPLACE VIEW quota_view
AS
SELECT q.id as quota_id,
    q.storage_pool_id as storage_pool_id,
    storage_pool.name as storage_pool_name,
    q.quota_name as quota_name,
    q.description as description,
    q.threshold_vds_group_percentage as threshold_vds_group_percentage,
    q.threshold_storage_percentage as threshold_storage_percentage,
    q.grace_vds_group_percentage as grace_vds_group_percentage,
    q.grace_storage_percentage as grace_storage_percentage,
    storage_pool.quota_enforcement_type as quota_enforcement_type
FROM  storage_pool, quota q
WHERE storage_pool.id = q.storage_pool_id;


CREATE OR REPLACE VIEW quota_global_view
AS
SELECT q_limit.quota_id as quota_id,
    q.storage_pool_id as storage_pool_id,
    storage_pool.name as storage_pool_name,
    q.quota_name as quota_name,
    q.description as description,
    q.threshold_vds_group_percentage as threshold_vds_group_percentage,
    q.threshold_storage_percentage as threshold_storage_percentage,
    q.grace_vds_group_percentage as grace_vds_group_percentage,
    q.grace_storage_percentage as grace_storage_percentage,
    virtual_cpu,
    (CalculateVdsGroupUsage(quota_id,null)).virtual_cpu_usage,
    mem_size_mb,
    (CalculateVdsGroupUsage(quota_id,null)).mem_size_mb_usage,
    storage_size_gb,
    CalculateStorageUsage(quota_id,null) as storage_size_gb_usage,
    storage_pool.quota_enforcement_type as quota_enforcement_type
FROM  storage_pool, quota q LEFT OUTER JOIN
quota_limitation q_limit on q_limit.quota_id = q.id
WHERE storage_pool.id = q.storage_pool_id
AND q_limit.vds_group_id IS NULL
AND q_limit.storage_id IS NULL;

CREATE OR REPLACE VIEW quota_limitations_view
AS
SELECT q_limit.quota_id as quota_id,
    q.storage_pool_id as storage_pool_id,
    storage_pool.name as storage_pool_name,
    q.quota_name as quota_name,
    q.description as description,
    q.threshold_vds_group_percentage as threshold_vds_group_percentage,
    q.threshold_storage_percentage as threshold_storage_percentage,
    q.grace_vds_group_percentage as grace_vds_group_percentage,
    q.grace_storage_percentage as grace_storage_percentage,
    virtual_cpu,
    mem_size_mb,
    storage_size_gb,
    storage_pool.quota_enforcement_type as quota_enforcement_type,
    vds_group_id,
    storage_id,
    (COALESCE(vds_group_id, storage_id) IS NULL ) AS is_global,
    (COALESCE(virtual_cpu, mem_size_mb, storage_size_gb) IS NULL) AS is_empty
FROM  quota q
INNER JOIN storage_pool ON storage_pool.id = q.storage_pool_id
LEFT OUTER JOIN quota_limitation q_limit on q_limit.quota_id = q.id;


CREATE OR REPLACE VIEW quota_storage_view
AS
SELECT q_limit.id as quota_storage_id,
    q_limit.quota_id as quota_id,
    storage_id,
    storage_domain_static.storage_name as storage_name,
    storage_size_gb,
    CalculateStorageUsage(quota_id,storage_id) as storage_size_gb_usage
FROM   quota_limitation q_limit, quota q, storage_domain_static
WHERE  q_limit.quota_id = q.id
AND  q_limit.vds_group_id IS NULL
AND  q_limit.storage_id IS NOT NULL
AND  storage_domain_static.id = q_limit.storage_id;


CREATE OR REPLACE VIEW quota_vds_group_view
AS
SELECT q_limit.id as quota_vds_group_id,
    q_limit.quota_id as quota_id,
    q_limit.vds_group_id,
    vds_groups.name as vds_group_name,
    virtual_cpu,
    (CalculateVdsGroupUsage(quota_id,q_limit.vds_group_id)).virtual_cpu_usage as virtual_cpu_usage,
    mem_size_mb,
    (CalculateVdsGroupUsage(quota_id,q_limit.vds_group_id)).mem_size_mb_usage as mem_size_mb_usage
FROM   quota_limitation q_limit, quota q, vds_groups
WHERE  q_limit.quota_id = q.id
AND  q_limit.vds_group_id IS NOT NULL
AND  q_limit.storage_id IS NULL
AND  vds_groups.vds_group_id = q_limit.vds_group_id;

----------------------------------------------
-- Network
----------------------------------------------
CREATE OR REPLACE VIEW network_cluster_view
AS
SELECT network_cluster.cluster_id AS cluster_id, network_cluster.network_id AS network_id, network.name as network_name,
       network_cluster.status as status, network_cluster.required as required, network_cluster.is_display as is_display,
       network_cluster.migration as migration, vds_groups.name as cluster_name
FROM network_cluster
INNER JOIN network ON network_cluster.network_id = network.id
INNER JOIN vds_groups ON network_cluster.cluster_id = vds_groups.vds_group_id;


CREATE OR REPLACE VIEW network_vds_view
AS
SELECT network.id AS network_id,
    network.name as network_name,
    vds_static.vds_name as vds_name
FROM vds_interface
INNER JOIN vds_static ON vds_interface.vds_id = vds_static.vds_id
INNER JOIN network ON vds_interface.network_name = network.name
INNER JOIN network_cluster ON network_cluster.network_id = network.id
WHERE network_cluster.cluster_id = vds_static.vds_group_id;


CREATE OR REPLACE VIEW network_view
AS
SELECT network.id AS id,
   network.name AS name,
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
   network.qos_id AS qos_id,
   network.label AS label,
   storage_pool.name AS storage_pool_name,
   storage_pool.compatibility_version AS compatibility_version,
   providers.name AS provider_name
FROM network
INNER JOIN storage_pool ON network.storage_pool_id = storage_pool.id
LEFT JOIN providers ON network.provider_network_provider_id = providers.id;


CREATE OR REPLACE VIEW vnic_profiles_view
AS
SELECT vnic_profiles.id AS id,
       vnic_profiles.name AS name,
       vnic_profiles.network_id as network_id,
       vnic_profiles.network_qos_id as network_qos_id,
       vnic_profiles.port_mirroring as port_mirroring,
       vnic_profiles.custom_properties as custom_properties,
       vnic_profiles.description as description,
       network.name as network_name,
       qos.name as network_qos_name,
       storage_pool.name as data_center_name,
       storage_pool.compatibility_version as compatibility_version,
       storage_pool.id as data_center_id
FROM vnic_profiles

INNER JOIN network ON vnic_profiles.network_id = network.id
LEFT JOIN qos ON vnic_profiles.network_qos_id = qos.id
INNER JOIN storage_pool ON network.storage_pool_id = storage_pool.id;


----------------------------------------------
-- Query Permissions
----------------------------------------------

-- Flatten all the objects a user can get permissions on them
CREATE OR REPLACE VIEW engine_session_user_flat_groups
AS
SELECT id AS engine_session_seq_id, user_id AS user_id, fnSplitterUuid(engine_sessions.group_ids) AS granted_id
FROM   engine_sessions
UNION ALL
-- The user itself
SELECT id, user_id, user_id FROM engine_sessions
UNION ALL
-- user is also member of 'Everyone'
SELECT id, user_id, 'EEE00000-0000-0000-0000-123456789EEE'
FROM   engine_sessions;

-- Permissions view for Clusters
-- The user has permissions on a cluster
CREATE OR REPLACE VIEW user_vds_groups_permissions_view_base (entity_id, granted_id)
AS
SELECT       object_id, ad_element_id
FROM         internal_permissions_view
WHERE        object_type_id = 9 AND role_type = 2
-- Or the object is a VM or Template in the cluster
UNION ALL
SELECT       DISTINCT vds_group_id, ad_element_id
FROM         vm_static
INNER JOIN   internal_permissions_view ON object_id = vm_guid AND (object_type_id = 2 OR object_type_id = 4) AND role_type=2 and vds_group_id is not null
-- Or the object is the Data Center containing the Cluster
UNION ALL
SELECT       vds_group_id, ad_element_id
FROM         vds_groups
INNER JOIN   internal_permissions_view ON object_id = vds_groups.storage_pool_id AND object_type_id = 14 AND role_type = 2
-- Or the user has permissions on system;
UNION ALL
SELECT       vds_group_id, ad_element_id
FROM         internal_permissions_view
CROSS JOIN   vds_groups
WHERE        object_type_id = 1 AND role_type=2;

CREATE OR REPLACE VIEW user_vds_groups_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vds_groups_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions view for Data Center
-- The user has permissions on a data center
CREATE OR REPLACE VIEW user_storage_pool_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 14 AND role_type = 2
-- Or the object is a cluster in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vds_groups
INNER JOIN internal_permissions_view ON object_id = vds_groups.vds_group_id AND object_type_id = 9 AND role_type = 2
-- Or the object is vm pool in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vds_groups
INNER JOIN vm_pools ON vds_groups.vds_group_id = vm_pools.vds_group_id
INNER JOIN internal_permissions_view ON object_id = vm_pools.vm_pool_id AND object_type_id = 5 AND role_type = 2
-- Or the object is a VM in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN internal_permissions_view ON object_id = vm_guid AND object_type_id = 2 AND role_type = 2
-- Or the user has permission on system
UNION ALL
SELECT     storage_pool.id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN storage_pool
WHERE      object_type_id = 1 AND role_type = 2;

CREATE OR REPLACE VIEW user_storage_pool_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_storage_pool_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions for Storage Domains
-- The user has permissions on a storage domain
CREATE OR REPLACE VIEW user_storage_domain_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 11 AND role_type = 2
-- Or the user has permissions on a VM in the storage domain
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       storage_domains
INNER JOIN vds_groups ON vds_groups.storage_pool_id = storage_domains.storage_pool_id
INNER JOIN vm_static ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN internal_permissions_view ON object_id = vm_static.vm_guid AND object_type_id = 2 AND role_type = 2
-- Or the user has permissions on a template in the storage domain
UNION ALL
SELECT     storage_id, ad_element_id
FROM       vm_templates_storage_domain
INNER JOIN internal_permissions_view ON vmt_guid = internal_permissions_view.object_id AND object_type_id = 4 AND role_type = 2
-- Or the user has permissions on a VM created from a template in the storage domain
UNION ALL
SELECT     storage_id, ad_element_id
FROM       vm_static
INNER JOIN vm_templates_storage_domain ON vm_static.vmt_guid = vm_templates_storage_domain.vmt_guid
INNER JOIN internal_permissions_view ON vm_static.vm_guid = object_id AND objecT_type_id = 2 AND role_type = 2
-- Or the user has permissions on the Data Center containing the storage domain
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       storage_domains
INNER JOIN internal_permissions_view ON object_id = storage_domains.storage_pool_id AND object_type_id = 14 AND role_type = 2
-- Or the user has permissions on System
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN storage_domains
WHERE      object_type_id = 1 AND role_type = 2;

CREATE OR REPLACE VIEW user_storage_domain_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_storage_domain_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions on Hosts
-- The user has permissions on a host
CREATE OR REPLACE VIEW user_vds_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 3 AND role_type = 2
-- Or the user has permissions on a VM in the cluster or Data Center that contains the host
UNION ALL
SELECT     vds_id, ad_element_id
FROM       vds
INNER JOIN internal_permissions_view ON (object_id = vds_group_id    AND object_type_id = 9) OR
                                    (object_id = storage_pool_id AND object_type_id = 14) AND role_type = 2
-- Or the user has permissions on System
UNION ALL
SELECT     vds_id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vds
WHERE      object_type_id = 1 AND role_type = 2;

CREATE OR REPLACE VIEW user_vds_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vds_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions on VM Pools
-- The user has permissions on the pool
CREATE OR REPLACE VIEW user_vm_pool_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 5 AND role_type = 2
-- Or the user has permissions on a VM from the pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pool_map
INNER JOIN internal_permissions_view ON object_id = vm_guid AND object_type_id = 2 AND role_type = 2
-- Or the user has permissions on the cluster containing the pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pools
INNER JOIN internal_permissions_view ON object_id = vds_group_id AND object_type_id = 9 AND allows_viewing_children AND role_type = 2
-- Or the user has permission on the data center containing the VM pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pools
INNER JOIN vds_groups ON vm_pools.vds_group_id =  vds_groups.vds_group_id
INNER JOIN internal_permissions_view ON object_id = storage_pool_id AND object_type_id = 14 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on System
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vm_pools
WHERE      object_type_id = 1 AND allows_viewing_children AND role_type = 2;

CREATE OR REPLACE VIEW user_vm_pool_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_pool_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions on Templates
-- The user has permissions on the template
CREATE OR REPLACE VIEW user_vm_template_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 4 AND role_type = 2
-- Or the user has permissions on a VM created from the tempalate
UNION ALL
SELECT     vmt_guid, ad_element_id
FROM       vm_static
INNER JOIN internal_permissions_view ON object_id = vm_static.vm_guid AND object_type_id = 2 AND role_type = 2
-- Or the user has permissions on the data center containing the template
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN internal_permissions_view ON object_id = storage_pool_id AND object_type_id = 14 AND allows_viewing_children AND role_type = 2 AND vm_static.entity_type::text = 'TEMPLATE'::text
-- Or the user has permissions on system
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vm_static
WHERE      object_type_id = 1 AND allows_viewing_children AND role_type = 2 AND
(vm_static.entity_type::text = 'TEMPLATE'::text OR vm_static.entity_type::text = 'INSTANCE_TYPE'::text
 OR vm_static.entity_type::text = 'IMAGE_TYPE'::text);

CREATE OR REPLACE VIEW user_vm_template_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_template_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;


-- Permissions on VMs
-- The user has permission on the VM
CREATE OR REPLACE VIEW user_vm_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 2 AND role_type = 2
-- Or the user has permissions on the cluster containing the VM
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       vm_static
INNER JOIN internal_permissions_view ON object_id = vds_group_id AND object_type_id = 9 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on the data center containing the VM
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN internal_permissions_view ON object_id = storage_pool_id AND object_type_id = 14 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on system
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vm_static
WHERE      object_type_id = 1 AND allows_viewing_children AND role_type = 2;

CREATE OR REPLACE VIEW user_vm_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;

-- Permissions on disk
-- The user has permissions on the disk directly
CREATE OR REPLACE VIEW user_disk_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 19 AND role_type = 2
-- Or the user has permissions on the VM the disk is attached to
UNION ALL
SELECT     device_id, user_vm_permissions_view.user_id as ad_element_id
FROM       vm_device
INNER JOIN user_vm_permissions_view ON user_vm_permissions_view.entity_id = vm_device.vm_id
WHERE      vm_device.type = 'disk' and vm_device.device = 'disk'
-- Or the user has permissions on the template the disk is attached to
UNION ALL
SELECT     device_id, user_vm_template_permissions_view.user_id as ad_element_id
FROM       vm_device
INNER JOIN user_vm_template_permissions_view ON user_vm_template_permissions_view.entity_id = vm_device.vm_id
WHERE      type = 'disk' and device = 'disk'
-- Or the user has permissions on the storage domain containing the disk
UNION ALL
SELECT     images.image_group_id, ad_element_id
FROM       image_storage_domain_map
INNER JOIN images ON images.image_guid = image_storage_domain_map.image_id
INNER JOIN internal_permissions_view ON object_id = storage_domain_id AND object_type_id = 11 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on the data center containing the storage pool constaining the disk
UNION ALL
SELECT     images.image_group_id, ad_element_id
FROM       image_storage_domain_map
INNER JOIN storage_pool_iso_map ON image_storage_domain_map.storage_domain_id = storage_pool_iso_map.storage_id
INNER JOIN images ON images.image_guid = image_storage_domain_map.image_id
INNER JOIN internal_permissions_view ON object_id = storage_pool_id AND object_type_id = 14 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on system
UNION ALL
SELECT     device_id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vm_device
WHERE      object_type_id = 1 AND allows_viewing_children AND role_type = 2;

CREATE OR REPLACE VIEW user_disk_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_disk_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;

-- Permissions on permissions
CREATE OR REPLACE VIEW user_permissions_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT id, user_id
FROM         internal_permissions_view
JOIN         engine_session_user_flat_groups ON granted_id = ad_element_id;

-- Direct permissions assigned to user
CREATE OR REPLACE VIEW user_object_permissions_view AS
 SELECT DISTINCT permissions.object_id AS entity_id, engine_session_user_flat_groups.user_id
   FROM permissions
   JOIN roles ON permissions.role_id = roles.id
   JOIN engine_session_user_flat_groups ON engine_session_user_flat_groups.granted_id = permissions.ad_element_id
   WHERE permissions.ad_element_id != getGlobalIds('everyone');

-- Permissions to view users in db
CREATE OR REPLACE VIEW user_db_users_permissions_view AS
 SELECT DISTINCT permissions.ad_element_id, roles_groups.role_id, roles_groups.action_group_id
   FROM permissions
   JOIN roles_groups ON permissions.role_id = roles_groups.role_id
   WHERE roles_groups.action_group_id = 502;

CREATE OR REPLACE VIEW vm_device_view
AS
SELECT device_id, vm_id, type, device, address, boot_order, spec_params,
       is_managed, is_plugged, is_readonly, alias, custom_properties, snapshot_id, logical_name
  FROM vm_device;

-- Permissions on VNIC Profiles
-- The user has permissions on the Profile directly
CREATE OR REPLACE VIEW user_vnic_profile_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 27 AND role_type = 2
-- Or the user has permissions on the Network in which the profile belongs to
UNION ALL
SELECT     vnic_profiles.id, ad_element_id
FROM       vnic_profiles
INNER JOIN internal_permissions_view ON object_id = network_id
WHERE      object_type_id = 20 AND allows_viewing_children AND role_type = 2
-- Or the user has permissions on the Profile-Network's Data-Center directly
UNION ALL
SELECT     vnic_profiles.id, ad_element_id
FROM       vnic_profiles
INNER JOIN network ON network.id = network_id
INNER JOIN internal_permissions_view ON object_id = network.storage_pool_id
WHERE      object_type_id = 14 AND role_type = 2 AND allows_viewing_children
-- Or the user has permissions on the Cluster the networks are assigned to
UNION ALL
SELECT     vnic_profiles.id, ad_element_id
FROM       vnic_profiles
INNER JOIN network_cluster ON network_cluster.network_id = vnic_profiles.network_id
INNER JOIN internal_permissions_view ON object_id = network_cluster.cluster_id
WHERE      object_type_id = 9 AND role_type = 2 AND allows_viewing_children
--Or the user has permissions on the VM with this profile
UNION ALL
SELECT DISTINCT vnic_profile_id, ad_element_id
FROM       vm_interface
INNER JOIN internal_permissions_view ON object_id = vm_guid
WHERE object_type_id = 2 AND role_type = 2
-- Or the user has permissions on the Template with the profile
UNION ALL
SELECT DISTINCT    vnic_profile_id, ad_element_id
FROM       vm_interface
INNER JOIN internal_permissions_view ON object_id = vmt_guid
WHERE object_type_id = 4 AND role_type = 2
-- Or the user has permissions on system
UNION ALL
SELECT     vnic_profiles.id, ad_element_id
FROM       internal_permissions_view
CROSS JOIN vnic_profiles
WHERE      object_type_id = 1 AND allows_viewing_children AND role_type = 2;

CREATE OR REPLACE VIEW user_vnic_profile_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vnic_profile_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;

-- Permissions on Networks
CREATE OR REPLACE VIEW user_network_permissions_view_base (entity_id, granted_id)
AS
-- Or the user has permissions on one of the Network's VNIC Profiles
SELECT     network.id, user_id
FROM       network
INNER JOIN vnic_profiles ON network_id = network.id
INNER JOIN user_vnic_profile_permissions_view ON entity_id = vnic_profiles.id;

CREATE OR REPLACE VIEW user_network_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_network_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;

-- Permissions on disk profiles
-- The user has permissions on the disk profile directly
CREATE OR REPLACE VIEW user_disk_profile_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       internal_permissions_view
WHERE      object_type_id = 29 AND role_type = 2;

CREATE OR REPLACE VIEW user_disk_profile_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_disk_profile_permissions_view_base
NATURAL JOIN engine_session_user_flat_groups;

CREATE OR REPLACE VIEW gluster_volumes_view
AS
SELECT gluster_volumes.*,
       vds_groups.name AS vds_group_name
FROM gluster_volumes
INNER JOIN vds_groups ON gluster_volumes.cluster_id = vds_groups.vds_group_id;

CREATE OR REPLACE VIEW gluster_volume_snapshots_view
AS
SELECT gluster_volume_snapshots.*,
       gluster_volumes.cluster_id AS cluster_id,
       gluster_volumes.vol_name as volume_name
FROM gluster_volume_snapshots
INNER JOIN gluster_volumes ON gluster_volume_snapshots.volume_id = gluster_volumes.id;

CREATE OR REPLACE VIEW gluster_volume_bricks_view
AS
SELECT gluster_volume_bricks.*,
       vds_static.host_name AS vds_name,
       gluster_volumes.vol_name AS volume_name
FROM gluster_volume_bricks
INNER JOIN vds_static ON vds_static.vds_id = gluster_volume_bricks.server_id
INNER JOIN gluster_volumes ON gluster_volumes.id = gluster_volume_bricks.volume_id;

CREATE OR REPLACE VIEW gluster_volume_task_steps
AS
SELECT step.*,
       gluster_volumes.id as volume_id,
       job.job_id as job_job_id,
       job.action_type,
       job.description as job_description,
       job.status as job_status,
       job.start_time as job_start_time,
       job.end_time as job_end_time
FROM gluster_volumes
INNER JOIN job_subject_entity js ON js.entity_id = gluster_volumes.id
INNER JOIN job on job.job_id = js.job_id
               AND job.action_type in ('StartRebalanceGlusterVolume', 'StartRemoveGlusterVolumeBricks')
LEFT OUTER JOIN step on step.external_id = gluster_volumes.task_id AND step.external_system_type = 'GLUSTER'
                AND step.job_id = js.job_id;

CREATE OR REPLACE VIEW gluster_server_services_view
AS
SELECT gluster_server_services.*,
       gluster_services.service_name,
       gluster_services.service_type,
       vds_static.vds_name
FROM gluster_server_services
INNER JOIN gluster_services ON gluster_server_services.service_id = gluster_services.id
INNER JOIN vds_static ON gluster_server_services.server_id = vds_static.vds_id;

CREATE OR REPLACE VIEW gluster_server_hooks_view
AS
SELECT gluster_server_hooks.*,
       vds_static.vds_name AS server_name
FROM gluster_server_hooks
INNER JOIN vds_static ON gluster_server_hooks.server_id = vds_static.vds_id;

CREATE OR REPLACE VIEW gluster_georep_sessions_view
AS
SELECT session_id, master_volume_id, session_key, slave_host_uuid,
    slave_host_name, slave_volume_id, slave_volume_name, georep.status,
    georep._create_date, georep._update_date,
    gluster_volumes.vol_name AS master_volume_name,
    gluster_volumes.cluster_id AS cluster_id
FROM  gluster_georep_session georep
INNER JOIN gluster_volumes ON gluster_volumes.id = georep.master_volume_id;

-- Affinity Groups view, including members
CREATE OR REPLACE VIEW affinity_groups_view
AS
SELECT affinity_groups.*,
       array_to_string(array_agg(affinity_group_members.vm_id), ',') as vm_ids,
       array_to_string(array_agg(vm_static.vm_name), ',') as vm_names
FROM affinity_groups
LEFT JOIN affinity_group_members ON affinity_group_members.affinity_group_id = affinity_groups.id
LEFT JOIN vm_static ON vm_static.vm_guid = affinity_group_members.vm_id
-- postgres 8.X issue, need to group by all fields.
GROUP BY affinity_groups.id, affinity_groups.name, affinity_groups.description,
         affinity_groups.cluster_id, affinity_groups.positive, affinity_groups.enforcing,
         affinity_groups._create_date, affinity_groups._update_date;

-- Numa node cpus view
CREATE OR REPLACE VIEW numa_node_cpus_view
AS
SELECT numa_node.numa_node_id,
       numa_node.vds_id,
       numa_node.vm_id,
       numa_node_cpu_map.cpu_core_id
FROM numa_node
INNER JOIN numa_node_cpu_map ON numa_node.numa_node_id = numa_node_cpu_map.numa_node_id;

-- Numa node assignment view
CREATE OR REPLACE VIEW numa_node_assignment_view
AS
SELECT vm_vds_numa_node_map.vm_numa_node_id as assigned_vm_numa_node_id,
       vm_vds_numa_node_map.is_pinned as is_pinned,
       vm_vds_numa_node_map.vds_numa_node_index as last_run_in_vds_numa_node_index,
       vm_numa_node.vm_id as vm_numa_node_vm_id,
       vm_numa_node.numa_node_index as vm_numa_node_index,
       vm_numa_node.mem_total as vm_numa_node_mem_total,
       vm_numa_node.cpu_count as vm_numa_node_cpu_count,
       vm_numa_node.mem_free as vm_numa_node_mem_free,
       vm_numa_node.usage_mem_percent as vm_numa_node_usage_mem_percent,
       vm_numa_node.cpu_sys as vm_numa_node_cpu_sys,
       vm_numa_node.cpu_user as vm_numa_node_cpu_user,
       vm_numa_node.cpu_idle as vm_numa_node_cpu_idle,
       vm_numa_node.usage_cpu_percent as vm_numa_node_usage_cpu_percent,
       vm_numa_node.distance as vm_numa_node_distance,
       run_in_vds_numa_node.numa_node_id as run_in_vds_numa_node_id,
       run_in_vds_numa_node.vds_id as run_in_vds_id,
       run_in_vds_numa_node.numa_node_index as run_in_vds_numa_node_index,
       run_in_vds_numa_node.mem_total as run_in_vds_numa_node_mem_total,
       run_in_vds_numa_node.cpu_count as run_in_vds_numa_node_cpu_count,
       run_in_vds_numa_node.mem_free as run_in_vds_numa_node_mem_free,
       run_in_vds_numa_node.usage_mem_percent as run_in_vds_numa_node_usage_mem_percent,
       run_in_vds_numa_node.cpu_sys as run_in_vds_numa_node_cpu_sys,
       run_in_vds_numa_node.cpu_user as run_in_vds_numa_node_cpu_user,
       run_in_vds_numa_node.cpu_idle as run_in_vds_numa_node_cpu_idle,
       run_in_vds_numa_node.usage_cpu_percent as run_in_vds_numa_node_usage_cpu_percent,
       run_in_vds_numa_node.distance as run_in_vds_numa_node_distance
FROM vm_vds_numa_node_map
LEFT OUTER JOIN numa_node as vm_numa_node on vm_vds_numa_node_map.vm_numa_node_id = vm_numa_node.numa_node_id
LEFT OUTER JOIN numa_node as run_in_vds_numa_node on vm_vds_numa_node_map.vds_numa_node_id = run_in_vds_numa_node.numa_node_id;

-- Numa node with vds group view
CREATE OR REPLACE VIEW numa_node_with_vds_group_view
AS
SELECT vm_numa_node.numa_node_id as vm_numa_node_id,
       vm_numa_node.vm_id as vm_numa_node_vm_id,
       vm_numa_node.numa_node_index as vm_numa_node_index,
       vm_numa_node.mem_total as vm_numa_node_mem_total,
       vm_numa_node.cpu_count as vm_numa_node_cpu_count,
       vm_numa_node.mem_free as vm_numa_node_mem_free,
       vm_numa_node.usage_mem_percent as vm_numa_node_usage_mem_percent,
       vm_numa_node.cpu_sys as vm_numa_node_cpu_sys,
       vm_numa_node.cpu_user as vm_numa_node_cpu_user,
       vm_numa_node.cpu_idle as vm_numa_node_cpu_idle,
       vm_numa_node.usage_cpu_percent as vm_numa_node_usage_cpu_percent,
       vm_numa_node.distance as vm_numa_node_distance,
       vm_static.vds_group_id
FROM numa_node as vm_numa_node
LEFT OUTER JOIN vm_static on vm_numa_node.vm_id = vm_static.vm_guid;
