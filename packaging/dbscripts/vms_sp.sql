

----------------------------------------------------------------
-- [vm_ovf_generations] Table
CREATE OR REPLACE FUNCTION UpdateOvfGenerations (
    v_vms_ids VARCHAR(5000),
    v_vms_db_generations VARCHAR(5000),
    v_ovf_data TEXT,
    v_ovf_data_seperator VARCHAR(10)
    )
RETURNS VOID AS $PROCEDURE$
DECLARE curs_vmids CURSOR
FOR
SELECT *
FROM fnSplitterUuid(v_vms_ids);

curs_newovfgen CURSOR
FOR

SELECT *
FROM fnSplitter(v_vms_db_generations);

curs_newovfdata CURSOR
FOR

SELECT *
FROM fnSplitterWithSeperator(v_ovf_data, v_ovf_data_seperator);

id UUID;

new_ovf_gen BIGINT;

new_ovf_config TEXT;

BEGIN
    OPEN curs_vmids;

    OPEN curs_newovfgen;

    OPEN curs_newovfdata;

    LOOP

    FETCH curs_vmids
    INTO id;

    FETCH curs_newovfgen
    INTO new_ovf_gen;

    FETCH curs_newovfdata
    INTO new_ovf_config;

    IF NOT FOUND THEN EXIT;
    END IF;
    UPDATE vm_ovf_generations
    SET ovf_generation = new_ovf_gen,
        ovf_data = new_ovf_config
    WHERE vm_guid = id;
    END LOOP;

CLOSE curs_vmids;

CLOSE curs_newovfgen;

CLOSE curs_newovfdata;END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LoadOvfDataForIds (v_ids VARCHAR(5000))
RETURNS SETOF vm_ovf_generations STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_ovf_generations ovf
    WHERE ovf.vm_guid IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIdsForOvfDeletion (v_storage_pool_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT ovf.vm_guid AS vm_guid
    FROM vm_ovf_generations ovf
    WHERE ovf.storage_pool_id = v_storage_pool_id
        AND ovf.vm_guid NOT IN (
            SELECT vm_guid
            FROM vm_static
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetOvfGeneration (v_vm_id UUID)
RETURNS SETOF BIGINT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm.ovf_generation
    FROM vm_ovf_generations vm
    WHERE vm.vm_guid = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTemplatesIdsForOvfUpdate (v_storage_pool_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT templates.vmt_guid AS vm_guid
    FROM vm_templates_based_view templates,
        vm_ovf_generations generations
    WHERE generations.vm_guid = templates.vmt_guid
        AND templates.db_generation > generations.ovf_generation
        AND templates.storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmsIdsForOvfUpdate (v_storage_pool_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm.vm_guid AS vm_guid
    FROM vms vm,
        vm_ovf_generations ovf_gen
    WHERE vm.vm_guid = ovf_gen.vm_guid
        AND vm.db_generation > ovf_gen.ovf_generation
        AND vm.storage_pool_id = v_storage_pool_id
        -- filter out external VMs if needed.
        AND vm.origin != 4;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteOvfGenerations (v_vms_ids VARCHAR(5000))
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_ovf_generations
    WHERE vm_guid IN (
            SELECT *
            FROM fnSplitterUuid(v_vms_ids)
            )
        -- needed here to ensure that vm with the same id hasn't been added by import vm/template command
        AND vm_guid NOT IN (
            SELECT vm_guid
            FROM vm_static
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_statistics] Table
--
CREATE OR REPLACE FUNCTION InsertVmStatistics (
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_elapsed_time DECIMAL(18, 0),
    v_usage_cpu_percent INT,
    v_usage_mem_percent INT,
    v_usage_network_percent INT,
    v_disks_usage TEXT,
    v_vm_guid UUID,
    v_guest_mem_buffered BIGINT,
    v_guest_mem_cached BIGINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_statistics (
        cpu_sys,
        cpu_user,
        elapsed_time,
        usage_cpu_percent,
        usage_mem_percent,
        usage_network_percent,
        disks_usage,
        vm_guid,
        guest_mem_buffered,
        guest_mem_cached
        )
    VALUES (
        v_cpu_sys,
        v_cpu_user,
        v_elapsed_time,
        v_usage_cpu_percent,
        v_usage_mem_percent,
        v_usage_network_percent,
        v_disks_usage,
        v_vm_guid,
        v_guest_mem_buffered,
        v_guest_mem_cached
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmStatistics (
    v_cpu_sys DECIMAL(18, 0),
    v_cpu_user DECIMAL(18, 0),
    v_elapsed_time DECIMAL(18, 0),
    v_usage_cpu_percent INT,
    v_usage_mem_percent INT,
    v_usage_network_percent INT,
    v_disks_usage TEXT,
    v_vm_guid UUID,
    v_guest_mem_buffered BIGINT,
    v_guest_mem_cached BIGINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_statistics
    SET cpu_sys = v_cpu_sys,
        cpu_user = v_cpu_user,
        elapsed_time = v_elapsed_time,
        usage_cpu_percent = v_usage_cpu_percent,
        usage_mem_percent = v_usage_mem_percent,
        usage_network_percent = v_usage_network_percent,
        disks_usage = v_disks_usage,
        guest_mem_buffered = v_guest_mem_buffered,
        guest_mem_cached = v_guest_mem_cached,
        _update_date = LOCALTIMESTAMP
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmStatistics (v_vm_guid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_statistics
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmStatistics ()
RETURNS SETOF vm_statistics STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_statistics.*
    FROM vm_statistics;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmDynamic ()
RETURNS SETOF vm_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_dynamic.*
    FROM vm_dynamic;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmStatisticsByVmGuid (v_vm_guid UUID)
RETURNS SETOF vm_statistics STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_statistics.*
    FROM vm_statistics
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_dynamic] Table
--
CREATE OR REPLACE FUNCTION InsertVmDynamic (
    v_app_list TEXT,
    v_guest_cur_user_name TEXT,
    v_console_cur_user_name VARCHAR(255),
    v_runtime_name VARCHAR(255),
    v_console_user_id UUID,
    v_guest_os VARCHAR(255),
    v_migrating_to_vds UUID,
    v_run_on_vds UUID,
    v_status INT,
    v_vm_guid UUID,
    v_vm_host VARCHAR(255),
    v_vm_ip VARCHAR(255),
    v_vm_fqdn VARCHAR(255),
    v_last_start_time TIMESTAMP WITH TIME ZONE,
    v_boot_time TIMESTAMP WITH TIME ZONE,
    v_downtime BIGINT,
    v_last_stop_time TIMESTAMP WITH TIME ZONE,
    v_acpi_enable BOOLEAN,
    v_session INT,
    v_boot_sequence INT,
    v_utc_diff INT,
    v_client_ip VARCHAR(255),
    v_guest_requested_memory INT,
    v_exit_status INT,
    v_pause_status INT,
    v_exit_message VARCHAR(4000),
    v_guest_agent_nics_hash INT,
    v_last_watchdog_event NUMERIC,
    v_last_watchdog_action VARCHAR(8),
    v_is_run_once BOOLEAN,
    v_volatile_run BOOLEAN,
    v_cpu_name VARCHAR(255),
    v_emulated_machine VARCHAR(255),
    v_current_cd VARCHAR(4000),
    v_exit_reason INT,
    v_guest_cpu_count INT,
    v_spice_port INT,
    v_spice_tls_port INT,
    v_spice_ip VARCHAR(32),
    v_vnc_port INT,
    v_vnc_ip VARCHAR(32),
    v_ovirt_guest_agent_status INT,
    v_qemu_guest_agent_status INT,
    v_guest_timezone_offset INT,
    v_guest_timezone_name VARCHAR(255),
    v_guestos_arch INT,
    v_guestos_codename VARCHAR(255),
    v_guestos_distribution VARCHAR(255),
    v_guestos_kernel_version VARCHAR(255),
    v_guestos_type VARCHAR(255),
    v_guestos_version VARCHAR(255),
    v_guest_containers TEXT,
    v_current_cpu_pinning VARCHAR(4000),
    v_current_sockets INT,
    v_current_cores INT,
    v_current_threads INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_dynamic (
        app_list,
        guest_cur_user_name,
        console_cur_user_name,
        runtime_name,
        console_user_id,
        guest_os,
        migrating_to_vds,
        run_on_vds,
        status,
        vm_guid,
        vm_host,
        vm_ip,
        last_start_time,
        boot_time,
        downtime,
        last_stop_time,
        acpi_enable,
        session,
        boot_sequence,
        utc_diff,
        client_ip,
        guest_requested_memory,
        exit_status,
        pause_status,
        exit_message,
        guest_agent_nics_hash,
        last_watchdog_event,
        last_watchdog_action,
        is_run_once,
        volatile_run,
        vm_fqdn,
        cpu_name,
        emulated_machine,
        current_cd,
        exit_reason,
        guest_cpu_count,
        spice_port,
        spice_tls_port,
        spice_ip,
        vnc_port,
        vnc_ip,
        ovirt_guest_agent_status,
        qemu_guest_agent_status,
        guest_timezone_offset,
        guest_timezone_name,
        guestos_arch,
        guestos_codename,
        guestos_distribution,
        guestos_kernel_version,
        guestos_type,
        guestos_version,
        guest_containers,
        current_cpu_pinning,
        current_sockets,
        current_cores,
        current_threads
        )
    VALUES (
        v_app_list,
        v_guest_cur_user_name,
        v_console_cur_user_name,
        v_runtime_name,
        v_console_user_id,
        v_guest_os,
        v_migrating_to_vds,
        v_run_on_vds,
        v_status,
        v_vm_guid,
        v_vm_host,
        v_vm_ip,
        v_last_start_time,
        v_boot_time,
        v_downtime,
        v_last_stop_time,
        v_acpi_enable,
        v_session,
        v_boot_sequence,
        v_utc_diff,
        v_client_ip,
        v_guest_requested_memory,
        v_exit_status,
        v_pause_status,
        v_exit_message,
        v_guest_agent_nics_hash,
        v_last_watchdog_event,
        v_last_watchdog_action,
        v_is_run_once,
        v_volatile_run,
        v_vm_fqdn,
        v_cpu_name,
        v_emulated_machine,
        v_current_cd,
        v_exit_reason,
        v_guest_cpu_count,
        v_spice_port,
        v_spice_tls_port,
        v_spice_ip,
        v_vnc_port,
        v_vnc_ip,
        v_ovirt_guest_agent_status,
        v_qemu_guest_agent_status,
        v_guest_timezone_offset,
        v_guest_timezone_name,
        v_guestos_arch,
        v_guestos_codename,
        v_guestos_distribution,
        v_guestos_kernel_version,
        v_guestos_type,
        v_guestos_version,
        v_guest_containers,
        v_current_cpu_pinning,
        v_current_sockets,
        v_current_cores,
        v_current_threads
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmDynamic (
    v_app_list TEXT,
    v_guest_cur_user_name TEXT,
    v_console_cur_user_name VARCHAR(255),
    v_runtime_name VARCHAR(255),
    v_console_user_id UUID,
    v_guest_os VARCHAR(255),
    v_migrating_to_vds UUID,
    v_run_on_vds UUID,
    v_status INT,
    v_vm_guid UUID,
    v_vm_host VARCHAR(255),
    v_vm_ip VARCHAR(255),
    v_vm_fqdn VARCHAR(255),
    v_last_start_time TIMESTAMP WITH TIME ZONE,
    v_boot_time TIMESTAMP WITH TIME ZONE,
    v_downtime BIGINT,
    v_last_stop_time TIMESTAMP WITH TIME ZONE,
    v_acpi_enable BOOLEAN,
    v_session INT,
    v_boot_sequence INT,
    v_utc_diff INT,
    v_client_ip VARCHAR(255),
    v_guest_requested_memory INT,
    v_exit_status INT,
    v_pause_status INT,
    v_exit_message VARCHAR(4000),
    v_guest_agent_nics_hash INT,
    v_last_watchdog_event NUMERIC,
    v_last_watchdog_action VARCHAR(8),
    v_is_run_once BOOLEAN,
    v_volatile_run BOOLEAN,
    v_cpu_name VARCHAR(255),
    v_emulated_machine VARCHAR(255),
    v_current_cd VARCHAR(4000),
    v_reason VARCHAR(4000),
    v_exit_reason INT,
    v_guest_cpu_count INT,
    v_spice_port INT,
    v_spice_tls_port INT,
    v_spice_ip VARCHAR(32),
    v_vnc_port INT,
    v_vnc_ip VARCHAR(32),
    v_ovirt_guest_agent_status INT,
    v_qemu_guest_agent_status INT,
    v_guest_timezone_offset INT,
    v_guest_timezone_name VARCHAR(255),
    v_guestos_arch INT,
    v_guestos_codename VARCHAR(255),
    v_guestos_distribution VARCHAR(255),
    v_guestos_kernel_version VARCHAR(255),
    v_guestos_type VARCHAR(255),
    v_guestos_version VARCHAR(255),
    v_guest_containers TEXT,
    v_current_cpu_pinning VARCHAR(4000),
    v_current_sockets INT,
    v_current_cores INT,
    v_current_threads INT
    )
RETURNS VOID
    --The [vm_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vm_dynamic
    SET app_list = v_app_list,
        guest_cur_user_name = v_guest_cur_user_name,
        console_cur_user_name = v_console_cur_user_name,
        runtime_name = v_runtime_name,
        console_user_id = v_console_user_id,
        guest_os = v_guest_os,
        migrating_to_vds = v_migrating_to_vds,
        run_on_vds = v_run_on_vds,
        status = v_status,
        vm_host = v_vm_host,
        vm_ip = v_vm_ip,
        vm_fqdn = v_vm_fqdn,
        last_start_time = v_last_start_time,
        boot_time = v_boot_time,
        downtime = v_downtime,
        last_stop_time = v_last_stop_time,
        acpi_enable = v_acpi_enable,
        session = v_session,
        boot_sequence = v_boot_sequence,
        utc_diff = v_utc_diff,
        client_ip = v_client_ip,
        guest_requested_memory = v_guest_requested_memory,
        exit_status = v_exit_status,
        pause_status = v_pause_status,
        exit_message = v_exit_message,
        guest_agent_nics_hash = v_guest_agent_nics_hash,
        last_watchdog_event = v_last_watchdog_event,
        last_watchdog_action = v_last_watchdog_action,
        is_run_once = v_is_run_once,
        volatile_run = v_volatile_run,
        cpu_name = v_cpu_name,
        emulated_machine = v_emulated_machine,
        current_cd = v_current_cd,
        reason = v_reason,
        exit_reason = v_exit_reason,
        guest_cpu_count = v_guest_cpu_count,
        spice_port = v_spice_port,
        spice_tls_port = v_spice_tls_port,
        spice_ip = v_spice_ip,
        vnc_port = v_vnc_port,
        vnc_ip = v_vnc_ip,
        ovirt_guest_agent_status = v_ovirt_guest_agent_status,
        qemu_guest_agent_status = v_qemu_guest_agent_status,
        guest_timezone_offset = v_guest_timezone_offset,
        guest_timezone_name = v_guest_timezone_name,
        guestos_arch = v_guestos_arch,
        guestos_codename = v_guestos_codename,
        guestos_distribution = v_guestos_distribution,
        guestos_kernel_version = v_guestos_kernel_version,
        guestos_type = v_guestos_type,
        guestos_version = v_guestos_version,
        guest_containers = v_guest_containers,
        current_cpu_pinning = v_current_cpu_pinning,
        current_sockets = v_current_sockets,
        current_cores = v_current_cores,
        current_threads = v_current_threads
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateConsoleUserWithOptimisticLocking (
    v_vm_guid UUID,
    v_console_user_id UUID,
    v_guest_cur_user_name TEXT,
    v_console_cur_user_name VARCHAR(255),
    OUT v_updated BOOLEAN
    ) AS $PROCEDURE$
BEGIN
    UPDATE vm_dynamic
    SET console_user_id = v_console_user_id,
        guest_cur_user_name = v_guest_cur_user_name,
        console_cur_user_name = v_console_cur_user_name
    WHERE vm_guid = v_vm_guid
        AND (
            console_user_id = v_console_user_id
            OR console_user_id IS NULL
            );

    v_updated := FOUND;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmDynamicStatus (
    v_vm_guid UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_dynamic
    SET status = v_status
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ClearMigratingToVds (v_vm_guid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_dynamic
    SET migrating_to_vds = NULL
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmDynamic (v_vm_guid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_dynamic
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmDynamic ()
RETURNS SETOF vm_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_dynamic.*
    FROM vm_dynamic;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDynamicByVmGuid (v_vm_guid UUID)
RETURNS SETOF vm_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_dynamic.*
    FROM vm_dynamic
    WHERE vm_guid = v_vm_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetAllHashesFromVmDynamic_rs CASCADE;
CREATE TYPE GetAllHashesFromVmDynamic_rs AS (vm_guid UUID, hash VARCHAR);
CREATE OR REPLACE FUNCTION GetAllHashesFromVmDynamic ()
RETURNS SETOF GetAllHashesFromVmDynamic_rs STABLE
AS $procedure$
BEGIN
    RETURN QUERY

    SELECT vm_guid, hash
    FROM vm_dynamic;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetHashByVmGuid (v_vm_guid UUID, v_hash VARCHAR(30))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_dynamic
    SET hash = v_hash
    WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_static] Table
--
CREATE OR REPLACE FUNCTION InsertVmStatic (
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_mem_size_mb INT,
    v_max_memory_size_mb INT,
    v_num_of_io_threads INT,
    v_os INT,
    v_cluster_id UUID,
    v_vm_guid UUID,
    v_vm_name VARCHAR(255),
    v_vmt_guid UUID,
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_num_of_monitors INT,
    v_allow_console_reconnect BOOLEAN,
    v_is_initialized BOOLEAN,
    v_num_of_sockets INT,
    v_cpu_per_socket INT,
    v_threads_per_cpu INT,
    v_usb_policy INT,
    v_time_zone VARCHAR(40),
    v_auto_startup BOOLEAN,
    v_is_stateless BOOLEAN,
    v_is_smartcard_enabled BOOLEAN,
    v_is_delete_protected BOOLEAN,
    v_sso_method VARCHAR(32),
    v_dedicated_vm_for_vds TEXT,
    v_vm_type INT,
    v_nice_level INT,
    v_cpu_shares INT,
    v_default_boot_sequence INT,
    v_default_display_type INT,
    v_priority INT,
    v_iso_path VARCHAR(4000),
    v_origin INT,
    v_initrd_url VARCHAR(4000),
    v_kernel_url VARCHAR(4000),
    v_kernel_params VARCHAR(4000),
    v_migration_support INT,
    v_predefined_properties VARCHAR(4000),
    v_userdefined_properties VARCHAR(4000),
    v_min_allocated_mem INT,
    v_quota_id UUID,
    v_cpu_pinning VARCHAR(4000),
    v_host_cpu_flags BOOLEAN,
    v_tunnel_migration BOOLEAN,
    v_vnc_keyboard_layout VARCHAR(16),
    v_is_run_and_pause BOOLEAN,
    v_created_by_user_id UUID,
    v_instance_type_id UUID,
    v_image_type_id UUID,
    v_original_template_id UUID,
    v_original_template_name VARCHAR(255),
    v_migration_downtime INT,
    v_template_version_number INT,
    v_serial_number_policy SMALLINT,
    v_custom_serial_number VARCHAR(255),
    v_is_boot_menu_enabled BOOLEAN,
    v_is_spice_file_transfer_enabled BOOLEAN,
    v_is_spice_copy_paste_enabled BOOLEAN,
    v_cpu_profile_id UUID,
    v_is_auto_converge BOOLEAN,
    v_is_migrate_compressed BOOLEAN,
    v_is_migrate_encrypted BOOLEAN,
    v_custom_emulated_machine VARCHAR(40),
    v_bios_type INTEGER,
    v_custom_cpu_name VARCHAR(40),
    v_small_icon_id UUID,
    v_large_icon_id UUID,
    v_console_disconnect_action VARCHAR(64),
    v_resume_behavior VARCHAR(64),
    v_custom_compatibility_version VARCHAR(40),
    v_migration_policy_id UUID,
    v_lease_sd_id UUID,
    v_multi_queues_enabled BOOLEAN,
    v_virtio_scsi_multi_queues INT,
    v_use_tsc_frequency BOOLEAN,
    v_namespace VARCHAR(253),
    v_balloon_enabled BOOLEAN,
    v_console_disconnect_action_delay SMALLINT,
    v_cpu_pinning_policy SMALLINT)
  RETURNS VOID
   AS $procedure$
DECLARE
  v_val UUID;
BEGIN
-- lock template for child count update
select vm_guid into v_val FROM vm_static WHERE vm_guid = v_vmt_guid for update;

INSERT INTO vm_static(description,
                      free_text_comment,
                      mem_size_mb,
                      max_memory_size_mb,
                      num_of_io_threads,
                      os,
                      cluster_id,
                      vm_guid,
                      vm_name,
                      vmt_guid,
                      creation_date,
                      num_of_monitors,
                      allow_console_reconnect,
                      is_initialized,
                      num_of_sockets,
                      cpu_per_socket,
                      threads_per_cpu,
                      usb_policy,
                      time_zone,
                      auto_startup,
                      is_stateless,
                      default_boot_sequence,
                      vm_type,
                      nice_level,
                      cpu_shares,
                      default_display_type,
                      priority,
                      iso_path,
                      origin,
                      initrd_url,
                      kernel_url,
                      kernel_params,
                      migration_support,
                      predefined_properties,
                      userdefined_properties,
                      min_allocated_mem,
                      entity_type,
                      quota_id,
                      cpu_pinning,
                      is_smartcard_enabled,
                      is_delete_protected,
                      sso_method,
                      host_cpu_flags,
                      tunnel_migration,
                      vnc_keyboard_layout,
                      is_run_and_pause,
                      created_by_user_id,
                      instance_type_id,
                      image_type_id,
                      original_template_id,
                      original_template_name,
                      migration_downtime,
                      template_version_number,
                      serial_number_policy,
                      custom_serial_number,
                      is_boot_menu_enabled,
                      is_spice_file_transfer_enabled,
                      is_spice_copy_paste_enabled,
                      cpu_profile_id,
                      is_auto_converge,
                      is_migrate_compressed,
                      is_migrate_encrypted,
                      custom_emulated_machine,
                      bios_type,
                      custom_cpu_name,
                      small_icon_id,
                      large_icon_id,
                      console_disconnect_action,
                      resume_behavior,
                      custom_compatibility_version,
                      migration_policy_id,
                      lease_sd_id,
                      multi_queues_enabled,
                      virtio_scsi_multi_queues,
                      use_tsc_frequency,
                      namespace,
                      balloon_enabled,
                      console_disconnect_action_delay,
                      cpu_pinning_policy)
    VALUES(v_description,
           v_free_text_comment,
           v_mem_size_mb,
           v_max_memory_size_mb,
           v_num_of_io_threads,
           v_os,
           v_cluster_id,
           v_vm_guid,
           v_vm_name,
           v_vmt_guid,
           v_creation_date,
           v_num_of_monitors,
           v_allow_console_reconnect,
           v_is_initialized,
           v_num_of_sockets,
           v_cpu_per_socket,
           v_threads_per_cpu,
           v_usb_policy,
           v_time_zone,
           v_auto_startup,
           v_is_stateless,
           v_default_boot_sequence,
           v_vm_type,
           v_nice_level,
           v_cpu_shares,
           v_default_display_type,
           v_priority,
           v_iso_path,
           v_origin,
           v_initrd_url,
           v_kernel_url,
           v_kernel_params,
           v_migration_support,
           v_predefined_properties,
           v_userdefined_properties,
           v_min_allocated_mem,
           'VM',
           v_quota_id,
           v_cpu_pinning,
           v_is_smartcard_enabled,
           v_is_delete_protected,
           v_sso_method,
           v_host_cpu_flags,
           v_tunnel_migration,
           v_vnc_keyboard_layout,
           v_is_run_and_pause,
           v_created_by_user_id,
           v_instance_type_id,
           v_image_type_id,
           v_original_template_id,
           v_original_template_name,
           v_migration_downtime,
           v_template_version_number,
           v_serial_number_policy,
           v_custom_serial_number,
           v_is_boot_menu_enabled,
           v_is_spice_file_transfer_enabled,
           v_is_spice_copy_paste_enabled,
           v_cpu_profile_id,
           v_is_auto_converge,
           v_is_migrate_compressed,
           v_is_migrate_encrypted,
           v_custom_emulated_machine,
           v_bios_type,
           v_custom_cpu_name,
           v_small_icon_id,
           v_large_icon_id,
           v_console_disconnect_action,
           v_resume_behavior,
           v_custom_compatibility_version,
           v_migration_policy_id,
           v_lease_sd_id,
           v_multi_queues_enabled,
           v_virtio_scsi_multi_queues,
           v_use_tsc_frequency,
           v_namespace,
           v_balloon_enabled,
           v_console_disconnect_action_delay,
           v_cpu_pinning_policy);

    -- perform deletion from vm_ovf_generations to ensure that no record exists when performing insert to avoid PK violation.
    DELETE
    FROM vm_ovf_generations gen
    WHERE gen.vm_guid = v_vm_guid
        AND v_origin != 4;
    INSERT INTO vm_ovf_generations(vm_guid, storage_pool_id)
        SELECT
            v_vm_guid,
            storage_pool_id
        FROM cluster vg
        WHERE vg.cluster_id = v_cluster_id
           AND v_origin != 4;

    -- add connections to dedicated hosts
    PERFORM InsertDedicatedHostsToVm(
        v_vm_guid,
        v_dedicated_vm_for_vds);

    -- set child_count for the template
    UPDATE vm_static
    SET child_count = child_count+1
    WHERE vm_guid = v_vmt_guid;

END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION IncrementDbGeneration(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET db_generation  = db_generation + 1
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION IncrementDbGenerationForVms(v_vm_guids UUID[])
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_static
    SET db_generation = db_generation + 1
    WHERE vm_guid = ANY(v_vm_guids);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDbGeneration(v_vm_guid UUID)
RETURNS SETOF BIGINT STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT db_generation
      FROM vm_static
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION IncrementDbGenerationForAllInStoragePool(v_storage_pool_id UUID)
RETURNS VOID
   AS $procedure$
DECLARE
     curs CURSOR FOR SELECT vms.vm_guid FROM vm_static vms
                     WHERE vms.cluster_id IN (SELECT vgs.cluster_id FROM cluster vgs
                                                WHERE vgs.storage_pool_id=v_storage_pool_id)
                     ORDER BY vm_guid;
     id UUID;
BEGIN
      OPEN curs;
      LOOP
         FETCH curs INTO id;
         IF NOT FOUND THEN
            EXIT;
         END IF;
         UPDATE vm_static SET db_generation  = db_generation + 1 WHERE vm_guid = id;
      END LOOP;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsAndTemplatesIdsWithoutAttachedImageDisks(v_storage_pool_id UUID, v_shareable BOOLEAN)
RETURNS SETOF UUID STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vs.vm_guid
      FROM vm_static vs
      WHERE vs.vm_guid NOT IN (SELECT DISTINCT vd.vm_id
                               FROM vm_device vd
                               INNER JOIN base_disks i
                               ON i.disk_id = vd.device_id
                               AND vd.snapshot_id IS NULL
                               WHERE i.disk_storage_type in (0, 2)  -- Filter VMs with Images (0) or Cinder (2) disks.
                                 AND i.shareable = v_shareable)
      AND vs.cluster_id IN (SELECT vg.cluster_id
                              FROM cluster vg, storage_pool sp
                              WHERE vg.storage_pool_id = v_storage_pool_id);
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION UpdateVmStatic(v_description VARCHAR(4000) ,
 v_free_text_comment text,
 v_mem_size_mb INTEGER,
 v_max_memory_size_mb INTEGER,
 v_num_of_io_threads INTEGER,
 v_os INTEGER,
 v_cluster_id UUID,
 v_vm_guid UUID,
 v_vm_name VARCHAR(255),
 v_vmt_guid UUID,
 v_creation_date TIMESTAMP WITH TIME ZONE,
 v_num_of_monitors INTEGER,
 v_allow_console_reconnect BOOLEAN,
 v_is_initialized BOOLEAN,
    v_num_of_sockets INTEGER,
    v_cpu_per_socket INTEGER,
    v_threads_per_cpu INTEGER,
 v_usb_policy  INTEGER,
 v_time_zone VARCHAR(40) ,
 v_auto_startup BOOLEAN,
 v_is_stateless BOOLEAN,
 v_is_smartcard_enabled BOOLEAN,
 v_is_delete_protected BOOLEAN,
 v_sso_method VARCHAR(32),
 v_dedicated_vm_for_vds text ,
    v_vm_type INTEGER ,
    v_nice_level INTEGER,
    v_cpu_shares INTEGER,
    v_default_boot_sequence INTEGER,
 v_default_display_type INTEGER,
 v_priority INTEGER,
    v_iso_path VARCHAR(4000) ,
    v_origin INTEGER ,
    v_initrd_url    VARCHAR(4000) ,
    v_kernel_url    VARCHAR(4000) ,
    v_kernel_params VARCHAR(4000) ,
    v_migration_support INTEGER ,
v_predefined_properties VARCHAR(4000),
v_userdefined_properties VARCHAR(4000),
v_min_allocated_mem INTEGER,
v_quota_id UUID,
v_cpu_pinning VARCHAR(4000),
v_host_cpu_flags BOOLEAN,
v_tunnel_migration BOOLEAN,
v_vnc_keyboard_layout	VARCHAR(16),
v_is_run_and_pause BOOLEAN,
v_created_by_user_id UUID,
v_instance_type_id UUID,
v_image_type_id UUID,
v_original_template_id UUID,
v_original_template_name VARCHAR(255),
v_migration_downtime INTEGER,
v_template_version_number INTEGER,
v_serial_number_policy SMALLINT,
v_custom_serial_number VARCHAR(255),
v_is_boot_menu_enabled BOOLEAN,
v_is_spice_file_transfer_enabled BOOLEAN,
v_is_spice_copy_paste_enabled BOOLEAN,
v_cpu_profile_id UUID,
v_is_auto_converge BOOLEAN,
v_is_migrate_compressed BOOLEAN,
v_is_migrate_encrypted BOOLEAN,
v_custom_emulated_machine VARCHAR(40),
v_bios_type INTEGER,
v_custom_cpu_name VARCHAR(40),
v_small_icon_id UUID,
v_large_icon_id UUID,
v_provider_id UUID,
v_console_disconnect_action VARCHAR(64),
v_resume_behavior VARCHAR(64),
v_custom_compatibility_version VARCHAR(40),
v_migration_policy_id UUID,
v_lease_sd_id UUID,
v_multi_queues_enabled BOOLEAN,
v_virtio_scsi_multi_queues INTEGER,
v_use_tsc_frequency BOOLEAN,
v_namespace VARCHAR(253),
v_balloon_enabled BOOLEAN,
v_console_disconnect_action_delay SMALLINT,
v_cpu_pinning_policy SMALLINT)

RETURNS VOID

	--The [vm_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
     UPDATE vm_static
     SET
     description = v_description,
     free_text_comment = v_free_text_comment ,
     mem_size_mb = v_mem_size_mb,
     max_memory_size_mb = v_max_memory_size_mb,
     num_of_io_threads = v_num_of_io_threads,
     os = v_os,
     cluster_id = v_cluster_id,
     vm_name = v_vm_name,
     vmt_guid = v_vmt_guid,
     creation_date = v_creation_date,
     num_of_monitors = v_num_of_monitors,
     allow_console_reconnect = v_allow_console_reconnect,
     is_initialized = v_is_initialized,
     num_of_sockets = v_num_of_sockets,
     cpu_per_socket = v_cpu_per_socket,
     threads_per_cpu = v_threads_per_cpu,
     usb_policy = v_usb_policy,
     time_zone = v_time_zone,
     auto_startup = v_auto_startup,
     is_stateless = v_is_stateless,
     vm_type = v_vm_type,
     nice_level = v_nice_level,
     cpu_shares = v_cpu_shares,
     _update_date = LOCALTIMESTAMP,
     default_boot_sequence = v_default_boot_sequence,
     default_display_type = v_default_display_type,
     priority = v_priority,
     iso_path = v_iso_path,origin = v_origin,
     initrd_url = v_initrd_url,
     kernel_url = v_kernel_url,
     kernel_params = v_kernel_params,
     migration_support = v_migration_support,
     predefined_properties = v_predefined_properties,
     userdefined_properties = v_userdefined_properties,
     min_allocated_mem = v_min_allocated_mem,
     quota_id = v_quota_id,
     cpu_pinning = v_cpu_pinning,
     is_smartcard_enabled = v_is_smartcard_enabled,
     is_delete_protected = v_is_delete_protected,
     sso_method = v_sso_method,
     host_cpu_flags = v_host_cpu_flags,
     tunnel_migration = v_tunnel_migration,
     vnc_keyboard_layout = v_vnc_keyboard_layout,
     is_run_and_pause = v_is_run_and_pause,
     created_by_user_id = v_created_by_user_id,
     instance_type_id = v_instance_type_id,
     image_type_id = v_image_type_id,
     original_template_id = v_original_template_id,
     original_template_name = v_original_template_name,
     migration_downtime = v_migration_downtime,
     template_version_number = v_template_version_number,
     serial_number_policy = v_serial_number_policy,
     custom_serial_number = v_custom_serial_number,
     is_boot_menu_enabled = v_is_boot_menu_enabled,
     is_spice_file_transfer_enabled = v_is_spice_file_transfer_enabled,
     is_spice_copy_paste_enabled = v_is_spice_copy_paste_enabled,
     cpu_profile_id = v_cpu_profile_id,
     is_auto_converge = v_is_auto_converge,
     is_migrate_compressed = v_is_migrate_compressed,
     is_migrate_encrypted = v_is_migrate_encrypted,
     custom_emulated_machine = v_custom_emulated_machine,
     bios_type = v_bios_type,
     custom_cpu_name = v_custom_cpu_name,
     small_icon_id = v_small_icon_id,
     large_icon_id = v_large_icon_id,
     provider_id = v_provider_id,
     console_disconnect_action = v_console_disconnect_action,
     resume_behavior = v_resume_behavior,
     custom_compatibility_version=v_custom_compatibility_version,
     migration_policy_id = v_migration_policy_id,
     lease_sd_id = v_lease_sd_id,
     multi_queues_enabled = v_multi_queues_enabled,
     virtio_scsi_multi_queues = v_virtio_scsi_multi_queues,
     use_tsc_frequency = v_use_tsc_frequency,
     namespace = v_namespace,
     balloon_enabled = v_balloon_enabled,
     console_disconnect_action_delay = v_console_disconnect_action_delay,
     cpu_pinning_policy = v_cpu_pinning_policy
     WHERE vm_guid = v_vm_guid
         AND entity_type = 'VM';

      -- Update connections to dedicated hosts
      PERFORM UpdateDedicatedHostsToVm(
          v_vm_guid,
          v_dedicated_vm_for_vds);

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVmStatic(v_vm_guid UUID, v_remove_permissions boolean)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
   v_vmt_guid  UUID;
BEGIN
      -- store vmt_guid for setting the child_count
      SELECT   vm_static.vmt_guid INTO v_vmt_guid FROM vm_static WHERE vm_guid = v_vm_guid;

			-- Get (and keep) a shared lock with "right to upgrade to exclusive"
            -- in order to force locking parent before children
      select   vm_guid INTO v_val FROM vm_static  WHERE vm_guid = v_vm_guid     FOR UPDATE;
      DELETE FROM vm_static
      WHERE vm_guid = v_vm_guid
      AND   entity_type = 'VM';

			-- delete VM permissions --
      if v_remove_permissions then
        PERFORM DeletePermissionsByEntityId(v_vm_guid);
      end if;

      -- set the child_count for the template
      UPDATE vm_static
          SET child_count = child_count - 1 WHERE vm_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmStatic() RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmStaticWithoutIcon() RETURNS SETOF vm_static_view STABLE
AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE entity_type = 'VM'
      AND (small_icon_id IS NULL OR large_icon_id IS NULL);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateOriginalTemplateName(
v_original_template_id UUID,
v_original_template_name VARCHAR(255))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET original_template_name = v_original_template_name
      WHERE original_template_id = v_original_template_id;
END; $procedure$





LANGUAGE plpgsql;
Create or replace FUNCTION UpdateVmLeaseInfo(
v_vm_guid UUID,
v_lease_info VARCHAR(1000))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_dynamic
      SET lease_info = v_lease_info
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateVmLeaseStorageDomainId(
v_vm_guid UUID,
v_sd_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET lease_sd_id = v_sd_id
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmStaticByVmGuid(v_vm_guid UUID) RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE vm_guid = v_vm_guid
       AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetVmStaticByVmGuids(v_vm_guids UUID[]) RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE vm_guid = ANY(v_vm_guids)
       AND entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromVmStaticByStoragePoolId(v_sp_id uuid) RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view INNER JOIN
        cluster ON vm_static_view.cluster_id = cluster.cluster_id LEFT OUTER JOIN
        storage_pool ON vm_static_view.cluster_id = cluster.cluster_id
        AND cluster.storage_pool_id = storage_pool.id
   WHERE v_sp_id = storage_pool.id
       AND entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmStaticByName(v_vm_name VARCHAR(255)) RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE vm_name = v_vm_name
       AND entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmStaticByCluster(v_cluster_id UUID) RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static_view.*
   FROM vm_static_view
   WHERE cluster_id = v_cluster_id
       AND entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;




-----------------------------------------------------------------------------------------
---   [vms] - view
-----------------------------------------------------------------------------------------





Create or replace FUNCTION GetAllFromVms(v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
IF v_is_filtered THEN
   RETURN QUERY SELECT vms.*
      FROM vms INNER JOIN user_vm_permissions_view ON vms.vm_guid = user_vm_permissions_view.entity_id
      WHERE user_id = v_user_id
      ORDER BY vm_guid;
ELSE
   RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      ORDER BY vm_guid;
END IF;

END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetAllFromVmsFilteredAndSorted(v_user_id UUID, v_offset int, v_limit int) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
      FROM (
          -- VMs that are not part of a pool that the user has direct or inherited permissions on
          SELECT vms.*
          FROM vms INNER JOIN user_vm_permissions_view ON vms.vm_guid = user_vm_permissions_view.entity_id
          WHERE vm_pool_id IS NULL
              AND user_id = v_user_id
          UNION
          -- VMs that are part of a pool that the user has direct permissions on
          SELECT vms.*
          FROM vms INNER JOIN permissions ON vms.vm_guid = permissions.object_id
          WHERE vm_pool_id IS NOT NULL
              AND ad_element_id = v_user_id
      ) result
      ORDER BY vm_name ASC
      LIMIT v_limit OFFSET v_offset;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetAllRunningVmsForUserAndActionGroup(v_user_id UUID, v_action_group_id INTEGER) RETURNS SETOF vm_dynamic STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vm_dynamic.*
      FROM vm_dynamic, vm_permissions_view, permissions_view, engine_session_user_flat_groups
      WHERE vm_dynamic.run_on_vds IS NOT NULL
          AND   vm_dynamic.vm_guid = vm_permissions_view.entity_id
          AND   vm_permissions_view.user_id = v_user_id
          AND   engine_session_user_flat_groups.user_id = vm_permissions_view.user_id
          -- check the user has permission on any parent for this vm id and Object type 2 (vm)
          AND   permissions_view.object_id IN (SELECT id FROM fn_get_entity_parents(vm_dynamic.vm_guid, 2))
          AND   permissions_view.ad_element_id = engine_session_user_flat_groups.granted_id
          AND   permissions_view.role_id IN (SELECT role_id FROM roles_groups WHERE action_group_id = v_action_group_id)
      ORDER BY vm_guid;
END; $procedure$
LANGUAGE plpgsql;








Create or replace FUNCTION GetVmsByIds(v_vms_ids UUID[]) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm.*
             FROM vms vm
             WHERE vm.vm_guid = ANY(v_vms_ids);
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmByVmGuid(v_vm_guid UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vm_guid = v_vm_guid
       AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = v_vm_guid));
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetHostedEngineVm() RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE origin = 5 OR origin = 6;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmByVmNameForDataCenter(v_data_center_id UUID, v_vm_name VARCHAR(255), v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vm_name = v_vm_name
       AND   (v_data_center_id is null OR storage_pool_id = v_data_center_id)
       AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = vms.vm_guid));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION getByNameAndNamespaceForCluster(v_cluster_id UUID, v_vm_name VARCHAR(255), v_namespace VARCHAR(253)) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vm_name = v_vm_name
       AND namespace = v_namespace
       AND cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByVmtGuid(v_vmt_guid UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsByUserId(v_user_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$

DECLARE v_vm_ids  UUID[];
BEGIN
SELECT array_agg(object_id) INTO v_vm_ids
FROM permissions
    WHERE ad_element_id = v_user_id;

RETURN QUERY
SELECT vms.*
FROM vms
   WHERE vm_guid = ANY(v_vm_ids);
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsByInstanceTypeId(v_instance_type_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY select vms.* from vms
   WHERE instance_type_id = v_instance_type_id;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsByUserIdWithGroupsAndUserRoles(v_user_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   from vms
   inner join permissions_view as perms on vms.vm_guid = perms.object_id
   WHERE (perms.ad_element_id = v_user_id
       OR perms.ad_element_id IN(
           SELECT id
           FROM getUserAndGroupsById(v_user_id)))
       AND perms.role_type = 2;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsRunningOnVds(v_vds_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE run_on_vds = v_vds_id;

END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetVmsRunningOnMultipleVds(v_vds_ids UUID[])
RETURNS SETOF vms STABLE AS $procedure$
BEGIN
   RETURN QUERY

   SELECT DISTINCT vms.*
   FROM vms
   WHERE run_on_vds = ANY(v_vds_ids);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsRunningByVds(v_vds_id UUID) RETURNS SETOF vms_monitoring_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY
SELECT DISTINCT vms_monitoring_view.*
   FROM vms_monitoring_view
   WHERE run_on_vds = v_vds_id;

END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetVmsMigratingToVds(v_vds_id UUID)
RETURNS SETOF vm_dynamic STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vm_dynamic.*
   FROM vm_dynamic
   WHERE migrating_to_vds = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsRunningOnOrMigratingToVds(v_vds_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
    -- use migrating_to_vds column when the VM is in status Migrating From
    RETURN QUERY SELECT DISTINCT V.* FROM VMS V
    WHERE V.run_on_vds=v_vds_id
        OR (V.status = 5
            AND V.migrating_to_vds=v_vds_id)
    ORDER BY V.vm_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllForStoragePool(v_storage_pool_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
             FROM vms
             WHERE storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateOvirtGuestAgentStatus(
	v_vm_guid UUID,
	v_ovirt_guest_agent_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE vm_dynamic
      SET
      ovirt_guest_agent_status = v_ovirt_guest_agent_status
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateQemuGuestAgentStatus(
	v_vm_guid UUID,
	v_qemu_guest_agent_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE vm_dynamic
      SET
      qemu_guest_agent_status = v_qemu_guest_agent_status
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsDynamicRunningOnVds(v_vds_id UUID) RETURNS SETOF vm_dynamic STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_dynamic.*
      FROM vm_dynamic
      WHERE run_on_vds = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION IsAnyVmRunOnVds(v_vds_id UUID)
RETURNS SETOF booleanResultType STABLE
    AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT vm_guid
            FROM vm_dynamic
            WHERE run_on_vds = v_vds_id
           );
END;$PROCEDURE$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteVm(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_vmt_guid  UUID;
BEGIN
      SELECT vm_static.vmt_guid INTO v_vmt_guid FROM vm_static WHERE vm_guid = v_vm_guid;
      UPDATE vm_static
      SET child_count =(SELECT COUNT(*) FROM vm_static WHERE vmt_guid = v_vmt_guid) -1
      WHERE vm_guid = v_vmt_guid;
      DELETE FROM tags_vm_map
      WHERE vm_id = v_vm_guid;
      DELETE
      FROM   snapshots
      WHERE  vm_id = v_vm_guid;
      DELETE FROM vm_statistics WHERE vm_guid = v_vm_guid;
      DELETE FROM vm_dynamic WHERE vm_guid = v_vm_guid;
      DELETE FROM vm_static WHERE vm_guid = v_vm_guid;
      PERFORM DeletePermissionsByEntityId(v_vm_guid);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmsByAdGroupNames(v_ad_group_names VARCHAR(250)) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
RETURN QUERY select distinct vms.* from vms
   inner join permissions on vms.vm_guid = permissions.object_id
   inner join ad_groups on ad_groups.id = permissions.ad_element_id
   WHERE     (ad_groups.name in(select Id from fnSplitter(v_ad_group_names)));
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByDiskId(v_disk_guid UUID) RETURNS SETOF vms_with_plug_info STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms_with_plug_info.*
      FROM vms_with_plug_info
      WHERE device_id = v_disk_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllVMsWithDisksOnOtherStorageDomain(v_storage_domain_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN (SELECT vm_static.vm_guid
                  FROM vm_static
                  INNER JOIN vm_device vd ON vd.vm_id = vm_static.vm_guid
                  INNER JOIN images i ON i.image_group_id = vd.device_id
                  INNER JOIN (SELECT image_id
                              FROM image_storage_domain_map
                              WHERE image_storage_domain_map.storage_domain_id = v_storage_domain_id) isd_map
                              ON i.image_guid = isd_map.image_id WHERE entity_type = 'VM') vms_with_disks_on_storage_domain ON vms.vm_guid = vms_with_disks_on_storage_domain.vm_guid
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images i ON i.image_group_id = vd.device_id
      INNER JOIN image_storage_domain_map on i.image_guid = image_storage_domain_map.image_id
      WHERE image_storage_domain_map.storage_domain_id != v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetActiveVmsByStorageDomainId(v_storage_domain_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images i ON i.image_group_id = vd.device_id
      inner join image_storage_domain_map on i.image_guid = image_storage_domain_map.image_id
      WHERE status not in (0, 13)
      AND is_plugged = TRUE
      AND image_storage_domain_map.storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmsByStorageDomainId(v_storage_domain_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images ON images.image_group_id = vd.device_id
          AND images.active = TRUE
      INNER JOIN image_storage_domain_map on images.image_guid = image_storage_domain_map.image_id
      WHERE image_storage_domain_map.storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION getAllVmsRelatedToQuotaId(v_quota_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE quota_id = v_quota_id
      UNION
      SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images ON images.image_group_id = vd.device_id
          AND images.active = TRUE
      INNER JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
      WHERE image_storage_domain_map.quota_id = v_quota_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateIsInitialized(v_vm_guid UUID,
 v_is_initialized BOOLEAN)
RETURNS VOID

	--The [vm_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET is_initialized = v_is_initialized
      WHERE vm_guid = v_vm_guid
          AND entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;





DROP TYPE IF EXISTS GetOrderedVmGuidsForRunMultipleActions_rs CASCADE;
CREATE TYPE GetOrderedVmGuidsForRunMultipleActions_rs AS (vm_guid UUID);
Create or replace FUNCTION GetOrderedVmGuidsForRunMultipleActions(v_vm_guids VARCHAR(4000)) RETURNS SETOF GetOrderedVmGuidsForRunMultipleActions_rs STABLE
   AS $procedure$
   DECLARE
   v_ordered_guids GetOrderedVmGuidsForRunMultipleActions_rs;
BEGIN
   FOR v_ordered_guids IN
       EXECUTE 'SELECT vm_guid from vm_static where vm_guid in( ' || v_vm_guids || ' ) AND entity_type = ''VM''  order by auto_startup desc,priority desc, migration_support desc'
   LOOP
      RETURN NEXT v_ordered_guids;
   END LOOP;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByNetworkId(v_network_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vms
   WHERE EXISTS (
      SELECT 1
      FROM vm_interface
      INNER JOIN vnic_profiles
      ON vnic_profiles.id = vm_interface.vnic_profile_id
      WHERE vnic_profiles.network_id = v_network_id
          AND vm_interface.vm_guid = vms.vm_guid);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsByVnicProfileId(v_vnic_profile_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vms
   WHERE EXISTS (
      SELECT 1
      FROM vm_interface
      WHERE vm_interface.vnic_profile_id = v_vnic_profile_id
          AND vm_interface.vm_guid = vms.vm_guid);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsByClusterId(v_cluster_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByVmPoolId(v_vm_pool_id UUID) RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetFailedAutoStartVms() RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE auto_startup = TRUE
          AND status = 0
          AND exit_status = 1;
END; $procedure$
LANGUAGE plpgsql;

-- Get all running vms for cluster
Create or replace FUNCTION GetRunningVmsByClusterId(v_cluster_id UUID) RETURNS SETOF vms STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT DISTINCT vms.*
    FROM vms
    WHERE run_on_vds IS NOT NULL
        AND cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

---------------------
-- vm_init functions
---------------------

Create or replace FUNCTION GetVmInitByVmId(v_vm_id UUID) RETURNS SETOF vm_init STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_init.*
   FROM vm_init
   WHERE vm_id = v_vm_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmInitByids(v_vm_init_ids UUID[]) RETURNS SETOF vm_init STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
     FROM vm_init
     WHERE vm_init.vm_id = ANY(v_vm_init_ids);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmInit(
    v_vm_id UUID,
    v_host_name TEXT,
    v_domain TEXT,
    v_authorized_keys TEXT,
    v_regenerate_keys BOOLEAN,
    v_time_zone VARCHAR(40),
    v_dns_servers TEXT,
    v_dns_search_domains TEXT,
    v_networks TEXT,
    v_password TEXT,
    v_winkey VARCHAR(30),
    v_custom_script TEXT,
    v_input_locale VARCHAR(256),
    v_ui_language VARCHAR(256),
    v_system_locale VARCHAR(256),
    v_user_locale VARCHAR(256),
    v_user_name VARCHAR(256),
    v_active_directory_ou VARCHAR(256),
    v_org_name VARCHAR(256),
    v_cloud_init_network_protocol VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_init
    SET host_name = v_host_name,
        domain = v_domain,
        authorized_keys = v_authorized_keys,
        regenerate_keys = v_regenerate_keys,
        time_zone = v_time_zone,
        dns_servers = v_dns_servers,
        dns_search_domains = v_dns_search_domains,
        networks = v_networks,
        password = v_password,
        winkey = v_winkey,
        custom_script = v_custom_script,
        input_locale = v_input_locale,
        ui_language = v_ui_language,
        system_locale = v_system_locale,
        user_locale = v_user_locale,
        user_name = v_user_name,
        active_directory_ou = v_active_directory_ou,
        org_name = v_org_name,
        cloud_init_network_protocol = v_cloud_init_network_protocol
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmInit (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_init
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmInit (
    v_vm_id UUID,
    v_host_name TEXT,
    v_domain TEXT,
    v_authorized_keys TEXT,
    v_regenerate_keys BOOLEAN,
    v_time_zone VARCHAR(40),
    v_dns_servers TEXT,
    v_dns_search_domains TEXT,
    v_networks TEXT,
    v_password TEXT,
    v_winkey VARCHAR(30),
    v_custom_script TEXT,
    v_input_locale VARCHAR(256),
    v_ui_language VARCHAR(256),
    v_system_locale VARCHAR(256),
    v_user_locale VARCHAR(256),
    v_user_name VARCHAR(256),
    v_active_directory_ou VARCHAR(256),
    v_org_name VARCHAR(256),
    v_cloud_init_network_protocol VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_init (
        vm_id,
        host_name,
        domain,
        authorized_keys,
        regenerate_keys,
        time_zone,
        dns_servers,
        dns_search_domains,
        networks,
        password,
        winkey,
        custom_script,
        input_locale,
        ui_language,
        system_locale,
        user_locale,
        user_name,
        active_directory_ou,
        org_name,
        cloud_init_network_protocol
        )
    VALUES (
        v_vm_id,
        v_host_name,
        v_domain,
        v_authorized_keys,
        v_regenerate_keys,
        v_time_zone,
        v_dns_servers,
        v_dns_search_domains,
        v_networks,
        v_password,
        v_winkey,
        v_custom_script,
        v_input_locale,
        v_ui_language,
        v_system_locale,
        v_user_locale,
        v_user_name,
        v_active_directory_ou,
        v_org_name,
        v_cloud_init_network_protocol
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmIdsForVersionUpdate (v_base_template_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vs.vm_guid
    FROM vm_static vs natural
    INNER JOIN vm_dynamic
    WHERE (
            vmt_guid = v_base_template_id
            OR vmt_guid IN (
                SELECT vm_guid
                FROM vm_static
                WHERE vmt_guid = v_base_template_id
                )
            )
        AND template_version_number IS NULL
        AND entity_type = 'VM'
        AND status = 0
        AND (
            is_stateless = TRUE
            OR (
                EXISTS (
                    SELECT *
                    FROM vm_pool_map
                    WHERE vm_guid = vs.vm_guid
                    )
                AND NOT EXISTS (
                    SELECT *
                    FROM snapshots
                    WHERE vm_id = vs.vm_guid
                        AND snapshot_type = 'STATELESS'
                    )
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmCpuProfileIdForClusterId (
    v_cluster_id UUID,
    v_cpu_profile_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_static
    SET cpu_profile_id = v_cpu_profile_id
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertDedicatedHostsToVm (
    v_vm_guid UUID,
    v_dedicated_vm_for_vds TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_host_pinning_map (
        vm_id,
        vds_id
        )
    SELECT v_vm_guid,
        vds_id
    FROM fnSplitterUuid(v_dedicated_vm_for_vds) AS vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateDedicatedHostsToVm (
    v_vm_guid UUID,
    v_dedicated_vm_for_vds TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_host_pinning_map
    WHERE vm_id = v_vm_guid;

    PERFORM InsertDedicatedHostsToVm(v_vm_guid, v_dedicated_vm_for_vds);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmsByCpuProfileIds(v_cpu_profile_ids UUID[])
RETURNS SETOF vms STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT vms.*
        FROM vms
        WHERE cpu_profile_id = ANY(v_cpu_profile_ids);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllVmsRelatedToDiskProfiles(v_disk_profile_ids UUID[])
RETURNS SETOF vms STABLE
AS $procedure$
BEGIN
  RETURN QUERY SELECT vms.*
      FROM vms
          INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
          INNER JOIN images ON images.image_group_id = vd.device_id
              AND images.active = TRUE
          INNER JOIN image_storage_domain_map ON image_storage_domain_map.image_id = images.image_guid
          WHERE image_storage_domain_map.disk_profile_id = ANY(v_disk_profile_ids);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmsByOrigin(v_origins INT[])
RETURNS SETOF vms STABLE
   AS $procedure$
BEGIN
    RETURN QUERY
    SELECT vms.*
        FROM vms
        WHERE origin = ANY(v_origins);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetToUnknown (
    v_vm_ids UUID[],
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_dynamic
    SET status = v_status
    WHERE vm_guid = ANY(v_vm_ids)
        AND run_on_vds IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmsWithLeaseOnStorageDomain(v_storage_domain_id UUID)
RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vm_static_view.*
    FROM vm_static_view
    WHERE lease_sd_id = v_storage_domain_id
        AND entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetActiveVmNamesWithLeaseOnStorageDomain(v_storage_domain_id UUID)
RETURNS SETOF varchar(255) STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vs.vm_name
    FROM vm_static vs
    JOIN vm_dynamic vd ON vd.vm_guid = vs.vm_guid
    WHERE lease_sd_id = v_storage_domain_id
        AND vd.status <> 0;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetActiveVmNamesWithIsoOnStorageDomain(v_storage_domain_id UUID)
RETURNS SETOF varchar(255) STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vd.vm_name
    FROM images_storage_domain_view image, vms_monitoring_view vd
    WHERE image.storage_id = v_storage_domain_id
    AND vd.status not in (0, 14, 15) -- Down, ImageIllegal, ImageLocked
    AND image.image_group_id::VARCHAR = vd.current_cd;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetActiveVmNamesWithIsoAttached(v_iso_disk_id UUID)
RETURNS SETOF varchar(255) STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vs.vm_name
    FROM vm_static vs
    JOIN vm_dynamic vd ON vd.vm_guid = vs.vm_guid
    WHERE vs.iso_path = v_iso_disk_id::VARCHAR
        AND vd.status NOT IN (0, 14, 15);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmNamesWithSpecificIsoAttached(v_iso_disk_id UUID)
RETURNS SETOF varchar(255) STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vm.vm_name
    FROM vm_static vm
    WHERE vm.iso_path = v_iso_disk_id::VARCHAR;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmIdsWithSpecificIsoAttached(v_iso_disk_id UUID)
RETURNS SETOF UUID STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vd.vm_guid
    FROM vm_dynamic vd
    WHERE vd.current_cd = v_iso_disk_id::VARCHAR;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmsStaticRunningOnVds(v_vds_id UUID)
RETURNS SETOF vm_static_view STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT vs.*
    FROM vm_static_view vs
    JOIN vm_dynamic vd ON vd.vm_guid = vs.vm_guid
    WHERE run_on_vds = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmsPinnedToHost(v_host_id UUID)
RETURNS SETOF vms STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT vms.*
    FROM vms
        JOIN vm_host_pinning_map pin ON pin.vm_id = vms.vm_guid
    WHERE pin.vds_id = v_host_id;
END; $procedure$
LANGUAGE plpgsql;




DROP TYPE IF EXISTS VmExternalData_rs CASCADE;
CREATE TYPE VmExternalData_rs AS (data TEXT, hash TEXT);

CREATE OR REPLACE FUNCTION GetTpmData (
    v_vm_id UUID
)
RETURNS SETOF VmExternalData_rs STABLE AS $FUNCTION$
    SELECT tpm_data, tpm_hash
    FROM vm_external_data
    WHERE vm_id = v_vm_id
$FUNCTION$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION UpdateTpmData (
    v_vm_id UUID,
    v_tpm_data TEXT,
    v_tpm_hash TEXT
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_external_data (
        device_id,
        vm_id,
        tpm_data,
        tpm_hash
    )
    SELECT device_id, v_vm_id, v_tpm_data, v_tpm_hash
    FROM vm_device
    WHERE v_tpm_data IS NOT NULL AND vm_id = v_vm_id AND type = 'tpm'
    ON CONFLICT (device_id, vm_id) DO
    UPDATE
    SET tpm_data = v_tpm_data,
        tpm_hash = v_tpm_hash;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteTpmData (
    v_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
    DELETE FROM vm_external_data
    WHERE vm_id = v_vm_id
$PROCEDURE$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION CopyTpmData (
    v_source_vm_id UUID,
    v_target_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
DECLARE
    v_device_id UUID := (SELECT device_id
                         FROM vm_device
                         WHERE vm_id = v_target_vm_id AND type = 'tpm');
BEGIN
    IF v_device_id IS NOT NULL THEN
        INSERT INTO vm_external_data (
            device_id,
            vm_id,
            tpm_data,
            tpm_hash
        )
        SELECT v_device_id, v_target_vm_id, tpm_data, tpm_hash
        FROM vm_external_data
        WHERE vm_id = v_source_vm_id;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNvramData (
    v_vm_id UUID
)
RETURNS SETOF VmExternalData_rs AS $FUNCTION$
    SELECT nvram_data, nvram_hash
    FROM vm_nvram_data
    WHERE vm_id = v_vm_id
$FUNCTION$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION UpdateNvramData (
    v_vm_id UUID,
    v_nvram_data TEXT,
    v_nvram_hash TEXT
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_nvram_data (
        vm_id,
        nvram_data,
        nvram_hash
    )
    VALUES (
        v_vm_id,
        v_nvram_data,
        v_nvram_hash
    )
    ON CONFLICT (vm_id) DO
    UPDATE
    SET nvram_data = v_nvram_data,
        nvram_hash = v_nvram_hash;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNvramData (
    v_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
    DELETE FROM vm_nvram_data
    WHERE vm_id = v_vm_id
$PROCEDURE$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION CopyNvramData (
    v_source_vm_id UUID,
    v_target_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_nvram_data (
        vm_id,
        nvram_data,
        nvram_hash
    )
    SELECT v_target_vm_id, nvram_data, nvram_hash
    FROM vm_nvram_data
    WHERE vm_id = v_source_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TRIGGER
IF EXISTS remove_nvram_data_on_update
    ON vm_static;

CREATE OR REPLACE FUNCTION remove_nvram_data ()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.bios_type = 4 AND NEW.bios_type != 4 THEN
        DELETE FROM vm_nvram_data
        WHERE vm_id = OLD.vm_guid;
    END IF;
    RETURN NEW;
END;$$
LANGUAGE plpgsql;

CREATE TRIGGER remove_nvram_data_on_update AFTER
UPDATE
    ON vm_static
FOR EACH ROW
EXECUTE FUNCTION remove_nvram_data();
