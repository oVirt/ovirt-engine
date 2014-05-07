--************************************************
--            DataWarehouse Views
--
--IMPORTANT NOTE
-----------------
--These views are used by the history ETL process,
--please do not change their scheme without touching
--base with Eli first !!!
--************************************************

CREATE OR REPLACE VIEW dwh_datacenter_configuration_history_view
AS
SELECT     	id AS datacenter_id,
			name AS datacenter_name,
			description AS datacenter_description,
			is_local AS is_local_storage,
			_create_date AS create_date,
            _update_date AS update_date
FROM        storage_pool
WHERE     	(_create_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
            (_update_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync')));

CREATE OR REPLACE VIEW dwh_datacenter_history_view
AS
SELECT
    id as datacenter_id,
	cast(status as smallint) as datacenter_status
FROM storage_pool;

CREATE OR REPLACE VIEW dwh_storage_domain_configuration_history_view
AS
SELECT     	id AS storage_domain_id,
			storage_name AS storage_domain_name,
			cast(storage_domain_type as smallint) as storage_domain_type,
			cast(storage_type as smallint) as storage_type,
			_create_date AS create_date,
            _update_date AS update_date
FROM        storage_domain_static
WHERE     (_create_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (_update_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync')));

CREATE OR REPLACE VIEW dwh_datacenter_storage_map_history_view
AS
SELECT DISTINCT storage_pool_id AS datacenter_id,
                storage_id AS storage_domain_id
FROM storage_pool_iso_map;

CREATE OR REPLACE VIEW dwh_storage_domain_history_view
AS
SELECT storage_domain_dynamic.id as storage_domain_id,
       fn_get_storage_domain_shared_status_by_domain_id(storage_domain_static.id, status_table.status, storage_domain_static.storage_domain_type) AS storage_domain_status,
       storage_domain_dynamic.available_disk_size as available_disk_size_gb,
       storage_domain_dynamic.used_disk_size as used_disk_size_gb
FROM storage_domain_dynamic
         INNER JOIN storage_domain_static ON (storage_domain_dynamic.id = storage_domain_static.id)
             LEFT OUTER JOIN (SELECT storage_id, max(status) AS status
                              FROM storage_pool_iso_map
                              GROUP BY storage_id) AS status_table ON storage_domain_static.id=status_table.storage_id;

CREATE OR REPLACE VIEW dwh_cluster_configuration_history_view
AS
SELECT     	vds_group_id AS cluster_id,
			name AS cluster_name,
			description as cluster_description,
			storage_pool_id AS datacenter_id,
			cpu_name,
			compatibility_version,
            _create_date AS create_date,
			_update_date AS update_date
FROM        vds_groups
WHERE     (_create_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (_update_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync')));

CREATE OR REPLACE VIEW dwh_host_configuration_history_view
AS
SELECT     	a.vds_id AS host_id,
			a.vds_unique_id AS host_unique_id,
			a.vds_name AS host_name,
			a.vds_group_id AS cluster_id,
			cast(a.vds_type as smallint) AS host_type,
			a.host_name AS fqdn_or_ip,
			b.physical_mem_mb AS memory_size_mb,
			cast(c.swap_total as int) as swap_size_mb,
			b.cpu_model,
			cast(b.cpu_cores as smallint) AS number_of_cores,
                        cast(b.cpu_sockets as smallint) AS number_of_sockets,
                        b.cpu_speed_mh,
			b.host_os,
			b.kernel_version,
			b.kvm_version,
			b.libvirt_version,
			b.software_version as vdsm_version,
			a.port AS vdsm_port,
			a._create_date AS create_date,
			a._update_date AS update_date
FROM        vds_static AS a
			INNER JOIN
					vds_dynamic AS b ON a.vds_id = b.vds_id
			INNER JOIN
					vds_statistics AS c ON c.vds_id = a.vds_id
WHERE     (a._create_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (a._update_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync')));

CREATE OR REPLACE VIEW dwh_host_configuration_full_check_view
AS
SELECT     	a.vds_id AS host_id,
			a.vds_unique_id AS host_unique_id,
			a.vds_name AS host_name,
			a.vds_group_id AS cluster_id,
			cast(a.vds_type as smallint) AS host_type,
			a.host_name AS fqdn_or_ip,
			b.physical_mem_mb AS memory_size_mb,
			cast(c.swap_total as int) as swap_size_mb,
			b.cpu_model,
			cast(b.cpu_cores as smallint) AS number_of_cores,
                        cast(b.cpu_sockets as smallint) AS number_of_sockets,
                        b.cpu_speed_mh,
			b.host_os,
			b.kernel_version,
			b.kvm_version,
			b.libvirt_version,
      			CASE SUBSTR(b.software_version,1,3)
				WHEN '4.4' THEN '2.1' || SUBSTR(b.software_version,4,LENGTH(b.software_version))
				WHEN '4.5' THEN '2.2' || SUBSTR(b.software_version,4,LENGTH(b.software_version))
				WHEN '4.9' THEN '2.3' || SUBSTR(b.software_version,4,LENGTH(b.software_version))
	   	        ELSE b.software_version
	 	        END as vdsm_version,
			a.port AS vdsm_port,
			a._create_date AS create_date,
			a._update_date AS update_date
FROM        vds_static AS a
			INNER JOIN
					vds_dynamic AS b ON a.vds_id = b.vds_id
			INNER JOIN
					vds_statistics AS c ON c.vds_id = a.vds_id;

CREATE OR REPLACE VIEW dwh_host_history_view
AS
SELECT
	b.vds_id as host_id,
	cast(b.status as smallint) as host_status,
	cast(c.usage_mem_percent as smallint) as memory_usage_percent,
	cast(c.usage_cpu_percent as smallint) as cpu_usage_percent,
	cast(c.ksm_cpu_percent as smallint) as ksm_cpu_percent,
	cast(c.cpu_load as int) as cpu_load,
	cast(c.cpu_sys as smallint) as system_cpu_usage_percent,
	cast(c.cpu_user as smallint) as user_cpu_usage_percent,
	cast((c.swap_total - c.swap_free) as int) as swap_used_mb,
	cast(b.vm_active as smallint) as vm_active,
	cast(b.vm_count as smallint) as total_vms,
	b.vms_cores_count as total_vms_vcpus,
	c.mem_shared as ksm_shared_memory_mb
FROM vds_dynamic b, vds_statistics c
where	b.vds_id  = c.vds_id;

CREATE OR REPLACE VIEW dwh_host_interface_configuration_history_view AS
SELECT  a.id AS host_interface_id,
		a.name AS host_interface_name,
		a.vds_id AS host_id,
		cast(a.type as smallint) as host_interface_type,
		a.speed as host_interface_speed_bps,
		a.mac_addr AS mac_address,
		a.network_name as logical_network_name,
		a.addr AS ip_address,
		a.gateway,
		a.is_bond AS bond,
		a.bond_name,
		a.vlan_id,
		a._create_date AS create_date,
		a._update_date AS update_date
FROM         vds_interface as a
WHERE     ((a._create_date >
                          (SELECT     var_datetime
                            FROM          dwh_history_timekeeping
                            WHERE      (var_name = 'lastSync'))) OR
           (a._update_date >
                          (SELECT     var_datetime
                            FROM          dwh_history_timekeeping AS history_timekeeping_1
                            WHERE      (var_name = 'lastSync')))) AND
		   (a.is_bond IS NULL OR
		   a.is_bond = false OR
		   (a.is_bond = true and a.name in (SELECT b.bond_name
											FROM vds_interface AS b
											where b.is_bond is null and b.vds_id = a.vds_id)));

CREATE OR REPLACE VIEW dwh_host_interface_history_view
AS
SELECT     vds_interface_statistics.id as host_interface_id,
		   cast(vds_interface_statistics.rx_rate as smallint) as receive_rate_percent,
		   cast(vds_interface_statistics.tx_rate as smallint) as transmit_rate_percent
FROM       vds_interface_statistics;

CREATE OR REPLACE VIEW dwh_vm_configuration_history_view

AS
SELECT     	a.vm_guid AS vm_id,
			a.vm_name,
			a.description as vm_description,
			cast(a.vm_type as smallint) as vm_type,
			a.vds_group_id AS cluster_id,
			a.vmt_guid AS template_id,
            b.vm_name AS template_name,
			cast(a.cpu_per_socket as smallint) as cpu_per_socket,
			cast(a.num_of_sockets as smallint) AS number_of_sockets,
			a.mem_size_mb AS memory_size_mb,
            cast(a.os as smallint) AS operating_system,
			a.dedicated_vm_for_vds AS default_host,
			a.auto_startup AS high_availability,
            a.is_initialized AS initialized,
			a.is_stateless AS stateless,
			a.fail_back,
			cast(a.usb_policy as smallint) as usb_policy,
			a.time_zone,
			c.vm_pool_id,
			d.vm_pool_name,
			e.user_id AS created_by_user_id,
			a._create_date AS create_date,
            a._update_date AS update_date
FROM        vm_static as a
				INNER JOIN vm_static as b ON a.vmt_guid = b.vm_guid
				LEFT OUTER JOIN vm_pool_map as c ON a.vm_guid = c. vm_guid
					LEFT OUTER JOIN vm_pools as d ON c.vm_pool_id = d.vm_pool_id
				LEFT OUTER JOIN users as e ON a.created_by_user_id = e.user_id
WHERE     (a.entity_type = 'VM' AND b.entity_type = 'TEMPLATE') AND
          ((a._create_date >
                          (SELECT     var_datetime
                           FROM       dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (a._update_date >
                          (SELECT     var_datetime
                           FROM       dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync'))) OR
          (b._update_date >
                          (SELECT     var_datetime
                           FROM       dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync'))));

CREATE OR REPLACE VIEW dwh_vm_history_view
AS
SELECT
	c.vm_guid as vm_id,
	cast(b.status as smallint) as vm_status,
	cast(c.usage_cpu_percent as smallint) as cpu_usage_percent,
	cast(c.usage_mem_percent as smallint) as memory_usage_percent,
	cast(
		(c.cpu_sys / (vm_static.cpu_per_socket * vm_static.num_of_sockets))
		as smallint
	)
	as system_cpu_usage_percent,
	cast(
		(c.cpu_user / (vm_static.cpu_per_socket * vm_static.num_of_sockets))
		as smallint
	)
	as user_cpu_usage_percent,
	c.disks_usage,
	b.vm_ip,
	b.vm_fqdn,
	b.client_ip as vm_client_ip,
	b.console_user_id as current_user_id,
	CASE
		WHEN b.guest_cur_user_name IS NULL THEN FALSE
		ELSE TRUE
	END as user_logged_in_to_guest,
	b.run_on_vds as currently_running_on_host
FROM vm_dynamic b
    LEFT OUTER JOIN
        vm_statistics c ON  c.vm_guid = b.vm_guid
    INNER JOIN
        vm_static ON c.vm_guid = vm_static.vm_guid;

CREATE OR REPLACE VIEW dwh_vm_interface_configuration_history_view
AS
SELECT     	vm_interface.id AS vm_interface_id,
            vm_interface.name AS vm_interface_name,
            vm_interface.vm_guid AS vm_id,
            cast(vm_interface.type as smallint) as vm_interface_type,
            vm_interface.speed as vm_interface_speed_bps,
            vm_interface.mac_addr AS mac_address,
            network.name AS logical_network_name,
            vm_interface._create_date AS create_date,
            vm_interface._update_date AS update_date
FROM        vm_interface
            LEFT OUTER JOIN (vnic_profiles JOIN network ON network.id = vnic_profiles.network_id)
            ON vnic_profiles.id = vm_interface.vnic_profile_id
WHERE     vmt_guid IS NULL AND
          ((vm_interface._create_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (vm_interface._update_date >
                          (SELECT     var_datetime
                           FROM          dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync'))));

CREATE OR REPLACE VIEW dwh_vm_interface_history_view
AS
SELECT     vm_interface_statistics.id as vm_interface_id,
		   cast(vm_interface_statistics.rx_rate as smallint) as receive_rate_percent,
		   cast(vm_interface_statistics.tx_rate as smallint) as transmit_rate_percent
FROM       vm_interface_statistics;

CREATE OR REPLACE VIEW dwh_vm_disk_configuration_history_view
AS
SELECT d.disk_id AS vm_disk_id,
       d.disk_alias as vm_disk_name,
       d.disk_description as vm_disk_description,
       i.image_guid AS image_id,
       image_storage_domain_map.storage_domain_id as storage_domain_id,
       cast(i.size / 1048576 as int) as vm_disk_size_mb,
       cast(i.volume_type as smallint) AS vm_disk_type,
       cast(i.volume_format as smallint) AS vm_disk_format,
       CASE
           WHEN d.disk_interface = 'IDE' THEN cast(0 as smallint)
           WHEN d.disk_interface = 'SCSI' THEN cast(1 as smallint)
           WHEN d.disk_interface = 'VirtIO' THEN cast(2 as smallint)
       END AS vm_disk_interface,
       d.shareable as is_shared,
       i._create_date AS create_date,
       i._update_date AS update_date
FROM   images as i
           INNER JOIN
               base_disks as d ON i.image_group_id = d.disk_id
           INNER JOIN
               image_storage_domain_map ON image_storage_domain_map.image_id = i.image_guid
           LEFT OUTER JOIN vm_device ON vm_device.device_id = i.image_group_id
                               LEFT OUTER JOIN vm_static ON vm_static.vm_guid = vm_device.vm_id
WHERE     i.active = true AND
          (vm_static.entity_type = 'VM' OR vm_static.entity_type IS NULL) AND
          ((i._create_date >
                          (SELECT     var_datetime
                           FROM         dwh_history_timekeeping
                           WHERE      (var_name = 'lastSync'))) OR
          (i._update_date >
                          (SELECT     var_datetime
                           FROM         dwh_history_timekeeping AS history_timekeeping_1
                           WHERE      (var_name = 'lastSync'))));

CREATE OR REPLACE VIEW dwh_disk_vm_map_history_view
AS
SELECT image_guid as vm_disk_id,
       vm_device.vm_id
  FROM vm_device
           INNER JOIN
               images ON vm_device.device_id = images.image_group_id
WHERE images.active = true;


CREATE OR REPLACE VIEW dwh_vm_device_history_view
AS
SELECT device_id,
       vm_id,
       type,
       address,
       is_managed,
       is_plugged,
       is_readonly,
       _create_date as create_date,
       _update_date as update_date
  FROM vm_device
WHERE  ((type = 'disk' AND
       device = 'disk') OR
       (type = 'interface')) AND
       ((_create_date >  (SELECT     var_datetime
                         FROM         dwh_history_timekeeping
                         WHERE      (var_name = 'lastSync'))) OR
       (_update_date >  (SELECT     var_datetime
                         FROM         dwh_history_timekeeping AS history_timekeeping_1
                         WHERE      (var_name = 'lastSync'))));

CREATE OR REPLACE VIEW dwh_vm_disks_history_view
AS
SELECT  d.disk_id as vm_disk_id,
	images.image_guid as image_id,
	cast(images.imageStatus as smallint) as vm_disk_status,
	cast(disk_image_dynamic.actual_size / 1048576 as int) as vm_disk_actual_size_mb,
	disk_image_dynamic.read_rate as read_rate_bytes_per_second,
	disk_image_dynamic.read_latency_seconds as read_latency_seconds,
	disk_image_dynamic.write_rate as write_rate_bytes_per_second,
	disk_image_dynamic.write_latency_seconds as write_latency_seconds,
	disk_image_dynamic.flush_latency_seconds as flush_latency_seconds
FROM    images
            INNER JOIN
                 disk_image_dynamic ON images.image_guid = disk_image_dynamic.image_id
            INNER JOIN
                 base_disks as d ON images.image_group_id = d.disk_id
            LEFT OUTER JOIN vm_device ON vm_device.device_id = images.image_group_id
                                LEFT OUTER JOIN vm_static ON vm_static.vm_guid = vm_device.vm_id
WHERE images.active = true AND
      (vm_static.entity_type = 'VM' OR vm_static.entity_type IS NULL);

CREATE OR REPLACE VIEW dwh_remove_tags_relations_history_view AS
SELECT    tag_id as entity_id,
		  parent_id as parent_id
FROM      tags
UNION ALL
SELECT    vds_id as vds_id,
          tag_id as tag_id
FROM      tags_vds_map
UNION ALL
SELECT     vm_pool_id as vm_pool_id,
           tag_id as tag_id
FROM       tags_vm_pool_map

UNION ALL
SELECT     vm_id as vm_id,
		   tag_id as tag_id
FROM       tags_vm_map

UNION ALL
SELECT     user_id as user_id,
		   tag_id as tag_id
FROM       tags_user_map

UNION ALL
SELECT    group_id as group_id,
		  tag_id as tag_id
FROM      tags_user_group_map;


CREATE OR REPLACE VIEW dwh_add_tags_relations_history_view AS
SELECT    tag_id as entity_id,
		  parent_id as parent_id,
		  cast(18 as smallint) as entity_type,
		  _create_date as attach_date,
		  _update_date as move_date
FROM      tags
WHERE	  (_create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'))) OR
		  (_update_date >(SELECT     var_datetime as var_datetime
					   	  FROM       dwh_history_timekeeping AS history_timekeeping_1
						  WHERE      (var_name = 'lastSync')))
UNION ALL
SELECT    vds_id as vds_id,
          tag_id as tag_id,
          cast(3 as smallint),
		  _create_date,
		  null
FROM      tags_vds_map
WHERE	  _create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'))
UNION ALL
SELECT     vm_pool_id as vm_pool_id,
           tag_id as tag_id,
		   cast(5 as smallint),
		  _create_date,
		  null
FROM       tags_vm_pool_map
WHERE	  _create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'))
UNION ALL
SELECT     vm_id as vm_id,
		   tag_id as tag_id,
		   cast(2 as smallint),
		  _create_date,
		  null
FROM       tags_vm_map
WHERE	  _create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'))
UNION ALL
SELECT     user_id as user_id,
		   tag_id as tag_id,
		   cast(15 as smallint),
		  _create_date,
		  null
FROM       tags_user_map
WHERE	  _create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'))
UNION ALL
SELECT    group_id as group_id,
		  tag_id as tag_id,
		  cast(17 as smallint),
		  _create_date,
		  null
FROM      tags_user_group_map
WHERE	  _create_date >(SELECT     var_datetime as var_datetime
					      FROM          dwh_history_timekeeping
					      WHERE      (var_name = 'lastSync'));


CREATE OR REPLACE VIEW dwh_tags_details_history_view AS
SELECT     	tag_id as tag_id,
			tag_name as tag_name,
			description as tag_description,
			_create_date as create_date,
			_update_date as update_date
FROM       tags
WHERE     (_create_date >(SELECT     var_datetime as var_datetime
						  FROM       dwh_history_timekeeping
						  WHERE      (var_name = 'lastSync'))) OR
          (_update_date >(SELECT     var_datetime as var_datetime
						  FROM       dwh_history_timekeeping AS history_timekeeping_1
						  WHERE      (var_name = 'lastSync')));

CREATE OR REPLACE VIEW dwh_users_history_view
AS
SELECT
    user_id,
    name AS first_name,
    surname AS last_name,
    domain,
    username,
    department,
    role AS user_role_title,
    email,
    external_id,
    active,
    _create_date AS create_date,
    _update_date AS update_date
FROM users;