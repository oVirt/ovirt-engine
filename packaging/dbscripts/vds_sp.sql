


----------------------------------------------------------------
-- [vds_statistics] Table
--
CREATE OR REPLACE FUNCTION InsertVdsStatistics (
    v_cpu_idle DECIMAL(18, 0),
    v_cpu_load DECIMAL(18, 0),
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_usage_cpu_percent INT,
    v_usage_mem_percent INT,
    v_usage_network_percent INT,
    v_vds_id UUID,
    v_mem_available BIGINT,
    v_mem_free BIGINT,
    v_mem_shared BIGINT,
    v_swap_free BIGINT,
    v_swap_total BIGINT,
    v_ksm_cpu_percent INT,
    v_ksm_pages BIGINT,
    v_ksm_state BOOLEAN,
    v_anonymous_hugepages INT,
    v_boot_time BIGINT,
    v_ha_score INT,
    v_ha_configured BOOLEAN,
    v_ha_active BOOLEAN,
    v_ha_global_maintenance BOOLEAN,
    v_ha_local_maintenance BOOLEAN,
    v_cpu_over_commit_time_stamp TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vds_statistics (
            cpu_idle,
            cpu_load,
            cpu_sys,
            cpu_user,
            usage_cpu_percent,
            usage_mem_percent,
            usage_network_percent,
            vds_id,
            mem_available,
            mem_free,
            mem_shared,
            swap_free,
            swap_total,
            ksm_cpu_percent,
            ksm_pages,
            ksm_state,
            anonymous_hugepages,
            boot_time,
            ha_score,
            ha_configured,
            ha_active,
            ha_global_maintenance,
            ha_local_maintenance,
            cpu_over_commit_time_stamp
            )
        VALUES (
            v_cpu_idle,
            v_cpu_load,
            v_cpu_sys,
            v_cpu_user,
            v_usage_cpu_percent,
            v_usage_mem_percent,
            v_usage_network_percent,
            v_vds_id,
            v_mem_available,
            v_mem_free,
            v_mem_shared,
            v_swap_free,
            v_swap_total,
            v_ksm_cpu_percent,
            v_ksm_pages,
            v_ksm_state,
            v_anonymous_hugepages,
            v_boot_time,
            v_ha_score,
            v_ha_configured,
            v_ha_active,
            v_ha_global_maintenance,
            v_ha_local_maintenance,
            v_cpu_over_commit_time_stamp
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsStatistics (
    v_cpu_idle DECIMAL(18, 0),
    v_cpu_load DECIMAL(18, 0),
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_usage_cpu_percent INT,
    v_usage_mem_percent INT,
    v_usage_network_percent INT,
    v_vds_id UUID,
    v_mem_available BIGINT,
    v_mem_free BIGINT,
    v_mem_shared BIGINT,
    v_swap_free BIGINT,
    v_swap_total BIGINT,
    v_ksm_cpu_percent INT,
    v_ksm_pages BIGINT,
    v_ksm_state BOOLEAN,
    v_anonymous_hugepages INT,
    v_boot_time BIGINT,
    v_ha_score INT,
    v_ha_configured BOOLEAN,
    v_ha_active BOOLEAN,
    v_ha_global_maintenance BOOLEAN,
    v_ha_local_maintenance BOOLEAN,
    v_cpu_over_commit_time_stamp TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID
    --The [vds_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_statistics
        SET cpu_idle = v_cpu_idle,
            cpu_load = v_cpu_load,
            cpu_sys = v_cpu_sys,
            cpu_user = v_cpu_user,
            usage_cpu_percent = v_usage_cpu_percent,
            usage_mem_percent = v_usage_mem_percent,
            usage_network_percent = v_usage_network_percent,
            mem_available = v_mem_available,
            mem_free = v_mem_free,
            mem_shared = v_mem_shared,
            swap_free = v_swap_free,
            swap_total = v_swap_total,
            ksm_cpu_percent = v_ksm_cpu_percent,
            ksm_pages = v_ksm_pages,
            ksm_state = v_ksm_state,
            anonymous_hugepages = v_anonymous_hugepages,
            boot_time = v_boot_time,
            ha_score = v_ha_score,
            ha_configured = v_ha_configured,
            ha_active = v_ha_active,
            ha_global_maintenance = v_ha_global_maintenance,
            ha_local_maintenance = v_ha_local_maintenance,
            _update_date = LOCALTIMESTAMP,
            cpu_over_commit_time_stamp = v_cpu_over_commit_time_stamp
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdsStatistics (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM vds_statistics
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdsStatistics ()
RETURNS SETOF vds_statistics STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_statistics.*
        FROM vds_statistics;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStatisticsByVdsId (v_vds_id UUID)
RETURNS SETOF vds_statistics STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_statistics.*
        FROM vds_statistics
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vds_dynamic] Table
--
CREATE OR REPLACE FUNCTION InsertVdsDynamic (
    v_cpu_cores INT,
    v_cpu_threads INT,
    v_cpu_model VARCHAR(255),
    v_cpu_speed_mh DECIMAL(18, 0),
    v_if_total_speed VARCHAR(40),
    v_kvm_enabled BOOLEAN,
    v_mem_commited INT,
    v_physical_mem_mb INT,
    v_status INT,
    v_vds_id UUID,
    v_vm_active INT,
    v_vm_count INT,
    v_vms_cores_count INT,
    v_vm_migrating INT,
    v_incoming_migrations INT,
    v_outgoing_migrations INT,
    v_reserved_mem INT,
    v_guest_overhead INT,
    v_rpm_version VARCHAR(255),
    v_software_version VARCHAR(40),
    v_version_name VARCHAR(40),
    v_build_name VARCHAR(40),
    v_previous_status INT,
    v_cpu_flags VARCHAR(4000),
    v_pending_vcpus_count INT,
    v_pending_vmem_size INT,
    v_cpu_sockets INT,
    v_net_config_dirty BOOLEAN,
    v_supported_cluster_levels VARCHAR(40),
    v_supported_engines VARCHAR(40),
    v_host_os VARCHAR(4000),
    v_kvm_version VARCHAR(4000),
    v_libvirt_version VARCHAR(4000),
    v_spice_version VARCHAR(4000),
    v_gluster_version VARCHAR(4000),
    v_librbd1_version VARCHAR(4000),
    v_glusterfs_cli_version VARCHAR(4000),
    v_kernel_version VARCHAR(4000),
    v_iscsi_initiator_name VARCHAR(4000),
    v_transparent_hugepages_state INT,
    v_hooks TEXT,
    v_hw_manufacturer VARCHAR(255),
    v_hw_product_name VARCHAR(255),
    v_hw_version VARCHAR(255),
    v_hw_serial_number VARCHAR(255),
    v_hw_uuid VARCHAR(255),
    v_hw_family VARCHAR(255),
    v_hbas TEXT,
    v_supported_emulated_machines TEXT,
    v_controlled_by_pm_policy BOOLEAN,
    v_kdump_status SMALLINT,
    v_selinux_enforce_mode INT,
    v_auto_numa_balancing SMALLINT,
    v_is_numa_supported BOOLEAN,
    v_supported_rng_sources VARCHAR(255),
    v_online_cpus TEXT,
    v_is_update_available BOOLEAN,
    v_is_hostdev_enabled BOOLEAN,
    v_kernel_args TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vds_dynamic (
            cpu_cores,
            cpu_threads,
            cpu_model,
            cpu_speed_mh,
            if_total_speed,
            kvm_enabled,
            mem_commited,
            physical_mem_mb,
            status,
            vds_id,
            vm_active,
            vm_count,
            vm_migrating,
            incoming_migrations,
            outgoing_migrations,
            reserved_mem,
            guest_overhead,
            rpm_version,
            software_version,
            version_name,
            build_name,
            previous_status,
            cpu_flags,
            vms_cores_count,
            pending_vcpus_count,
            pending_vmem_size,
            cpu_sockets,
            net_config_dirty,
            supported_cluster_levels,
            supported_engines,
            host_os,
            kvm_version,
            libvirt_version,
            spice_version,
            gluster_version,
            librbd1_version,
            glusterfs_cli_version,
            kernel_version,
            iscsi_initiator_name,
            transparent_hugepages_state,
            hooks,
            hw_manufacturer,
            hw_product_name,
            hw_version,
            hw_serial_number,
            hw_uuid,
            hw_family,
            hbas,
            supported_emulated_machines,
            controlled_by_pm_policy,
            kdump_status,
            selinux_enforce_mode,
            auto_numa_balancing,
            is_numa_supported,
            supported_rng_sources,
            online_cpus,
            is_update_available,
            is_hostdev_enabled,
            kernel_args
            )
        VALUES (
            v_cpu_cores,
            v_cpu_threads,
            v_cpu_model,
            v_cpu_speed_mh,
            v_if_total_speed,
            v_kvm_enabled,
            v_mem_commited,
            v_physical_mem_mb,
            v_status,
            v_vds_id,
            v_vm_active,
            v_vm_count,
            v_vm_migrating,
            v_incoming_migrations,
            v_outgoing_migrations,
            v_reserved_mem,
            v_guest_overhead,
            v_rpm_version,
            v_software_version,
            v_version_name,
            v_build_name,
            v_previous_status,
            v_cpu_flags,
            v_vms_cores_count,
            v_pending_vcpus_count,
            v_pending_vmem_size,
            v_cpu_sockets,
            v_net_config_dirty,
            v_supported_cluster_levels,
            v_supported_engines,
            v_host_os,
            v_kvm_version,
            v_libvirt_version,
            v_spice_version,
            v_gluster_version,
            v_librbd1_version,
            v_glusterfs_cli_version,
            v_kernel_version,
            v_iscsi_initiator_name,
            v_transparent_hugepages_state,
            v_hooks,
            v_hw_manufacturer,
            v_hw_product_name,
            v_hw_version,
            v_hw_serial_number,
            v_hw_uuid,
            v_hw_family,
            v_hbas,
            v_supported_emulated_machines,
            v_controlled_by_pm_policy,
            v_kdump_status,
            v_selinux_enforce_mode,
            v_auto_numa_balancing,
            v_is_numa_supported,
            v_supported_rng_sources,
            v_online_cpus,
            v_is_update_available,
            v_is_hostdev_enabled,
            v_kernel_args
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamicPowerManagementPolicyFlag (
    v_vds_id UUID,
    v_controlled_by_pm_policy BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_dynamic
        SET controlled_by_pm_policy = v_controlled_by_pm_policy
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamic (
    v_cpu_cores INT,
    v_cpu_threads INT,
    v_cpu_model VARCHAR(255),
    v_cpu_speed_mh DECIMAL(18, 0),
    v_if_total_speed VARCHAR(40),
    v_kvm_enabled BOOLEAN,
    v_mem_commited INT,
    v_physical_mem_mb INT,
    v_status INT,
    v_vds_id UUID,
    v_vm_active INT,
    v_vm_count INT,
    v_vms_cores_count INT,
    v_vm_migrating INT,
    v_incoming_migrations INT,
    v_outgoing_migrations INT,
    v_reserved_mem INT,
    v_guest_overhead INT,
    v_rpm_version VARCHAR(255),
    v_software_version VARCHAR(40),
    v_version_name VARCHAR(40),
    v_build_name VARCHAR(40),
    v_previous_status INT,
    v_cpu_flags VARCHAR(4000),
    v_pending_vcpus_count INT,
    v_pending_vmem_size INT,
    v_cpu_sockets INT,
    v_net_config_dirty BOOLEAN,
    v_supported_cluster_levels VARCHAR(40),
    v_supported_engines VARCHAR(40),
    v_host_os VARCHAR(4000),
    v_kvm_version VARCHAR(4000),
    v_libvirt_version VARCHAR(4000),
    v_spice_version VARCHAR(4000),
    v_gluster_version VARCHAR(4000),
    v_librbd1_version VARCHAR(4000),
    v_glusterfs_cli_version VARCHAR(4000),
    v_kernel_version VARCHAR(4000),
    v_iscsi_initiator_name VARCHAR(4000),
    v_transparent_hugepages_state INT,
    v_hooks TEXT,
    v_non_operational_reason INT,
    v_hw_manufacturer VARCHAR(255),
    v_hw_product_name VARCHAR(255),
    v_hw_version VARCHAR(255),
    v_hw_serial_number VARCHAR(255),
    v_hw_uuid VARCHAR(255),
    v_hw_family VARCHAR(255),
    v_hbas TEXT,
    v_supported_emulated_machines TEXT,
    v_kdump_status SMALLINT,
    v_selinux_enforce_mode INT,
    v_auto_numa_balancing SMALLINT,
    v_is_numa_supported BOOLEAN,
    v_supported_rng_sources VARCHAR(255),
    v_online_cpus TEXT,
    v_maintenance_reason TEXT,
    v_is_update_available BOOLEAN,
    v_is_hostdev_enabled BOOLEAN,
    v_kernel_args TEXT,
    v_pretty_name VARCHAR(255)
    )
RETURNS VOID
    --The [vds_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_dynamic
        SET cpu_cores = v_cpu_cores,
            cpu_threads = v_cpu_threads,
            cpu_model = v_cpu_model,
            cpu_speed_mh = v_cpu_speed_mh,
            if_total_speed = v_if_total_speed,
            kvm_enabled = v_kvm_enabled,
            mem_commited = v_mem_commited,
            physical_mem_mb = v_physical_mem_mb,
            status = v_status,
            vm_active = v_vm_active,
            vm_count = v_vm_count,
            vm_migrating = v_vm_migrating,
            incoming_migrations = v_incoming_migrations,
            outgoing_migrations = v_outgoing_migrations,
            reserved_mem = v_reserved_mem,
            guest_overhead = v_guest_overhead,
            rpm_version = v_rpm_version,
            software_version = v_software_version,
            version_name = v_version_name,
            build_name = v_build_name,
            previous_status = v_previous_status,
            cpu_flags = v_cpu_flags,
            vms_cores_count = v_vms_cores_count,
            pending_vcpus_count = v_pending_vcpus_count,
            pending_vmem_size = v_pending_vmem_size,
            cpu_sockets = v_cpu_sockets,
            net_config_dirty = v_net_config_dirty,
            supported_cluster_levels = v_supported_cluster_levels,
            supported_engines = v_supported_engines,
            host_os = v_host_os,
            kvm_version = v_kvm_version,
            libvirt_version = v_libvirt_version,
            spice_version = v_spice_version,
            gluster_version = v_gluster_version,
            librbd1_version = v_librbd1_version,
            glusterfs_cli_version = v_glusterfs_cli_version,
            kernel_version = v_kernel_version,
            iscsi_initiator_name = v_iscsi_initiator_name,
            transparent_hugepages_state = v_transparent_hugepages_state,
            hooks = v_hooks,
            _update_date = LOCALTIMESTAMP,
            non_operational_reason = v_non_operational_reason,
            hw_manufacturer = v_hw_manufacturer,
            hw_product_name = v_hw_product_name,
            hw_version = v_hw_version,
            hw_serial_number = v_hw_serial_number,
            hw_uuid = v_hw_uuid,
            hw_family = v_hw_family,
            hbas = v_hbas,
            supported_emulated_machines = v_supported_emulated_machines,
            kdump_status = v_kdump_status,
            selinux_enforce_mode = v_selinux_enforce_mode,
            auto_numa_balancing = v_auto_numa_balancing,
            is_numa_supported = v_is_numa_supported,
            supported_rng_sources = v_supported_rng_sources,
            online_cpus = v_online_cpus,
            maintenance_reason = v_maintenance_reason,
            is_update_available = v_is_update_available,
            is_hostdev_enabled = v_is_hostdev_enabled,
            kernel_args = v_kernel_args,
            pretty_name = v_pretty_name
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdsDynamic (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM vds_dynamic
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdsDynamic ()
RETURNS SETOF vds_dynamic STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_dynamic.*
        FROM vds_dynamic;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsDynamicByVdsId (v_vds_id UUID)
RETURNS SETOF vds_dynamic STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_dynamic.*
        FROM vds_dynamic
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vds_static] Table
--
CREATE OR REPLACE FUNCTION InsertVdsStatic (
    v_free_text_comment TEXT,
    v_vds_id UUID,
    v_host_name VARCHAR(255),
    v_vds_unique_id VARCHAR(128),
    v_port INT,
    v_protocol SMALLINT,
    v_cluster_id UUID,
    v_vds_name VARCHAR(255),
    v_server_SSL_enabled BOOLEAN,
    v_vds_type INT,
    v_vds_strength INT,
    v_pm_enabled BOOLEAN,
    v_pm_proxy_preferences VARCHAR(255),
    v_pm_detect_kdump BOOLEAN,
    v_vds_spm_priority INT,
    v_sshKeyFingerprint VARCHAR(128),
    v_console_address VARCHAR(255),
    v_ssh_port INT,
    v_ssh_username VARCHAR(255),
    v_disable_auto_pm BOOLEAN,
    v_host_provider_id UUID,
    v_openstack_network_provider_id UUID,
    v_kernel_cmdline TEXT,
    v_last_stored_kernel_cmdline TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF v_vds_unique_id IS NULL
        OR NOT EXISTS (
            SELECT vds_name
            FROM vds_static
            WHERE vds_unique_id = v_vds_unique_id
            ) THEN
    BEGIN
        INSERT INTO vds_static (
            vds_id,
            host_name,
            free_text_comment,
            vds_unique_id,
            port,
            protocol,
            cluster_id,
            vds_name,
            server_SSL_enabled,
            vds_type,
            vds_strength,
            pm_enabled,
            pm_proxy_preferences,
            pm_detect_kdump,
            vds_spm_priority,
            sshKeyFingerprint,
            console_address,
            ssh_port,
            ssh_username,
            disable_auto_pm,
            host_provider_id,
            openstack_network_provider_id,
            kernel_cmdline,
            last_stored_kernel_cmdline
            )
        VALUES (
            v_vds_id,
            v_host_name,
            v_free_text_comment,
            v_vds_unique_id,
            v_port,
            v_protocol,
            v_cluster_id,
            v_vds_name,
            v_server_SSL_enabled,
            v_vds_type,
            v_vds_strength,
            v_pm_enabled,
            v_pm_proxy_preferences,
            v_pm_detect_kdump,
            v_vds_spm_priority,
            v_sshKeyFingerprint,
            v_console_address,
            v_ssh_port,
            v_ssh_username,
            v_disable_auto_pm,
            v_host_provider_id,
            v_openstack_network_provider_id,
            v_kernel_cmdline,
            v_last_stored_kernel_cmdline
            );
    END;
END IF;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsStaticLastStoredKernelCmdline (
    v_vds_id UUID,
    v_last_stored_kernel_cmdline TEXT
    )
RETURNS VOID
    --The [vds_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_static
        SET last_stored_kernel_cmdline = v_last_stored_kernel_cmdline
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsStatic (
    v_host_name VARCHAR(255),
    v_free_text_comment TEXT,
    v_vds_unique_id VARCHAR(128),
    v_port INT,
    v_protocol SMALLINT,
    v_cluster_id UUID,
    v_vds_id UUID,
    v_vds_name VARCHAR(255),
    v_server_SSL_enabled BOOLEAN,
    v_vds_type INT,
    v_vds_strength INT,
    v_pm_enabled BOOLEAN,
    v_pm_proxy_preferences VARCHAR(255),
    v_pm_detect_kdump BOOLEAN,
    v_otp_validity BIGINT,
    v_vds_spm_priority INT,
    v_sshKeyFingerprint VARCHAR(128),
    v_console_address VARCHAR(255),
    v_ssh_port INT,
    v_ssh_username VARCHAR(255),
    v_disable_auto_pm BOOLEAN,
    v_host_provider_id UUID,
    v_openstack_network_provider_id UUID,
    v_kernel_cmdline TEXT
)
    RETURNS VOID
    --The [vds_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_static
        SET host_name = v_host_name,
            free_text_comment = v_free_text_comment,
            vds_unique_id = v_vds_unique_id,
            port = v_port,
            protocol = v_protocol,
            cluster_id = v_cluster_id,
            vds_name = v_vds_name,
            server_SSL_enabled = v_server_SSL_enabled,
            vds_type = v_vds_type,
            _update_date = LOCALTIMESTAMP,
            vds_strength = v_vds_strength,
            pm_enabled = v_pm_enabled,
            pm_proxy_preferences = v_pm_proxy_preferences,
            pm_detect_kdump = v_pm_detect_kdump,
            otp_validity = v_otp_validity,
            vds_spm_priority = v_vds_spm_priority,
            sshKeyFingerprint = v_sshKeyFingerprint,
            host_provider_id = v_host_provider_id,
            openstack_network_provider_id = v_openstack_network_provider_id,
            console_address = v_console_address,
            ssh_port = v_ssh_port,
            ssh_username = v_ssh_username,
            disable_auto_pm = v_disable_auto_pm,
            kernel_cmdline = v_kernel_cmdline
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdsStatic (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM tags_vds_map
        WHERE vds_id = v_vds_id;

        -- Delete all Vds Alerts from the database
        PERFORM DeleteAuditLogAlertsByVdsID(v_vds_id);

        DELETE
        FROM vds_static
        WHERE vds_id = v_vds_id;

        -- delete VDS permissions --
        DELETE
        FROM permissions
        WHERE object_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdsStatic ()
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.*
    FROM vds_static;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStaticByIp (v_ip VARCHAR(40))
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_static.*
        FROM vds_static vds_static,
            fence_agents fence_agents
        WHERE fence_agents.ip = v_ip
            AND fence_agents.vds_id = vds_static.vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStaticByHostName (v_host_name VARCHAR(255))
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.*
    FROM vds_static
    WHERE host_name = v_host_name;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStaticByVdsId (v_vds_id UUID)
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.*
    FROM vds_static
    WHERE vds_id = v_vds_id;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStaticByVdsName (v_host_name VARCHAR(255))
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.*
    FROM vds_static
    WHERE vds_name = v_host_name;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByUniqueID (v_vds_unique_id VARCHAR(128))
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE vds_unique_id = v_vds_unique_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsStaticByClusterId (v_cluster_id UUID)
RETURNS SETOF vds_static STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_static.*
        FROM vds_static vds_static
        WHERE cluster_id = v_cluster_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------------------------------------------------------------------------
--    [vds] - view
---------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetUpAndPrioritizedVds (v_storage_pool_id UUID)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds.*
        FROM vds vds,
            cluster cluster
        WHERE (vds.status = 3)
            AND (vds.storage_pool_id = v_storage_pool_id)
            AND (
                vds_spm_priority IS NULL
                OR vds_spm_priority > - 1
                )
            AND vds.cluster_id = cluster.cluster_id
            AND cluster.virt_service = true
        ORDER BY vds_spm_priority DESC, RANDOM();
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVds (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE (
                NOT v_is_filtered
                OR EXISTS (
                    SELECT 1
                    FROM user_vds_permissions_view
                    WHERE user_id = v_user_id
                        AND entity_id = vds_id
                    )
                );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByVdsId (
    v_vds_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
DECLARE v_columns TEXT [];

BEGIN
    BEGIN
        IF (v_is_filtered) THEN
            RETURN QUERY

            SELECT DISTINCT (rec).*
            FROM fn_db_mask_object('vds') AS q(rec vds)
            WHERE (rec).vds_id = v_vds_id
                AND EXISTS (
                    SELECT 1
                    FROM user_vds_permissions_view
                    WHERE user_id = v_user_id
                        AND entity_id = v_vds_id
                    );
        ELSE

            RETURN QUERY

            SELECT DISTINCT vds.*
            FROM vds
            WHERE vds_id = v_vds_id;
        END IF;

    END;
    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsWithoutMigratingVmsByClusterId (v_cluster_id UUID)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    -- this sp returns all vds in given cluster that have no pending vms and no vms in migration states
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE cluster_id = v_cluster_id
            AND pending_vcpus_count = 0
            AND vds.status = 3
            AND vds_id NOT IN (
                SELECT DISTINCT run_on_vds
                FROM vm_dynamic
                WHERE status IN (
                        5,
                        6,
                        11,
                        12
                        )
                );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVds (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM tags_vds_map
        WHERE vds_id = v_vds_id;

        -- Delete all Vds Alerts from the database
        PERFORM DeleteAuditLogAlertsByVdsID(v_vds_id);

        DELETE
        FROM vds_statistics
        WHERE vds_id = v_vds_id;

        DELETE
        FROM vds_dynamic
        WHERE vds_id = v_vds_id;

        DELETE
        FROM vds_static
        WHERE vds_id = v_vds_id;

        DELETE
        FROM permissions
        WHERE object_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByType (v_vds_type INT)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE vds_type = v_vds_type;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByName (v_vds_name VARCHAR(255))
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE vds_name = v_vds_name;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByHostName (v_host_name VARCHAR(255))
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE host_name = v_host_name;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByClusterId (
    v_cluster_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    -- this sp returns all vds for a given cluster
    BEGIN
        IF (v_is_filtered) THEN
            RETURN QUERY

        SELECT DISTINCT (rec).*
        FROM fn_db_mask_object('vds') AS q(rec vds)
        WHERE (rec).cluster_id = v_cluster_id
            AND EXISTS (
                SELECT 1
                FROM user_vds_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = (rec).vds_id
                );ELSE

        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE cluster_id = v_cluster_id;
        END IF;

    END;
    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByStoragePoolId (
    v_storage_pool_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT DISTINCT vds.*
        FROM vds
        WHERE storage_pool_id = v_storage_pool_id
            AND (
                NOT v_is_filtered
                OR EXISTS (
                    SELECT 1
                    FROM user_vds_permissions_view
                    WHERE user_id = v_user_id
                        AND entity_id = vds_id
                    )
                );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all VDS for a given cluster and having given status
CREATE OR REPLACE FUNCTION getVdsForClusterWithStatus (
    v_cluster_id UUID,
    v_status INT
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds.*
        FROM vds
        WHERE (status = v_status)
            AND (cluster_id = v_cluster_id)
        ORDER BY vds.vds_id ASC;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all gluster VDS for a given cluster and having given status, peer status
CREATE OR REPLACE FUNCTION getVdsForClusterWithPeerStatus (
    v_cluster_id UUID,
    v_status INT,
    v_peer_status VARCHAR(50)
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds.*
        FROM vds
        INNER JOIN gluster_server
            ON vds_id = server_id
        WHERE (status = v_status)
            AND (peer_status = v_peer_status)
            AND (cluster_id = v_cluster_id)
        ORDER BY vds.vds_id ASC;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all VDS for a given pool with one of the given statuses or in any status in case v_statuses is NULL.
CREATE OR REPLACE FUNCTION getVdsByStoragePoolIdWithStatuses(
    v_storage_pool_id UUID,
    v_statuses VARCHAR(150))
RETURNS SETOF vds STABLE
    AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY
        SELECT vds.*
        FROM vds
        INNER JOIN cluster cluster
            ON vds.cluster_id = cluster.cluster_id
        WHERE (v_statuses IS NULL
            OR vds.status IN (
                SELECT *
                FROM fnSplitterInteger(v_statuses)))
            AND (vds.storage_pool_id = v_storage_pool_id)
            AND cluster.virt_service = true;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getHostsForStorageOperation (
    v_storage_pool_id UUID,
    v_local_fs_only BOOLEAN
    )
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds.*
        FROM vds
        LEFT JOIN cluster vg
            ON vds.cluster_id = vg.cluster_id
        LEFT JOIN storage_pool sp
            ON vds.storage_pool_id = sp.id
        WHERE (
                v_storage_pool_id IS NULL
                OR vds.storage_pool_id = v_storage_pool_id
                )
            AND (vg.virt_service = true)
            AND (
                NOT v_local_fs_only
                OR sp.is_local = true
                )
            AND (
                v_storage_pool_id IS NOT NULL
                OR vds.status = 3
                );-- if DC is unspecified return only hosts with status = UP
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamicStatus (
    v_vds_guid UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET status = v_status
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamicStatusAndReasons (
    v_vds_guid UUID,
    v_status INT,
    v_non_operational_reason INT,
    v_maintenance_reason TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET status = v_status,
        non_operational_reason = v_non_operational_reason,
        maintenance_reason = v_maintenance_reason
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostExternalStatus (
    v_vds_guid UUID,
    v_external_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET external_status = v_external_status
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamicNetConfigDirty (
    v_vds_guid UUID,
    v_net_config_dirty BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET net_config_dirty = v_net_config_dirty
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsDynamicIsUpdateAvailable (
    v_vds_guid UUID,
    v_is_update_available BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET is_update_available = v_is_update_available
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsByNetworkId (v_network_id UUID)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds
    WHERE EXISTS (
            SELECT 1
            FROM vds_interface
            INNER JOIN network
                ON network.name = vds_interface.network_name
            INNER JOIN network_cluster
                ON network.id = network_cluster.network_id
            WHERE network_id = v_network_id
                AND vds.cluster_id = network_cluster.cluster_id
                AND vds_interface.vds_id = vds.vds_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsWithoutNetwork (v_network_id UUID)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds.*
    FROM vds
    INNER JOIN network_cluster
        ON vds.cluster_id = network_cluster.cluster_id
    WHERE network_cluster.network_id = v_network_id
        AND NOT EXISTS (
            SELECT 1
            FROM vds_interface
            INNER JOIN network
                ON network.name = vds_interface.network_name
            INNER JOIN network_cluster
                ON network.id = network_cluster.network_id
            WHERE network_cluster.network_id = v_network_id
                AND vds.cluster_id = network_cluster.cluster_id
                AND vds_interface.vds_id = vds.vds_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCpuFlags (
    v_vds_id UUID,
    v_cpu_flags VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_dynamic
    SET cpu_flags = v_cpu_flags
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIdsOfHostsWithStatus (v_status INT)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_id
    FROM vds_dynamic
    WHERE status = v_status;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vds_cpu_statistics] Table
--
CREATE OR REPLACE FUNCTION InsertVdsCpuStatistics (
    v_vds_cpu_id UUID,
    v_vds_id UUID,
    v_cpu_core_id INT,
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_cpu_idle DECIMAL(18, 0),
    v_usage_cpu_percent INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vds_cpu_statistics (
            vds_cpu_id,
            vds_id,
            cpu_core_id,
            cpu_sys,
            cpu_user,
            cpu_idle,
            usage_cpu_percent
            )
        VALUES (
            v_vds_cpu_id,
            v_vds_id,
            v_cpu_core_id,
            v_cpu_sys,
            v_cpu_user,
            v_cpu_idle,
            v_usage_cpu_percent
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsCpuStatistics (
    v_vds_id UUID,
    v_cpu_core_id INT,
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_cpu_idle DECIMAL(18, 0),
    v_usage_cpu_percent INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE vds_cpu_statistics
        SET cpu_sys = v_cpu_sys,
            cpu_user = v_cpu_user,
            cpu_idle = v_cpu_idle,
            usage_cpu_percent = v_usage_cpu_percent
        WHERE vds_id = v_vds_id
            AND cpu_core_id = v_cpu_core_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdsCpuStatisticsByVdsId (v_vds_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM vds_cpu_statistics
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsCpuStatisticsByVdsId (v_vds_id UUID)
RETURNS SETOF vds_cpu_statistics STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_cpu_statistics.*
        FROM vds_cpu_statistics
        WHERE vds_id = v_vds_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdsCpuStatistics ()
RETURNS SETOF vds_cpu_statistics STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_cpu_statistics.*
        FROM vds_cpu_statistics;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getFirstUpRhelForClusterId (v_cluster_id UUID)
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        -- both centos and RHEL return RHEL as host_os
        RETURN QUERY

        SELECT *
        FROM vds
        WHERE (
                host_os LIKE 'RHEL%'
                OR host_os LIKE 'oVirt Node%'
                OR host_os LIKE 'RHEV Hypervisor%'
                )
            AND status = 3
            AND cluster_id = v_cluster_id LIMIT 1;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Get host names dedicated to vm
CREATE OR REPLACE FUNCTION GetNamesOfHostsDedicatedToVm (v_vm_guid UUID)
RETURNS SETOF VARCHAR AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT vds_name
        FROM vm_host_pinning_view
        WHERE vm_id = v_vm_guid;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;


