-- ----------------------------------------------------------------------
-- Script generated with: DeZign for Databases v4.1.1
-- Target DBMS:           MS SQL Server 2005
-- Project file:          vdc_3.dez
-- Project name:
-- Author:
-- Script type:           Database creation script
-- Created on:            2007-02-15 15:03
-- ----------------------------------------------------------------------


-- ----------------------------------------------------------------------
-- Tables
-- ----------------------------------------------------------------------


-- ----------------------------------------------------------------------
-- Add table "tags_vm_pool_map"
-- ----------------------------------------------------------------------
CREATE TABLE tags_vm_pool_map
(
   tag_id UUID NOT NULL,
   vm_pool_id UUID NOT NULL,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   CONSTRAINT pk_tags_vm_pool_map PRIMARY KEY(tag_id,vm_pool_id)
) WITH OIDS;

-- ----------------------------------------------------------------------
-- Add table "tags_vm_map"
-- ----------------------------------------------------------------------
CREATE TABLE tags_vm_map
(
   tag_id UUID NOT NULL,
   vm_id UUID NOT NULL,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   DefaultDisplayType INTEGER DEFAULT 0,
   constraint pk_tags_vm_map primary key(tag_id,vm_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "tags_vds_map"
-- ----------------------------------------------------------------------
CREATE TABLE tags_vds_map
(
   tag_id UUID NOT NULL,
   vds_id UUID NOT NULL,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   constraint pk_tags_vds_map primary key(tag_id,vds_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "tags"
-- ----------------------------------------------------------------------
CREATE TABLE tags
(
   tag_id UUID NOT NULL,
   tag_name VARCHAR(50)  NOT NULL default '',
   description VARCHAR(4000),
   parent_id  UUID,
   readonly  BOOLEAN,
   type INTEGER NOT NULL DEFAULT 0,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   constraint pk_tags_id primary key(tag_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "tags_user_map"
-- ----------------------------------------------------------------------
CREATE TABLE tags_user_map
(
   tag_id UUID NOT NULL,
   user_id UUID NOT NULL,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   constraint pk_tags_user_map primary key(tag_id,user_id)
) WITH OIDS;

-- ----------------------------------------------------------------------
-- Add table "tags_user_group_map"
-- ----------------------------------------------------------------------
CREATE TABLE tags_user_group_map
(
   tag_id UUID NOT NULL,
   group_id UUID NOT NULL,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   constraint pk_tags_user_group_map primary key(tag_id,group_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "custom_actions"
-- ----------------------------------------------------------------------

CREATE SEQUENCE custom_actions_seq INCREMENT BY 1 START WITH 1;
CREATE TABLE custom_actions
(
   action_id INTEGER DEFAULT NEXTVAL('custom_actions_seq') NOT NULL,
   action_name VARCHAR(50) NOT NULL,
   path VARCHAR(300) NOT NULL,
   tab INTEGER,
   description VARCHAR(4000),
   CONSTRAINT PK_custom_actions PRIMARY KEY(action_name,tab)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "image_templates"
-- ----------------------------------------------------------------------

CREATE TABLE image_templates
(
   it_guid UUID NOT NULL,
   creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
   size BIGINT NOT NULL,
   os VARCHAR(40),
   os_version VARCHAR(40),
   bootable BOOLEAN DEFAULT false,
   description VARCHAR(4000),
   CONSTRAINT PK_image_templates PRIMARY KEY(it_guid)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "images"
-- ----------------------------------------------------------------------

CREATE TABLE images
(
   image_guid UUID NOT NULL,
   creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
   size BIGINT NOT NULL,
   it_guid UUID NOT NULL,
   internal_drive_mapping VARCHAR(50),
   description VARCHAR(4000),
   ParentId UUID,
   imageStatus INTEGER DEFAULT 0,
   lastModified TIMESTAMP WITH TIME ZONE,
   app_list TEXT,
   storage_id UUID,
   vm_snapshot_id UUID,
   volume_type INTEGER NOT NULL DEFAULT 2,
   volume_format INTEGER NOT NULL DEFAULT 4,
   disk_type INTEGER NOT NULL DEFAULT 1,
   image_group_id UUID,
   disk_interface INTEGER NOT NULL DEFAULT 0,
   boot BOOLEAN NOT NULL DEFAULT false,
   wipe_after_delete BOOLEAN NOT NULL DEFAULT false,
   propagate_errors INTEGER NOT NULL DEFAULT 0,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   CONSTRAINT PK_images PRIMARY KEY(image_guid)
) WITH OIDS;

-- ----------------------------------------------------------------------
-- Add table "image_vm_map"
-- ----------------------------------------------------------------------

CREATE TABLE image_vm_map
(
   image_id UUID not null,
   vm_id UUID not null,
   active BOOLEAN,
   CONSTRAINT pk_image_vm_map PRIMARY KEY(image_id,vm_id)

) WITH OIDS;

-- ----------------------------------------------------------------------
-- Add table "vm_templates"
-- ----------------------------------------------------------------------

CREATE TABLE vm_templates
(
   vmt_guid UUID NOT NULL,
   name VARCHAR(40) NOT NULL,
   mem_size_mb INTEGER NOT NULL,
   os INTEGER  NOT NULL DEFAULT 0,
   creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
   child_count INTEGER  NOT NULL DEFAULT 0,
   num_of_sockets INTEGER  NOT NULL DEFAULT 1,
   cpu_per_socket INTEGER  NOT NULL DEFAULT 1,
   description VARCHAR(4000),
   vds_group_id UUID NOT NULL,
   domain VARCHAR(40),
   num_of_monitors	INTEGER	NOT NULL,
   status INTEGER NOT NULL,
   usb_policy		INTEGER,
   time_zone VARCHAR(40),
   is_auto_suspend	BOOLEAN	 default false,
   fail_back BOOLEAN  NOT NULL DEFAULT false,
   vm_type INTEGER  NOT NULL DEFAULT 0,
   hypervisor_type INTEGER  NOT NULL DEFAULT 0,
   operation_mode INTEGER  NOT NULL DEFAULT 0,
   nice_level INTEGER  NOT NULL DEFAULT 0,
   default_boot_sequence INTEGER NOT NULL default 0,
   default_display_type INTEGER NOT NULL default 0,
   priority INTEGER NOT NULL default 0,
   auto_startup BOOLEAN,
   is_stateless BOOLEAN,
   iso_path VARCHAR(4000)  default '',
   initrd_url VARCHAR(4000),
   kernel_url VARCHAR(4000),
   kernel_params VARCHAR(4000),
   origin INTEGER  default 0,
   _update_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT PK_vm_templates PRIMARY KEY(vmt_guid)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "vds_static"
-- ----------------------------------------------------------------------

CREATE TABLE vds_static
(
   vds_id UUID NOT NULL,
   vds_name VARCHAR(255) NOT NULL,
   ip VARCHAR(255),
   vds_unique_id VARCHAR(128),
   host_name VARCHAR(255) NOT NULL,
   port INTEGER NOT NULL,
   vds_group_id UUID NOT NULL,
   server_SSL_enabled BOOLEAN,
   vds_type INTEGER NOT NULL DEFAULT 0,
   vds_strength INTEGER NOT NULL DEFAULT 100,
   pm_type VARCHAR(20),
   pm_user VARCHAR(50),
   pm_password VARCHAR(50),
   pm_port INTEGER,
   pm_options VARCHAR(4000) not null default '',
   pm_enabled BOOLEAN  not null default false,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE,
   otp_validity BIGINT,
   CONSTRAINT PK_vds_static PRIMARY KEY(vds_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "vm_static"
-- ----------------------------------------------------------------------

CREATE TABLE vm_static
(
   vm_guid UUID NOT NULL,
   vm_name VARCHAR(255) NOT NULL,
   mem_size_mb INTEGER NOT NULL,
   vmt_guid UUID NOT NULL,
   os INTEGER  NOT NULL DEFAULT 0,
   description VARCHAR(4000),
   vds_group_id UUID NOT NULL,
   domain VARCHAR(40),
   creation_date TIMESTAMP WITH TIME ZONE,
   num_of_monitors	INTEGER	NOT NULL,
   is_initialized	BOOLEAN,
   is_auto_suspend	BOOLEAN	 default false,
   num_of_sockets INTEGER  NOT NULL DEFAULT 1,
   cpu_per_socket INTEGER  NOT NULL DEFAULT 1,
   usb_policy		INTEGER,
   time_zone	VARCHAR(40),
   is_stateless BOOLEAN,
   fail_back		BOOLEAN  NOT NULL DEFAULT false,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE,
   dedicated_vm_for_vds UUID,
   auto_startup BOOLEAN,
   vm_type INTEGER  NOT NULL DEFAULT 0,
   hypervisor_type INTEGER  NOT NULL DEFAULT 0,
   operation_mode INTEGER  NOT NULL DEFAULT 0,
   nice_level INTEGER  NOT NULL DEFAULT 0,
   default_boot_sequence INTEGER NOT NULL default 0,
   default_display_type INTEGER NOT NULL default 0,
   priority INTEGER NOT NULL default 0,
   iso_path VARCHAR(4000)  default '',
   origin INTEGER  default 0,
   initrd_url VARCHAR(4000),
   kernel_url VARCHAR(4000),
   kernel_params VARCHAR(4000),
   migration_support INTEGER NOT NULL default 0,
   userdefined_properties VARCHAR(4000),
   predefined_properties VARCHAR(4000),
   min_allocated_mem INTEGER not null default 0, --indicates how much memory at least VM need to run, value is in MB
   CONSTRAINT PK_vm_static PRIMARY KEY(vm_guid)
) WITH OIDS;


ALTER TABLE vm_static ADD CONSTRAINT FK_vds_static_vm_static FOREIGN KEY(dedicated_vm_for_vds) REFERENCES vds_static(vds_id);


-- add non clustered index on vm_name
CREATE INDEX IDX_vm_static_vm_name ON vm_static
(vm_name);




-- ----------------------------------------------------------------------
-- Add table "users"
-- ----------------------------------------------------------------------

CREATE TABLE users
(
   user_id UUID NOT NULL,
   name VARCHAR(255),
   surname VARCHAR(255),
   domain VARCHAR(255) NOT NULL,
   username VARCHAR(255) NOT NULL,
   groups VARCHAR NOT NULL,
   department VARCHAR(255),
   role VARCHAR(255),
   user_icon_path VARCHAR(255),
   desktop_device VARCHAR(255),
   email VARCHAR(255),
   note VARCHAR(255),
   status INTEGER NOT NULL,
   session_count INTEGER  NOT NULL default 0,
   last_admin_check_status BOOLEAN  NOT NULL default false,
   group_ids TEXT,
   CONSTRAINT PK_users PRIMARY KEY(user_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "vds_groups"
-- ----------------------------------------------------------------------

CREATE TABLE vds_groups
(
   vds_group_id UUID NOT NULL,
   name VARCHAR(40) NOT NULL,
   description VARCHAR(4000),
   cpu_name VARCHAR(255),
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE,
   selection_algorithm INTEGER NOT NULL DEFAULT 0,
   high_utilization INTEGER NOT NULL DEFAULT 75,
   low_utilization INTEGER NOT NULL DEFAULT 0,
   cpu_over_commit_duration_minutes INTEGER NOT NULL DEFAULT 2,
   hypervisor_type INTEGER NOT NULL DEFAULT 0,
   storage_pool_id UUID,
   max_vds_memory_over_commit INTEGER NOT NULL DEFAULT 100,
   compatibility_version VARCHAR(40) NOT NULL DEFAULT '2.2',
   transparent_hugepages BOOLEAN NOT NULL DEFAULT '0',
   migrate_on_error INTEGER NOT NULL DEFAULT '1',
   CONSTRAINT PK_vds_groups PRIMARY KEY(vds_group_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "vm_template_image_map"
-- ----------------------------------------------------------------------

CREATE TABLE vm_template_image_map
(
   it_guid UUID NOT NULL,
   vmt_guid UUID NOT NULL,
   internal_drive_mapping VARCHAR(50) NOT NULL,
   CONSTRAINT PK_vm_template_image_map PRIMARY KEY(it_guid,vmt_guid)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "audit_log"
-- ----------------------------------------------------------------------

CREATE SEQUENCE audit_log_seq INCREMENT BY 1 START WITH 1;
CREATE TABLE audit_log
(
   audit_log_id BIGINT DEFAULT NEXTVAL('audit_log_seq') NOT NULL,
   user_id UUID,
   user_name VARCHAR(255),
   vm_id UUID,
   vm_name VARCHAR(255),
   vm_template_id UUID,
   vm_template_name VARCHAR(40),
   vds_id UUID,
   vds_name VARCHAR(255),
   log_time TIMESTAMP WITH TIME ZONE NOT NULL,
   log_type_name VARCHAR(100)  default '',
   log_type INTEGER NOT NULL,
   severity INTEGER NOT NULL,
   message TEXT NOT NULL,
   processed BOOLEAN  NOT NULL default false,
   storage_pool_id UUID,
   storage_pool_name VARCHAR(40),
   storage_domain_id UUID,
   storage_domain_name VARCHAR(250),
   vds_group_id UUID,
   vds_group_name VARCHAR(255),
   CONSTRAINT PK_audit_log PRIMARY KEY(audit_log_id)
) WITH OIDS;


-- add non clustered index on log_time (desc)
CREATE INDEX IDX_audit_log_log_time ON audit_log
(log_time);


-- add non clustered index on user_name
CREATE INDEX IDX_audit_log_user_name ON audit_log
(user_name);


-- add non clustered index on vm_name (
CREATE INDEX IDX_audit_log_vm_name ON audit_log
(vm_name);


-- add non clustered index on vm_template_name
CREATE INDEX IDX_audit_log_vm_template_name ON audit_log
(vm_template_name);


-- add non clustered index on vds_name
CREATE INDEX IDX_audit_log_vds_name ON audit_log
(vds_name);


-- add non clustered index on storage_pool_name
CREATE INDEX IDX_audit_log_storage_pool_name ON audit_log
(storage_pool_name);


-- add non clustered index on storage_domain_name
CREATE INDEX IDX_audit_log_storage_domain_name ON audit_log
(storage_domain_name);


-- ----------------------------------------------------------------------
-- Add table "vds_dynamic"
-- ----------------------------------------------------------------------

CREATE TABLE vds_dynamic
(
   vds_id UUID NOT NULL,
   status INTEGER NOT NULL,
   cpu_cores INTEGER,
   cpu_model VARCHAR(255),
   cpu_speed_mh DECIMAL(18,0),
   if_total_speed VARCHAR(40),
   kvm_enabled BOOLEAN,
   physical_mem_mb INTEGER,
   mem_commited INTEGER  DEFAULT 0,
   vm_active INTEGER  DEFAULT 0,
   vm_count INTEGER NOT NULL DEFAULT 0,
   vm_migrating INTEGER  DEFAULT 0,
   reserved_mem INTEGER,
   guest_overhead INTEGER,
   software_version VARCHAR(40),
   version_name VARCHAR(40),
   build_name VARCHAR(40),
   previous_status INTEGER,
   cpu_flags VARCHAR(4000),
   cpu_over_commit_time_stamp TIMESTAMP WITH TIME ZONE,
   hypervisor_type INTEGER,
   vms_cores_count INTEGER,
   pending_vcpus_count INTEGER,
   cpu_sockets INTEGER,
   net_config_dirty BOOLEAN,
   supported_cluster_levels VARCHAR(40),
   host_os VARCHAR(4000),
   kvm_version VARCHAR(4000),
   spice_version VARCHAR(4000),
   kernel_version VARCHAR(4000),
   iscsi_initiator_name VARCHAR(4000),
   transparent_hugepages_state INTEGER NOT NULL DEFAULT '0',
   anonymous_hugepages INTEGER NOT NULL DEFAULT '0',
   hooks VARCHAR(4000) default '',
   _update_date TIMESTAMP WITH TIME ZONE,
   non_operational_reason INTEGER NOT NULL DEFAULT '0',
   pending_vmem_size INTEGER NOT NULL DEFAULT 0,

   CONSTRAINT PK_vds_dynamic PRIMARY KEY(vds_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "vm_dynamic"
-- ----------------------------------------------------------------------

CREATE TABLE vm_dynamic
(
   vm_guid UUID NOT NULL,
   status INTEGER NOT NULL,
   vm_ip TEXT,
   vm_host VARCHAR(255),
   vm_pid INTEGER,
   vm_last_up_time TIMESTAMP WITH TIME ZONE,
   vm_last_boot_time TIMESTAMP WITH TIME ZONE,
   guest_cur_user_name VARCHAR(255),
   guest_cur_user_id UUID,
   guest_last_login_time TIMESTAMP WITH TIME ZONE,
   guest_last_logout_time TIMESTAMP WITH TIME ZONE,
   guest_os VARCHAR(255),
   run_on_vds UUID,
   migrating_to_vds UUID,
   app_list TEXT,
   display INTEGER,
   acpi_enable BOOLEAN,
   session INTEGER,
   display_ip VARCHAR(255),
   display_type INTEGER,
   kvm_enable BOOLEAN,
   display_secure_port INTEGER,
   utc_diff INTEGER,
   last_vds_run_on UUID,
   client_ip VARCHAR(255),
   guest_requested_memory INTEGER,
   hibernation_vol_handle VARCHAR(255),
   boot_sequence INTEGER,
   exit_status INTEGER NOT NULL DEFAULT 0,
   pause_status INTEGER NOT NULL DEFAULT 0,
   exit_message VARCHAR(4000),
   CONSTRAINT PK_vm_dynamic PRIMARY KEY(vm_guid)
) WITH OIDS;


-- add non clustered index on run_on_vds
CREATE INDEX IDX_vm_dynamic_run_on_vds ON vm_dynamic
(run_on_vds);



-- ----------------------------------------------------------------------
--	bookmarks
-- ----------------------------------------------------------------------

CREATE TABLE bookmarks
(
   bookmark_id UUID NOT NULL,
   bookmark_name  VARCHAR(40),
   bookmark_value VARCHAR(300) NOT NULL,
   CONSTRAINT PK_bookmarks PRIMARY KEY(bookmark_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--	vm_pools
-- ----------------------------------------------------------------------
CREATE TABLE vm_pools
(
   vm_pool_id UUID NOT NULL,
   vm_pool_name VARCHAR(255) NOT NULL,
   vm_pool_description VARCHAR(4000) NOT NULL,
   vm_pool_type INTEGER,
   parameters  VARCHAR(200),
   vds_group_id UUID,
   CONSTRAINT PK_vm_pools PRIMARY KEY(vm_pool_id)
) WITH OIDS;



-- ----------------------------------------------------------------------
--	vm_pool_map
-- ----------------------------------------------------------------------
CREATE TABLE vm_pool_map
(
   vm_pool_id UUID,
   vm_guid UUID,
   CONSTRAINT PK_vm_pool_map PRIMARY KEY(vm_guid)
) WITH OIDS;

-- ----------------------------------------------------------------------
--	images_vm_pool_map
-- ----------------------------------------------------------------------
CREATE TABLE image_vm_pool_map
(
   vm_guid UUID not null,
   image_guid UUID not NULL,
   internal_drive_mapping VARCHAR(50),
   CONSTRAINT PK_image_vm_pool_map PRIMARY KEY(image_guid)
) WITH OIDS;


CREATE TABLE time_lease_vm_pool_map
(
   vm_pool_id UUID NOT NULL,
   id UUID not null,
   start_time TIMESTAMP WITH TIME ZONE NOT NULL,
   end_time TIMESTAMP WITH TIME ZONE NOT NULL,
   type INTEGER NOT NULL,
   CONSTRAINT pk_time_user_vm_pool_map PRIMARY KEY(vm_pool_id,id)
) WITH OIDS;

-- ----------------------------------------------------------------------
--         ad_groups
-- ----------------------------------------------------------------------
CREATE TABLE  ad_groups
(
   id UUID not null,
   name VARCHAR(256) not NULL,
   status INTEGER NOT NULL,
   domain VARCHAR(100),
   distinguishedname VARCHAR(4000) default NULL,
   constraint pk_ad_group_id primary key(id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--           options table
-- ----------------------------------------------------------------------
CREATE SEQUENCE vdc_options_seq INCREMENT BY 1 START WITH 1;
CREATE TABLE vdc_options
(
   option_id INTEGER DEFAULT NEXTVAL('vdc_options_seq') NOT NULL,
   option_name VARCHAR(100)  NOT NULL,
   option_value VARCHAR(4000) NOT NULL,
   version VARCHAR(40) NOT NULL DEFAULT 'general',
   CONSTRAINT PK_vdc_options PRIMARY KEY(option_id)
) WITH OIDS;

CREATE INDEX IX_vdc_options ON vdc_options
(option_name);


-- ----------------------------------------------------------------------
--           vds_statistics table
-- ----------------------------------------------------------------------
CREATE TABLE vds_statistics
(
   vds_id UUID NOT NULL,
   cpu_idle DECIMAL(18,0)  DEFAULT 0,
   cpu_load DECIMAL(18,0)  DEFAULT 0,
   cpu_sys DECIMAL(18,0)  DEFAULT 0,
   cpu_user DECIMAL(18,0)  DEFAULT 0,
   usage_mem_percent INTEGER  DEFAULT 0,
   usage_cpu_percent INTEGER  DEFAULT 0,
   usage_network_percent INTEGER,
   mem_available BIGINT,
   mem_shared BIGINT,
   swap_free BIGINT,
   swap_total BIGINT,
   ksm_cpu_percent INTEGER  DEFAULT 0,
   ksm_pages BIGINT,
   ksm_state BOOLEAN,

   CONSTRAINT PK_vds_statistics PRIMARY KEY(vds_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--           vm_statistics table
-- ----------------------------------------------------------------------
CREATE TABLE vm_statistics
(
   vm_guid UUID NOT NULL,
   cpu_user DECIMAL(18,0)  DEFAULT 0,
   cpu_sys DECIMAL(18,0)  DEFAULT 0,
   elapsed_time DECIMAL(18,0)  DEFAULT 0,
   usage_network_percent INTEGER  DEFAULT 0,
   usage_mem_percent INTEGER  DEFAULT 0,
   usage_cpu_percent INTEGER  DEFAULT 0,
   disks_usage text,
   CONSTRAINT PK_vm_statistics PRIMARY KEY(vm_guid)
) WITH OIDS;



-- ----------------------------------------------------------------------
--           stateless_vm_image_map table
-- ----------------------------------------------------------------------
CREATE TABLE stateless_vm_image_map
(
   vm_guid UUID not null,
   image_guid UUID not NULL,
   internal_drive_mapping VARCHAR(50),
   CONSTRAINT PK_stateless_vm_image_map PRIMARY KEY(image_guid),
   CONSTRAINT FK_stateless_vm_static
   FOREIGN KEY(vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE
) WITH OIDS;


-- ----------------------------------------------------------------------
--           user_sessions table
-- ----------------------------------------------------------------------


CREATE TABLE user_sessions
(
   session_id CHAR(32) NOT NULL,
   user_id UUID NOT NULL,
   os CHAR(10),
   browser CHAR(10),
   client_type CHAR(10),
   login_time TIMESTAMP WITH TIME ZONE  default LOCALTIMESTAMP,
   constraint pk_user_sessions primary key(user_id,session_id)
) WITH OIDS;



-- ----------------------------------------------------------------------
--           vdc_db_log table
-- ----------------------------------------------------------------------

CREATE SEQUENCE vdc_db_log_seq INCREMENT BY 1 START WITH 1;
CREATE TABLE vdc_db_log
(
   error_id BIGINT DEFAULT NEXTVAL('vdc_db_log_seq') NOT NULL,
   occured_at TIMESTAMP WITH TIME ZONE  NOT NULL default LOCALTIMESTAMP,
   error_code VARCHAR(16) NOT NULL,
   error_message VARCHAR(2048),
   error_proc VARCHAR(126),
   error_line INTEGER,
   constraint pk_vdc_db_log primary key(error_id)
) WITH OIDS;



-- ----------------------------------------------------------------------
--           multi level administration tables
-- ----------------------------------------------------------------------
CREATE TABLE permissions
(
   id UUID NOT NULL,
   role_id UUID NOT NULL,
   ad_element_id UUID   NOT NULL,
   object_id UUID NOT NULL,
   object_type_id INTEGER NOT NULL,
   constraint pk_permissions_id primary key(id)
) WITH OIDS;



CREATE TABLE roles
(
   id UUID not null,
   name VARCHAR(126)  NOT NULL,
   description VARCHAR(4000),
   is_readonly BOOLEAN not null,
   role_type INTEGER not null,
   constraint pk_roles_id primary key(id)
) WITH OIDS;


CREATE TABLE roles_groups
(
   role_id UUID not null,
   action_group_id INTEGER not null,
   constraint pk_roles_groups primary key(role_id,action_group_id)
) WITH OIDS;

ALTER TABLE roles_groups ADD CONSTRAINT fk_roles_groups_action_id FOREIGN KEY(role_id) REFERENCES roles(id) ON DELETE CASCADE;


CREATE TABLE roles_relations
(
   role_id UUID not null,
   role_container_id UUID not null,
   constraint pk_roles_relations primary key(role_id,role_container_id)
) WITH OIDS;


ALTER TABLE roles_relations ADD CONSTRAINT fk_roles_relations_role_id FOREIGN KEY(role_id) REFERENCES roles(id);

ALTER TABLE roles_relations ADD CONSTRAINT fk_roles_relations_container_id FOREIGN KEY(role_container_id) REFERENCES roles(id);


---StoragePool handling
CREATE TABLE storage_pool
(
   id UUID not null,
   name VARCHAR(40) NOT NULL,
   description VARCHAR(4000) NOT NULL,
   storage_pool_type INTEGER NOT NULL,
   storage_pool_format_type varchar(50),
   status INTEGER NOT NULL,
   master_domain_version INTEGER NOT NULL,
   spm_vds_id UUID,
   compatibility_version VARCHAR(40) NOT NULL DEFAULT '2.2',
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   constraint pk_storage_pool primary key(id)
) WITH OIDS;

CREATE TABLE storage_domain_static
(
   id UUID not null,
   storage VARCHAR(250) NOT NULL,
   storage_name VARCHAR(250) NOT NULL,
   storage_domain_type INTEGER NOT NULL,
   storage_type INTEGER NOT NULL,
   storage_domain_format_type varchar(50) NOT NULL default '0',
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   constraint pk_storage primary key(id)
) WITH OIDS;

CREATE TABLE storage_domain_dynamic
(
   id UUID NOT NULL,
   available_disk_size INTEGER,
   used_disk_size INTEGER,
   CONSTRAINT PK_storage_domain_dynamic PRIMARY KEY(id)
) WITH OIDS;

ALTER TABLE storage_domain_dynamic  ADD  CONSTRAINT FK_storage_domain_dynamic_storage_domain_static FOREIGN KEY(id)
REFERENCES storage_domain_static(id);


CREATE TABLE storage_server_connections
(
   id VARCHAR(50)  NOT NULL,
	-- use collate in order to support case sensitive queries
   connection VARCHAR(250)  NOT NULL,
   user_name VARCHAR(50),
   password VARCHAR(50),
   iqn VARCHAR(128),
   port VARCHAR(50),
   portal VARCHAR(50),
   storage_type INTEGER NOT NULL,
   CONSTRAINT PK_storage_server PRIMARY KEY(id)
) WITH OIDS;

CREATE TABLE storage_pool_iso_map
(
   storage_id UUID NOT NULL,
   storage_pool_id UUID NOT NULL,
   status INTEGER,
   owner INTEGER,
   CONSTRAINT PK_storage_domain_pool_map PRIMARY KEY(storage_id,storage_pool_id)
) WITH OIDS;


ALTER TABLE storage_pool_iso_map  ADD  CONSTRAINT FK_storage_domain_pool_map_storage_pool FOREIGN KEY(storage_pool_id)
REFERENCES storage_pool(id) ON DELETE CASCADE;

ALTER TABLE storage_pool_iso_map  ADD  CONSTRAINT FK_storage_domain_pool_map_storage_domain_static FOREIGN KEY(storage_id)
REFERENCES storage_domain_static(id) ON DELETE CASCADE;

CREATE TABLE LUNs
(
   phisical_volume_id VARCHAR(50),
   LUN_id VARCHAR(50) NOT NULL,
   volume_group_id VARCHAR(50),
   serial VARCHAR(4000),
   lun_mapping INTEGER,
   vendor_id VARCHAR(50),
   product_id VARCHAR(50),
   device_size INTEGER default 0,
   CONSTRAINT PK_LUNs PRIMARY KEY(LUN_id)
) WITH OIDS;


CREATE TABLE LUN_storage_server_connection_map
(
   LUN_id VARCHAR(50) NOT NULL,
   storage_server_connection VARCHAR(50) NOT NULL,
   CONSTRAINT PK_LUN_storage_server_connection_map PRIMARY KEY(LUN_id,storage_server_connection)
) WITH OIDS;


ALTER TABLE LUN_storage_server_connection_map  ADD  CONSTRAINT FK_LUN_storage_server_connection_map_LUNs FOREIGN KEY(LUN_id)
REFERENCES LUNs(LUN_id) ON DELETE CASCADE;


ALTER TABLE LUN_storage_server_connection_map   ADD  CONSTRAINT FK_LUN_storage_server_connection_map_storage_server_connections FOREIGN KEY(storage_server_connection)
REFERENCES storage_server_connections(id) ON DELETE CASCADE;


CREATE TABLE async_tasks
(
   task_id UUID NOT NULL,
   action_type INTEGER NOT NULL,
   status INTEGER NOT NULL,
   result INTEGER NOT NULL,
   action_parameters BYTEA,
   CONSTRAINT PK_async_tasks PRIMARY KEY(task_id)
) WITH OIDS;

-- ----------------------------------------------------------------------
-- ----------------------------------------------------------------------
                             -- NETWORS --
-- ----------------------------------------------------------------------
-- ----------------------------------------------------------------------
-- ----------------------------------------------------------------------
--           vm_interface table
-- ----------------------------------------------------------------------
CREATE TABLE vm_interface
(
   id UUID NOT NULL,
   network_name VARCHAR(50),
   vm_guid UUID,
   vmt_guid UUID,
   mac_addr VARCHAR(20),
   name VARCHAR(50)  NOT NULL,
   speed INTEGER,
   type INTEGER  DEFAULT 0,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   CONSTRAINT PK_vm_interface PRIMARY KEY(id)
) WITH OIDS;


-- add non clustered index on [vm_guid],[vmt_guid]
CREATE INDEX IDX_vm_interface_vm_vmt_guid ON vm_interface
(vm_guid,
vmt_guid);



-- ----------------------------------------------------------------------
--           vm_interface_statistics table
-- ----------------------------------------------------------------------
CREATE TABLE vm_interface_statistics
(
   id UUID NOT NULL,
   vm_id UUID,
   rx_rate DECIMAL(18,0),
   tx_rate DECIMAL(18,0),
   rx_drop DECIMAL(18,0),
   tx_drop DECIMAL(18,0),
   iface_status INTEGER,
   CONSTRAINT PK_vm_interface_statistics PRIMARY KEY(id)
) WITH OIDS;


ALTER TABLE vm_interface_statistics ADD CONSTRAINT fk_vm_interface_statistics_vm_static
FOREIGN KEY(vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;



-- ----------------------------------------------------------------------
--           vds_interface_statistics table
-- ----------------------------------------------------------------------
CREATE TABLE vds_interface_statistics
(
   id UUID NOT NULL,
   vds_id UUID,
   rx_rate DECIMAL(18,0),
   tx_rate DECIMAL(18,0),
   rx_drop DECIMAL(18,0),
   tx_drop DECIMAL(18,0),
   iface_status INTEGER,
   CONSTRAINT PK_vds_interface_statistics PRIMARY KEY(id)
) WITH OIDS;


ALTER TABLE vds_interface_statistics ADD CONSTRAINT fk_vds_interface_statistics_vds_static
FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


-- ----------------------------------------------------------------------
--           vds_interface table
-- ----------------------------------------------------------------------
CREATE TABLE vds_interface
(
   id UUID NOT NULL,
   name VARCHAR(50)  NOT NULL,
   network_name VARCHAR(50),
   vds_id UUID,
   mac_addr VARCHAR(20),
   is_bond BOOLEAN  DEFAULT false,
   bond_name VARCHAR(50),
   bond_type INTEGER,
   bond_opts VARCHAR(4000),
   vlan_id INTEGER,
   speed INTEGER,
   addr VARCHAR(20),
   subnet VARCHAR(20),
   gateway VARCHAR(20),
   boot_protocol INTEGER,
   type INTEGER  DEFAULT 0,
   _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   _update_date TIMESTAMP WITH TIME ZONE default NULL,
   CONSTRAINT PK_vds_interface PRIMARY KEY(id)
) WITH OIDS;



-- add non clustered index on [vds_id]
CREATE INDEX IDX_vds_interface_vds_id ON vds_interface
(vds_id);




-- ----------------------------------------------------------------------
--           network table
-- ----------------------------------------------------------------------
CREATE TABLE network
(
   id UUID NOT NULL,
   name VARCHAR(50)  NOT NULL,
   description VARCHAR(4000),
   type INTEGER,
   addr VARCHAR(50),
   subnet VARCHAR(20),
   gateway VARCHAR(20),
   vlan_id INTEGER,
   stp BOOLEAN NOT NULL DEFAULT false,
   storage_pool_id UUID,
   CONSTRAINT PK_network PRIMARY KEY(id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--           network_cluster table
-- ----------------------------------------------------------------------
CREATE TABLE network_cluster
(
   network_id UUID NOT NULL,
   cluster_id UUID NOT NULL,
   status INTEGER NOT NULL DEFAULT 0,
   is_display BOOLEAN NOT NULL DEFAULT false,
   CONSTRAINT PK_network_cluster PRIMARY KEY(network_id,cluster_id)
) WITH OIDS;


-- -----------------------------------------------------------------------
-- Event Notification
-- -----------------------------------------------------------------------

CREATE TABLE event_map
(
   event_up_name VARCHAR(100) NOT NULL,
   event_down_name VARCHAR(100) NOT NULL,
   CONSTRAINT PK_EVENT_MAP PRIMARY KEY(event_up_name)
) WITH OIDS;



CREATE TABLE event_notification_methods
(
   method_id INTEGER NOT NULL,
   method_type CHAR(10)  NOT NULL,
   CONSTRAINT PK_EVENT_NOTIFICATION_METHODS PRIMARY KEY(method_id)
) WITH OIDS;



CREATE TABLE event_subscriber
(
   subscriber_id UUID NOT NULL,
   event_up_name VARCHAR(100) NOT NULL,
   method_id INTEGER NOT NULL,
   method_address VARCHAR(255),
   tag_name VARCHAR(50)  NOT NULL default '',
   CONSTRAINT PK_EVENT_SUBSCRIBER PRIMARY KEY(subscriber_id,event_up_name,method_id,tag_name)
) WITH OIDS;



CREATE TABLE event_notification_hist
(
   subscriber_id UUID NOT NULL,
   event_name VARCHAR(100) NOT NULL,
   audit_log_id BIGINT NOT NULL,
   method_type CHAR(10) NOT NULL,
   sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
   status BOOLEAN NOT NULL,
   reason CHAR(255)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- vds_spm_id_map table
-- ----------------------------------------------------------------------
CREATE TABLE vds_spm_id_map
(
   storage_pool_id UUID NOT NULL,
   vds_spm_id INTEGER NOT NULL,
   vds_id UUID NOT NULL,
   CONSTRAINT PK_vds_spm_id_map PRIMARY KEY(storage_pool_id,vds_spm_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Add table "disk_image_dynamic"
-- ----------------------------------------------------------------------
CREATE TABLE disk_image_dynamic
(
   image_id UUID NOT NULL,
   read_rate INTEGER,
   write_rate INTEGER,
   actual_size BIGINT NOT NULL,
   read_latency_seconds DECIMAL(18,9),
   write_latency_seconds DECIMAL(18,9),
   flush_latency_seconds DECIMAL(18,9),
   CONSTRAINT PK_disk_image_dynamic PRIMARY KEY(image_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--  table "repo_file_meta_data"
-- ----------------------------------------------------------------------
CREATE TABLE repo_file_meta_data
(
   repo_domain_id UUID NOT NULL,
   repo_file_name VARCHAR(256) NOT NULL,
   size bigint default 0,
   date_created TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
   last_refreshed BIGINT default 0,
   file_type INTEGER default 0,
   CONSTRAINT PK_repo_file_meta_data PRIMARY KEY(repo_domain_id,repo_file_name)
) WITH OIDS;


-- ----------------------------------------------------------------------
--  table "image_group_storage_domain_map"
-- ----------------------------------------------------------------------
CREATE TABLE image_group_storage_domain_map
(
   image_group_id UUID NOT NULL,
   storage_domain_id UUID NOT NULL,
   CONSTRAINT PK_image_group_storage_domain_map PRIMARY KEY(image_group_id,storage_domain_id)
) WITH OIDS;


-- ----------------------------------------------------------------------
--  table action_version_map
-- ----------------------------------------------------------------------
CREATE TABLE action_version_map
(
   action_type INTEGER NOT NULL,
   cluster_minimal_version VARCHAR(40) NOT NULL,
   storage_pool_minimal_version VARCHAR(40) NOT NULL,
   CONSTRAINT PK_action_version_map PRIMARY KEY(action_type)
) WITH OIDS;

-- ----------------------------------------------------------------------
--  table dwh_history_timekeeping
-- ----------------------------------------------------------------------

CREATE TABLE dwh_history_timekeeping
(
var_name VARCHAR(50) NOT NULL,
var_value VARCHAR(255),
var_datetime TIMESTAMP WITH TIME ZONE
) WITH OIDS;

-- ----------------------------------------------------------------------
--  table business_entity_snapshot
-- ----------------------------------------------------------------------


CREATE TABLE business_entity_snapshot
(
  id uuid NOT NULL,
  command_id uuid NOT NULL,
  command_type character varying(256) NOT NULL,
  entity_id character varying(128),
  entity_type character varying(128),
  entity_snapshot text,
  snapshot_class character varying(128),
  snapshot_type INTEGER,
  insertion_order INTEGER,
  CONSTRAINT PK_id PRIMARY KEY (id),
  CONSTRAINT UQ_command_id_entity_id UNIQUE (command_id, entity_id, entity_type, snapshot_type)
)
WITH OIDS;

CREATE SEQUENCE schema_version_seq INCREMENT BY 1 START WITH 1;
CREATE TABLE schema_version
(
    id INTEGER DEFAULT NEXTVAL('schema_version_seq') NOT NULL,
    "version" varchar(10) NOT NULL,
    script varchar(255) NOT NULL,
    checksum varchar(128),
    installed_by varchar(30) NOT NULL,
    started_at timestamp  DEFAULT now(),
    ended_at timestamp ,
    state character varying(15) NOT NULL,
    "current" boolean NOT NULL,
    "comment" text NULL default '',
    CONSTRAINT schema_version_primary_key PRIMARY KEY (id)
) WITH OIDS;


-- ----------------------------------------------------------------------
-- Foreign key constraints
-- ----------------------------------------------------------------------
ALTER TABLE tags_user_group_map ADD CONSTRAINT tags_user_group_map_tag
FOREIGN KEY(tag_id) REFERENCES tags(tag_id);

ALTER TABLE tags_user_group_map ADD CONSTRAINT tags_user_map_user_group
FOREIGN KEY(group_id) REFERENCES ad_groups(id);

ALTER TABLE image_vm_pool_map ADD CONSTRAINT vm_pool_map_image
FOREIGN KEY(vm_guid) REFERENCES vm_pool_map(vm_guid) ON DELETE CASCADE;


ALTER TABLE images ADD CONSTRAINT image_templates_images
FOREIGN KEY(it_guid) REFERENCES image_templates(it_guid);




ALTER TABLE vm_templates ADD CONSTRAINT vds_groups_vm_templates
FOREIGN KEY(vds_group_id) REFERENCES vds_groups(vds_group_id);


ALTER TABLE vm_static ADD CONSTRAINT vm_templates_vm_static
FOREIGN KEY(vmt_guid) REFERENCES vm_templates(vmt_guid);


ALTER TABLE vm_static ADD CONSTRAINT vds_groups_vm_static
FOREIGN KEY(vds_group_id) REFERENCES vds_groups(vds_group_id);


ALTER TABLE vds_static ADD CONSTRAINT vds_groups_vds_static
FOREIGN KEY(vds_group_id) REFERENCES vds_groups(vds_group_id);


ALTER TABLE vm_template_image_map ADD CONSTRAINT image_templates_vm_template_image_map
FOREIGN KEY(it_guid) REFERENCES image_templates(it_guid);


ALTER TABLE image_vm_map ADD CONSTRAINT FK_image_vm_map_vm_static
FOREIGN KEY(vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


ALTER TABLE vm_template_image_map ADD CONSTRAINT vm_templates_vm_template_image_map
FOREIGN KEY(vmt_guid) REFERENCES vm_templates(vmt_guid);


ALTER TABLE vds_dynamic ADD CONSTRAINT vds_static_vds_dynamic
FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id);


ALTER TABLE vm_dynamic ADD CONSTRAINT vm_static_vm_dynamic
FOREIGN KEY(vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


ALTER TABLE vm_dynamic ADD CONSTRAINT vds_static_vm_dynamic_r
FOREIGN KEY(run_on_vds) REFERENCES vds_static(vds_id);


ALTER TABLE vm_dynamic ADD CONSTRAINT vds_static_vm_dynamic_m
FOREIGN KEY(migrating_to_vds) REFERENCES vds_static(vds_id);


ALTER TABLE vm_pool_map ADD CONSTRAINT vm_pools_vm
FOREIGN KEY(vm_pool_id) REFERENCES vm_pools(vm_pool_id);


ALTER TABLE vm_pool_map ADD CONSTRAINT vm_guid_pools
FOREIGN KEY(vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


ALTER TABLE vds_statistics ADD CONSTRAINT vds_static_vds_statistics
FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id);


ALTER TABLE vm_statistics ADD CONSTRAINT vm_static_vm_statistics
FOREIGN KEY(vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


ALTER TABLE tags_user_map ADD CONSTRAINT tags_user_map_tag
FOREIGN KEY(tag_id) REFERENCES tags(tag_id);

ALTER TABLE tags_user_map ADD CONSTRAINT tags_user_map_user
FOREIGN KEY(user_id) REFERENCES users(user_id);

ALTER TABLE tags_vds_map ADD CONSTRAINT tags_vds_map_tag
FOREIGN KEY(tag_id) REFERENCES tags(tag_id);

ALTER TABLE tags_vds_map ADD CONSTRAINT tags_vds_map_vds
FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id);

ALTER TABLE tags_vm_map ADD CONSTRAINT tags_vm_map_tag
FOREIGN KEY(tag_id) REFERENCES tags(tag_id);

ALTER TABLE tags_vm_map ADD CONSTRAINT tags_vm_map_vm
FOREIGN KEY(vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;

ALTER TABLE vm_pools ADD CONSTRAINT FK_vds_groups_vm_pools
FOREIGN KEY(vds_group_id) REFERENCES vds_groups(vds_group_id);


ALTER TABLE vds_groups ADD  CONSTRAINT Fk_vds_groups_storage_pool_id FOREIGN KEY(storage_pool_id)
REFERENCES storage_pool(id) ON DELETE SET NULL;


ALTER TABLE images ADD  CONSTRAINT Fk_images_storage_id FOREIGN KEY(storage_id)
REFERENCES storage_domain_static(id);

ALTER TABLE tags_vm_pool_map   ADD  CONSTRAINT fk_tags_vm_pool_map_tag FOREIGN KEY(tag_id)
REFERENCES tags(tag_id) ON DELETE CASCADE;

ALTER TABLE tags_vm_pool_map   ADD  CONSTRAINT fk_tags_vm_pool_map_vm_pool FOREIGN KEY(vm_pool_id)
REFERENCES vm_pools(vm_pool_id) ON DELETE CASCADE;

ALTER TABLE user_sessions ADD CONSTRAINT fk_users
FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE;


ALTER TABLE vm_interface ADD  CONSTRAINT FK_vm_interface_vm_static FOREIGN KEY(vm_guid)
REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


ALTER TABLE vds_interface  ADD  CONSTRAINT FK_vds_interface_vds_interface FOREIGN KEY(vds_id)
REFERENCES vds_static(vds_id)
ON DELETE CASCADE;


ALTER TABLE network_cluster  ADD  CONSTRAINT FK_network_cluster_network FOREIGN KEY(network_id)
REFERENCES network(id) ON DELETE CASCADE;


ALTER TABLE network_cluster  ADD  CONSTRAINT FK_network_cluster_vds_groups FOREIGN KEY(cluster_id)
REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


ALTER TABLE network ADD CONSTRAINT FK_network_storage_pool FOREIGN KEY(storage_pool_id)
REFERENCES storage_pool(id) ON DELETE SET NULL;


-- Event Notification
ALTER TABLE event_subscriber  ADD  CONSTRAINT FK_EVENT_SUBSCRIBER_EVENT_MAP FOREIGN KEY(event_up_name)
REFERENCES event_map(event_up_name)  ON DELETE CASCADE;


ALTER TABLE event_subscriber  ADD  CONSTRAINT FK_EVENT_SUBSCRIBER_EVENT_NOTIFICATION_METHODS FOREIGN KEY(method_id)
REFERENCES event_notification_methods(method_id)  ON DELETE CASCADE;


ALTER TABLE event_subscriber  ADD  CONSTRAINT FK_EVENT_SUBSCRIBER_users FOREIGN KEY(subscriber_id)
REFERENCES users(user_id)  ON DELETE CASCADE;


ALTER TABLE event_notification_hist  ADD  CONSTRAINT FK_EVENT_NOTIFICATION_HIST_audit_log FOREIGN KEY(audit_log_id)
REFERENCES audit_log(audit_log_id)  ON DELETE CASCADE;


ALTER TABLE event_notification_hist  ADD  CONSTRAINT FK_EVENT_NOTIFICATION_users FOREIGN KEY(subscriber_id)
REFERENCES users(user_id)  ON DELETE CASCADE;


-- [vds_spm_id_map]
ALTER TABLE vds_spm_id_map  ADD  CONSTRAINT FK_vds_spm_id_map_storage_pool FOREIGN KEY(storage_pool_id)
REFERENCES storage_pool(id)
ON DELETE CASCADE;


ALTER TABLE vds_spm_id_map  ADD  CONSTRAINT FK_vds_spm_id_map_vds_id FOREIGN KEY(vds_id)
REFERENCES vds_static(vds_id) ON DELETE CASCADE;


-- [image_group_storage_domain_map]
ALTER TABLE image_group_storage_domain_map ADD CONSTRAINT FK_image_group_storage_domain_map_storage_domain_static FOREIGN KEY(storage_domain_id)
REFERENCES storage_domain_static(id) ON DELETE CASCADE;


-- [repo_file_meta_data]
ALTER TABLE repo_file_meta_data ADD CONSTRAINT FK_repo_file_meta_data_storage_domain_static
FOREIGN KEY(repo_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


ALTER TABLE disk_image_dynamic ADD CONSTRAINT FK_disk_image_dynamic_images
FOREIGN KEY(image_id) REFERENCES images(image_guid) ON DELETE CASCADE;



ALTER TABLE permissions ADD  CONSTRAINT fk_permissions_roles FOREIGN KEY(role_id)
REFERENCES roles(id) ON DELETE CASCADE;

CREATE INDEX IDX_permissions_ad_element_id
ON permissions
(ad_element_id);


CREATE INDEX IDX_permissions_object_id
ON permissions
(object_id);


CREATE INDEX IDX_permissions_role_id
ON permissions
(role_id);


CREATE INDEX IDX_repo_file_file_type
ON repo_file_meta_data
(file_type);


CREATE INDEX IDX_roles_groups_action_group_id
ON roles_groups
(action_group_id);


CREATE UNIQUE INDEX IDX_combined_ad_role_object ON permissions
(ad_element_id,
role_id,
object_id);

CREATE INDEX IDX_business_entity_snapshot_command_id  ON business_entity_snapshot(command_id);



