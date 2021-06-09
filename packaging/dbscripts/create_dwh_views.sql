

--************************************************
--            DataWarehouse Views
--
--IMPORTANT NOTE
-----------------
--These views are used by the history ETL process,
--please do not change their scheme without touching
--base with Eli first !!!
--************************************************
CREATE OR REPLACE VIEW dwh_datacenter_configuration_history_view AS

SELECT id AS datacenter_id,
    name AS datacenter_name,
    description AS datacenter_description,
    is_local AS is_local_storage,
    _create_date AS create_date,
    _update_date AS update_date
FROM storage_pool
WHERE (
        _create_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        _update_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        );

CREATE OR REPLACE VIEW dwh_storage_domain_configuration_history_view AS

SELECT id AS storage_domain_id,
    storage_name AS storage_domain_name,
    CAST(storage_domain_type AS SMALLINT) AS storage_domain_type,
    CAST(storage_type AS SMALLINT) AS storage_type,
    _create_date AS create_date,
    _update_date AS update_date
FROM storage_domain_static
WHERE (
        _create_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        _update_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        );

CREATE OR REPLACE VIEW dwh_datacenter_storage_map_history_view AS

SELECT DISTINCT storage_pool_id AS datacenter_id,
    storage_id AS storage_domain_id
FROM storage_pool_iso_map;

CREATE OR REPLACE VIEW dwh_storage_domain_history_view AS

SELECT storage_domain_dynamic.id AS storage_domain_id,
    COALESCE(storage_domain_shared_status.status, 0) AS storage_domain_status,
    storage_domain_dynamic.available_disk_size AS available_disk_size_gb,
    storage_domain_dynamic.used_disk_size AS used_disk_size_gb,
    storage_domain_dynamic.confirmed_available_disk_size AS confirmed_available_disk_size_gb,
    storage_domain_dynamic.vdo_savings AS vdo_savings
FROM storage_domain_dynamic
INNER JOIN storage_domain_static
    ON (storage_domain_dynamic.id = storage_domain_static.id)
LEFT JOIN storage_domain_shared_status
    ON storage_domain_shared_status.storage_id = storage_domain_static.id;

CREATE OR REPLACE VIEW dwh_cluster_configuration_history_view AS

SELECT cluster_id AS cluster_id,
    name AS cluster_name,
    description AS cluster_description,
    storage_pool_id AS datacenter_id,
    cpu_name,
    count_threads_as_cores AS count_threads_as_cores,
    compatibility_version,
    _create_date AS create_date,
    _update_date AS update_date
FROM cluster
WHERE (
        _create_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        _update_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        );

CREATE OR REPLACE VIEW dwh_host_configuration_history_view AS

SELECT a.vds_id AS host_id,
    a.vds_unique_id AS host_unique_id,
    a.vds_name AS host_name,
    a.cluster_id AS cluster_id,
    CAST(a.vds_type AS SMALLINT) AS host_type,
    a.host_name AS fqdn_or_ip,
    b.physical_mem_mb AS memory_size_mb,
    CAST(c.swap_total AS INT) AS swap_size_mb,
    b.cpu_model,
    CAST(b.cpu_cores AS SMALLINT) AS number_of_cores,
    CAST(b.cpu_sockets AS SMALLINT) AS number_of_sockets,
    b.cpu_speed_mh,
    b.host_os,
    b.kernel_version,
    b.kvm_version,
    b.libvirt_version,
    b.software_version AS vdsm_version,
    a.port AS vdsm_port,
    CAST(b.cpu_threads AS SMALLINT) AS number_of_threads,
    b.hw_manufacturer AS hardware_manufacturer,
    b.hw_product_name AS hardware_product_name,
    b.hw_version AS hardware_version,
    b.hw_serial_number AS hardware_serial_number,
    a._create_date AS create_date,
    a._update_date AS update_date
FROM vds_static AS a
INNER JOIN vds_dynamic AS b
    ON a.vds_id = b.vds_id
INNER JOIN vds_statistics AS c
    ON c.vds_id = a.vds_id
WHERE (
        a._create_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        a._update_date > (
            SELECT var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        );

CREATE OR REPLACE VIEW dwh_host_configuration_full_check_view AS

SELECT a.vds_id AS host_id,
    a.vds_unique_id AS host_unique_id,
    a.vds_name AS host_name,
    a.cluster_id AS cluster_id,
    CAST(a.vds_type AS SMALLINT) AS host_type,
    a.host_name AS fqdn_or_ip,
    b.physical_mem_mb AS memory_size_mb,
    CAST(c.swap_total AS INT) AS swap_size_mb,
    b.cpu_model,
    CAST(b.cpu_cores AS SMALLINT) AS number_of_cores,
    CAST(b.cpu_sockets AS SMALLINT) AS number_of_sockets,
    b.cpu_speed_mh,
    b.host_os,
    b.kernel_version,
    b.kvm_version,
    b.libvirt_version,
    CASE SUBSTR(b.software_version, 1, 3)
        WHEN '4.4'
            THEN '2.1' || SUBSTR(b.software_version, 4, LENGTH(b.software_version))
        WHEN '4.5'
            THEN '2.2' || SUBSTR(b.software_version, 4, LENGTH(b.software_version))
        WHEN '4.9'
            THEN '2.3' || SUBSTR(b.software_version, 4, LENGTH(b.software_version))
        ELSE b.software_version
        END AS vdsm_version,
    a.port AS vdsm_port,
    CAST(b.cpu_threads AS SMALLINT) AS number_of_threads,
    b.hw_manufacturer AS hardware_manufacturer,
    b.hw_product_name AS hardware_product_name,
    b.hw_version AS hardware_version,
    b.hw_serial_number AS hardware_serial_number,
    a._create_date AS create_date,
    a._update_date AS update_date
FROM vds_static AS a
INNER JOIN vds_dynamic AS b
    ON a.vds_id = b.vds_id
INNER JOIN vds_statistics AS c
    ON c.vds_id = a.vds_id;

CREATE OR REPLACE VIEW dwh_host_history_view AS

SELECT b.vds_id AS host_id,
    CAST(b.status AS SMALLINT) AS host_status,
    CAST(c.usage_mem_percent AS SMALLINT) AS memory_usage_percent,
    CAST(c.usage_cpu_percent AS SMALLINT) AS cpu_usage_percent,
    CAST(c.ksm_cpu_percent AS SMALLINT) AS ksm_cpu_percent,
    CAST(c.cpu_load AS INT) AS cpu_load,
    CAST(c.cpu_sys AS SMALLINT) AS system_cpu_usage_percent,
    CAST(c.cpu_user AS SMALLINT) AS user_cpu_usage_percent,
    CAST((c.swap_total - c.swap_free) AS INT) AS swap_used_mb,
    CAST(b.vm_active AS SMALLINT) AS vm_active,
    CAST(b.vm_count AS SMALLINT) AS total_vms,
    b.vms_cores_count AS total_vms_vcpus,
    c.mem_shared AS ksm_shared_memory_mb
FROM vds_dynamic b,
    vds_statistics c
WHERE b.vds_id = c.vds_id;

CREATE OR REPLACE VIEW dwh_host_interface_configuration_history_view AS

SELECT a.id AS host_interface_id,
    a.name AS host_interface_name,
    a.vds_id AS host_id,
    CAST(a.type AS SMALLINT) AS host_interface_type,
    a.speed AS host_interface_speed_bps,
    a.mac_addr AS mac_address,
    a.network_name AS logical_network_name,
    a.addr AS ip_address,
    a.gateway,
    a.is_bond AS bond,
    a.bond_name,
    a.vlan_id,
    a._create_date AS create_date,
    a._update_date AS update_date
FROM vds_interface AS a
WHERE (
        (
            a._create_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping
                WHERE (var_name = 'lastSync')
                )
            )
        OR (
            a._update_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping AS history_timekeeping_1
                WHERE (var_name = 'lastSync')
                )
            )
        )
    AND (
        a.is_bond IS NULL
        OR a.is_bond = FALSE
        OR (
            a.is_bond = TRUE
            AND a.name IN (
                SELECT b.bond_name
                FROM vds_interface AS b
                WHERE b.is_bond IS NULL
                    AND b.vds_id = a.vds_id
                )
            )
        );

CREATE OR REPLACE VIEW dwh_host_interface_history_view AS

SELECT vds_interface_statistics.id AS host_interface_id,
    vds_interface_statistics.rx_rate AS receive_rate_percent,
    vds_interface_statistics.tx_rate AS transmit_rate_percent,
    vds_interface_statistics.rx_total AS received_total_byte,
    vds_interface_statistics.tx_total AS transmitted_total_byte,
    vds_interface_statistics.rx_drop AS received_dropped_total_packets,
    vds_interface_statistics.tx_drop AS transmitted_dropped_total_packets
FROM vds_interface_statistics;

CREATE OR REPLACE VIEW dwh_vm_configuration_history_view AS

SELECT a.vm_guid AS vm_id,
    a.vm_name,
    a.description AS vm_description,
    CAST(a.vm_type AS SMALLINT) AS vm_type,
    a.cluster_id AS cluster_id,
    a.vmt_guid AS template_id,
    b.vm_name AS template_name,
    CAST(a.cpu_per_socket AS SMALLINT) AS cpu_per_socket,
    CAST(a.num_of_sockets AS SMALLINT) AS number_of_sockets,
    a.mem_size_mb AS memory_size_mb,
    CAST(a.os AS SMALLINT) AS operating_system,
    f.vds_id AS default_host,
    a.auto_startup AS high_availability,
    a.is_initialized AS initialized,
    a.is_stateless AS stateless,
    CAST(FALSE AS BOOLEAN) AS fail_back,
    CAST(a.usb_policy AS SMALLINT) AS usb_policy,
    a.time_zone,
    c.vm_pool_id,
    d.vm_pool_name,
    e.user_id AS created_by_user_id,
    a._create_date AS create_date,
    a._update_date AS update_date
FROM vm_static AS a
INNER JOIN vm_static AS b
    ON a.vmt_guid = b.vm_guid
LEFT JOIN vm_pool_map AS c
    ON a.vm_guid = c.vm_guid
LEFT JOIN vm_pools AS d
    ON c.vm_pool_id = d.vm_pool_id
LEFT JOIN users AS e
    ON a.created_by_user_id = e.user_id
LEFT JOIN (
    SELECT DISTINCT
        ON (vm_id) vm_id,
            vds_id
    FROM vm_host_pinning_map
    ORDER BY vm_id
    ) f
    ON f.vm_id = a.vm_guid,
        (
            SELECT var_datetime
            FROM dwh_history_timekeeping
            WHERE var_name = 'lastSync'
            ) AS lastSync
WHERE (
        a.entity_type = 'VM'
        AND b.entity_type = 'TEMPLATE'
        )
    AND greatest(a._create_date, a._update_date, b._update_date) > lastSync.var_datetime;

CREATE OR REPLACE VIEW dwh_vm_history_view AS

SELECT c.vm_guid AS vm_id,
    CAST(b.status AS SMALLINT) AS vm_status,
    CAST(c.usage_cpu_percent AS SMALLINT) AS cpu_usage_percent,
    CAST(c.usage_mem_percent AS SMALLINT) AS memory_usage_percent,
    CAST((c.cpu_sys / (vm_static.cpu_per_socket * vm_static.num_of_sockets)) AS SMALLINT) AS system_cpu_usage_percent,
    CAST((c.cpu_user / (vm_static.cpu_per_socket * vm_static.num_of_sockets)) AS SMALLINT) AS user_cpu_usage_percent,
    c.disks_usage,
    b.vm_ip,
    b.vm_fqdn,
    b.client_ip AS vm_client_ip,
    b.console_user_id AS current_user_id,
    CASE
        WHEN b.guest_cur_user_name IS NULL
            THEN FALSE
        ELSE TRUE
        END AS user_logged_in_to_guest,
    b.run_on_vds AS currently_running_on_host,
    c.guest_mem_buffered AS memory_buffered_kb,
    c.guest_mem_cached AS memory_cached_kb
FROM vm_dynamic b
LEFT JOIN vm_statistics c
    ON c.vm_guid = b.vm_guid
INNER JOIN vm_static
    ON c.vm_guid = vm_static.vm_guid;

CREATE OR REPLACE VIEW dwh_vm_interface_configuration_history_view AS

SELECT vm_interface.id AS vm_interface_id,
    vm_interface.name AS vm_interface_name,
    vm_interface.vm_guid AS vm_id,
    CAST(vm_interface.type AS SMALLINT) AS vm_interface_type,
    vm_interface.speed AS vm_interface_speed_bps,
    vm_interface.mac_addr AS mac_address,
    network.name AS logical_network_name,
    vm_interface._create_date AS create_date,
    vm_interface._update_date AS update_date
FROM vm_interface
LEFT JOIN (
    vnic_profiles INNER JOIN network
        ON network.id = vnic_profiles.network_id
    )
    ON vnic_profiles.id = vm_interface.vnic_profile_id
INNER JOIN vm_static
    ON vm_interface.vm_guid = vm_static.vm_guid
WHERE entity_type = 'VM'
    AND (
        (
            vm_interface._create_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping
                WHERE (var_name = 'lastSync')
                )
            )
        OR (
            vm_interface._update_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping AS history_timekeeping_1
                WHERE (var_name = 'lastSync')
                )
            )
        );

CREATE OR REPLACE VIEW dwh_vm_interface_history_view AS

SELECT vm_interface_statistics.id AS vm_interface_id,
    vm_interface_statistics.rx_rate AS receive_rate_percent,
    vm_interface_statistics.tx_rate AS transmit_rate_percent,
    vm_interface_statistics.rx_total AS received_total_byte,
    vm_interface_statistics.tx_total AS transmitted_total_byte,
    vm_interface_statistics.rx_drop AS received_dropped_total_packets,
    vm_interface_statistics.tx_drop AS transmitted_dropped_total_packets
FROM vm_interface_statistics;

CREATE OR REPLACE VIEW dwh_vm_disk_configuration_history_view AS

SELECT d.disk_id AS vm_disk_id,
    d.disk_alias AS vm_disk_name,
    d.disk_description AS vm_disk_description,
    image_storage_domain_map.storage_domain_id AS storage_domain_id,
    CAST(i.size / 1048576 AS INT) AS vm_disk_size_mb,
    CAST(i.volume_type AS SMALLINT) AS vm_disk_type,
    CAST(i.volume_format AS SMALLINT) AS vm_disk_format,
     d.shareable AS is_shared,
    i._create_date AS create_date,
    i._update_date AS update_date
FROM images AS i
INNER JOIN base_disks AS d
    ON i.image_group_id = d.disk_id
INNER JOIN image_storage_domain_map
    ON image_storage_domain_map.image_id = i.image_guid
WHERE i.active = TRUE
    AND i.image_group_id in (
        SELECT device_id
        FROM vm_device
        LEFT JOIN vm_static
            ON vm_static.vm_guid = vm_device.vm_id
        WHERE
            vm_static.entity_type = 'VM'
            OR vm_static.entity_type IS NULL
        )
    AND (
        (
            i._create_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping
                WHERE (var_name = 'lastSync')
                )
            )
        OR (
            i._update_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping AS history_timekeeping_1
                WHERE (var_name = 'lastSync')
                )
            )
        )
GROUP BY vm_disk_id, storage_domain_id, vm_disk_size_mb, vm_disk_type, vm_disk_format, create_date, update_date;

CREATE OR REPLACE VIEW dwh_vm_device_history_view AS

SELECT device_id,
    vm_id,
    type,
    address,
    is_managed,
    is_plugged,
    is_readonly,
    _create_date AS create_date,
    _update_date AS update_date
FROM vm_device
WHERE (
        (
            type = 'disk'
            AND device = 'disk'
            )
        OR (type = 'interface')
        )
    AND (
        (
            _create_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping
                WHERE (var_name = 'lastSync')
                )
            )
        OR (
            _update_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping AS history_timekeeping_1
                WHERE (var_name = 'lastSync')
                )
            )
        );

CREATE OR REPLACE VIEW dwh_vm_disks_history_view AS

SELECT d.disk_id AS vm_disk_id,
    images.image_guid AS image_id,
    cast(images.imageStatus AS SMALLINT) AS vm_disk_status,
    vm_disk_actual_size.vm_disk_actual_size_mb AS vm_disk_actual_size_mb,
    disk_image_dynamic.read_rate AS read_rate_bytes_per_second,
    disk_image_dynamic.read_ops AS read_ops_per_second,
    disk_image_dynamic.read_latency_seconds AS read_latency_seconds,
    disk_image_dynamic.write_rate AS write_rate_bytes_per_second,
    disk_image_dynamic.write_ops AS write_ops_per_second,
    disk_image_dynamic.write_latency_seconds AS write_latency_seconds,
    disk_image_dynamic.flush_latency_seconds AS flush_latency_seconds
FROM images
INNER JOIN disk_image_dynamic
    ON images.image_guid = disk_image_dynamic.image_id
INNER JOIN base_disks AS d
    ON images.image_group_id = d.disk_id
LEFT JOIN vm_device
    ON vm_device.device_id = images.image_group_id
LEFT JOIN vm_static
    ON vm_static.vm_guid = vm_device.vm_id
LEFT JOIN (
    SELECT e.disk_id AS vm_disk_id,
        cast(SUM(disk_image_dynamic.actual_size / 1048576) AS INT) AS vm_disk_actual_size_mb
    FROM images images_b
    INNER JOIN disk_image_dynamic
        ON images_b.image_guid = disk_image_dynamic.image_id
    INNER JOIN base_disks e
        ON images_b.image_group_id = e.disk_id
    LEFT JOIN vm_device
        ON vm_device.device_id = images_b.image_group_id
    LEFT JOIN vm_static
        ON vm_static.vm_guid = vm_device.vm_id
    WHERE vm_static.entity_type = 'VM'
        OR vm_static.entity_type IS NULL
    GROUP BY vm_disk_id
    ) AS vm_disk_actual_size
    ON d.disk_id = vm_disk_actual_size.vm_disk_id
WHERE images.active = TRUE
    AND (
        vm_static.entity_type = 'VM'
        OR vm_static.entity_type IS NULL
        );

CREATE OR REPLACE VIEW dwh_remove_tags_relations_history_view AS

SELECT tag_id AS entity_id,
    parent_id AS parent_id
FROM tags

UNION ALL

SELECT vds_id AS vds_id,
    tag_id AS tag_id
FROM tags_vds_map

UNION ALL

SELECT vm_pool_id AS vm_pool_id,
    tag_id AS tag_id
FROM tags_vm_pool_map

UNION ALL

SELECT vm_id AS vm_id,
    tag_id AS tag_id
FROM tags_vm_map

UNION ALL

SELECT user_id AS user_id,
    tag_id AS tag_id
FROM tags_user_map

UNION ALL

SELECT group_id AS group_id,
    tag_id AS tag_id
FROM tags_user_group_map;

CREATE OR REPLACE VIEW dwh_add_tags_relations_history_view AS

SELECT tag_id AS entity_id,
    parent_id AS parent_id,
    CAST(18 AS SMALLINT) AS entity_type,
    _create_date AS attach_date,
    _update_date AS move_date
FROM tags
WHERE (
        _create_date > (
            SELECT var_datetime AS var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        _update_date > (
            SELECT var_datetime AS var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        )

UNION ALL

SELECT vds_id AS vds_id,
    tag_id AS tag_id,
    CAST(3 AS SMALLINT),
    _create_date,
    NULL
FROM tags_vds_map
WHERE _create_date > (
        SELECT var_datetime AS var_datetime
        FROM dwh_history_timekeeping
        WHERE (var_name = 'lastSync')
        )

UNION ALL

SELECT vm_pool_id AS vm_pool_id,
    tag_id AS tag_id,
    CAST(5 AS SMALLINT),
    _create_date,
    NULL
FROM tags_vm_pool_map
WHERE _create_date > (
        SELECT var_datetime AS var_datetime
        FROM dwh_history_timekeeping
        WHERE (var_name = 'lastSync')
        )

UNION ALL

SELECT vm_id AS vm_id,
    tag_id AS tag_id,
    CAST(2 AS SMALLINT),
    _create_date,
    NULL
FROM tags_vm_map
WHERE _create_date > (
        SELECT var_datetime AS var_datetime
        FROM dwh_history_timekeeping
        WHERE (var_name = 'lastSync')
        )

UNION ALL

SELECT user_id AS user_id,
    tag_id AS tag_id,
    CAST(15 AS SMALLINT),
    _create_date,
    NULL
FROM tags_user_map
WHERE _create_date > (
        SELECT var_datetime AS var_datetime
        FROM dwh_history_timekeeping
        WHERE (var_name = 'lastSync')
        )

UNION ALL

SELECT group_id AS group_id,
    tag_id AS tag_id,
    CAST(17 AS SMALLINT),
    _create_date,
    NULL
FROM tags_user_group_map
WHERE _create_date > (
        SELECT var_datetime AS var_datetime
        FROM dwh_history_timekeeping
        WHERE (var_name = 'lastSync')
        );

CREATE OR REPLACE VIEW dwh_tags_details_history_view AS

SELECT tag_id AS tag_id,
    tag_name AS tag_name,
    description AS tag_description,
    _create_date AS create_date,
    _update_date AS update_date
FROM tags
WHERE (
        _create_date > (
            SELECT var_datetime AS var_datetime
            FROM dwh_history_timekeeping
            WHERE (var_name = 'lastSync')
            )
        )
    OR (
        _update_date > (
            SELECT var_datetime AS var_datetime
            FROM dwh_history_timekeeping AS history_timekeeping_1
            WHERE (var_name = 'lastSync')
            )
        );

CREATE OR REPLACE VIEW dwh_users_history_view AS

SELECT user_id,
    name AS first_name,
    surname AS last_name,
    DOMAIN,
    username,
    department,
    '' AS user_role_title,
    email,
    external_id,
    TRUE AS active,
    _create_date AS create_date,
    _update_date AS update_date
FROM users
WHERE (
        (
            _create_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping
                WHERE (var_name = 'lastSync')
                )
            )
        OR (
            _update_date > (
                SELECT var_datetime
                FROM dwh_history_timekeeping AS history_timekeeping_1
                WHERE (var_name = 'lastSync')
                )
            )
        );


