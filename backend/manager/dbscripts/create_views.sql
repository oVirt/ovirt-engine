-- ----------------------------------------------------------------------
-- Views
-- ----------------------------------------------------------------------

CREATE OR REPLACE VIEW storage_domain_static_view
AS

SELECT     storage_domain_static.id as id,
			storage_domain_static.storage as storage,
			storage_domain_static.storage_name as storage_name,
			storage_pool_iso_map.storage_pool_id as storage_pool_id,
                        storage_pool_iso_map.status as status,
			storage_domain_static.storage_domain_type as storage_domain_type,
			storage_domain_static.storage_type as storage_type,
                        storage_domain_static.storage_domain_format_type as storage_domain_format_type,
			storage_pool.name as storage_pool_name
FROM        storage_domain_static LEFT OUTER JOIN
storage_pool_iso_map on storage_pool_iso_map.storage_id = storage_domain_static.id
LEFT OUTER JOIN storage_pool ON storage_pool.id = storage_pool_iso_map.storage_pool_id;



CREATE OR REPLACE VIEW images_storage_domain_view
AS

-- TODO: Change code to treat disks values directly instead of through this view.
SELECT DISTINCT images.image_guid as image_guid, vm_device.vm_id as vm_guid,
    storage_domain_static_view.storage_name as storage_name,
    storage_domain_static_view.storage as storage_path,
	storage_domain_static_view.storage_pool_id as storage_pool_id,
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
    images.boot as boot,
    images.imageStatus as imageStatus,
    images.image_group_id as image_group_id,
    images.active,
    vm_static.entity_type as entity_type,
    CAST (disks.internal_drive_mapping AS VARCHAR(50)) as internal_drive_mapping,
    CASE WHEN disks.disk_type = 'Unassigned' THEN 0
         WHEN disks.disk_type = 'System' THEN 1
         WHEN disks.disk_type = 'Data' THEN 2
         WHEN disks.disk_type = 'Shared' THEN 3
         WHEN disks.disk_type = 'Swap' THEN 4
         WHEN disks.disk_type = 'Temp' THEN 5
         ELSE NULL
    END AS disk_type,
    CASE WHEN disks.disk_interface = 'IDE' THEN 0
         WHEN disks.disk_interface = 'SCSI' THEN 1
         WHEN disks.disk_interface = 'VirtIO' THEN 2
         ELSE NULL
    END AS disk_interface,
    disks.wipe_after_delete as wipe_after_delete,
    CASE WHEN disks.propagate_errors = 'Off' THEN 0
         WHEN disks.propagate_errors = 'On' THEN 1
         ELSE NULL
    END AS propagate_errors,
    images.quota_id as quota_id,
    quota.quota_name as quota_name,
    disk_image_dynamic.actual_size as actual_size,
    disk_image_dynamic.read_rate as read_rate,
    disk_image_dynamic.write_rate as write_rate
FROM
images
left outer join disk_image_dynamic on images.image_guid = disk_image_dynamic.image_id
LEFT OUTER JOIN disks ON images.image_group_id = disks.disk_id left outer JOIN vm_device on vm_device.device_id = images.image_group_id left outer join vm_static on vm_static.vm_guid = vm_device.vm_id
LEFT JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
LEFT OUTER JOIN storage_domain_static_view ON image_storage_domain_map.storage_domain_id = storage_domain_static_view.id
LEFT OUTER JOIN snapshots ON images.vm_snapshot_id = snapshots.snapshot_id
LEFT OUTER JOIN quota ON images.quota_id = quota.id
WHERE images.image_guid != '00000000-0000-0000-0000-000000000000';


CREATE OR REPLACE VIEW storage_domain_file_repos
AS
SELECT
storage_domain_static.id as storage_domain_id,
			storage_domain_static.storage_domain_type as storage_domain_type,
	        storage_pool_iso_map.storage_pool_id as storage_pool_id,
	       	storage_pool_iso_map.status as storage_domain_status,
	repo_file_meta_data.repo_file_name as repo_file_name,
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


CREATE OR REPLACE VIEW vm_images_view
AS
SELECT     images_storage_domain_view.storage_path as storage_path, images_storage_domain_view.storage_name as storage_name, images_storage_domain_view.storage_pool_id as storage_pool_id, images_storage_domain_view.image_guid as image_guid,
                      images_storage_domain_view.creation_date as creation_date, disk_image_dynamic.actual_size as actual_size, disk_image_dynamic.read_rate as read_rate, disk_image_dynamic.write_rate as write_rate,
                      images_storage_domain_view.size as size, images_storage_domain_view.it_guid as it_guid,
                      images_storage_domain_view.internal_drive_mapping as internal_drive_mapping, images_storage_domain_view.description as description,
                      images_storage_domain_view.ParentId as ParentId, images_storage_domain_view.imageStatus as imageStatus, images_storage_domain_view.lastModified as lastModified,
                      images_storage_domain_view.app_list as app_list, images_storage_domain_view.storage_id as storage_id, images_storage_domain_view.vm_snapshot_id as vm_snapshot_id,
                      images_storage_domain_view.volume_type as volume_type, images_storage_domain_view.image_group_id as image_group_id, images_storage_domain_view.vm_guid as vm_guid,
                      images_storage_domain_view.active as active, images_storage_domain_view.volume_format as volume_format, images_storage_domain_view.disk_type as disk_type,
                      images_storage_domain_view.disk_interface as disk_interface, images_storage_domain_view.boot as boot, images_storage_domain_view.wipe_after_delete as wipe_after_delete, images_storage_domain_view.propagate_errors as propagate_errors,
                      images_storage_domain_view.entity_type as entity_type,images_storage_domain_view.quota_id as quota_id, images_storage_domain_view.quota_name as quota_name, disks.disk_alias as disk_alias, disks.disk_description as disk_description
FROM         images_storage_domain_view
INNER JOIN disk_image_dynamic ON images_storage_domain_view.image_guid = disk_image_dynamic.image_id
INNER JOIN disks ON images_storage_domain_view.image_group_id = disks.disk_id
WHERE images_storage_domain_view.active = TRUE;


CREATE OR REPLACE VIEW storage_domains
AS
SELECT
storage_domain_static.id as id,
		storage_domain_static.storage as storage,
		storage_domain_static.storage_name as storage_name,
        storage_pool_iso_map.storage_pool_id as storage_pool_id,
		storage_domain_dynamic.available_disk_size as available_disk_size,
		storage_domain_dynamic.used_disk_size as used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
        storage_pool_iso_map.status as status,
		storage_pool.name as storage_pool_name,
		storage_domain_static.storage_type as storage_type,
		storage_domain_static.storage_domain_type as storage_domain_type,
                storage_domain_static.storage_domain_format_type as storage_domain_format_type,
        storage_pool_iso_map.owner as owner,
        fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_domain_static.storage,storage_domain_static.storage_type) as storage_domain_shared_status,
	storage_domain_static.recoverable as recoverable
FROM    storage_domain_static
INNER JOIN storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id
LEFT OUTER JOIN storage_pool_iso_map ON storage_domain_static.id = storage_pool_iso_map.storage_id
LEFT OUTER JOIN storage_pool ON storage_pool_iso_map.storage_pool_id = storage_pool.id;




CREATE OR REPLACE VIEW storage_domains_without_storage_pools
AS
SELECT
storage_domain_static.id as id, storage_domain_static.storage as storage, storage_domain_static.storage_name as storage_name,
		storage_domain_static.storage_type as storage_type, storage_domain_static.storage_domain_type as storage_domain_type,
                storage_domain_static.storage_domain_format_type as storage_domain_format_type,
		null as status, null as owner, null as storage_pool_id, null as storage_pool_name,
		storage_domain_dynamic.available_disk_size as available_disk_size,
		storage_domain_dynamic.used_disk_size as used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
        fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_domain_static.storage,storage_domain_static.storage_type) as storage_domain_shared_status,
		storage_domain_static.recoverable as recoverable
FROM
storage_domain_static
INNER JOIN storage_domain_dynamic ON storage_domain_static.id = storage_domain_dynamic.id;


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
       vm_templates.vds_group_id as vds_group_id,
       vm_templates.domain as domain,
       vm_templates.num_of_monitors as num_of_monitors,
       vm_templates.template_status as status,
       vm_templates.usb_policy as usb_policy,
       vm_templates.time_zone as time_zone,
       vm_templates.is_auto_suspend as is_auto_suspend,
       vm_templates.fail_back as fail_back,
       vds_groups.name as vds_group_name,
       vm_templates.vm_type as vm_type,
       vm_templates.hypervisor_type as hypervisor_type,
       vm_templates.operation_mode as operation_mode,
       vm_templates.nice_level as nice_level,
       storage_pool.id as storage_pool_id,
       storage_pool.name as storage_pool_name,
       vm_templates.default_boot_sequence as default_boot_sequence,
       vm_templates.default_display_type as default_display_type,
       vm_templates.priority as priority,
       vm_templates.auto_startup as auto_startup,
       vm_templates.is_stateless as is_stateless,
       vm_templates.iso_path as iso_path,
       vm_templates.origin as origin,
       vm_templates.initrd_url as initrd_url,
       vm_templates.kernel_url as kernel_url,
       vm_templates.kernel_params as kernel_params,
       vm_templates.quota_id as quota_id,
       quota.quota_name as quota_name

FROM       vm_static AS vm_templates  INNER JOIN
vds_groups ON vm_templates.vds_group_id = vds_groups.vds_group_id
left outer JOIN
storage_pool ON storage_pool.id = vds_groups.storage_pool_id
left outer JOIN
quota ON vm_templates.quota_id = quota.id
WHERE entity_type = 'TEMPLATE';



CREATE OR REPLACE VIEW vm_templates_storage_domain
AS
	SELECT            vm_templates.vm_guid AS vmt_guid, vm_templates.vm_name AS name, vm_templates.mem_size_mb,
                      vm_templates.os, vm_templates.creation_date,		
                      vm_templates.child_count, vm_templates.num_of_sockets, vm_templates.cpu_per_socket,		
	                  vm_templates.num_of_sockets*vm_templates.cpu_per_socket AS num_of_cpus, vm_templates.description,		
	                  vm_templates.vds_group_id, vm_templates.domain, vm_templates.num_of_monitors, vm_templates.template_status AS status,		
	                  vm_templates.usb_policy, vm_templates.time_zone, vm_templates.is_auto_suspend, vm_templates.fail_back,		
	                  vds_groups.name AS vds_group_name, vm_templates.vm_type, vm_templates.hypervisor_type, vm_templates.operation_mode,		                      vm_templates.nice_level, storage_pool.id AS storage_pool_id, storage_pool.name
                      AS storage_pool_name,		
	                  vm_templates.default_boot_sequence, vm_templates.default_display_type, vm_templates.priority, vm_templates.auto_startup,		
	                  vm_templates.is_stateless, vm_templates.iso_path, vm_templates.origin, vm_templates.initrd_url, vm_templates.kernel_url,		
	                  vm_templates.kernel_params, image_storage_domain_map.storage_domain_id AS storage_id	
FROM                  vm_static AS vm_templates INNER JOIN		
	                  vds_groups ON vm_templates.vds_group_id = vds_groups.vds_group_id LEFT OUTER JOIN		
                      storage_pool ON storage_pool.id = vds_groups.storage_pool_id INNER JOIN		
                      vm_device ON vm_device.vm_id = vm_templates.vm_guid LEFT JOIN
	                  images ON images.image_group_id = vm_device.device_id
	                  LEFT JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid	
WHERE      entity_type = 'TEMPLATE'		
UNION
SELECT                vm_templates_1.vm_guid AS vmt_guid, vm_templates_1.vm_name AS name, vm_templates_1.mem_size_mb, vm_templates_1.os, vm_templates_1.creation_date,
                      vm_templates_1.child_count, vm_templates_1.num_of_sockets, vm_templates_1.cpu_per_socket,
                      vm_templates_1.num_of_sockets*vm_templates_1.cpu_per_socket AS num_of_cpus, vm_templates_1.description, vm_templates_1.vds_group_id,
                      vm_templates_1.domain, vm_templates_1.num_of_monitors, vm_templates_1.template_status AS status, vm_templates_1.usb_policy, vm_templates_1.time_zone,
                      vm_templates_1.is_auto_suspend, vm_templates_1.fail_back, vds_groups_1.name AS vds_group_name, vm_templates_1.vm_type,
                      vm_templates_1.hypervisor_type, vm_templates_1.operation_mode, vm_templates_1.nice_level, storage_pool_1.id AS storage_pool_id,
                      storage_pool_1.name AS storage_pool_name, vm_templates_1.default_boot_sequence, vm_templates_1.default_display_type,
                      vm_templates_1.priority, vm_templates_1.auto_startup, vm_templates_1.is_stateless, vm_templates_1.iso_path, vm_templates_1.origin,
                      vm_templates_1.initrd_url, vm_templates_1.kernel_url, vm_templates_1.kernel_params,
                      image_storage_domain_map.storage_domain_id AS storage_id
FROM                  vm_static AS vm_templates_1 INNER JOIN
                      vds_groups AS vds_groups_1 ON vm_templates_1.vds_group_id = vds_groups_1.vds_group_id LEFT OUTER JOIN
                      storage_pool AS storage_pool_1 ON storage_pool_1.id = vds_groups_1.storage_pool_id INNER JOIN
                      vm_device AS vm_device_1 ON vm_device_1.vm_id = vm_templates_1.vm_guid INNER JOIN
                      images AS images_1 ON images_1.image_group_id = vm_device_1.device_id INNER JOIN
                      image_storage_domain_map ON image_storage_domain_map.image_id = images_1.image_guid
WHERE                 entity_type = 'TEMPLATE';


CREATE OR REPLACE VIEW vm_pool_map_view
AS
SELECT
vm_pool_map.vm_guid as vm_guid,
vm_pool_map.vm_pool_id as vm_pool_id,
vm_pools.vm_pool_name as vm_pool_name
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
SELECT     vm_static.vm_name as vm_name, vm_static.mem_size_mb as vm_mem_size_mb, vm_static.nice_level as nice_level,
                      vm_static.vmt_guid as vmt_guid, vm_static.os as vm_os, vm_static.description as vm_description, vm_static.vds_group_id as vds_group_id,
                      vm_static.domain as vm_domain, vm_static.creation_date as vm_creation_date, vm_static.auto_startup as auto_startup, vm_static.is_stateless as is_stateless, vm_static.dedicated_vm_for_vds as dedicated_vm_for_vds,
                      vm_static.fail_back as fail_back, vm_static.default_boot_sequence as default_boot_sequence, vm_static.vm_type as vm_type,
					  vm_static.hypervisor_type as hypervisor_type, vm_static.operation_mode as operation_mode, vds_groups.name as vds_group_name, vds_groups.selection_algorithm as selection_algorithm, vds_groups.transparent_hugepages as transparent_hugepages,
					  storage_pool.id as storage_pool_id, storage_pool.name as storage_pool_name,
                      vds_groups.description as vds_group_description, vm_templates.vm_name as vmt_name,
                      vm_templates.mem_size_mb as vmt_mem_size_mb, vm_templates.os as vmt_os, vm_templates.creation_date as vmt_creation_date,
                      vm_templates.child_count as vmt_child_count, vm_templates.num_of_sockets as vmt_num_of_sockets,
                      vm_templates.cpu_per_socket as vmt_cpu_per_socket, vm_templates.num_of_sockets*vm_templates.cpu_per_socket as vmt_num_of_cpus,
                      vm_templates.description as vmt_description, vm_dynamic.status as status, vm_dynamic.vm_ip as vm_ip, vm_dynamic.vm_host as vm_host,
                      vm_dynamic.vm_pid as vm_pid, vm_dynamic.vm_last_up_time as vm_last_up_time, vm_dynamic.vm_last_boot_time as vm_last_boot_time, vm_dynamic.guest_cur_user_name as guest_cur_user_name,
                      vm_dynamic.guest_last_login_time as guest_last_login_time, vm_dynamic.guest_cur_user_id as guest_cur_user_id, vm_dynamic.guest_last_logout_time as guest_last_logout_time, vm_dynamic.guest_os as guest_os,
                      vm_dynamic.run_on_vds as run_on_vds, vm_dynamic.migrating_to_vds as migrating_to_vds, vm_dynamic.app_list as app_list, vm_dynamic.display as display, vm_dynamic.hibernation_vol_handle as hibernation_vol_handle,
                      vm_pool_map_view.vm_pool_name as vm_pool_name, vm_pool_map_view.vm_pool_id as vm_pool_id, vm_static.vm_guid as vm_guid, vm_static.num_of_monitors as num_of_monitors, vm_static.is_initialized as is_initialized,
                      vm_static.is_auto_suspend as is_auto_suspend, vm_static.num_of_sockets as num_of_sockets, vm_static.cpu_per_socket as cpu_per_socket, vm_static.usb_policy as usb_policy, vm_dynamic.acpi_enable as acpi_enable, vm_dynamic.session as session,
                      vm_static.num_of_sockets*vm_static.cpu_per_socket as num_of_cpus, vm_static.quota_id as quota_id, quota.quota_name as quota_name,
                      vm_dynamic.display_ip as display_ip, vm_dynamic.display_type as display_type, vm_dynamic.kvm_enable as kvm_enable, vm_dynamic.boot_sequence as boot_sequence,
                      vm_dynamic.display_secure_port as display_secure_port, vm_dynamic.utc_diff as utc_diff, vm_dynamic.last_vds_run_on as last_vds_run_on,
					  vm_dynamic.client_ip as client_ip,vm_dynamic.guest_requested_memory as guest_requested_memory, vm_static.time_zone as time_zone, vm_statistics.cpu_user as cpu_user, vm_statistics.cpu_sys as cpu_sys,
                      vm_statistics.elapsed_time as elapsed_time, vm_statistics.usage_network_percent as usage_network_percent,
                      vm_statistics.usage_mem_percent as usage_mem_percent, vm_statistics.usage_cpu_percent as usage_cpu_percent, vds_static.vds_name as run_on_vds_name, vds_groups.cpu_name as vds_group_cpu_name,
                      vm_static.default_display_type as default_display_type, vm_static.priority as priority,vm_static.iso_path as iso_path, vm_static.origin as origin, vds_groups.compatibility_version as vds_group_compatibility_version,
                      vm_static.initrd_url as initrd_url, vm_static.kernel_url as kernel_url, vm_static.kernel_params as kernel_params, vm_dynamic.pause_status as pause_status, vm_dynamic.exit_message as exit_message, vm_dynamic.exit_status as exit_status,vm_static.migration_support as migration_support,vm_static.predefined_properties as predefined_properties,vm_static.userdefined_properties as userdefined_properties,vm_static.min_allocated_mem as min_allocated_mem,  vm_dynamic.hash as hash
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
WHERE vm_static.entity_type = 'VM';



CREATE OR REPLACE VIEW vms_with_tags
AS
SELECT      vms.vm_name, vms.vm_mem_size_mb, vms.nice_level, vms.vmt_guid, vms.vm_os, vms.vm_description,
            vms.vds_group_id, vms.vm_domain, vms.vm_creation_date, vms.auto_startup, vms.is_stateless,
            vms.dedicated_vm_for_vds, vms.fail_back, vms.default_boot_sequence, vms.vm_type, vms.hypervisor_type,
            vms.operation_mode, vms.vds_group_name, vms.selection_algorithm, vms.storage_pool_id, vms.storage_pool_name,
            vms.vds_group_description, vms.vmt_name, vms.vmt_mem_size_mb, vms.vmt_os, vms.vmt_creation_date,
            vms.vmt_child_count, vms.vmt_num_of_sockets, vms.vmt_cpu_per_socket, vms.vmt_description, vms.status, vms.vm_ip,
            vms.vm_host, vms.vmt_num_of_sockets * vms.vmt_cpu_per_socket AS vmt_num_of_cpus, vms.vm_pid, vms.vm_last_up_time,
            vms.vm_last_boot_time, vms.guest_cur_user_name, vms.guest_last_login_time, vms.guest_cur_user_id,
            vms.guest_last_logout_time, vms.guest_os, vms.run_on_vds, vms.migrating_to_vds, vms.app_list, vms.display,
            vms.hibernation_vol_handle, vms.vm_pool_name, vms.vm_pool_id, vms.vm_guid, vms.num_of_monitors,
            vms.is_initialized, vms.is_auto_suspend, vms.num_of_sockets, vms.cpu_per_socket, vms.usb_policy, vms.acpi_enable,
            vms.session, vms.num_of_sockets * vms.cpu_per_socket AS num_of_cpus, vms.display_ip, vms.display_type,
            vms.kvm_enable, vms.boot_sequence, vms.display_secure_port, vms.utc_diff, vms.last_vds_run_on, vms.client_ip,
            vms.guest_requested_memory, vms.time_zone, vms.cpu_user, vms.cpu_sys, vms.elapsed_time,
            vms.usage_network_percent, vms.usage_mem_percent, vms.usage_cpu_percent, vms.run_on_vds_name,
            vms.vds_group_cpu_name, tags_vm_map_view.tag_name, tags_vm_map_view.tag_id, vms.default_display_type, vms.priority,
            vms.vds_group_compatibility_version, vms.initrd_url, vms.kernel_url, vms.kernel_params, vms.pause_status,
            vms.exit_status, vms.exit_message, vms.min_allocated_mem, storage_domain_static.id AS storage_id
FROM        vms LEFT OUTER JOIN
            tags_vm_map_view ON vms.vm_guid = tags_vm_map_view.vm_id LEFT OUTER JOIN
            vm_device ON vm_device.vm_id = vms.vm_guid LEFT OUTER JOIN
            images ON images.image_group_id = vm_device.device_id LEFT OUTER JOIN
            image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid LEFT OUTER JOIN
            storage_domain_static ON storage_domain_static.id = image_storage_domain_map.storage_domain_id
WHERE       images.active IS NULL OR images.active = TRUE;

CREATE OR REPLACE VIEW server_vms
as
SELECT * FROM vms
WHERE vm_type = '1';



CREATE OR REPLACE VIEW desktop_vms
as
SELECT * FROM vms
WHERE vm_type = '0';





CREATE OR REPLACE VIEW vds
as
SELECT     vds_groups.vds_group_id as vds_group_id, vds_groups.name as vds_group_name, vds_groups.description as vds_group_description,
                      vds_groups.selection_algorithm as selection_algorithm, vds_static.vds_id as vds_id, vds_static.vds_name as vds_name, vds_static.ip as ip, vds_static.vds_unique_id as vds_unique_id,
                      vds_static.host_name as host_name, vds_static.port as port, vds_static.vds_strength as vds_strength, vds_static.server_SSL_enabled as server_SSL_enabled, vds_static.vds_type as vds_type,
                      vds_static.pm_type as pm_type, vds_static.pm_user as pm_user, vds_static.pm_password as pm_password, vds_static.pm_port as pm_port,
                      vds_static.pm_options as pm_options, vds_static.pm_enabled as pm_enabled, vds_static.vds_spm_priority as vds_spm_priority, vds_dynamic.hooks as hooks,vds_dynamic.status as status, vds_dynamic.cpu_cores as cpu_cores, vds_dynamic.cpu_model as cpu_model,
                      vds_dynamic.cpu_speed_mh as cpu_speed_mh, vds_dynamic.if_total_speed as if_total_speed, vds_dynamic.kvm_enabled as kvm_enabled, vds_dynamic.physical_mem_mb as physical_mem_mb,
                      vds_dynamic.pending_vcpus_count as pending_vcpus_count, vds_dynamic.pending_vmem_size as pending_vmem_size,vds_dynamic.mem_commited as mem_commited, vds_dynamic.vm_active as vm_active, vds_dynamic.vm_count as vm_count,
                      vds_dynamic.vm_migrating as vm_migrating, vds_dynamic.vms_cores_count as vms_cores_count, vds_dynamic.cpu_over_commit_time_stamp as cpu_over_commit_time_stamp,
                      vds_dynamic.hypervisor_type as hypervisor_type, vds_dynamic.net_config_dirty as net_config_dirty, vds_groups.high_utilization as high_utilization, vds_groups.low_utilization as low_utilization,
                      vds_groups.max_vds_memory_over_commit as max_vds_memory_over_commit, vds_groups.cpu_over_commit_duration_minutes as cpu_over_commit_duration_minutes,
                      storage_pool.id as storage_pool_id, storage_pool.name as storage_pool_name, vds_dynamic.reserved_mem as reserved_mem,
                      vds_dynamic.guest_overhead as guest_overhead, vds_dynamic.software_version as software_version, vds_dynamic.version_name as version_name, vds_dynamic.build_name as build_name,
                      vds_dynamic.previous_status as previous_status, vds_statistics.cpu_idle as cpu_idle, vds_statistics.cpu_load as cpu_load, vds_statistics.cpu_sys as cpu_sys, vds_statistics.cpu_user as cpu_user,
                      vds_statistics.usage_mem_percent as usage_mem_percent, vds_statistics.usage_cpu_percent as usage_cpu_percent, vds_statistics.usage_network_percent as usage_network_percent,
                      vds_statistics.mem_available as mem_available, vds_statistics.mem_shared as mem_shared, vds_statistics.swap_free as swap_free,
					  vds_statistics.swap_total as swap_total,  vds_statistics.ksm_cpu_percent as ksm_cpu_percent,  vds_statistics.ksm_pages as ksm_pages,  vds_statistics.ksm_state as ksm_state,
                      vds_dynamic.cpu_flags as cpu_flags,vds_groups.cpu_name as vds_group_cpu_name, vds_dynamic.cpu_sockets as cpu_sockets, vds_spm_id_map.vds_spm_id as vds_spm_id, vds_static.otp_validity as otp_validity,
                      CASE WHEN storage_pool.spm_vds_id = vds_static.vds_id THEN CASE WHEN storage_pool.status = 5 THEN 1 ELSE 2 END ELSE 0 END as spm_status, vds_dynamic.supported_cluster_levels as supported_cluster_levels, vds_dynamic.supported_engines as supported_engines, vds_groups.compatibility_version as vds_group_compatibility_version,
                      vds_dynamic.host_os as host_os, vds_dynamic.kvm_version as kvm_version, vds_dynamic.spice_version as spice_version, vds_dynamic.kernel_version as kernel_version, vds_dynamic.iscsi_initiator_name as iscsi_initiator_name,
                      vds_dynamic.transparent_hugepages_state as transparent_hugepages_state, vds_dynamic.anonymous_hugepages as anonymous_hugepages, vds_dynamic.non_operational_reason as non_operational_reason,
			vds_static.recoverable as recoverable
FROM         vds_groups INNER JOIN
vds_static ON vds_groups.vds_group_id = vds_static.vds_group_id INNER JOIN
vds_dynamic ON vds_static.vds_id = vds_dynamic.vds_id INNER JOIN
vds_statistics ON vds_static.vds_id = vds_statistics.vds_id LEFT OUTER JOIN
storage_pool ON vds_groups.storage_pool_id = storage_pool.id LEFT OUTER JOIN
vds_spm_id_map ON vds_static.vds_id = vds_spm_id_map.vds_id;



CREATE OR REPLACE VIEW vds_with_tags
as
SELECT     vds_groups.vds_group_id, vds_groups.name AS vds_group_name, vds_groups.description AS vds_group_description,
                      vds_groups.selection_algorithm, vds_static.vds_id, vds_static.vds_name, vds_static.ip, vds_static.vds_unique_id,
                      vds_static.host_name, vds_static.port, vds_static.vds_strength, vds_static.server_SSL_enabled, vds_static.vds_type,
                      vds_static.pm_type, vds_static.pm_user, vds_static.pm_password, vds_static.pm_port,
                      vds_static.pm_options, vds_static.pm_enabled, vds_dynamic.hooks, vds_dynamic.status, vds_dynamic.cpu_cores,
                      vds_dynamic.cpu_model, vds_dynamic.cpu_speed_mh, vds_dynamic.if_total_speed, vds_dynamic.kvm_enabled,
                      vds_dynamic.physical_mem_mb, vds_dynamic.pending_vcpus_count, vds_dynamic.pending_vmem_size,
                      vds_dynamic.mem_commited, vds_dynamic.vm_active, vds_dynamic.vm_count, vds_dynamic.vm_migrating,
                      vds_dynamic.vms_cores_count, vds_dynamic.cpu_over_commit_time_stamp, vds_dynamic.hypervisor_type,
                      vds_dynamic.net_config_dirty, vds_groups.high_utilization, vds_groups.low_utilization,
                      vds_groups.max_vds_memory_over_commit, vds_groups.cpu_over_commit_duration_minutes,
                      storage_pool.id AS storage_pool_id, storage_pool.name AS storage_pool_name, tags_vds_map_view.tag_name,
                      tags_vds_map_view.tag_id, vds_dynamic.reserved_mem, vds_dynamic.guest_overhead, vds_dynamic.software_version,
                      vds_dynamic.version_name, vds_dynamic.build_name, vds_dynamic.previous_status, vds_statistics.cpu_idle,
                      vds_statistics.cpu_load, vds_statistics.cpu_sys, vds_statistics.cpu_user, vds_statistics.usage_mem_percent,
                      vds_statistics.usage_cpu_percent, vds_statistics.usage_network_percent, vds_statistics.mem_available,
                      vds_statistics.mem_shared, vds_statistics.swap_free, vds_statistics.swap_total, vds_statistics.ksm_cpu_percent,
                      vds_statistics.ksm_pages, vds_statistics.ksm_state, vds_dynamic.cpu_flags, vds_groups.cpu_name AS vds_group_cpu_name,
                      vds_dynamic.cpu_sockets, vds_spm_id_map.vds_spm_id, vds_static.otp_validity as otp_validity,
                      CASE WHEN storage_pool.spm_vds_id = vds_static.vds_id THEN CASE WHEN storage_pool.status = 5 THEN 1 ELSE 2 END ELSE 0 END AS
spm_status, vds_dynamic.supported_cluster_levels, vds_dynamic.supported_engines,
                      vds_groups.compatibility_version AS vds_group_compatibility_version, vds_dynamic.host_os, vds_dynamic.kvm_version,
                      vds_dynamic.spice_version, vds_dynamic.kernel_version, vds_dynamic.iscsi_initiator_name,
                      vds_dynamic.transparent_hugepages_state, vds_dynamic.anonymous_hugepages, vds_dynamic.non_operational_reason,
                      storage_pool_iso_map.storage_id
FROM         vds_groups INNER JOIN
vds_static ON vds_groups.vds_group_id = vds_static.vds_group_id INNER JOIN
vds_dynamic ON vds_static.vds_id = vds_dynamic.vds_id INNER JOIN
vds_statistics ON vds_static.vds_id = vds_statistics.vds_id LEFT OUTER JOIN
storage_pool ON vds_groups.storage_pool_id = storage_pool.id LEFT OUTER JOIN
tags_vds_map_view ON vds_static.vds_id = tags_vds_map_view.vds_id LEFT OUTER JOIN
vds_spm_id_map ON vds_static.vds_id = vds_spm_id_map.vds_id LEFT OUTER JOIN
storage_pool_iso_map ON storage_pool_iso_map.storage_pool_id = storage_pool.id;



CREATE OR REPLACE VIEW users_and_groups_to_vm_pool_map_view
AS
select	p.vm_pool_id as vm_pool_id, p.vm_pool_name as vm_pool_name, per.ad_element_id as user_id, tMap.start_time as "from", tMap.end_time as "to"
FROM    vm_pools AS p
INNER JOIN permissions as per on per.object_id = p.vm_pool_id
left outer JOIN time_lease_vm_pool_map as tMap on tMap.vm_pool_id = p.vm_pool_id;




CREATE OR REPLACE VIEW vdc_users
AS

SELECT     'user' as user_group, users_1.name as name, users_1.user_id as user_id, users_1.surname as surname, users_1.domain as domain, users_1.username as username, users_1.groups as groups, users_1.department as department,
                      users_1.role as role, users_1.user_icon_path as user_icon_path, users_1.desktop_device as desktop_device, users_1.email as email, users_1.note as note, users_1.status as status, 0 as vm_admin,
                      users_1.session_count as session_count, users_1.last_admin_check_status as last_admin_check_status, users_1.group_ids as group_ids
FROM         users AS users_1
UNION
SELECT     'group' as user_group, ad_groups.name as name, ad_groups.id as id, '' as surname, ad_groups.domain as domain, '' as username, '' as groups, '' as department, '' as role,
                      '' as user_icon_path, '' as desktop_device, '' as email, '' as note, ad_groups.status as status, 1 as vm_admin, 0 as session_count, null as last_admin_check_status, '' as group_ids
FROM         ad_groups;


-- create the new vdc_users_with_tags view with no use of the tag_permission_map

CREATE OR REPLACE VIEW vdc_users_with_tags
AS

SELECT     users_1.user_group as user_group, users_1.name as name, permissions.object_id as vm_guid, users_1.user_id as user_id, users_1.surname as surname, users_1.domain as domain,
                      users_1.username as username, users_1.groups as groups, users_1.department as department, users_1.role as role, roles1.name as mla_role, users_1.user_icon_path as user_icon_path, users_1.desktop_device as desktop_device, users_1.email as email,
                      users_1.note as note, users_1.status as status, users_1.vm_admin as vm_admin, tags_user_map_view_1.tag_name as tag_name, tags_user_map_view_1.tag_id as tag_id, users_1.session_count as session_count, users_1.last_admin_check_status as last_admin_check_status, users_1.group_ids as group_ids,
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
                      users_2.username as username, users_2.groups as groups, users_2.department as department, users_2.role as role, roles2.name as mla_role, users_2.user_icon_path as user_icon_path, users_2.desktop_device as desktop_device, users_2.email as email,
                      users_2.note as note, users_2.status as status, users_2.vm_admin as vm_admin, tags_user_group_map_view.tag_name as tag_name, tags_user_group_map_view.tag_id as tag_id,
                      users_2.session_count as session_count, users_2.last_admin_check_status as last_admin_check_status, users_2.group_ids as group_ids , pools1.vm_pool_name as vm_pool_name
FROM         vdc_users AS users_2 LEFT OUTER JOIN
users_and_groups_to_vm_pool_map_view AS pools1 ON users_2.user_id = pools1.user_id LEFT OUTER JOIN
permissions AS permissions_1 ON users_2.user_id = permissions_1.ad_element_id LEFT OUTER JOIN
tags AS tags_1 ON tags_1.type = 1 LEFT OUTER JOIN
tags_user_group_map_view ON users_2.user_id = tags_user_group_map_view.group_id LEFT OUTER JOIN
roles AS roles2 ON roles2.id = permissions_1.role_id
WHERE     (users_2.user_group = 'group');




CREATE OR REPLACE VIEW vm_pools_view
AS
SELECT     vm_pools.vm_pool_id as vm_pool_id, vm_pools.vm_pool_name as vm_pool_name, vm_pools.vm_pool_description as vm_pool_description, vm_pools.vm_pool_type as vm_pool_type,
                      vm_pools.parameters as parameters, vm_pools.prestarted_vms as prestarted_vms, vm_pools.vds_group_id as vds_group_id, vds_groups.name as vds_group_name
FROM         vm_pools INNER JOIN
vds_groups ON vm_pools.vds_group_id = vds_groups.vds_group_id;



CREATE OR REPLACE VIEW vm_pools_full_view
AS

SELECT     vm_pool_id as vm_pool_id, vm_pool_name as vm_pool_name, vm_pool_description as vm_pool_description, vm_pool_type as vm_pool_type, parameters as parameters, prestarted_vms as prestarted_vms, vds_group_id as vds_group_id, vds_group_name as vds_group_name,
              (SELECT     COUNT(vm_pool_map.vm_pool_id) as Expr1
   FROM	vm_pools_view AS v1 LEFT OUTER JOIN
   vm_pool_map ON v1.vm_pool_id = vm_pool_map.vm_pool_id AND v1.vm_pool_id = vmp.vm_pool_id) as assigned_vm_count,
              (SELECT     COUNT(v2.vm_pool_id) as Expr1
   FROM	vm_pools AS v2 LEFT OUTER JOIN
   vm_pool_map AS vm_pool_map_1 ON v2.vm_pool_id = vm_pool_map_1.vm_pool_id AND
   v2.vm_pool_id = vmp.vm_pool_id LEFT OUTER JOIN
   vm_dynamic ON vm_pool_map_1.vm_guid = vm_dynamic.vm_guid
   WHERE vm_dynamic.status not in(0,15)
   GROUP BY v2.vm_pool_id) as vm_running_count
FROM         vm_pools_view AS vmp;



CREATE OR REPLACE VIEW permissions_view
AS

SELECT     permissions.id as id, permissions.role_id as role_id, permissions.ad_element_id as ad_element_id, permissions.object_id as object_id, permissions.object_type_id as object_type_id,
	       roles.name as role_name, roles.role_type as role_type, fn_get_entity_name(permissions.object_id,permissions.object_type_id) as object_name,
			fn_get_ad_element_name(permissions.ad_element_id) as owner_name
FROM         permissions INNER JOIN
roles ON permissions.role_id = roles.id;


--
--SELECT     storages.id, storages.storage, storages.storage_pool_id, storages.storage_type, storage_pool.name,
--                      storage_pool.storage_pool_type
--FROM         storage_pool INNER JOIN
--                      storages ON storage_pool.id = storages.storage_pool_id
--

/*************************************************
            network view
*************************************************/


CREATE OR REPLACE VIEW network_view
AS
SELECT  distinct   network.id as id, network.name as name, network.description as description, network.type as type, network.addr as addr, network.subnet as subnet, network.gateway as gateway,
                      network.vlan_id as vlan_id, network.stp as stp, network.storage_pool_id as storage_pool_id, network_cluster.is_display as is_display, network_cluster.status as status
FROM         network LEFT OUTER JOIN
network_cluster ON network.id = network_cluster.network_id;



/*************************************************
        vds/vm/ interface view
*************************************************/
CREATE OR REPLACE VIEW vds_interface_view AS
  SELECT vds_interface_statistics.rx_rate, vds_interface_statistics.tx_rate, vds_interface_statistics.rx_drop,
      vds_interface_statistics.tx_drop, vds_interface_statistics.iface_status, vds_interface.type, vds_interface.gateway,
      vds_interface.subnet, vds_interface.addr, vds_interface.speed, vds_interface.vlan_id, vds_interface.bond_type,
      vds_interface.bond_name, vds_interface.is_bond, vds_interface.bond_opts, vds_interface.mac_addr,
      vds_interface.network_name, vds_interface.name, vds_static.vds_id, vds_static.vds_name,  vds_interface.id,
      vds_interface.boot_protocol, 1 AS is_vds
  FROM vds_interface_statistics
  JOIN vds_interface ON vds_interface_statistics.id = vds_interface.id
  JOIN vds_static ON vds_interface.vds_id = vds_static.vds_id;

CREATE OR REPLACE VIEW vm_interface_view AS
  SELECT vm_interface_statistics.rx_rate, vm_interface_statistics.tx_rate, vm_interface_statistics.rx_drop,
      vm_interface_statistics.tx_drop, vm_interface_statistics.iface_status, vm_interface.type, vm_interface.speed,
      vm_interface.mac_addr, vm_interface.network_name, vm_interface.name, vm_static.vm_guid, vm_interface.vmt_guid,
      vm_static.vm_name, vm_interface.id, 0 AS boot_protocol, 0 AS is_vds
  FROM vm_interface_statistics
  JOIN vm_interface ON vm_interface_statistics.id = vm_interface.id
  JOIN vm_static ON vm_interface.vm_guid = vm_static.vm_guid
  UNION
  SELECT vm_interface_statistics.rx_rate, vm_interface_statistics.tx_rate, vm_interface_statistics.rx_drop,
      vm_interface_statistics.tx_drop, vm_interface_statistics.iface_status, vm_interface.type, vm_interface.speed,
      vm_interface.mac_addr, vm_interface.network_name, vm_interface.name, NULL::uuid as vm_guid,
      vm_interface.vmt_guid, vm_templates.vm_name AS vm_name, vm_interface.id, 0 AS boot_protocol, 0 AS is_vds
  FROM vm_interface_statistics
  RIGHT JOIN vm_interface ON vm_interface_statistics.id = vm_interface.id
  JOIN vm_static AS vm_templates ON vm_interface.vmt_guid = vm_templates.vm_guid;

----------------------------------------------
-- Event Notification Views
----------------------------------------------



CREATE OR REPLACE VIEW event_audit_log_subscriber_view

AS
SELECT     1 as event_type, event_subscriber_1.subscriber_id as subscriber_id, event_subscriber_1.event_up_name as event_up_name, event_subscriber_1.method_id as method_id,
                      event_subscriber_1.method_address as method_address, event_subscriber_1.tag_name as tag_name, audit_log_1.audit_log_id as audit_log_id, audit_log_1.user_id as user_id, audit_log_1.user_name as user_name,
                      audit_log_1.vm_id as vm_id, audit_log_1.vm_name as vm_name, audit_log_1.vm_template_id as vm_template_id, audit_log_1.vm_template_name as vm_template_name, audit_log_1.vds_id as vds_id, audit_log_1.vds_name as vds_name,
                      audit_log_1.storage_pool_id as storage_pool_id, audit_log_1.storage_pool_name as storage_pool_name, audit_log_1.storage_domain_id as storage_domain_id, audit_log_1.storage_domain_name as storage_domain_name,
                      audit_log_1.log_time as log_time, audit_log_1.severity as severity, audit_log_1.message as message
FROM         audit_log AS audit_log_1 INNER JOIN
event_subscriber AS event_subscriber_1 ON audit_log_1.log_type_name = event_subscriber_1.event_up_name
WHERE     (audit_log_1.processed = false)
UNION
SELECT     distinct 0 as event_type, event_subscriber.subscriber_id as subscriber_id, audit_log.log_type_name as event_up_name, event_subscriber.method_id as method_id, event_subscriber.method_address as method_address,
                      event_subscriber.tag_name as tag_name, audit_log.audit_log_id as audit_log_id, audit_log.user_id as user_id, audit_log.user_name as user_name, audit_log.vm_id as vm_id, audit_log.vm_name as vm_name,
                      audit_log.vm_template_id as vm_template_id, audit_log.vm_template_name as vm_template_name, audit_log.vds_id as vds_id, audit_log.vds_name as vds_name, audit_log.storage_pool_id as storage_pool_id,
                      audit_log.storage_pool_name as storage_pool_name, audit_log.storage_domain_id as storage_domain_id, audit_log.storage_domain_name as storage_domain_name, audit_log.log_time as log_time, audit_log.severity as severity,
                      audit_log.message as message
FROM         audit_log AS audit_log INNER JOIN
event_map ON audit_log.log_type_name = event_map.event_down_name INNER JOIN
event_subscriber AS event_subscriber ON event_subscriber.event_up_name = event_map.event_up_name
WHERE     (audit_log.processed = false);




CREATE OR REPLACE VIEW event_subscriber_notification_methods_view

AS
SELECT     event_subscriber.subscriber_id as subscriber_id, event_subscriber.event_up_name as event_up_name, event_notification_methods.method_type as method_type
FROM         event_notification_methods INNER JOIN
event_subscriber ON event_notification_methods.method_id = event_subscriber.method_id;


----------------------------------------------
-- Storage Pool
----------------------------------------------
CREATE OR REPLACE VIEW storage_pool_with_storage_domain

AS
SELECT     storage_pool.id as id, storage_pool.name as name, storage_pool.description as description, storage_pool.storage_pool_type as storage_pool_type, storage_pool.status as status,
		   storage_pool.master_domain_version as master_domain_version, storage_pool.spm_vds_id as spm_vds_id, storage_pool.compatibility_version as compatibility_version, storage_pool._create_date as _create_date,
		   storage_pool._update_date as _update_date, storage_pool_iso_map.storage_id as storage_id, storage_pool_iso_map.storage_pool_id as storage_pool_id,
		   storage_pool_iso_map.owner as owner, storage_domain_static.storage_type as storage_type, storage_domain_static.storage_domain_type as storage_domain_type,
                   storage_domain_static.storage_domain_format_type as storage_domain_format_type,
		   storage_domain_static.storage_name as storage_name, storage_domain_static.storage as storage
FROM         storage_pool LEFT OUTER JOIN
		   storage_pool_iso_map ON storage_pool.id = storage_pool_iso_map.storage_pool_id LEFT OUTER JOIN
		   storage_domain_static ON storage_pool_iso_map.storage_id = storage_domain_static.id;


----------------------------------------------
-- Clusters
----------------------------------------------
CREATE OR REPLACE VIEW vds_groups_storage_domain

AS
SELECT     vds_groups.vds_group_id, vds_groups.name, vds_groups.description, vds_groups.cpu_name, vds_groups._create_date,
                      vds_groups._update_date, vds_groups.selection_algorithm, vds_groups.high_utilization, vds_groups.low_utilization,
                      vds_groups.cpu_over_commit_duration_minutes, vds_groups.hypervisor_type, vds_groups.storage_pool_id,
                      vds_groups.max_vds_memory_over_commit, vds_groups.compatibility_version,
                      vds_groups.transparent_hugepages, vds_groups.migrate_on_error,
                      storage_pool_iso_map.storage_id
FROM         vds_groups LEFT JOIN
storage_pool_iso_map ON vds_groups.storage_pool_id = storage_pool_iso_map.storage_pool_id;

CREATE OR REPLACE VIEW storage_domains_with_hosts_view

AS
SELECT
storage_domain_static.id,
		storage_domain_static.storage,
		storage_domain_static.storage_name,
		storage_domain_dynamic.available_disk_size,
		storage_domain_dynamic.used_disk_size,
		fn_get_disk_commited_value_by_storage(storage_domain_static.id) as commited_disk_size,
		storage_pool.name as storage_pool_name,
		storage_domain_static.storage_type,
		storage_domain_static.storage_domain_type,
                storage_domain_static.storage_domain_format_type,
		fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id,storage_domain_static.storage,storage_domain_static.storage_type) AS
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
    storage_pool.quota_enforcement_type as quota_enforcement_type,
    is_default_quota
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
    storage_pool.quota_enforcement_type as quota_enforcement_type,
    is_default_quota
FROM  storage_pool, quota q LEFT OUTER JOIN
quota_limitation q_limit on q_limit.quota_id = q.id
WHERE storage_pool.id = q.storage_pool_id
AND q_limit.vds_group_id IS NULL
AND q_limit.storage_id IS NULL;


CREATE OR REPLACE VIEW quota_storage_view
AS
SELECT q_limit.id as quota_storage_id,
    q_limit.quota_id as quota_id,
    storage_id,
    storage_domain_static.storage_name as storage_name,
    storage_size_gb,
    CalculateStorageUsage(quota_id,storage_id) as storage_size_gb_usage,
    is_default_quota
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
    (CalculateVdsGroupUsage(quota_id,q_limit.vds_group_id)).mem_size_mb_usage as mem_size_mb_usage,
    is_default_quota
FROM   quota_limitation q_limit, quota q, vds_groups
WHERE  q_limit.quota_id = q.id
AND  q_limit.vds_group_id IS NOT NULL
AND  q_limit.storage_id IS NULL
AND  vds_groups.vds_group_id = q_limit.vds_group_id;


----------------------------------------------
-- Query Permissions
----------------------------------------------

-- Flatten all the objects a user can get permissions on them
CREATE OR REPLACE VIEW user_flat_groups
AS
-- User with all its groups
SELECT users.user_id AS user_id, ad_groups.id AS granted_id
FROM   ad_groups, users
WHERE  ad_groups.id IN (SELECT id FROM fnsplitteruuid(users.group_ids))
UNION ALL
-- The user itself
SELECT user_id, user_id FROM users
UNION ALL
-- user is also member of 'Everyone'
SELECT user_id, 'EEE00000-0000-0000-0000-123456789EEE'
FROM   users;

-- A view that contains only the user permissions
CREATE OR REPLACE VIEW user_permissions_view
AS
SELECT * FROM permissions_view WHERE role_type = 2;


-- Permissions view for Clusters
-- The user has permissions on a cluster
CREATE OR REPLACE VIEW user_vds_groups_permissions_view_base (entity_id, granted_id)
AS
SELECT       object_id, ad_element_id
FROM         user_permissions_view
WHERE        object_type_id = 9
-- Or the object is a VM in the cluster
UNION ALL
SELECT       vds_group_id, ad_element_id
FROM         vm_static
INNER JOIN   user_permissions_view ON object_id = vm_guid AND object_type_id = 2
-- Or the object is the Data Center containing the Cluster
UNION ALL
SELECT       vds_group_id, ad_element_id
FROM         vds_groups
INNER JOIN   user_permissions_view ON object_id = vds_groups.storage_pool_id AND object_type_id = 14
-- Or the user has permissions on system;
UNION ALL
SELECT       vds_group_id, ad_element_id
FROM         user_permissions_view
CROSS JOIN   vds_groups
WHERE        object_type_id = 1;

CREATE OR REPLACE VIEW user_vds_groups_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vds_groups_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions view for Data Center
-- The user has permissions on a data center
CREATE OR REPLACE VIEW user_storage_pool_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 14
-- Or the object is a cluster in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vds_groups
INNER JOIN user_permissions_view ON object_id = vds_groups.vds_group_id AND object_type_id = 9
-- Or the object is vm pool in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vds_groups
INNER JOIN vm_pools ON vds_groups.vds_group_id = vm_pools.vds_group_id
INNER JOIN user_permissions_view ON object_id = vm_pools.vm_pool_id AND object_type_id = 5
-- Or the object is a VM in the data center
UNION ALL
SELECT     storage_pool_id, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN user_permissions_view ON object_id = vm_guid AND object_type_id = 2
-- Or the user has permission on system
UNION ALL
SELECT     storage_pool.id, ad_element_id
FROM       user_permissions_view
CROSS JOIN storage_pool
WHERE      object_type_id = 1;

CREATE OR REPLACE VIEW user_storage_pool_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_storage_pool_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions for Storage Domains
-- The user has permissions on a storage domain
CREATE OR REPLACE VIEW user_storage_domain_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 11
-- Or the user has permissions on a VM in the storage domain
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       storage_domains
INNER JOIN vds_groups ON vds_groups.storage_pool_id = storage_domains.storage_pool_id
INNER JOIN vm_static ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN user_permissions_view ON object_id = vm_static.vm_guid AND object_type_id = 2
-- Or the user has permissions on a template in the storage domain
UNION ALL
SELECT     storage_id, ad_element_id
FROM       vm_templates_storage_domain
INNER JOIN user_permissions_view ON vmt_guid = user_permissions_view.object_id AND object_type_id = 4
-- Or the user has permissions on a VM created from a template in the storage domain
UNION ALL
SELECT     storage_id, ad_element_id
FROM       vm_static
INNER JOIN vm_templates_storage_domain ON vm_static.vmt_guid = vm_templates_storage_domain.vmt_guid
INNER JOIN user_permissions_view ON vm_static.vm_guid = object_id AND objecT_type_id = 2
-- Or the user has permissions on the Data Center containing the storage domain
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       storage_domains
INNER JOIN user_permissions_view ON object_id = storage_domains.storage_pool_id AND object_type_id = 14
-- Or the user has permissions on System
UNION ALL
SELECT     storage_domains.id, ad_element_id
FROM       user_permissions_view
CROSS JOIN storage_domains
WHERE      object_type_id = 1;

CREATE OR REPLACE VIEW user_storage_domain_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_storage_domain_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions on Hosts
-- The user has permissions on a host
CREATE OR REPLACE VIEW user_vds_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 3
-- Or the user has permissions on a VM in the cluster or Data Center that contains the host
UNION ALL
SELECT     vds_id, ad_element_id
FROM       vds
INNER JOIN user_permissions_view ON (object_id = vds_group_id    AND object_type_id = 9) OR
                                    (object_id = storage_pool_id AND object_type_id = 14)
-- Or the user has permissions on System
UNION ALL
SELECT     vds_id, ad_element_id
FROM       user_permissions_view
CROSS JOIN vds
WHERE      object_type_id = 1;

CREATE OR REPLACE VIEW user_vds_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vds_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions on VM Pools
-- The user has permissions on the pool
CREATE OR REPLACE VIEW user_vm_pool_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 5
-- Or the user has permissions on a VM from the pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pool_map
INNER JOIN user_permissions_view ON object_id = vm_guid AND object_type_id = 2
-- Or the user has permissions on the cluster containing the pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pools
INNER JOIN user_permissions_view ON object_id = vds_group_id AND object_type_id = 9
-- Or the user has permission on the data center containing the VM pool
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       vm_pools
INNER JOIN vds_groups ON vm_pools.vds_group_id =  vds_groups.vds_group_id
INNER JOIN user_permissions_view ON object_id = storage_pool_id AND object_type_id = 14
-- Or the user has permissions on System
UNION ALL
SELECT     vm_pool_id, ad_element_id
FROM       user_permissions_view
CROSS JOIN vm_pools
WHERE      object_type_id = 1;

CREATE OR REPLACE VIEW user_vm_pool_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_pool_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions on Templates
-- The user has permissions on the template
CREATE OR REPLACE VIEW user_vm_template_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 4
-- Or the user has permissions on a VM created from the tempalate
UNION ALL
SELECT     vmt_guid, ad_element_id
FROM       vm_static
INNER JOIN user_permissions_view ON object_id = vm_static.vm_guid AND object_type_id = 2
-- Or the user has permissions on the data center containing the template
UNION ALL
SELECT     vmt_guid, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN user_permissions_view ON object_id = storage_pool_id AND object_type_id = 14
-- Or the user has permissions on system
UNION ALL
SELECT     vmt_guid, ad_element_id
FROM       user_permissions_view
CROSS JOIN vm_static
WHERE      object_type_id = 1 AND vm_static.entity_type::text = 'TEMPLATE'::text;

CREATE OR REPLACE VIEW user_vm_template_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_template_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions on VMs
-- The user has permission on the VM
CREATE OR REPLACE VIEW user_vm_permissions_view_base (entity_id, granted_id)
AS
SELECT     object_id, ad_element_id
FROM       user_permissions_view
WHERE      object_type_id = 2
-- Or the user has permissions on the cluster containing the VM
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       vm_static
INNER JOIN user_permissions_view ON object_id = vds_group_id AND object_type_id = 9
-- Or the user has permissions on the data center containing the VM
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       vm_static
INNER JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
INNER JOIN user_permissions_view ON object_id = storage_pool_id AND object_type_id = 14
-- Or the user has permissions on system
UNION ALL
SELECT     vm_guid, ad_element_id
FROM       user_permissions_view
CROSS JOIN vm_static
WHERE      object_type_id = 1;

CREATE OR REPLACE VIEW user_vm_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT entity_id, user_id
FROM         user_vm_permissions_view_base
NATURAL JOIN user_flat_groups;


-- Permissions on permissions
CREATE OR REPLACE VIEW user_permissions_permissions_view (entity_id, user_id)
AS
SELECT       DISTINCT id, ad_element_id
FROM         user_permissions_view
JOIN         user_flat_groups ON granted_id = ad_element_id;
