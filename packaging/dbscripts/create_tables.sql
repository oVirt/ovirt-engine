--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6

--
-- Name: all_cluster_usage_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE all_cluster_usage_rs AS (
	quota_cluster_id uuid,
	quota_id uuid,
	cluster_id uuid,
	cluster_name character varying(40),
	virtual_cpu integer,
	virtual_cpu_usage integer,
	mem_size_mb bigint,
	mem_size_mb_usage bigint
);



--
-- Name: all_storage_usage_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE all_storage_usage_rs AS (
	quota_storage_id uuid,
	quota_id uuid,
	storage_id uuid,
	storage_name character varying(250),
	storage_size_gb bigint,
	storage_size_gb_usage double precision
);



--
-- Name: async_tasks_info_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE async_tasks_info_rs AS (
	dc_id uuid,
	dc_name character varying,
	spm_host_id uuid,
	spm_host_name character varying,
	task_count integer
);



--
-- Name: authzentryinfotype; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE authzentryinfotype AS (
	name text,
	namespace character varying(2048),
	authz character varying(255)
);



--
-- Name: booleanresulttype; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE booleanresulttype AS (
	result boolean
);



--
-- Name: cluster_usage_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE cluster_usage_rs AS (
	virtual_cpu_usage integer,
	mem_size_mb_usage bigint
);



--
-- Name: idtexttype; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE idtexttype AS (
	id text
);



--
-- Name: iduuidtype; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE iduuidtype AS (
	id uuid
);



--
-- Name: pm_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE pm_rs AS (
	vds_id uuid,
	ip character varying(255),
	pm_type character varying(255),
	pm_user character varying(50),
	pm_password text,
	pm_port integer,
	pm_options character varying(4000),
	pm_secondary_ip character varying(255),
	pm_secondary_type character varying(255),
	pm_secondary_user character varying(50),
	pm_secondary_password text,
	pm_secondary_port integer,
	pm_secondary_options character varying(4000),
	pm_secondary_concurrent boolean
);



--
-- Name: user_permissions; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE user_permissions AS (
	permission_id uuid,
	role_id uuid,
	user_id uuid
);





--
-- Name: ad_groups; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE ad_groups (
    id uuid NOT NULL,
    name character varying(256) NOT NULL,
    domain character varying(100),
    distinguishedname character varying(4000) DEFAULT NULL::character varying,
    external_id text NOT NULL,
    namespace character varying(2048) DEFAULT '*'::character varying
);



--
-- Name: affinity_group_members; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE affinity_group_members (
    affinity_group_id uuid NOT NULL,
    vm_id uuid NOT NULL
);



--
-- Name: affinity_groups; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE affinity_groups (
    id uuid NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(4000),
    cluster_id uuid NOT NULL,
    positive boolean DEFAULT true NOT NULL,
    enforcing boolean DEFAULT true NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: async_tasks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE async_tasks (
    task_id uuid NOT NULL,
    action_type integer NOT NULL,
    status integer NOT NULL,
    result integer NOT NULL,
    step_id uuid,
    command_id uuid NOT NULL,
    started_at timestamp with time zone,
    storage_pool_id uuid,
    task_type integer DEFAULT 0 NOT NULL,
    vdsm_task_id uuid,
    root_command_id uuid,
    user_id uuid
);



--
-- Name: async_tasks_entities; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE async_tasks_entities (
    async_task_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(128)
);



--
-- Name: audit_log_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE audit_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE audit_log (
    audit_log_id bigint DEFAULT nextval('audit_log_seq'::regclass) NOT NULL,
    user_id uuid,
    user_name character varying(255),
    vm_id uuid,
    vm_name character varying(255),
    vm_template_id uuid,
    vm_template_name character varying(40),
    vds_id uuid,
    vds_name character varying(255),
    log_time timestamp with time zone NOT NULL,
    log_type_name character varying(100) DEFAULT ''::character varying,
    log_type integer NOT NULL,
    severity integer NOT NULL,
    message text NOT NULL,
    processed boolean DEFAULT false NOT NULL,
    storage_pool_id uuid,
    storage_pool_name character varying(40),
    storage_domain_id uuid,
    storage_domain_name character varying(250),
    cluster_id uuid,
    cluster_name character varying(255),
    correlation_id character varying(50),
    job_id uuid,
    quota_id uuid,
    quota_name character varying(60),
    gluster_volume_id uuid,
    gluster_volume_name character varying(1000),
    origin character varying(255) DEFAULT 'oVirt'::character varying,
    custom_event_id integer DEFAULT '-1'::integer,
    event_flood_in_sec integer DEFAULT 30,
    custom_data text DEFAULT ''::text,
    deleted boolean DEFAULT false,
    call_stack text DEFAULT ''::text,
    brick_id uuid,
    brick_path text DEFAULT ''::text
);



--
-- Name: base_disks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE base_disks (
    disk_id uuid NOT NULL,
    wipe_after_delete boolean DEFAULT false NOT NULL,
    propagate_errors character varying(32) DEFAULT 'Off'::character varying NOT NULL,
    disk_alias character varying(255),
    disk_description character varying(500),
    shareable boolean DEFAULT false,
    sgio smallint,
    alignment smallint DEFAULT 0 NOT NULL,
    last_alignment_scan timestamp with time zone,
    disk_storage_type smallint,
    cinder_volume_type character varying(255)
);



--
-- Name: bookmarks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE bookmarks (
    bookmark_id uuid NOT NULL,
    bookmark_name character varying(40),
    bookmark_value character varying(300) NOT NULL
);



--
-- Name: business_entity_snapshot; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE business_entity_snapshot (
    id uuid NOT NULL,
    command_id uuid NOT NULL,
    command_type character varying(256) NOT NULL,
    entity_id character varying(128),
    entity_type character varying(128),
    entity_snapshot text,
    snapshot_class character varying(128),
    snapshot_type integer,
    insertion_order integer,
    started_at timestamp with time zone DEFAULT now()
);



--
-- Name: cluster; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE cluster (
    cluster_id uuid NOT NULL,
    name character varying(40) NOT NULL,
    description character varying(4000),
    cpu_name character varying(255),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    storage_pool_id uuid,
    max_vds_memory_over_commit integer DEFAULT 100 NOT NULL,
    compatibility_version character varying(40) DEFAULT '2.2'::character varying NOT NULL,
    transparent_hugepages boolean DEFAULT false NOT NULL,
    migrate_on_error integer DEFAULT 1 NOT NULL,
    virt_service boolean DEFAULT true NOT NULL,
    gluster_service boolean DEFAULT false NOT NULL,
    count_threads_as_cores boolean DEFAULT false NOT NULL,
    emulated_machine character varying(40),
    trusted_service boolean DEFAULT false NOT NULL,
    tunnel_migration boolean DEFAULT false NOT NULL,
    cluster_policy_id uuid,
    cluster_policy_custom_properties text,
    enable_balloon boolean DEFAULT false NOT NULL,
    free_text_comment text,
    detect_emulated_machine boolean DEFAULT false,
    architecture integer DEFAULT 0 NOT NULL,
    optimization_type smallint DEFAULT 0,
    spice_proxy character varying(255),
    ha_reservation boolean DEFAULT false NOT NULL,
    enable_ksm boolean DEFAULT true NOT NULL,
    serial_number_policy smallint,
    custom_serial_number character varying(255) DEFAULT NULL::character varying,
    optional_reason boolean DEFAULT false NOT NULL,
    required_rng_sources character varying(255),
    skip_fencing_if_sd_active boolean DEFAULT false,
    skip_fencing_if_connectivity_broken boolean DEFAULT false,
    hosts_with_broken_connectivity_threshold smallint DEFAULT 50,
    fencing_enabled boolean DEFAULT true,
    is_auto_converge boolean,
    is_migrate_compressed boolean,
    maintenance_reason_required boolean DEFAULT false NOT NULL,
    gluster_tuned_profile character varying(50),
    gluster_cli_based_snapshot_scheduled boolean DEFAULT true NOT NULL,
    ksm_merge_across_nodes boolean DEFAULT true,
    migration_bandwidth_limit_type character varying(16) DEFAULT 'AUTO'::character varying NOT NULL,
    custom_migration_bandwidth_limit integer,
    migration_policy_id uuid,
    CONSTRAINT check_cluster_custom_migration_bandwidth_set CHECK ((((migration_bandwidth_limit_type)::text <> 'CUSTOM'::text) OR (custom_migration_bandwidth_limit IS NOT NULL))),
    CONSTRAINT check_cluster_migration_bandwidth_limit_type_enum CHECK ((((migration_bandwidth_limit_type)::text = 'AUTO'::text) OR ((migration_bandwidth_limit_type)::text = 'VDSM_CONFIG'::text) OR ((migration_bandwidth_limit_type)::text = 'CUSTOM'::text)))
);



--
-- Name: cluster_features; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE cluster_features (
    feature_id uuid NOT NULL,
    feature_name character varying(256) NOT NULL,
    version character varying(40),
    category integer NOT NULL,
    description text
);



--
-- Name: cluster_policies; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE cluster_policies (
    id uuid NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(4000),
    is_locked boolean NOT NULL,
    is_default boolean NOT NULL,
    custom_properties text
);



--
-- Name: cluster_policy_units; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE cluster_policy_units (
    cluster_policy_id uuid,
    policy_unit_id uuid,
    filter_sequence integer DEFAULT 0,
    factor integer DEFAULT 1
);



--
-- Name: command_assoc_entities; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE command_assoc_entities (
    command_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(128)
);



--
-- Name: command_entities; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE command_entities (
    command_id uuid NOT NULL,
    command_type integer NOT NULL,
    root_command_id uuid,
    command_parameters text,
    command_params_class character varying(256),
    created_at timestamp with time zone,
    status character varying(20) DEFAULT NULL::character varying,
    callback_enabled boolean DEFAULT false,
    callback_notified boolean DEFAULT false,
    return_value text,
    return_value_class character varying(256),
    job_id uuid,
    step_id uuid,
    executed boolean DEFAULT false,
    user_id uuid,
    parent_command_id uuid,
    data text,
    engine_session_seq_id bigint
);



--
-- Name: cpu_profiles; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE cpu_profiles (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    cluster_id uuid NOT NULL,
    qos_id uuid,
    description text,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: custom_actions_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE custom_actions_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: custom_actions; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE custom_actions (
    action_id integer DEFAULT nextval('custom_actions_seq'::regclass) NOT NULL,
    action_name character varying(50) NOT NULL,
    path character varying(300) NOT NULL,
    tab integer NOT NULL,
    description character varying(4000)
);



--
-- Name: disk_image_dynamic; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE disk_image_dynamic (
    image_id uuid NOT NULL,
    read_rate integer,
    write_rate integer,
    actual_size bigint NOT NULL,
    read_latency_seconds numeric(18,9),
    write_latency_seconds numeric(18,9),
    flush_latency_seconds numeric(18,9),
    _update_date timestamp with time zone
);



--
-- Name: disk_lun_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE disk_lun_map (
    disk_id uuid NOT NULL,
    lun_id character varying NOT NULL
);



--
-- Name: disk_profiles; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE disk_profiles (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    storage_domain_id uuid NOT NULL,
    qos_id uuid,
    description text,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: disk_vm_element; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE disk_vm_element (
    disk_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    is_boot boolean DEFAULT false NOT NULL,
    disk_interface character varying(32) NOT NULL
);



--
-- Name: dwh_history_timekeeping; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE dwh_history_timekeeping (
    var_name character varying(50) NOT NULL,
    var_value character varying(255),
    var_datetime timestamp with time zone
);



--
-- Name: dwh_osinfo; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE dwh_osinfo (
    os_id integer NOT NULL,
    os_name character varying(255)
);



--
-- Name: engine_backup_log; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE engine_backup_log (
    scope character varying(64) NOT NULL,
    done_at timestamp with time zone NOT NULL,
    is_passed boolean,
    output_message text,
    fqdn character varying(255),
    log_path text
);



--
-- Name: engine_session_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE engine_session_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: engine_sessions; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE engine_sessions (
    id bigint DEFAULT nextval('engine_session_seq'::regclass) NOT NULL,
    engine_session_id text NOT NULL,
    user_id uuid NOT NULL,
    user_name character varying(255) NOT NULL,
    group_ids text,
    role_ids text,
    source_ip character varying(50),
    authz_name character varying(255) DEFAULT ''::character varying NOT NULL
);



--
-- Name: event_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE event_map (
    event_up_name character varying(100) NOT NULL,
    event_down_name character varying(100) NOT NULL
);



--
-- Name: event_notification_hist; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE event_notification_hist (
    event_name character varying(100) NOT NULL,
    audit_log_id bigint NOT NULL,
    method_type character(10) NOT NULL,
    sent_at timestamp with time zone NOT NULL,
    status boolean NOT NULL,
    reason character(255)
);



--
-- Name: event_subscriber; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE event_subscriber (
    subscriber_id uuid NOT NULL,
    event_up_name character varying(100) NOT NULL,
    method_address character varying(255),
    tag_name character varying(50) DEFAULT ''::character varying NOT NULL,
    notification_method character varying(32),
    CONSTRAINT event_subscriber_method_check CHECK (((notification_method)::text = ANY (ARRAY[('smtp'::character varying)::text, ('snmp'::character varying)::text])))
);



--
-- Name: external_variable; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE external_variable (
    var_name character varying(100) NOT NULL,
    var_value character varying(4000),
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL
);



--
-- Name: fence_agents; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE fence_agents (
    id uuid NOT NULL,
    vds_id uuid NOT NULL,
    agent_order integer NOT NULL,
    ip character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    agent_user character varying(255) NOT NULL,
    agent_password text NOT NULL,
    port integer,
    options text DEFAULT ''::character varying NOT NULL,
    encrypt_options boolean DEFAULT false NOT NULL
);



--
-- Name: gluster_cluster_services; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_cluster_services (
    cluster_id uuid NOT NULL,
    service_type character varying(100) NOT NULL,
    status character varying(32) NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_config_master; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_config_master (
    config_key character varying(50) NOT NULL,
    config_description character varying(300),
    minimum_supported_cluster character varying(50),
    config_possible_values character varying(50),
    config_feature character varying(50)
);



--
-- Name: gluster_georep_config; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_georep_config (
    session_id uuid NOT NULL,
    config_key character varying(50) NOT NULL,
    config_value text,
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL
);



--
-- Name: gluster_georep_session; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_georep_session (
    session_id uuid NOT NULL,
    master_volume_id uuid NOT NULL,
    session_key character varying(150) NOT NULL,
    slave_host_uuid uuid,
    slave_host_name character varying(50),
    slave_volume_id uuid,
    slave_volume_name character varying(50),
    status character varying,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    user_name character varying(255)
);



--
-- Name: gluster_georep_session_details; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_georep_session_details (
    session_id uuid NOT NULL,
    master_brick_id uuid NOT NULL,
    slave_host_uuid uuid,
    slave_host_name character varying(50) NOT NULL,
    status character varying(20),
    checkpoint_status character varying(20),
    crawl_status character varying(20),
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    data_pending bigint,
    entry_pending bigint,
    meta_pending bigint,
    failures bigint,
    last_synced_at timestamp with time zone,
    checkpoint_time timestamp with time zone,
    checkpoint_completed_time timestamp with time zone,
    is_checkpoint_completed boolean DEFAULT false
);



--
-- Name: gluster_hooks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_hooks (
    id uuid NOT NULL,
    cluster_id uuid NOT NULL,
    gluster_command character varying(128) NOT NULL,
    stage character varying(50) NOT NULL,
    name character varying(256) NOT NULL,
    hook_status character varying(50),
    content_type character varying(50),
    checksum character varying(256),
    content text,
    conflict_status integer DEFAULT 0 NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_server; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_server (
    server_id uuid NOT NULL,
    gluster_server_uuid uuid NOT NULL,
    known_addresses character varying(250)
);



--
-- Name: gluster_server_hooks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_server_hooks (
    hook_id uuid NOT NULL,
    server_id uuid NOT NULL,
    hook_status character varying(50),
    content_type character varying(50),
    checksum character varying(256),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_server_services; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_server_services (
    id uuid NOT NULL,
    server_id uuid NOT NULL,
    service_id uuid NOT NULL,
    pid integer,
    status character varying(32) NOT NULL,
    message character varying(1000),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_service_types; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_service_types (
    service_type character varying(100) NOT NULL
);



--
-- Name: gluster_services; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_services (
    id uuid NOT NULL,
    service_type character varying(100) NOT NULL,
    service_name character varying(100) NOT NULL
);



--
-- Name: gluster_volume_access_protocols; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_access_protocols (
    volume_id uuid NOT NULL,
    access_protocol character varying(32) NOT NULL
);



--
-- Name: gluster_volume_brick_details; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_brick_details (
    brick_id uuid NOT NULL,
    total_space bigint,
    used_space bigint,
    free_space bigint,
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL
);



--
-- Name: gluster_volume_bricks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_bricks (
    volume_id uuid NOT NULL,
    server_id uuid NOT NULL,
    brick_dir character varying(4096) NOT NULL,
    status character varying(32) NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone,
    id uuid NOT NULL,
    brick_order integer DEFAULT 0,
    task_id uuid,
    network_id uuid,
    unsynced_entries integer,
    unsynced_entries_history text
);



--
-- Name: gluster_volume_details; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_details (
    volume_id uuid NOT NULL,
    total_space bigint,
    used_space bigint,
    free_space bigint,
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL
);



--
-- Name: gluster_volume_options; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_options (
    volume_id uuid NOT NULL,
    option_key character varying(8192) NOT NULL,
    option_val character varying(8192) NOT NULL,
    id uuid NOT NULL
);



--
-- Name: gluster_volume_snapshot_config; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_snapshot_config (
    cluster_id uuid NOT NULL,
    volume_id uuid,
    param_name character varying(128) NOT NULL,
    param_value character varying(128),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_volume_snapshot_schedules; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_snapshot_schedules (
    volume_id uuid NOT NULL,
    job_id character varying(256) NOT NULL,
    snapshot_name_prefix character varying(128),
    snapshot_description character varying(1024),
    recurrence character varying(128) NOT NULL,
    time_zone character varying(128),
    "interval" integer,
    start_date timestamp with time zone,
    execution_time time without time zone,
    days character varying(256),
    end_by timestamp with time zone,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_volume_snapshots; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_snapshots (
    snapshot_id uuid NOT NULL,
    volume_id uuid NOT NULL,
    snapshot_name character varying(1000) NOT NULL,
    description character varying(1024),
    status character varying(32),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);



--
-- Name: gluster_volume_transport_types; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volume_transport_types (
    volume_id uuid NOT NULL,
    transport_type character varying(32) NOT NULL
);



--
-- Name: gluster_volumes; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE gluster_volumes (
    id uuid NOT NULL,
    cluster_id uuid NOT NULL,
    vol_name character varying(1000) NOT NULL,
    vol_type character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    replica_count integer DEFAULT 0 NOT NULL,
    stripe_count integer DEFAULT 0 NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone,
    task_id uuid,
    snapshot_count integer DEFAULT 0 NOT NULL,
    snapshot_scheduled boolean DEFAULT false,
    disperse_count integer DEFAULT 0 NOT NULL,
    redundancy_count integer DEFAULT 0 NOT NULL
);



--
-- Name: host_device; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE host_device (
    host_id uuid NOT NULL,
    device_name character varying(255) NOT NULL,
    parent_device_name character varying(255) NOT NULL,
    capability character varying(32) NOT NULL,
    iommu_group integer,
    product_name character varying(255),
    product_id character varying(255),
    vendor_name character varying(255),
    vendor_id character varying(255),
    physfn character varying(255),
    total_vfs integer,
    vm_id uuid,
    net_iface_name character varying(50),
    driver character varying(255),
    is_assignable boolean DEFAULT true NOT NULL
);



--
-- Name: host_nic_vfs_config; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE host_nic_vfs_config (
    id uuid NOT NULL,
    nic_id uuid NOT NULL,
    is_all_networks_allowed boolean NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: image_storage_domain_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE image_storage_domain_map (
    image_id uuid NOT NULL,
    storage_domain_id uuid NOT NULL,
    quota_id uuid,
    disk_profile_id uuid
);



--
-- Name: image_transfers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE image_transfers (
    command_id uuid NOT NULL,
    command_type integer NOT NULL,
    phase integer NOT NULL,
    last_updated timestamp with time zone NOT NULL,
    message character varying,
    vds_id uuid,
    disk_id uuid,
    imaged_ticket_id uuid,
    proxy_uri character varying,
    signed_ticket character varying,
    bytes_sent bigint,
    bytes_total bigint
);



--
-- Name: images; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE images (
    image_guid uuid NOT NULL,
    creation_date timestamp with time zone NOT NULL,
    size bigint NOT NULL,
    it_guid uuid NOT NULL,
    parentid uuid,
    imagestatus integer DEFAULT 0,
    lastmodified timestamp with time zone,
    vm_snapshot_id uuid,
    volume_type integer DEFAULT 2 NOT NULL,
    volume_format integer DEFAULT 4 NOT NULL,
    image_group_id uuid,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    active boolean DEFAULT false NOT NULL,
    volume_classification smallint
);



--
-- Name: iscsi_bonds; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE iscsi_bonds (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(4000),
    storage_pool_id uuid NOT NULL
);



--
-- Name: iscsi_bonds_networks_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE iscsi_bonds_networks_map (
    iscsi_bond_id uuid NOT NULL,
    network_id uuid NOT NULL
);



--
-- Name: iscsi_bonds_storage_connections_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE iscsi_bonds_storage_connections_map (
    iscsi_bond_id uuid NOT NULL,
    connection_id character varying(50) NOT NULL
);



--
-- Name: job; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE job (
    job_id uuid NOT NULL,
    action_type character varying(50) NOT NULL,
    description text NOT NULL,
    status character varying(32) NOT NULL,
    owner_id uuid,
    visible boolean DEFAULT true NOT NULL,
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone,
    last_update_time timestamp with time zone,
    correlation_id character varying(50) NOT NULL,
    is_external boolean DEFAULT false,
    is_auto_cleared boolean DEFAULT true,
    engine_session_seq_id bigint
);



--
-- Name: job_subject_entity; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE job_subject_entity (
    job_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(32) NOT NULL
);



--
-- Name: labels; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE labels (
    label_id uuid NOT NULL,
    label_name character varying(50) NOT NULL,
    read_only boolean DEFAULT false NOT NULL
);



--
-- Name: labels_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE labels_map (
    label_id uuid NOT NULL,
    vm_id uuid,
    vds_id uuid
);



--
-- Name: libvirt_secrets; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE libvirt_secrets (
    secret_id uuid NOT NULL,
    secret_value text NOT NULL,
    secret_usage_type integer NOT NULL,
    secret_description text,
    provider_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: lun_storage_server_connection_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE lun_storage_server_connection_map (
    lun_id character varying(255) NOT NULL,
    storage_server_connection character varying(50) NOT NULL
);



--
-- Name: luns; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE luns (
    physical_volume_id character varying(50),
    lun_id character varying(255) NOT NULL,
    volume_group_id character varying(50) NOT NULL,
    serial character varying(4000),
    lun_mapping integer,
    vendor_id character varying(50),
    product_id character varying(50),
    device_size integer DEFAULT 0
);



--
-- Name: mac_pool_ranges; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE mac_pool_ranges (
    mac_pool_id uuid NOT NULL,
    from_mac character varying(17) NOT NULL,
    to_mac character varying(17) NOT NULL
);



--
-- Name: mac_pools; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE mac_pools (
    id uuid NOT NULL,
    name character varying(255),
    description character varying(4000),
    allow_duplicate_mac_addresses boolean DEFAULT false NOT NULL,
    default_pool boolean DEFAULT false NOT NULL
);



--
-- Name: materialized_views; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE materialized_views (
    mv_name name NOT NULL,
    v_name name NOT NULL,
    refresh_rate_in_sec integer,
    last_refresh timestamp with time zone,
    avg_cost_ms integer DEFAULT 0 NOT NULL,
    min_refresh_rate_in_sec integer DEFAULT 0,
    custom boolean DEFAULT false,
    active boolean DEFAULT true
);



--
-- Name: network; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE network (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(4000),
    type integer,
    addr character varying(50),
    subnet character varying(20),
    gateway character varying(20),
    vlan_id integer,
    stp boolean DEFAULT false NOT NULL,
    storage_pool_id uuid,
    mtu integer,
    vm_network boolean DEFAULT true NOT NULL,
    provider_network_provider_id uuid,
    provider_network_external_id text,
    free_text_comment text,
    label text,
    qos_id uuid
);



--
-- Name: network_attachments; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE network_attachments (
    id uuid NOT NULL,
    network_id uuid NOT NULL,
    nic_id uuid NOT NULL,
    boot_protocol character varying(20),
    address character varying(20),
    netmask character varying(20),
    gateway character varying(20),
    custom_properties text,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    ipv6_boot_protocol character varying(20) DEFAULT NULL::character varying,
    ipv6_address character varying(50) DEFAULT NULL::character varying,
    ipv6_prefix integer,
    ipv6_gateway character varying(50) DEFAULT NULL::character varying,
    CONSTRAINT boot_protocol_enum_values CHECK ((((boot_protocol)::text = 'DHCP'::text) OR ((boot_protocol)::text = 'STATIC_IP'::text) OR ((boot_protocol)::text = 'NONE'::text)))
);



--
-- Name: network_cluster; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE network_cluster (
    network_id uuid NOT NULL,
    cluster_id uuid NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    is_display boolean DEFAULT false NOT NULL,
    required boolean DEFAULT true NOT NULL,
    migration boolean DEFAULT false NOT NULL,
    management boolean DEFAULT false NOT NULL,
    is_gluster boolean DEFAULT false NOT NULL
);



--
-- Name: network_filter; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE network_filter (
    filter_id uuid NOT NULL,
    filter_name character varying(50) NOT NULL,
    version character varying(40) NOT NULL
);



--
-- Name: numa_node; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE numa_node (
    numa_node_id uuid NOT NULL,
    vds_id uuid,
    vm_id uuid,
    numa_node_index smallint,
    mem_total bigint,
    cpu_count smallint,
    mem_free bigint,
    usage_mem_percent integer,
    cpu_sys numeric(5,2),
    cpu_user numeric(5,2),
    cpu_idle numeric(5,2),
    usage_cpu_percent integer,
    distance text,
    hugepages text
);



--
-- Name: numa_node_cpu_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE numa_node_cpu_map (
    id uuid NOT NULL,
    numa_node_id uuid NOT NULL,
    cpu_core_id integer
);



--
-- Name: object_column_white_list; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE object_column_white_list (
    object_name character varying(128) NOT NULL,
    column_name character varying(128) NOT NULL
);



--
-- Name: object_column_white_list_sql; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE object_column_white_list_sql (
    object_name character varying(128) NOT NULL,
    sql text NOT NULL
);



--
-- Name: permissions; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE permissions (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    ad_element_id uuid NOT NULL,
    object_id uuid NOT NULL,
    object_type_id integer NOT NULL,
    creation_date bigint DEFAULT date_part('epoch'::text, now())
);



--
-- Name: policy_units; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE policy_units (
    id uuid NOT NULL,
    name character varying(128) NOT NULL,
    is_internal boolean NOT NULL,
    custom_properties_regex text,
    type smallint DEFAULT 0,
    enabled boolean DEFAULT true NOT NULL,
    description text
);



--
-- Name: providers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE providers (
    id uuid NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(4000),
    url character varying(512) NOT NULL,
    provider_type character varying(32) NOT NULL,
    auth_required boolean NOT NULL,
    auth_username character varying(64),
    auth_password text,
    _create_date timestamp with time zone DEFAULT now(),
    _update_date timestamp with time zone,
    custom_properties text,
    tenant_name character varying(128),
    plugin_type character varying(64),
    agent_configuration text,
    auth_url text,
    additional_properties text,
    read_only boolean DEFAULT false NOT NULL
);



--
-- Name: qos; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qos (
    id uuid NOT NULL,
    qos_type smallint NOT NULL,
    name character varying(50),
    description text,
    storage_pool_id uuid,
    max_throughput integer,
    max_read_throughput integer,
    max_write_throughput integer,
    max_iops integer,
    max_read_iops integer,
    max_write_iops integer,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    cpu_limit smallint,
    inbound_average smallint,
    inbound_peak smallint,
    inbound_burst smallint,
    outbound_average smallint,
    outbound_peak smallint,
    outbound_burst smallint,
    out_average_linkshare integer,
    out_average_upperlimit integer,
    out_average_realtime integer
);



--
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);



--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_calendars (
    sched_name character varying(120) NOT NULL,
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);



--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_cron_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);



--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_fired_triggers (
    sched_name character varying(120) NOT NULL,
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_nonconcurrent boolean,
    requests_recovery boolean
);



--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_job_details (
    sched_name character varying(120) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description text,
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_nonconcurrent boolean NOT NULL,
    is_update_data boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);



--
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_locks (
    sched_name character varying(120) NOT NULL,
    lock_name character varying(40) NOT NULL
);



--
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_paused_trigger_grps (
    sched_name character varying(120) NOT NULL,
    trigger_group character varying(200) NOT NULL
);



--
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_scheduler_state (
    sched_name character varying(120) NOT NULL,
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);



--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_simple_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);



--
-- Name: qrtz_simprop_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_simprop_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);



--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE qrtz_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description text,
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);



--
-- Name: quota; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE quota (
    id uuid NOT NULL,
    storage_pool_id uuid NOT NULL,
    quota_name character varying(65) NOT NULL,
    description character varying(250),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    threshold_cluster_percentage integer DEFAULT 80,
    threshold_storage_percentage integer DEFAULT 80,
    grace_cluster_percentage integer DEFAULT 20,
    grace_storage_percentage integer DEFAULT 20
);



--
-- Name: quota_limitation; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE quota_limitation (
    id uuid NOT NULL,
    quota_id uuid NOT NULL,
    storage_id uuid,
    cluster_id uuid,
    virtual_cpu integer,
    mem_size_mb bigint,
    storage_size_gb bigint
);



--
-- Name: repo_file_meta_data; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE repo_file_meta_data (
    repo_domain_id uuid NOT NULL,
    repo_image_id character varying(256) NOT NULL,
    size bigint,
    date_created timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    last_refreshed bigint DEFAULT 0,
    file_type integer DEFAULT 0,
    repo_image_name character varying(256)
);



--
-- Name: roles; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE roles (
    id uuid NOT NULL,
    name character varying(126) NOT NULL,
    description character varying(4000),
    is_readonly boolean NOT NULL,
    role_type integer NOT NULL,
    allows_viewing_children boolean DEFAULT false NOT NULL,
    app_mode integer NOT NULL
);



--
-- Name: roles_groups; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE roles_groups (
    role_id uuid NOT NULL,
    action_group_id integer NOT NULL
);



--
-- Name: schema_version_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE schema_version_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: schema_version; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE schema_version (
    id integer DEFAULT nextval('schema_version_seq'::regclass) NOT NULL,
    version character varying(10) NOT NULL,
    script character varying(255) NOT NULL,
    checksum character varying(128),
    installed_by character varying(63) NOT NULL,
    started_at timestamp without time zone DEFAULT now(),
    ended_at timestamp without time zone,
    state character varying(15) NOT NULL,
    current boolean NOT NULL,
    comment text DEFAULT ''::text
);



--
-- Name: snapshots; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE snapshots (
    snapshot_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    snapshot_type character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    description character varying(4000),
    creation_date timestamp with time zone NOT NULL,
    app_list text,
    vm_configuration text,
    _create_date timestamp with time zone DEFAULT now(),
    _update_date timestamp with time zone,
    memory_volume character varying(255),
    memory_metadata_disk_id uuid,
    memory_dump_disk_id uuid
);



--
-- Name: sso_clients_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE sso_clients_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: sso_clients; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE sso_clients (
    id bigint DEFAULT nextval('sso_clients_seq'::regclass) NOT NULL,
    client_id character varying(128) NOT NULL,
    client_secret character varying(1024) NOT NULL,
    callback_prefix character varying(1024),
    certificate_location character varying(1024),
    notification_callback character varying(1024),
    description text,
    email character varying(256),
    scope character varying(1024),
    trusted boolean DEFAULT true NOT NULL,
    notification_callback_protocol character varying(32) NOT NULL,
    notification_callback_verify_host boolean DEFAULT false NOT NULL,
    notification_callback_verify_chain boolean DEFAULT true NOT NULL
);



--
-- Name: sso_scope_dependency_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE sso_scope_dependency_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: sso_scope_dependency; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE sso_scope_dependency (
    id bigint DEFAULT nextval('sso_scope_dependency_seq'::regclass) NOT NULL,
    scope character varying(128) NOT NULL,
    dependencies text
);



--
-- Name: step; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE step (
    step_id uuid NOT NULL,
    parent_step_id uuid,
    job_id uuid NOT NULL,
    step_type character varying(32) NOT NULL,
    description text NOT NULL,
    step_number integer NOT NULL,
    status character varying(32) NOT NULL,
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone,
    correlation_id character varying(50) NOT NULL,
    external_id uuid,
    external_system_type character varying(32),
    is_external boolean DEFAULT false
);



--
-- Name: storage_device; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_device (
    id uuid NOT NULL,
    name text NOT NULL,
    device_uuid character varying(38),
    filesystem_uuid character varying(38),
    vds_id uuid NOT NULL,
    description text,
    device_type character varying(50),
    device_path text,
    filesystem_type character varying(50),
    mount_point text,
    size bigint,
    is_free boolean,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    is_gluster_brick boolean DEFAULT false
);



--
-- Name: storage_domain_dynamic; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_domain_dynamic (
    id uuid NOT NULL,
    available_disk_size integer,
    used_disk_size integer,
    _update_date timestamp with time zone,
    external_status integer DEFAULT 0 NOT NULL
);



--
-- Name: storage_domain_static; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_domain_static (
    id uuid NOT NULL,
    storage character varying(250) NOT NULL,
    storage_name character varying(250) NOT NULL,
    storage_domain_type integer NOT NULL,
    storage_type integer NOT NULL,
    storage_domain_format_type character varying(50) DEFAULT '0'::character varying NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    recoverable boolean DEFAULT true NOT NULL,
    last_time_used_as_master bigint,
    storage_description character varying(4000),
    storage_comment text,
    wipe_after_delete boolean DEFAULT false NOT NULL,
    warning_low_space_indicator integer,
    critical_space_action_blocker integer
);



--
-- Name: storage_domains_ovf_info; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_domains_ovf_info (
    storage_domain_id uuid,
    status integer DEFAULT 0,
    ovf_disk_id uuid NOT NULL,
    stored_ovfs_ids text,
    last_updated timestamp with time zone
);



--
-- Name: storage_pool; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_pool (
    id uuid NOT NULL,
    name character varying(40) NOT NULL,
    description character varying(4000) NOT NULL,
    storage_pool_type integer,
    storage_pool_format_type character varying(50),
    status integer NOT NULL,
    master_domain_version integer NOT NULL,
    spm_vds_id uuid,
    compatibility_version character varying(40) DEFAULT '2.2'::character varying NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    quota_enforcement_type integer,
    free_text_comment text,
    is_local boolean,
    mac_pool_id uuid NOT NULL
);



--
-- Name: storage_pool_iso_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_pool_iso_map (
    storage_id uuid NOT NULL,
    storage_pool_id uuid NOT NULL,
    status integer
);



--
-- Name: storage_server_connection_extension; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_server_connection_extension (
    id uuid NOT NULL,
    vds_id uuid NOT NULL,
    iqn character varying(128) NOT NULL,
    user_name text NOT NULL,
    password text NOT NULL
);



--
-- Name: storage_server_connections; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE storage_server_connections (
    id character varying(50) NOT NULL,
    connection character varying(250) NOT NULL,
    user_name text,
    password text,
    iqn character varying(128),
    port character varying(50),
    portal character varying(50),
    storage_type integer NOT NULL,
    mount_options character varying(500),
    vfs_type character varying(128),
    nfs_version character varying(4),
    nfs_timeo smallint,
    nfs_retrans smallint
);



--
-- Name: supported_cluster_features; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE supported_cluster_features (
    cluster_id uuid NOT NULL,
    feature_id uuid NOT NULL,
    is_enabled boolean
);



--
-- Name: supported_host_features; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE supported_host_features (
    host_id uuid NOT NULL,
    feature_name character varying(256) NOT NULL
);



--
-- Name: tags; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags (
    tag_id uuid NOT NULL,
    tag_name character varying(50) DEFAULT ''::character varying NOT NULL,
    description character varying(4000),
    parent_id uuid,
    readonly boolean,
    type integer DEFAULT 0 NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: tags_user_group_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags_user_group_map (
    tag_id uuid NOT NULL,
    group_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_user_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags_user_map (
    tag_id uuid NOT NULL,
    user_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_vds_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags_vds_map (
    tag_id uuid NOT NULL,
    vds_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_vm_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags_vm_map (
    tag_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    defaultdisplaytype integer DEFAULT 0
);



--
-- Name: tags_vm_pool_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE tags_vm_pool_map (
    tag_id uuid NOT NULL,
    vm_pool_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: unregistered_disks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE unregistered_disks (
    disk_id uuid NOT NULL,
    disk_alias character varying(255),
    disk_description character varying(255),
    storage_domain_id uuid NOT NULL,
    creation_date timestamp with time zone,
    last_modified timestamp with time zone,
    volume_type integer,
    volume_format integer,
    actual_size bigint,
    size bigint,
    image_id uuid
);



--
-- Name: unregistered_disks_to_vms; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE unregistered_disks_to_vms (
    disk_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_name character varying(255)
);



--
-- Name: unregistered_ovf_of_entities; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE unregistered_ovf_of_entities (
    entity_guid uuid NOT NULL,
    entity_name character varying(255) NOT NULL,
    entity_type character varying(32) NOT NULL,
    architecture integer,
    lowest_comp_version character varying(40),
    storage_domain_id uuid NOT NULL,
    ovf_data text,
    ovf_extra_data text
);



--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE user_profiles (
    profile_id uuid NOT NULL,
    user_id uuid NOT NULL,
    ssh_public_key text,
    user_portal_vm_auto_login boolean DEFAULT true,
    ssh_public_key_id uuid
);



--
-- Name: users; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE users (
    user_id uuid NOT NULL,
    name character varying(255),
    surname character varying(255),
    domain character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    department character varying(255),
    email character varying(255),
    note character varying(255),
    last_admin_check_status boolean DEFAULT false NOT NULL,
    external_id text NOT NULL,
    _create_date timestamp with time zone DEFAULT now(),
    _update_date timestamp with time zone,
    namespace character varying(2048) DEFAULT '*'::character varying
);



--
-- Name: uuid_sequence; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE uuid_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: vdc_db_log_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE vdc_db_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: vdc_db_log; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vdc_db_log (
    error_id bigint DEFAULT nextval('vdc_db_log_seq'::regclass) NOT NULL,
    occured_at timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    error_code character varying(16) NOT NULL,
    error_message character varying(2048),
    error_proc character varying(126),
    error_line integer
);



--
-- Name: vdc_options_seq; Type: SEQUENCE; Schema: public; Owner: engine
--

CREATE SEQUENCE vdc_options_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: vdc_options; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vdc_options (
    option_id integer DEFAULT nextval('vdc_options_seq'::regclass) NOT NULL,
    option_name character varying(100) NOT NULL,
    option_value character varying(4000) NOT NULL,
    version character varying(40) DEFAULT 'general'::character varying NOT NULL,
    default_value character varying(4000)
);



--
-- Name: vds_cpu_statistics; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_cpu_statistics (
    vds_cpu_id uuid NOT NULL,
    vds_id uuid NOT NULL,
    cpu_core_id smallint,
    cpu_sys numeric(5,2),
    cpu_user numeric(5,2),
    cpu_idle numeric(5,2),
    usage_cpu_percent integer
);



--
-- Name: vds_dynamic; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_dynamic (
    vds_id uuid NOT NULL,
    status integer NOT NULL,
    cpu_cores integer,
    cpu_model character varying(255),
    cpu_speed_mh numeric(18,0),
    if_total_speed character varying(40),
    kvm_enabled boolean,
    physical_mem_mb integer,
    mem_commited integer DEFAULT 0,
    vm_active integer DEFAULT 0,
    vm_count integer DEFAULT 0 NOT NULL,
    vm_migrating integer DEFAULT 0,
    reserved_mem integer,
    guest_overhead integer,
    software_version character varying(40),
    version_name character varying(40),
    build_name character varying(40),
    previous_status integer,
    cpu_flags character varying(4000),
    vms_cores_count integer,
    pending_vcpus_count integer,
    cpu_sockets integer,
    net_config_dirty boolean,
    supported_cluster_levels character varying(40),
    host_os character varying(4000),
    kvm_version character varying(4000),
    spice_version character varying(4000),
    kernel_version character varying(4000),
    iscsi_initiator_name character varying(4000),
    transparent_hugepages_state integer DEFAULT 0 NOT NULL,
    hooks text DEFAULT ''::character varying,
    _update_date timestamp with time zone,
    non_operational_reason integer DEFAULT 0 NOT NULL,
    pending_vmem_size integer DEFAULT 0 NOT NULL,
    rpm_version character varying(256) DEFAULT NULL::character varying,
    supported_engines character varying(40),
    libvirt_version character varying(256) DEFAULT NULL::character varying,
    cpu_threads integer,
    hw_manufacturer character varying(255),
    hw_product_name character varying(255),
    hw_version character varying(255),
    hw_serial_number character varying(255),
    hw_uuid character varying(255),
    hw_family character varying(255),
    hbas text,
    supported_emulated_machines text,
    gluster_version character varying(4000),
    controlled_by_pm_policy boolean DEFAULT false,
    kdump_status smallint DEFAULT '-1'::integer NOT NULL,
    selinux_enforce_mode integer,
    auto_numa_balancing smallint,
    is_numa_supported boolean,
    supported_rng_sources character varying(255),
    online_cpus text,
    maintenance_reason text,
    incoming_migrations integer DEFAULT 0 NOT NULL,
    outgoing_migrations integer DEFAULT 0 NOT NULL,
    is_update_available boolean DEFAULT false NOT NULL,
    external_status integer DEFAULT 0 NOT NULL,
    is_hostdev_enabled boolean DEFAULT false NOT NULL,
    librbd1_version character varying(4000),
    glusterfs_cli_version character varying(4000),
    kernel_args text
);



--
-- Name: vds_groups; Type: VIEW; Schema: public; Owner: engine
--

CREATE VIEW vds_groups AS
 SELECT cluster.cluster_id,
    cluster.name,
    cluster.description,
    cluster.cpu_name,
    cluster._create_date,
    cluster._update_date,
    cluster.storage_pool_id,
    cluster.max_vds_memory_over_commit,
    cluster.compatibility_version,
    cluster.transparent_hugepages,
    cluster.migrate_on_error,
    cluster.virt_service,
    cluster.gluster_service,
    cluster.count_threads_as_cores,
    cluster.emulated_machine,
    cluster.trusted_service,
    cluster.tunnel_migration,
    cluster.cluster_policy_id,
    cluster.cluster_policy_custom_properties,
    cluster.enable_balloon,
    cluster.free_text_comment,
    cluster.detect_emulated_machine,
    cluster.architecture,
    cluster.optimization_type,
    cluster.spice_proxy,
    cluster.ha_reservation,
    cluster.enable_ksm,
    cluster.serial_number_policy,
    cluster.custom_serial_number,
    cluster.optional_reason,
    cluster.required_rng_sources,
    cluster.skip_fencing_if_sd_active,
    cluster.skip_fencing_if_connectivity_broken,
    cluster.hosts_with_broken_connectivity_threshold,
    cluster.fencing_enabled,
    cluster.is_auto_converge,
    cluster.is_migrate_compressed,
    cluster.maintenance_reason_required,
    cluster.gluster_tuned_profile,
    cluster.gluster_cli_based_snapshot_scheduled,
    cluster.ksm_merge_across_nodes
   FROM cluster;



--
-- Name: vds_interface; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_interface (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    network_name character varying(50),
    vds_id uuid,
    mac_addr character varying(59),
    is_bond boolean DEFAULT false,
    bond_name character varying(50),
    bond_type integer,
    bond_opts character varying(4000),
    vlan_id integer,
    speed integer,
    addr character varying(20),
    subnet character varying(20),
    gateway character varying(20),
    boot_protocol integer,
    type integer DEFAULT 0,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    mtu integer,
    bridged boolean DEFAULT true NOT NULL,
    labels text,
    qos_overridden boolean DEFAULT false NOT NULL,
    base_interface character varying(50),
    ipv6_boot_protocol integer,
    ipv6_address character varying(50),
    ipv6_prefix integer,
    ipv6_gateway character varying(50),
    ad_partner_mac character varying(59)
);



--
-- Name: vds_interface_statistics; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_interface_statistics (
    id uuid NOT NULL,
    vds_id uuid,
    rx_rate numeric(18,0),
    tx_rate numeric(18,0),
    rx_drop numeric(18,0),
    tx_drop numeric(18,0),
    iface_status integer,
    _update_date timestamp with time zone,
    rx_total bigint,
    rx_offset bigint,
    tx_total bigint,
    tx_offset bigint,
    sample_time double precision
);



--
-- Name: vds_kdump_status; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_kdump_status (
    vds_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    address character varying(255) NOT NULL
);



--
-- Name: vds_spm_id_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_spm_id_map (
    storage_pool_id uuid NOT NULL,
    vds_spm_id integer NOT NULL,
    vds_id uuid NOT NULL
);



--
-- Name: vds_static; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_static (
    vds_id uuid NOT NULL,
    vds_name character varying(255) NOT NULL,
    vds_unique_id character varying(128),
    host_name character varying(255) NOT NULL,
    port integer NOT NULL,
    cluster_id uuid NOT NULL,
    server_ssl_enabled boolean,
    vds_type integer DEFAULT 0 NOT NULL,
    vds_strength integer DEFAULT 100 NOT NULL,
    pm_enabled boolean DEFAULT false NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    otp_validity bigint,
    vds_spm_priority smallint DEFAULT 5,
    recoverable boolean DEFAULT true NOT NULL,
    sshkeyfingerprint character varying(1024),
    pm_proxy_preferences character varying(255) DEFAULT ''::character varying,
    console_address character varying(255) DEFAULT NULL::character varying,
    ssh_username character varying(255),
    ssh_port integer,
    free_text_comment text,
    disable_auto_pm boolean DEFAULT false,
    pm_detect_kdump boolean DEFAULT false NOT NULL,
    protocol smallint DEFAULT 0 NOT NULL,
    host_provider_id uuid,
    openstack_network_provider_id uuid,
    kernel_cmdline text,
    last_stored_kernel_cmdline text,
    CONSTRAINT vds_static_vds_spm_priority_check CHECK (((vds_spm_priority >= '-1'::integer) AND (vds_spm_priority <= 10)))
);



--
-- Name: vds_statistics; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vds_statistics (
    vds_id uuid NOT NULL,
    cpu_idle numeric(18,0) DEFAULT 0,
    cpu_load numeric(18,0) DEFAULT 0,
    cpu_sys numeric(18,0) DEFAULT 0,
    cpu_user numeric(18,0) DEFAULT 0,
    usage_mem_percent integer DEFAULT 0,
    usage_cpu_percent integer DEFAULT 0,
    usage_network_percent integer,
    mem_available bigint,
    mem_shared bigint,
    swap_free bigint,
    swap_total bigint,
    ksm_cpu_percent integer DEFAULT 0,
    ksm_pages bigint,
    ksm_state boolean,
    _update_date timestamp with time zone,
    mem_free bigint,
    ha_score integer DEFAULT 0 NOT NULL,
    anonymous_hugepages integer,
    ha_configured boolean DEFAULT false NOT NULL,
    ha_active boolean DEFAULT false NOT NULL,
    ha_global_maintenance boolean DEFAULT false NOT NULL,
    ha_local_maintenance boolean DEFAULT false NOT NULL,
    boot_time bigint,
    cpu_over_commit_time_stamp timestamp with time zone
);



--
-- Name: vfs_config_labels; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vfs_config_labels (
    vfs_config_id uuid NOT NULL,
    label text NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: vfs_config_networks; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vfs_config_networks (
    vfs_config_id uuid NOT NULL,
    network_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: vm_device; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_device (
    device_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    type character varying(30) NOT NULL,
    device character varying(255) NOT NULL,
    address character varying(255) NOT NULL,
    boot_order integer DEFAULT 0,
    spec_params text,
    is_managed boolean DEFAULT false NOT NULL,
    is_plugged boolean,
    is_readonly boolean DEFAULT false NOT NULL,
    _create_date timestamp with time zone DEFAULT now(),
    _update_date timestamp with time zone,
    alias character varying(255) DEFAULT ''::character varying,
    custom_properties text,
    snapshot_id uuid,
    logical_name character varying(255),
    is_using_scsi_reservation boolean DEFAULT false NOT NULL,
    host_device character varying(255)
);



--
-- Name: vm_dynamic; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_dynamic (
    vm_guid uuid NOT NULL,
    status integer NOT NULL,
    vm_ip text,
    vm_host character varying(255),
    vm_pid integer,
    last_start_time timestamp with time zone,
    guest_cur_user_name character varying(255),
    guest_os character varying(255),
    run_on_vds uuid,
    migrating_to_vds uuid,
    app_list text,
    acpi_enable boolean,
    session integer,
    kvm_enable boolean,
    utc_diff integer,
    last_vds_run_on uuid,
    client_ip character varying(255),
    guest_requested_memory integer,
    boot_sequence integer,
    exit_status integer DEFAULT 0 NOT NULL,
    pause_status integer DEFAULT 0 NOT NULL,
    exit_message character varying(4000),
    hash character varying(30),
    console_user_id uuid,
    guest_agent_nics_hash integer,
    console_cur_user_name character varying(255),
    last_watchdog_event bigint,
    last_watchdog_action character varying(8),
    is_run_once boolean DEFAULT false NOT NULL,
    vm_fqdn text DEFAULT ''::text,
    cpu_name character varying(255),
    last_stop_time timestamp with time zone,
    current_cd character varying(4000) DEFAULT NULL::character varying,
    reason text,
    exit_reason integer DEFAULT '-1'::integer,
    guest_cpu_count integer,
    emulated_machine character varying(255),
    spice_port integer,
    spice_tls_port integer,
    spice_ip character varying(255) DEFAULT NULL::character varying,
    vnc_port integer,
    vnc_ip character varying(255) DEFAULT NULL::character varying,
    guest_agent_status integer DEFAULT 0,
    guest_mem_free bigint,
    guest_mem_buffered bigint,
    guest_mem_cached bigint,
    guest_timezone_offset integer,
    guest_timezone_name character varying(255),
    guestos_arch integer DEFAULT 0 NOT NULL,
    guestos_codename character varying(255),
    guestos_distribution character varying(255),
    guestos_kernel_version character varying(255),
    guestos_type character varying(255) DEFAULT 'Other'::character varying NOT NULL,
    guestos_version character varying(255),
    guest_containers text DEFAULT '[]'::text
);



--
-- Name: vm_guest_agent_interfaces; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_guest_agent_interfaces (
    vm_id uuid NOT NULL,
    interface_name text,
    mac_address character varying(59),
    ipv4_addresses text,
    ipv6_addresses text
);



--
-- Name: vm_host_pinning_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_host_pinning_map (
    vm_id uuid NOT NULL,
    vds_id uuid NOT NULL
);



--
-- Name: vm_icon_defaults; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_icon_defaults (
    id uuid NOT NULL,
    os_id integer NOT NULL,
    small_icon_id uuid NOT NULL,
    large_icon_id uuid NOT NULL
);



--
-- Name: vm_icons; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_icons (
    id uuid NOT NULL,
    data_url character varying(32768) NOT NULL
);



--
-- Name: vm_init; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_init (
    vm_id uuid NOT NULL,
    host_name text,
    domain text,
    authorized_keys text,
    regenerate_keys boolean DEFAULT false,
    time_zone character varying(40) DEFAULT NULL::character varying,
    dns_servers text,
    dns_search_domains text,
    networks text,
    password text,
    winkey character varying(30) DEFAULT NULL::character varying,
    custom_script text,
    input_locale character varying(256) DEFAULT NULL::character varying,
    ui_language character varying(256) DEFAULT NULL::character varying,
    system_locale character varying(256) DEFAULT NULL::character varying,
    user_locale character varying(256) DEFAULT NULL::character varying,
    user_name character varying(256) DEFAULT NULL::character varying,
    active_directory_ou character varying(256) DEFAULT NULL::character varying,
    org_name character varying(256) DEFAULT NULL::character varying
);



--
-- Name: vm_interface; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_interface (
    id uuid NOT NULL,
    vm_guid uuid,
    vmt_guid uuid,
    mac_addr character varying(20),
    name character varying(50) NOT NULL,
    speed integer,
    type integer DEFAULT 0,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    linked boolean DEFAULT true NOT NULL,
    vnic_profile_id uuid
);



--
-- Name: vm_interface_statistics; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_interface_statistics (
    id uuid NOT NULL,
    vm_id uuid,
    rx_rate numeric(18,0),
    tx_rate numeric(18,0),
    rx_drop numeric(18,0),
    tx_drop numeric(18,0),
    iface_status integer,
    _update_date timestamp with time zone,
    rx_total bigint,
    rx_offset bigint,
    tx_total bigint,
    tx_offset bigint,
    sample_time double precision
);



--
-- Name: vm_jobs; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_jobs (
    vm_job_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    job_state integer DEFAULT 0 NOT NULL,
    job_type integer NOT NULL,
    block_job_type integer,
    bandwidth integer,
    cursor_cur bigint,
    cursor_end bigint,
    image_group_id uuid
);



--
-- Name: vm_ovf_generations; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_ovf_generations (
    vm_guid uuid NOT NULL,
    storage_pool_id uuid,
    ovf_generation bigint DEFAULT 0,
    ovf_data text
);



--
-- Name: vm_pool_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_pool_map (
    vm_pool_id uuid,
    vm_guid uuid NOT NULL
);



--
-- Name: vm_pools; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_pools (
    vm_pool_id uuid NOT NULL,
    vm_pool_name character varying(255) NOT NULL,
    vm_pool_description character varying(4000) NOT NULL,
    vm_pool_type integer,
    parameters character varying(200),
    cluster_id uuid,
    prestarted_vms smallint DEFAULT 0,
    max_assigned_vms_per_user smallint DEFAULT 1,
    vm_pool_comment text,
    spice_proxy character varying(255),
    is_being_destroyed boolean DEFAULT false NOT NULL,
    stateful boolean DEFAULT false NOT NULL
);



--
-- Name: vm_static; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_static (
    vm_guid uuid NOT NULL,
    vm_name character varying(255) NOT NULL,
    mem_size_mb integer NOT NULL,
    vmt_guid uuid NOT NULL,
    os integer DEFAULT 0 NOT NULL,
    description character varying(4000),
    cluster_id uuid,
    creation_date timestamp with time zone,
    num_of_monitors integer NOT NULL,
    is_initialized boolean,
    is_auto_suspend boolean DEFAULT false,
    num_of_sockets integer DEFAULT 1 NOT NULL,
    cpu_per_socket integer DEFAULT 1 NOT NULL,
    usb_policy integer,
    time_zone character varying(40),
    is_stateless boolean,
    fail_back boolean DEFAULT false NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    dedicated_vm_for_vds text,
    auto_startup boolean,
    vm_type integer DEFAULT 0 NOT NULL,
    nice_level integer DEFAULT 0 NOT NULL,
    default_boot_sequence integer DEFAULT 0 NOT NULL,
    default_display_type integer DEFAULT 0 NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    iso_path character varying(4000) DEFAULT ''::character varying,
    origin integer DEFAULT 0,
    initrd_url character varying(4000),
    kernel_url character varying(4000),
    kernel_params character varying(4000),
    migration_support integer DEFAULT 0 NOT NULL,
    userdefined_properties character varying(4000),
    predefined_properties character varying(4000),
    min_allocated_mem integer DEFAULT 0 NOT NULL,
    entity_type character varying(32) NOT NULL,
    child_count integer DEFAULT 0,
    template_status integer,
    quota_id uuid,
    allow_console_reconnect boolean DEFAULT false NOT NULL,
    cpu_pinning character varying(4000) DEFAULT NULL::character varying,
    is_smartcard_enabled boolean DEFAULT false,
    host_cpu_flags boolean DEFAULT false,
    db_generation bigint DEFAULT 1,
    is_delete_protected boolean DEFAULT false,
    is_disabled boolean DEFAULT false,
    is_run_and_pause boolean DEFAULT false NOT NULL,
    created_by_user_id uuid,
    tunnel_migration boolean,
    free_text_comment text,
    single_qxl_pci boolean DEFAULT false NOT NULL,
    cpu_shares integer DEFAULT 0 NOT NULL,
    vnc_keyboard_layout character varying(16) DEFAULT NULL::character varying,
    instance_type_id uuid,
    image_type_id uuid,
    sso_method character varying(32) DEFAULT 'guest_agent'::character varying NOT NULL,
    original_template_id uuid,
    original_template_name character varying(255) DEFAULT NULL::character varying,
    migration_downtime integer,
    template_version_number integer,
    template_version_name character varying(40) DEFAULT NULL::character varying,
    serial_number_policy smallint,
    custom_serial_number character varying(255) DEFAULT NULL::character varying,
    is_boot_menu_enabled boolean DEFAULT false NOT NULL,
    numatune_mode character varying(20),
    is_spice_file_transfer_enabled boolean DEFAULT true NOT NULL,
    is_spice_copy_paste_enabled boolean DEFAULT true NOT NULL,
    cpu_profile_id uuid,
    is_auto_converge boolean,
    is_migrate_compressed boolean,
    custom_emulated_machine character varying(40),
    custom_cpu_name character varying(40),
    small_icon_id uuid,
    large_icon_id uuid,
    provider_id uuid,
    num_of_io_threads integer DEFAULT 0 NOT NULL,
    console_disconnect_action character varying(64),
    threads_per_cpu integer DEFAULT 1 NOT NULL,
    custom_compatibility_version character varying(40),
    migration_policy_id uuid
);



--
-- Name: vm_statistics; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_statistics (
    vm_guid uuid NOT NULL,
    cpu_user numeric(18,0) DEFAULT 0,
    cpu_sys numeric(18,0) DEFAULT 0,
    elapsed_time numeric(18,0) DEFAULT 0,
    usage_network_percent integer DEFAULT 0,
    usage_mem_percent integer DEFAULT 0,
    usage_cpu_percent integer DEFAULT 0,
    disks_usage text,
    _update_date timestamp with time zone,
    migration_progress_percent integer DEFAULT 0,
    memory_usage_history text,
    cpu_usage_history text,
    network_usage_history text
);



--
-- Name: vm_vds_numa_node_map; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vm_vds_numa_node_map (
    id uuid NOT NULL,
    vm_numa_node_id uuid NOT NULL,
    vds_numa_node_id uuid,
    vds_numa_node_index smallint,
    is_pinned boolean DEFAULT false NOT NULL
);



--
-- Name: vnic_profiles; Type: TABLE; Schema: public; Owner: engine
--

CREATE TABLE vnic_profiles (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    network_id uuid NOT NULL,
    port_mirroring boolean NOT NULL,
    custom_properties text,
    description text,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    network_qos_id uuid,
    passthrough boolean DEFAULT false NOT NULL,
    network_filter_id uuid
);



--
-- Name: affinity_groups affinity_group_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_groups
    ADD CONSTRAINT affinity_group_pk PRIMARY KEY (id);


--
-- Name: cluster_policies cluster_policy_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster_policies
    ADD CONSTRAINT cluster_policy_pk PRIMARY KEY (id);


--
-- Name: command_assoc_entities command_assoc_entities_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY command_assoc_entities
    ADD CONSTRAINT command_assoc_entities_pkey PRIMARY KEY (command_id, entity_id);


--
-- Name: cpu_profiles cpu_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cpu_profiles
    ADD CONSTRAINT cpu_profiles_pkey PRIMARY KEY (id);


--
-- Name: disk_lun_map disk_lun_map_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_map_pk PRIMARY KEY (disk_id, lun_id);


--
-- Name: disk_profiles disk_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_profiles
    ADD CONSTRAINT disk_profiles_pkey PRIMARY KEY (id);


--
-- Name: engine_backup_log engine_backup_log_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY engine_backup_log
    ADD CONSTRAINT engine_backup_log_pkey PRIMARY KEY (scope, done_at);


--
-- Name: fence_agents fence_agent_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY fence_agents
    ADD CONSTRAINT fence_agent_pk PRIMARY KEY (id);


--
-- Name: gluster_volumes gluster_volumes_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT gluster_volumes_name_unique UNIQUE (cluster_id, vol_name);


--
-- Name: ad_groups groups_domain_external_id_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY ad_groups
    ADD CONSTRAINT groups_domain_external_id_unique UNIQUE (domain, external_id);


--
-- Name: host_device host_device_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_device
    ADD CONSTRAINT host_device_pk PRIMARY KEY (host_id, device_name);


--
-- Name: gluster_volume_bricks idx_gluster_volume_bricks_volume_server_brickdir; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT idx_gluster_volume_bricks_volume_server_brickdir UNIQUE (volume_id, server_id, brick_dir);


--
-- Name: gluster_volume_options idx_gluster_volume_options_volume_id_option_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT idx_gluster_volume_options_volume_id_option_key UNIQUE (volume_id, option_key);


--
-- Name: labels label_name; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY labels
    ADD CONSTRAINT label_name UNIQUE (label_name);


--
-- Name: mac_pools mac_pools_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY mac_pools
    ADD CONSTRAINT mac_pools_pkey PRIMARY KEY (id);


--
-- Name: materialized_views materialized_views_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY materialized_views
    ADD CONSTRAINT materialized_views_pkey PRIMARY KEY (mv_name);


--
-- Name: network_attachments network_attachments_network_id_nic_id_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_attachments
    ADD CONSTRAINT network_attachments_network_id_nic_id_key UNIQUE (network_id, nic_id);


--
-- Name: network_filter network_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_filter
    ADD CONSTRAINT network_filter_pkey PRIMARY KEY (filter_name);


--
-- Name: ad_groups pk_ad_group_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY ad_groups
    ADD CONSTRAINT pk_ad_group_id PRIMARY KEY (id);


--
-- Name: async_tasks pk_async_tasks; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY async_tasks
    ADD CONSTRAINT pk_async_tasks PRIMARY KEY (task_id);


--
-- Name: audit_log pk_audit_log; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY audit_log
    ADD CONSTRAINT pk_audit_log PRIMARY KEY (audit_log_id);


--
-- Name: bookmarks pk_bookmarks; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY bookmarks
    ADD CONSTRAINT pk_bookmarks PRIMARY KEY (bookmark_id);


--
-- Name: cluster pk_cluster; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster
    ADD CONSTRAINT pk_cluster PRIMARY KEY (cluster_id);


--
-- Name: cluster_features pk_cluster_features; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster_features
    ADD CONSTRAINT pk_cluster_features PRIMARY KEY (feature_id);


--
-- Name: command_entities pk_command_entities; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY command_entities
    ADD CONSTRAINT pk_command_entities PRIMARY KEY (command_id);


--
-- Name: gluster_config_master pk_config_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_config_master
    ADD CONSTRAINT pk_config_key PRIMARY KEY (config_key);


--
-- Name: custom_actions pk_custom_actions; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY custom_actions
    ADD CONSTRAINT pk_custom_actions PRIMARY KEY (action_name, tab);


--
-- Name: unregistered_disks pk_disk_id_storage_domain_unregistered; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_disks
    ADD CONSTRAINT pk_disk_id_storage_domain_unregistered PRIMARY KEY (disk_id, storage_domain_id);


--
-- Name: unregistered_disks_to_vms pk_disk_id_unregistered; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_disks_to_vms
    ADD CONSTRAINT pk_disk_id_unregistered PRIMARY KEY (disk_id, entity_id);


--
-- Name: disk_image_dynamic pk_disk_image_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_image_dynamic
    ADD CONSTRAINT pk_disk_image_dynamic PRIMARY KEY (image_id);


--
-- Name: disk_vm_element pk_disk_vm_element; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_vm_element
    ADD CONSTRAINT pk_disk_vm_element PRIMARY KEY (vm_id, disk_id);


--
-- Name: base_disks pk_disks; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY base_disks
    ADD CONSTRAINT pk_disks PRIMARY KEY (disk_id);


--
-- Name: engine_sessions pk_engine_session; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY engine_sessions
    ADD CONSTRAINT pk_engine_session PRIMARY KEY (id);


--
-- Name: unregistered_ovf_of_entities pk_entity_guid_storage_domain_unregistered; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_ovf_of_entities
    ADD CONSTRAINT pk_entity_guid_storage_domain_unregistered PRIMARY KEY (entity_guid, storage_domain_id);


--
-- Name: event_map pk_event_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_map
    ADD CONSTRAINT pk_event_map PRIMARY KEY (event_up_name);


--
-- Name: external_variable pk_external_variable; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY external_variable
    ADD CONSTRAINT pk_external_variable PRIMARY KEY (var_name);


--
-- Name: gluster_cluster_services pk_gluster_cluster_services; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT pk_gluster_cluster_services PRIMARY KEY (cluster_id, service_type);


--
-- Name: gluster_georep_config pk_gluster_georep_config; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_config
    ADD CONSTRAINT pk_gluster_georep_config PRIMARY KEY (session_id, config_key);


--
-- Name: gluster_georep_session pk_gluster_georep_session; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_session
    ADD CONSTRAINT pk_gluster_georep_session PRIMARY KEY (session_id);


--
-- Name: gluster_georep_session_details pk_gluster_georep_session_details; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_session_details
    ADD CONSTRAINT pk_gluster_georep_session_details PRIMARY KEY (session_id, master_brick_id);


--
-- Name: gluster_hooks pk_gluster_hooks; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_hooks
    ADD CONSTRAINT pk_gluster_hooks PRIMARY KEY (id);


--
-- Name: gluster_server pk_gluster_server; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server
    ADD CONSTRAINT pk_gluster_server PRIMARY KEY (server_id);


--
-- Name: gluster_server_services pk_gluster_server_services; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT pk_gluster_server_services PRIMARY KEY (id);


--
-- Name: gluster_service_types pk_gluster_service_types; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_service_types
    ADD CONSTRAINT pk_gluster_service_types PRIMARY KEY (service_type);


--
-- Name: gluster_services pk_gluster_services; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT pk_gluster_services PRIMARY KEY (id);


--
-- Name: gluster_volume_access_protocols pk_gluster_volume_access_protocols; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_access_protocols
    ADD CONSTRAINT pk_gluster_volume_access_protocols PRIMARY KEY (volume_id, access_protocol);


--
-- Name: gluster_volume_brick_details pk_gluster_volume_brick_details; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_brick_details
    ADD CONSTRAINT pk_gluster_volume_brick_details PRIMARY KEY (brick_id);


--
-- Name: gluster_volume_bricks pk_gluster_volume_bricks; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT pk_gluster_volume_bricks PRIMARY KEY (id);


--
-- Name: gluster_volume_details pk_gluster_volume_details; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_details
    ADD CONSTRAINT pk_gluster_volume_details PRIMARY KEY (volume_id);


--
-- Name: gluster_volume_options pk_gluster_volume_options; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT pk_gluster_volume_options PRIMARY KEY (id);


--
-- Name: gluster_volume_transport_types pk_gluster_volume_transport_types; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_transport_types
    ADD CONSTRAINT pk_gluster_volume_transport_types PRIMARY KEY (volume_id, transport_type);


--
-- Name: gluster_volumes pk_gluster_volumes; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT pk_gluster_volumes PRIMARY KEY (id);


--
-- Name: business_entity_snapshot pk_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY business_entity_snapshot
    ADD CONSTRAINT pk_id PRIMARY KEY (id);


--
-- Name: image_storage_domain_map pk_image_storage_domain_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT pk_image_storage_domain_map PRIMARY KEY (image_id, storage_domain_id);


--
-- Name: image_transfers pk_image_transfers; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_transfers
    ADD CONSTRAINT pk_image_transfers PRIMARY KEY (command_id);


--
-- Name: images pk_images; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY images
    ADD CONSTRAINT pk_images PRIMARY KEY (image_guid);


--
-- Name: iscsi_bonds pk_iscsi_bonds; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds
    ADD CONSTRAINT pk_iscsi_bonds PRIMARY KEY (id);


--
-- Name: iscsi_bonds_networks_map pk_iscsi_bonds_networks_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT pk_iscsi_bonds_networks_map PRIMARY KEY (iscsi_bond_id, network_id);


--
-- Name: iscsi_bonds_storage_connections_map pk_iscsi_bonds_storage_connections_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT pk_iscsi_bonds_storage_connections_map PRIMARY KEY (iscsi_bond_id, connection_id);


--
-- Name: job pk_jobs; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY job
    ADD CONSTRAINT pk_jobs PRIMARY KEY (job_id);


--
-- Name: job_subject_entity pk_jobs_subject_entity; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY job_subject_entity
    ADD CONSTRAINT pk_jobs_subject_entity PRIMARY KEY (job_id, entity_id);


--
-- Name: labels pk_labels_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY labels
    ADD CONSTRAINT pk_labels_id PRIMARY KEY (label_id);


--
-- Name: lun_storage_server_connection_map pk_lun_storage_server_connection_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT pk_lun_storage_server_connection_map PRIMARY KEY (lun_id, storage_server_connection);


--
-- Name: luns pk_luns; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY luns
    ADD CONSTRAINT pk_luns PRIMARY KEY (lun_id);


--
-- Name: network pk_network; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT pk_network PRIMARY KEY (id);


--
-- Name: network_attachments pk_network_attachments_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_attachments
    ADD CONSTRAINT pk_network_attachments_id PRIMARY KEY (id);


--
-- Name: network_cluster pk_network_cluster; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT pk_network_cluster PRIMARY KEY (network_id, cluster_id);


--
-- Name: numa_node pk_numa_node; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY numa_node
    ADD CONSTRAINT pk_numa_node PRIMARY KEY (numa_node_id);


--
-- Name: numa_node_cpu_map pk_numa_node_cpu_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY numa_node_cpu_map
    ADD CONSTRAINT pk_numa_node_cpu_map PRIMARY KEY (id);


--
-- Name: object_column_white_list pk_object_column_white_list; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY object_column_white_list
    ADD CONSTRAINT pk_object_column_white_list PRIMARY KEY (object_name, column_name);


--
-- Name: object_column_white_list_sql pk_object_column_white_list_sql; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY object_column_white_list_sql
    ADD CONSTRAINT pk_object_column_white_list_sql PRIMARY KEY (object_name);


--
-- Name: dwh_osinfo pk_os_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY dwh_osinfo
    ADD CONSTRAINT pk_os_id PRIMARY KEY (os_id);


--
-- Name: permissions pk_permissions_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT pk_permissions_id PRIMARY KEY (id);


--
-- Name: user_profiles pk_profile_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY user_profiles
    ADD CONSTRAINT pk_profile_id PRIMARY KEY (profile_id);


--
-- Name: qos pk_qos_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qos
    ADD CONSTRAINT pk_qos_id PRIMARY KEY (id);


--
-- Name: quota pk_quota; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT pk_quota PRIMARY KEY (id);


--
-- Name: quota_limitation pk_quota_limitation; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT pk_quota_limitation PRIMARY KEY (id);


--
-- Name: repo_file_meta_data pk_repo_file_meta_data; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY repo_file_meta_data
    ADD CONSTRAINT pk_repo_file_meta_data PRIMARY KEY (repo_domain_id, repo_image_id);


--
-- Name: roles_groups pk_roles_groups; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY roles_groups
    ADD CONSTRAINT pk_roles_groups PRIMARY KEY (role_id, action_group_id);


--
-- Name: roles pk_roles_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT pk_roles_id PRIMARY KEY (id);


--
-- Name: libvirt_secrets pk_secret_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY libvirt_secrets
    ADD CONSTRAINT pk_secret_id PRIMARY KEY (secret_id);


--
-- Name: gluster_volume_snapshots pk_snapshot_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshots
    ADD CONSTRAINT pk_snapshot_id PRIMARY KEY (snapshot_id);


--
-- Name: snapshots pk_snapshots; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT pk_snapshots PRIMARY KEY (snapshot_id);


--
-- Name: sso_clients pk_sso_clients; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY sso_clients
    ADD CONSTRAINT pk_sso_clients PRIMARY KEY (id);


--
-- Name: sso_scope_dependency pk_sso_scope_dependency; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY sso_scope_dependency
    ADD CONSTRAINT pk_sso_scope_dependency PRIMARY KEY (id);


--
-- Name: step pk_steps; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY step
    ADD CONSTRAINT pk_steps PRIMARY KEY (step_id);


--
-- Name: storage_domain_static pk_storage; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domain_static
    ADD CONSTRAINT pk_storage PRIMARY KEY (id);


--
-- Name: storage_device pk_storage_device; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_device
    ADD CONSTRAINT pk_storage_device PRIMARY KEY (id);


--
-- Name: storage_domain_dynamic pk_storage_domain_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domain_dynamic
    ADD CONSTRAINT pk_storage_domain_dynamic PRIMARY KEY (id);


--
-- Name: storage_pool_iso_map pk_storage_domain_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT pk_storage_domain_pool_map PRIMARY KEY (storage_id, storage_pool_id);


--
-- Name: storage_pool pk_storage_pool; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool
    ADD CONSTRAINT pk_storage_pool PRIMARY KEY (id);


--
-- Name: storage_server_connections pk_storage_server; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_server_connections
    ADD CONSTRAINT pk_storage_server PRIMARY KEY (id);


--
-- Name: storage_server_connection_extension pk_storage_server_connection_extension; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_server_connection_extension
    ADD CONSTRAINT pk_storage_server_connection_extension PRIMARY KEY (id);


--
-- Name: supported_cluster_features pk_supported_cluster_features; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY supported_cluster_features
    ADD CONSTRAINT pk_supported_cluster_features PRIMARY KEY (cluster_id, feature_id);


--
-- Name: supported_host_features pk_supported_host_features; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY supported_host_features
    ADD CONSTRAINT pk_supported_host_features PRIMARY KEY (host_id, feature_name);


--
-- Name: tags pk_tags_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags
    ADD CONSTRAINT pk_tags_id PRIMARY KEY (tag_id);


--
-- Name: tags_user_group_map pk_tags_user_group_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT pk_tags_user_group_map PRIMARY KEY (tag_id, group_id);


--
-- Name: tags_user_map pk_tags_user_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT pk_tags_user_map PRIMARY KEY (tag_id, user_id);


--
-- Name: tags_vds_map pk_tags_vds_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT pk_tags_vds_map PRIMARY KEY (tag_id, vds_id);


--
-- Name: tags_vm_map pk_tags_vm_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT pk_tags_vm_map PRIMARY KEY (tag_id, vm_id);


--
-- Name: tags_vm_pool_map pk_tags_vm_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT pk_tags_vm_pool_map PRIMARY KEY (tag_id, vm_pool_id);


--
-- Name: users pk_users; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY users
    ADD CONSTRAINT pk_users PRIMARY KEY (user_id);


--
-- Name: vdc_db_log pk_vdc_db_log; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vdc_db_log
    ADD CONSTRAINT pk_vdc_db_log PRIMARY KEY (error_id);


--
-- Name: vdc_options pk_vdc_options; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vdc_options
    ADD CONSTRAINT pk_vdc_options PRIMARY KEY (option_id);


--
-- Name: vds_cpu_statistics pk_vds_cpu_statistics; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_cpu_statistics
    ADD CONSTRAINT pk_vds_cpu_statistics PRIMARY KEY (vds_cpu_id);


--
-- Name: vds_dynamic pk_vds_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_dynamic
    ADD CONSTRAINT pk_vds_dynamic PRIMARY KEY (vds_id);


--
-- Name: vds_interface pk_vds_interface; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT pk_vds_interface PRIMARY KEY (id);


--
-- Name: vds_interface_statistics pk_vds_interface_statistics; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface_statistics
    ADD CONSTRAINT pk_vds_interface_statistics PRIMARY KEY (id);


--
-- Name: vds_kdump_status pk_vds_kdump_status; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_kdump_status
    ADD CONSTRAINT pk_vds_kdump_status PRIMARY KEY (vds_id);


--
-- Name: vds_spm_id_map pk_vds_spm_id_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT pk_vds_spm_id_map PRIMARY KEY (storage_pool_id, vds_spm_id);


--
-- Name: vds_static pk_vds_static; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT pk_vds_static PRIMARY KEY (vds_id);


--
-- Name: vds_statistics pk_vds_statistics; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_statistics
    ADD CONSTRAINT pk_vds_statistics PRIMARY KEY (vds_id);


--
-- Name: vm_device pk_vm_device; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT pk_vm_device PRIMARY KEY (device_id, vm_id);


--
-- Name: vm_dynamic pk_vm_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT pk_vm_dynamic PRIMARY KEY (vm_guid);


--
-- Name: vm_icon_defaults pk_vm_icon_defaults; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_icon_defaults
    ADD CONSTRAINT pk_vm_icon_defaults PRIMARY KEY (id);


--
-- Name: vm_icons pk_vm_icons; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_icons
    ADD CONSTRAINT pk_vm_icons PRIMARY KEY (id);


--
-- Name: vm_init pk_vm_init; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_init
    ADD CONSTRAINT pk_vm_init PRIMARY KEY (vm_id);


--
-- Name: vm_interface pk_vm_interface; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT pk_vm_interface PRIMARY KEY (id);


--
-- Name: vm_interface_statistics pk_vm_interface_statistics; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface_statistics
    ADD CONSTRAINT pk_vm_interface_statistics PRIMARY KEY (id);


--
-- Name: vm_jobs pk_vm_jobs; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_jobs
    ADD CONSTRAINT pk_vm_jobs PRIMARY KEY (vm_job_id);


--
-- Name: vm_pool_map pk_vm_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT pk_vm_pool_map PRIMARY KEY (vm_guid);


--
-- Name: vm_pools pk_vm_pools; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pools
    ADD CONSTRAINT pk_vm_pools PRIMARY KEY (vm_pool_id);


--
-- Name: vm_static pk_vm_static; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT pk_vm_static PRIMARY KEY (vm_guid);


--
-- Name: vm_statistics pk_vm_statistics; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_statistics
    ADD CONSTRAINT pk_vm_statistics PRIMARY KEY (vm_guid);


--
-- Name: vm_vds_numa_node_map pk_vm_vds_numa_node_map; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_vds_numa_node_map
    ADD CONSTRAINT pk_vm_vds_numa_node_map PRIMARY KEY (id);


--
-- Name: vnic_profiles pk_vnic_profiles_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT pk_vnic_profiles_id PRIMARY KEY (id);


--
-- Name: gluster_volume_snapshot_schedules pk_volume_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshot_schedules
    ADD CONSTRAINT pk_volume_id PRIMARY KEY (volume_id);


--
-- Name: policy_units policy_unit_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY policy_units
    ADD CONSTRAINT policy_unit_pk PRIMARY KEY (id);


--
-- Name: providers providers_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY providers
    ADD CONSTRAINT providers_pk PRIMARY KEY (id);


--
-- Name: qos qos_qos_type_name_storage_pool_id_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qos
    ADD CONSTRAINT qos_qos_type_name_storage_pool_id_key UNIQUE (qos_type, name, storage_pool_id);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_calendars qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_fired_triggers qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);


--
-- Name: qrtz_job_details qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);


--
-- Name: qrtz_locks qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);


--
-- Name: qrtz_paused_trigger_grps qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);


--
-- Name: qrtz_scheduler_state qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: quota quota_quota_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT quota_quota_name_unique UNIQUE (quota_name);


--
-- Name: schema_version schema_version_primary_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY schema_version
    ADD CONSTRAINT schema_version_primary_key PRIMARY KEY (id);


--
-- Name: storage_domains_ovf_info storage_domains_ovf_info_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domains_ovf_info
    ADD CONSTRAINT storage_domains_ovf_info_pkey PRIMARY KEY (ovf_disk_id);


--
-- Name: storage_server_connection_extension storage_server_connection_extension_vds_id_iqn; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_server_connection_extension
    ADD CONSTRAINT storage_server_connection_extension_vds_id_iqn UNIQUE (vds_id, iqn);


--
-- Name: gluster_server_services unique_gluster_server_services_server_service; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT unique_gluster_server_services_server_service UNIQUE (server_id, service_id);


--
-- Name: gluster_services unique_gluster_services_type_name; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT unique_gluster_services_type_name UNIQUE (service_type, service_name);


--
-- Name: network_filter unique_network_filter_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_filter
    ADD CONSTRAINT unique_network_filter_id UNIQUE (filter_id);


--
-- Name: vm_icon_defaults unique_vm_icon_defaults_record; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_icon_defaults
    ADD CONSTRAINT unique_vm_icon_defaults_record UNIQUE (os_id);


--
-- Name: unregistered_disks unregistered_disks_disk_id_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_disks
    ADD CONSTRAINT unregistered_disks_disk_id_key UNIQUE (disk_id);


--
-- Name: business_entity_snapshot uq_command_id_entity_id; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY business_entity_snapshot
    ADD CONSTRAINT uq_command_id_entity_id UNIQUE (command_id, entity_id, entity_type, snapshot_type);


--
-- Name: users users_domain_external_id_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_domain_external_id_unique UNIQUE (domain, external_id);


--
-- Name: vds_interface vds_interface_vds_id_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT vds_interface_vds_id_name_unique UNIQUE (vds_id, name);


--
-- Name: vds_interface vds_interface_vds_id_network_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT vds_interface_vds_id_network_name_unique UNIQUE (vds_id, network_name);


--
-- Name: vds_static vds_static_host_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT vds_static_host_name_unique UNIQUE (host_name);


--
-- Name: vds_static vds_static_vds_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT vds_static_vds_name_unique UNIQUE (vds_name);


--
-- Name: host_nic_vfs_config vfs_config_id_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_nic_vfs_config
    ADD CONSTRAINT vfs_config_id_pk PRIMARY KEY (id);


--
-- Name: vfs_config_labels vfs_config_labels_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vfs_config_labels
    ADD CONSTRAINT vfs_config_labels_pk PRIMARY KEY (vfs_config_id, label);


--
-- Name: vfs_config_networks vfs_config_networks_pk; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vfs_config_networks
    ADD CONSTRAINT vfs_config_networks_pk PRIMARY KEY (vfs_config_id, network_id);


--
-- Name: host_nic_vfs_config vfs_config_nic_id_unique; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_nic_vfs_config
    ADD CONSTRAINT vfs_config_nic_id_unique UNIQUE (nic_id);


--
-- Name: vm_host_pinning_map vm_host_pinning_map_vm_id_vds_id_key; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_host_pinning_map
    ADD CONSTRAINT vm_host_pinning_map_vm_id_vds_id_key UNIQUE (vm_id, vds_id);


--
-- Name: vm_ovf_generations vm_ovf_generations_pkey; Type: CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_ovf_generations
    ADD CONSTRAINT vm_ovf_generations_pkey PRIMARY KEY (vm_guid);


--
-- Name: audit_log_origin_custom_event_id_idx; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX audit_log_origin_custom_event_id_idx ON audit_log USING btree (origin, custom_event_id) WHERE ((origin)::text !~~* 'ovirt'::text);


--
-- Name: idx_affinity_group_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_affinity_group_cluster_id ON affinity_groups USING btree (cluster_id);


--
-- Name: idx_affinity_group_members_affinity_group_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_affinity_group_members_affinity_group_id ON affinity_group_members USING btree (affinity_group_id);


--
-- Name: idx_affinity_group_members_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_affinity_group_members_vm_id ON affinity_group_members USING btree (vm_id);


--
-- Name: idx_async_tasks_entities_async_task_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_async_tasks_entities_async_task_id ON async_tasks_entities USING btree (async_task_id);


--
-- Name: idx_audit_correlation_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_correlation_id ON audit_log USING btree (correlation_id);


--
-- Name: idx_audit_log_gluster_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_gluster_volume_id ON audit_log USING btree (gluster_volume_id) WHERE (gluster_volume_id IS NOT NULL);


--
-- Name: idx_audit_log_job_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_job_id ON audit_log USING btree (job_id);


--
-- Name: idx_audit_log_log_time; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_log_time ON audit_log USING btree (log_time);


--
-- Name: idx_audit_log_storage_domain_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_storage_domain_name ON audit_log USING btree (storage_domain_name);


--
-- Name: idx_audit_log_storage_pool_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_storage_pool_name ON audit_log USING btree (storage_pool_name);


--
-- Name: idx_audit_log_type_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_type_name ON audit_log USING btree (log_type, log_type_name);


--
-- Name: idx_audit_log_user_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_user_name ON audit_log USING btree (user_name);


--
-- Name: idx_audit_log_vds_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_vds_name ON audit_log USING btree (vds_name);


--
-- Name: idx_audit_log_vm_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_vm_name ON audit_log USING btree (vm_name);


--
-- Name: idx_audit_log_vm_template_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_audit_log_vm_template_name ON audit_log USING btree (vm_template_name);


--
-- Name: idx_base_disks_disk_alias; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_base_disks_disk_alias ON base_disks USING btree (disk_alias);


--
-- Name: idx_business_entity_snapshot_command_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_business_entity_snapshot_command_id ON business_entity_snapshot USING btree (command_id);


--
-- Name: idx_cluster_cluster_policy_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cluster_cluster_policy_id ON cluster USING btree (cluster_policy_id);


--
-- Name: idx_cluster_features_version_and_category; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cluster_features_version_and_category ON cluster_features USING btree (category, version);


--
-- Name: idx_cluster_policy_units_cluster_policy_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cluster_policy_units_cluster_policy_id ON cluster_policy_units USING btree (cluster_policy_id);


--
-- Name: idx_cluster_policy_units_policy_unit_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cluster_policy_units_policy_unit_id ON cluster_policy_units USING btree (policy_unit_id);


--
-- Name: idx_cluster_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cluster_storage_pool_id ON cluster USING btree (storage_pool_id);


--
-- Name: idx_combined_ad_role_object; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_combined_ad_role_object ON permissions USING btree (ad_element_id, role_id, object_id);


--
-- Name: idx_command_assoc_entities_command_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_command_assoc_entities_command_id ON command_assoc_entities USING btree (command_id);


--
-- Name: idx_cpu_profiles_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cpu_profiles_cluster_id ON cpu_profiles USING btree (cluster_id);


--
-- Name: idx_cpu_profiles_create_date; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cpu_profiles_create_date ON cpu_profiles USING btree (_create_date);


--
-- Name: idx_cpu_profiles_qos_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_cpu_profiles_qos_id ON cpu_profiles USING btree (qos_id);


--
-- Name: idx_disk_lun_map_disk_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_disk_lun_map_disk_id ON disk_lun_map USING btree (disk_id);


--
-- Name: idx_disk_lun_map_lun_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_disk_lun_map_lun_id ON disk_lun_map USING btree (lun_id);


--
-- Name: idx_disk_profiles_qos_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_disk_profiles_qos_id ON disk_profiles USING btree (qos_id);


--
-- Name: idx_disk_profiles_storage_domain_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_disk_profiles_storage_domain_id ON disk_profiles USING btree (storage_domain_id);


--
-- Name: idx_engine_backup_log; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_engine_backup_log ON engine_backup_log USING btree (scope, done_at DESC);


--
-- Name: idx_engine_session_session_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_engine_session_session_id ON engine_sessions USING btree (engine_session_id);


--
-- Name: idx_event_notification_hist; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_event_notification_hist ON event_notification_hist USING btree (audit_log_id);


--
-- Name: idx_event_notification_hist_audit_log_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_event_notification_hist_audit_log_id ON event_notification_hist USING btree (audit_log_id);


--
-- Name: idx_event_subscriber_subscriber_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_event_subscriber_subscriber_id ON event_subscriber USING btree (subscriber_id);


--
-- Name: idx_fence_agents_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_fence_agents_vds_id ON fence_agents USING btree (vds_id);


--
-- Name: idx_georep_slave_host_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_georep_slave_host_name ON gluster_georep_session USING btree (slave_host_name);


--
-- Name: idx_georep_slave_volume_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_georep_slave_volume_name ON gluster_georep_session USING btree (slave_volume_name);


--
-- Name: idx_gluster_bricks_task_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_bricks_task_id ON gluster_volume_bricks USING btree (task_id);


--
-- Name: idx_gluster_cluster_services_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_cluster_services_cluster_id ON gluster_cluster_services USING btree (cluster_id);


--
-- Name: idx_gluster_cluster_services_service_type; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_cluster_services_service_type ON gluster_cluster_services USING btree (service_type);


--
-- Name: idx_gluster_georep_config_session_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_georep_config_session_id ON gluster_georep_config USING btree (session_id);


--
-- Name: idx_gluster_georep_session_details_master_brick_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_georep_session_details_master_brick_id ON gluster_georep_session_details USING btree (master_brick_id);


--
-- Name: idx_gluster_georep_session_details_session_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_georep_session_details_session_id ON gluster_georep_session_details USING btree (session_id);


--
-- Name: idx_gluster_georep_session_master_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_georep_session_master_volume_id ON gluster_georep_session USING btree (master_volume_id);


--
-- Name: idx_gluster_georep_session_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_georep_session_unique ON gluster_georep_session USING btree (master_volume_id, session_key);


--
-- Name: idx_gluster_hooks_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_hooks_cluster_id ON gluster_hooks USING btree (cluster_id);


--
-- Name: idx_gluster_hooks_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_hooks_unique ON gluster_hooks USING btree (cluster_id, gluster_command, stage, name);


--
-- Name: idx_gluster_server_hooks_hook_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_server_hooks_hook_id ON gluster_server_hooks USING btree (hook_id);


--
-- Name: idx_gluster_server_hooks_server_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_server_hooks_server_id ON gluster_server_hooks USING btree (server_id);


--
-- Name: idx_gluster_server_hooks_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_server_hooks_unique ON gluster_server_hooks USING btree (hook_id, server_id);


--
-- Name: idx_gluster_server_services_server_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_server_services_server_id ON gluster_server_services USING btree (server_id);


--
-- Name: idx_gluster_server_services_service_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_server_services_service_id ON gluster_server_services USING btree (service_id);


--
-- Name: idx_gluster_server_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_server_unique ON gluster_server USING btree (server_id, gluster_server_uuid);


--
-- Name: idx_gluster_services_service_type; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_services_service_type ON gluster_services USING btree (service_type);


--
-- Name: idx_gluster_volume_access_protocols_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_access_protocols_volume_id ON gluster_volume_access_protocols USING btree (volume_id);


--
-- Name: idx_gluster_volume_bricks_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_bricks_network_id ON gluster_volume_bricks USING btree (network_id);


--
-- Name: idx_gluster_volume_bricks_server_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_bricks_server_id ON gluster_volume_bricks USING btree (server_id);


--
-- Name: idx_gluster_volume_bricks_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_bricks_volume_id ON gluster_volume_bricks USING btree (volume_id);


--
-- Name: idx_gluster_volume_options_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_options_volume_id ON gluster_volume_options USING btree (volume_id);


--
-- Name: idx_gluster_volume_snapshot_config_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_snapshot_config_cluster_id ON gluster_volume_snapshot_config USING btree (cluster_id);


--
-- Name: idx_gluster_volume_snapshot_config_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_volume_snapshot_config_unique ON gluster_volume_snapshot_config USING btree (cluster_id, volume_id, param_name);


--
-- Name: idx_gluster_volume_snapshot_config_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_snapshot_config_volume_id ON gluster_volume_snapshot_config USING btree (volume_id);


--
-- Name: idx_gluster_volume_snapshots_unique; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_gluster_volume_snapshots_unique ON gluster_volume_snapshots USING btree (volume_id, snapshot_name);


--
-- Name: idx_gluster_volume_snapshots_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_snapshots_volume_id ON gluster_volume_snapshots USING btree (volume_id);


--
-- Name: idx_gluster_volume_transport_types_volume_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volume_transport_types_volume_id ON gluster_volume_transport_types USING btree (volume_id);


--
-- Name: idx_gluster_volumes_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_gluster_volumes_cluster_id ON gluster_volumes USING btree (cluster_id);


--
-- Name: idx_host_device_host_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_host_device_host_id ON host_device USING btree (host_id);


--
-- Name: idx_host_device_host_id_parent_device_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_host_device_host_id_parent_device_name ON host_device USING btree (host_id, parent_device_name);


--
-- Name: idx_host_device_host_id_physfn; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_host_device_host_id_physfn ON host_device USING btree (host_id, physfn);


--
-- Name: idx_host_device_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_host_device_vm_id ON host_device USING btree (vm_id);


--
-- Name: idx_image_storage_domain_map_image_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_image_storage_domain_map_image_id ON image_storage_domain_map USING btree (image_id);


--
-- Name: idx_image_storage_domain_map_profile_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_image_storage_domain_map_profile_id ON image_storage_domain_map USING btree (disk_profile_id);


--
-- Name: idx_image_storage_domain_map_quota_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_image_storage_domain_map_quota_id ON image_storage_domain_map USING btree (quota_id);


--
-- Name: idx_image_storage_domain_map_storage_domain_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_image_storage_domain_map_storage_domain_id ON image_storage_domain_map USING btree (storage_domain_id);


--
-- Name: idx_image_transfers_disk_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_image_transfers_disk_id ON image_transfers USING btree (disk_id);


--
-- Name: idx_images_images_group_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_images_images_group_id ON images USING btree (image_group_id);


--
-- Name: idx_iscsi_bonds_networks_map_iscsi_bond_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_iscsi_bonds_networks_map_iscsi_bond_id ON iscsi_bonds_networks_map USING btree (iscsi_bond_id);


--
-- Name: idx_iscsi_bonds_networks_map_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_iscsi_bonds_networks_map_network_id ON iscsi_bonds_networks_map USING btree (network_id);


--
-- Name: idx_iscsi_bonds_storage_connections_map_connection_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_iscsi_bonds_storage_connections_map_connection_id ON iscsi_bonds_storage_connections_map USING btree (connection_id);


--
-- Name: idx_iscsi_bonds_storage_connections_map_iscsi_bond_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_iscsi_bonds_storage_connections_map_iscsi_bond_id ON iscsi_bonds_storage_connections_map USING btree (iscsi_bond_id);


--
-- Name: idx_iscsi_bonds_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_iscsi_bonds_storage_pool_id ON iscsi_bonds USING btree (storage_pool_id);


--
-- Name: idx_job_engine_session_seq_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_job_engine_session_seq_id ON job USING btree (engine_session_seq_id);


--
-- Name: idx_job_start_time; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_job_start_time ON job USING btree (start_time);


--
-- Name: idx_job_subject_entity_entity_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_job_subject_entity_entity_id ON job_subject_entity USING btree (entity_id);


--
-- Name: idx_job_subject_entity_job_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_job_subject_entity_job_id ON job_subject_entity USING btree (job_id);


--
-- Name: idx_labels_map_label_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_labels_map_label_id ON labels_map USING btree (label_id);


--
-- Name: idx_labels_map_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_labels_map_vds_id ON labels_map USING btree (vds_id);


--
-- Name: idx_labels_map_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_labels_map_vm_id ON labels_map USING btree (vm_id);


--
-- Name: idx_libvirt_secrets_provider_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_libvirt_secrets_provider_id ON libvirt_secrets USING btree (provider_id);


--
-- Name: idx_lun_storage_server_connection_map_lun_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_lun_storage_server_connection_map_lun_id ON lun_storage_server_connection_map USING btree (lun_id);


--
-- Name: idx_lun_storage_server_connection_map_storage_server_connection; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_lun_storage_server_connection_map_storage_server_connection ON lun_storage_server_connection_map USING btree (storage_server_connection);


--
-- Name: idx_mac_pool_ranges_mac_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_mac_pool_ranges_mac_pool_id ON mac_pool_ranges USING btree (mac_pool_id);


--
-- Name: idx_network_attachments_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_attachments_network_id ON network_attachments USING btree (network_id);


--
-- Name: idx_network_attachments_nic_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_attachments_nic_id ON network_attachments USING btree (nic_id);


--
-- Name: idx_network_cluster_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_cluster_cluster_id ON network_cluster USING btree (cluster_id);


--
-- Name: idx_network_cluster_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_cluster_network_id ON network_cluster USING btree (network_id);


--
-- Name: idx_network_external_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_external_id ON network USING btree (provider_network_external_id) WHERE (provider_network_external_id IS NOT NULL);


--
-- Name: idx_network_provider_network_provider_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_provider_network_provider_id ON network USING btree (provider_network_provider_id);


--
-- Name: idx_network_qos_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_qos_id ON network USING btree (qos_id);


--
-- Name: idx_network_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_network_storage_pool_id ON network USING btree (storage_pool_id);


--
-- Name: idx_numa_node_cpu_map_numa_node_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_numa_node_cpu_map_numa_node_id ON numa_node_cpu_map USING btree (numa_node_id);


--
-- Name: idx_numa_node_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_numa_node_vds_id ON numa_node USING btree (vds_id);


--
-- Name: idx_numa_node_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_numa_node_vm_id ON numa_node USING btree (vm_id);


--
-- Name: idx_permissions_ad_element_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_permissions_ad_element_id ON permissions USING btree (ad_element_id);


--
-- Name: idx_permissions_object_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_permissions_object_id ON permissions USING btree (object_id);


--
-- Name: idx_permissions_role_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_permissions_role_id ON permissions USING btree (role_id);


--
-- Name: idx_qos_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qos_storage_pool_id ON qos USING btree (storage_pool_id);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_c ON qrtz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_g; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_g ON qrtz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_j ON qrtz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_state; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_qrtz_t_state ON qrtz_triggers USING btree (sched_name, trigger_state);


--
-- Name: idx_quota_limitation_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_quota_limitation_cluster_id ON quota_limitation USING btree (cluster_id) WHERE (cluster_id IS NOT NULL);


--
-- Name: idx_quota_limitation_quota_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_quota_limitation_quota_id ON quota_limitation USING btree (quota_id);


--
-- Name: idx_quota_limitation_storage_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_quota_limitation_storage_id ON quota_limitation USING btree (storage_id) WHERE (storage_id IS NOT NULL);


--
-- Name: idx_repo_file_file_type; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_repo_file_file_type ON repo_file_meta_data USING btree (file_type);


--
-- Name: idx_repo_file_meta_data_repo_domain_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_repo_file_meta_data_repo_domain_id ON repo_file_meta_data USING btree (repo_domain_id);


--
-- Name: idx_roles__app_mode; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_roles__app_mode ON roles USING btree (app_mode);


--
-- Name: idx_roles_groups_action_group_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_roles_groups_action_group_id ON roles_groups USING btree (action_group_id);


--
-- Name: idx_roles_groups_role_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_roles_groups_role_id ON roles_groups USING btree (role_id);


--
-- Name: idx_root_command_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_root_command_id ON command_entities USING btree (root_command_id) WHERE (root_command_id IS NOT NULL);


--
-- Name: idx_snapshots_snapshot_type; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_snapshots_snapshot_type ON snapshots USING btree (snapshot_type);


--
-- Name: idx_snapshots_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_snapshots_vm_id ON snapshots USING btree (vm_id);


--
-- Name: idx_step_external_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_step_external_id ON step USING btree (external_id);


--
-- Name: idx_step_job_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_step_job_id ON step USING btree (job_id);


--
-- Name: idx_step_parent_step_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_step_parent_step_id ON step USING btree (parent_step_id);


--
-- Name: idx_storage_device_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_device_vds_id ON storage_device USING btree (vds_id);


--
-- Name: idx_storage_domains_ovf_info_storage_domain_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_domains_ovf_info_storage_domain_id ON storage_domains_ovf_info USING btree (storage_domain_id);


--
-- Name: idx_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_pool_id ON quota USING btree (storage_pool_id) WHERE (storage_pool_id IS NOT NULL);


--
-- Name: idx_storage_pool_iso_map_storage_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_pool_iso_map_storage_id ON storage_pool_iso_map USING btree (storage_id);


--
-- Name: idx_storage_pool_iso_map_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_pool_iso_map_storage_pool_id ON storage_pool_iso_map USING btree (storage_pool_id);


--
-- Name: idx_storage_pool_mac_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_storage_pool_mac_pool_id ON storage_pool USING btree (mac_pool_id);


--
-- Name: idx_supported_cluster_features; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX idx_supported_cluster_features ON supported_cluster_features USING btree (cluster_id, feature_id);


--
-- Name: idx_supported_cluster_features_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_supported_cluster_features_cluster_id ON supported_cluster_features USING btree (cluster_id);


--
-- Name: idx_supported_cluster_features_feature_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_supported_cluster_features_feature_id ON supported_cluster_features USING btree (feature_id);


--
-- Name: idx_supported_host_features_host_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_supported_host_features_host_id ON supported_host_features USING btree (host_id);


--
-- Name: idx_tags_user_group_map_group_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_user_group_map_group_id ON tags_user_group_map USING btree (group_id);


--
-- Name: idx_tags_user_group_map_tag_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_user_group_map_tag_id ON tags_user_group_map USING btree (tag_id);


--
-- Name: idx_tags_user_map_tag_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_user_map_tag_id ON tags_user_map USING btree (tag_id);


--
-- Name: idx_tags_user_map_user_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_user_map_user_id ON tags_user_map USING btree (user_id);


--
-- Name: idx_tags_vds_map_tag_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vds_map_tag_id ON tags_vds_map USING btree (tag_id);


--
-- Name: idx_tags_vds_map_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vds_map_vds_id ON tags_vds_map USING btree (vds_id);


--
-- Name: idx_tags_vm_map_tag_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vm_map_tag_id ON tags_vm_map USING btree (tag_id);


--
-- Name: idx_tags_vm_map_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vm_map_vm_id ON tags_vm_map USING btree (vm_id);


--
-- Name: idx_tags_vm_pool_map_tag_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vm_pool_map_tag_id ON tags_vm_pool_map USING btree (tag_id);


--
-- Name: idx_tags_vm_pool_map_vm_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_tags_vm_pool_map_vm_pool_id ON tags_vm_pool_map USING btree (vm_pool_id);


--
-- Name: idx_unregistered_ovf_of_entities_storage_domain_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_unregistered_ovf_of_entities_storage_domain_id ON unregistered_ovf_of_entities USING btree (storage_domain_id);


--
-- Name: idx_user_profiles_user_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_user_profiles_user_id ON user_profiles USING btree (user_id);


--
-- Name: idx_vds_cpu_statistics_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_cpu_statistics_vds_id ON vds_cpu_statistics USING btree (vds_id);


--
-- Name: idx_vds_dynamic_status; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_dynamic_status ON vds_dynamic USING btree (status);


--
-- Name: idx_vds_interface_statistics_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_interface_statistics_vds_id ON vds_interface_statistics USING btree (vds_id);


--
-- Name: idx_vds_interface_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_interface_vds_id ON vds_interface USING btree (vds_id);


--
-- Name: idx_vds_kdump_status_status; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_kdump_status_status ON vds_kdump_status USING btree (status);


--
-- Name: idx_vds_spm_id_map_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_spm_id_map_storage_pool_id ON vds_spm_id_map USING btree (storage_pool_id);


--
-- Name: idx_vds_spm_id_map_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_spm_id_map_vds_id ON vds_spm_id_map USING btree (vds_id);


--
-- Name: idx_vds_static_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_static_cluster_id ON vds_static USING btree (cluster_id);


--
-- Name: idx_vds_static_host_provider_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vds_static_host_provider_id ON vds_static USING btree (host_provider_id);


--
-- Name: idx_vdsm_task_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vdsm_task_id ON async_tasks USING btree (vdsm_task_id);


--
-- Name: idx_vfs_config_labels_vfs_config_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vfs_config_labels_vfs_config_id ON vfs_config_labels USING btree (vfs_config_id);


--
-- Name: idx_vfs_config_networks_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vfs_config_networks_network_id ON vfs_config_networks USING btree (network_id);


--
-- Name: idx_vfs_config_networks_vfs_config_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vfs_config_networks_vfs_config_id ON vfs_config_networks USING btree (vfs_config_id);


--
-- Name: idx_vm_device_alias_type_device; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_device_alias_type_device ON vm_device USING btree (alias, type, device);


--
-- Name: idx_vm_device_snapshot_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_device_snapshot_id ON vm_device USING btree (snapshot_id);


--
-- Name: idx_vm_device_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_device_vm_id ON vm_device USING btree (vm_id);


--
-- Name: idx_vm_dynamic_migrating_to_vds; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_dynamic_migrating_to_vds ON vm_dynamic USING btree (migrating_to_vds);


--
-- Name: idx_vm_dynamic_run_on_vds; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_dynamic_run_on_vds ON vm_dynamic USING btree (run_on_vds);


--
-- Name: idx_vm_guest_agent_interfaces_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_guest_agent_interfaces_vm_id ON vm_guest_agent_interfaces USING btree (vm_id);


--
-- Name: idx_vm_host_pinning_map_vds_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_host_pinning_map_vds_id ON vm_host_pinning_map USING btree (vds_id);


--
-- Name: idx_vm_host_pinning_map_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_host_pinning_map_vm_id ON vm_host_pinning_map USING btree (vm_id);


--
-- Name: idx_vm_icon_defaults_large_icon_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_icon_defaults_large_icon_id ON vm_icon_defaults USING btree (large_icon_id);


--
-- Name: idx_vm_icon_defaults_small_icon_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_icon_defaults_small_icon_id ON vm_icon_defaults USING btree (small_icon_id);


--
-- Name: idx_vm_interface_statistics_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_interface_statistics_vm_id ON vm_interface_statistics USING btree (vm_id);


--
-- Name: idx_vm_interface_vm_guid; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_interface_vm_guid ON vm_interface USING btree (vm_guid);


--
-- Name: idx_vm_interface_vm_vmt_guid; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_interface_vm_vmt_guid ON vm_interface USING btree (vm_guid, vmt_guid);


--
-- Name: idx_vm_interface_vmt_guid; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_interface_vmt_guid ON vm_interface USING btree (vmt_guid);


--
-- Name: idx_vm_interface_vnic_profile_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_interface_vnic_profile_id ON vm_interface USING btree (vnic_profile_id);


--
-- Name: idx_vm_jobs_vm_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_jobs_vm_id ON vm_jobs USING btree (vm_id);


--
-- Name: idx_vm_ovf_generations_storage_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_ovf_generations_storage_pool_id ON vm_ovf_generations USING btree (storage_pool_id);


--
-- Name: idx_vm_ovf_generations_vm_guid; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_ovf_generations_vm_guid ON vm_ovf_generations USING btree (vm_guid);


--
-- Name: idx_vm_pool_map_vm_pool_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_pool_map_vm_pool_id ON vm_pool_map USING btree (vm_pool_id);


--
-- Name: idx_vm_pools_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_pools_cluster_id ON vm_pools USING btree (cluster_id);


--
-- Name: idx_vm_static_cluster_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_cluster_id ON vm_static USING btree (cluster_id);


--
-- Name: idx_vm_static_cpu_profile_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_cpu_profile_id ON vm_static USING btree (cpu_profile_id);


--
-- Name: idx_vm_static_large_icon_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_large_icon_id ON vm_static USING btree (large_icon_id);


--
-- Name: idx_vm_static_origin; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_origin ON vm_static USING btree (origin);


--
-- Name: idx_vm_static_provider_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_provider_id ON vm_static USING btree (provider_id);


--
-- Name: idx_vm_static_quota_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_quota_id ON vm_static USING btree (quota_id);


--
-- Name: idx_vm_static_small_icon_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_small_icon_id ON vm_static USING btree (small_icon_id);


--
-- Name: idx_vm_static_template_version_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_template_version_name ON vm_static USING btree (template_version_number);


--
-- Name: idx_vm_static_vm_name; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_vm_name ON vm_static USING btree (vm_name);


--
-- Name: idx_vm_static_vmt_guid; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_static_vmt_guid ON vm_static USING btree (vmt_guid);


--
-- Name: idx_vm_vds_numa_node_map_vds_numa_node_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_vds_numa_node_map_vds_numa_node_id ON vm_vds_numa_node_map USING btree (vds_numa_node_id);


--
-- Name: idx_vm_vds_numa_node_map_vm_numa_node_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vm_vds_numa_node_map_vm_numa_node_id ON vm_vds_numa_node_map USING btree (vm_numa_node_id);


--
-- Name: idx_vnic_profiles_network_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vnic_profiles_network_id ON vnic_profiles USING btree (network_id);


--
-- Name: idx_vnic_profiles_network_qos_id; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX idx_vnic_profiles_network_qos_id ON vnic_profiles USING btree (network_qos_id);


--
-- Name: ix_vdc_options; Type: INDEX; Schema: public; Owner: engine
--

CREATE INDEX ix_vdc_options ON vdc_options USING btree (option_name);


--
-- Name: vm_icons_data_url_unique_index; Type: INDEX; Schema: public; Owner: engine
--

CREATE UNIQUE INDEX vm_icons_data_url_unique_index ON vm_icons USING btree (md5((data_url)::text));


--
-- Name: affinity_groups affinity_group_cluster_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_groups
    ADD CONSTRAINT affinity_group_cluster_id_fk FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: affinity_group_members affinity_group_member_affinity_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_group_members
    ADD CONSTRAINT affinity_group_member_affinity_id_fk FOREIGN KEY (affinity_group_id) REFERENCES affinity_groups(id) ON DELETE CASCADE;


--
-- Name: affinity_group_members affinity_group_member_vm_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_group_members
    ADD CONSTRAINT affinity_group_member_vm_id_fk FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: cpu_profiles cpu_profiles_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cpu_profiles
    ADD CONSTRAINT cpu_profiles_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: cpu_profiles cpu_profiles_qos_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cpu_profiles
    ADD CONSTRAINT cpu_profiles_qos_id_fkey FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL;


--
-- Name: disk_lun_map disk_lun_to_disk_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_to_disk_fk FOREIGN KEY (disk_id) REFERENCES base_disks(disk_id);


--
-- Name: disk_lun_map disk_lun_to_lun_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_to_lun_fk FOREIGN KEY (lun_id) REFERENCES luns(lun_id);


--
-- Name: disk_profiles disk_profiles_qos_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_profiles
    ADD CONSTRAINT disk_profiles_qos_id_fkey FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL;


--
-- Name: disk_profiles disk_profiles_storage_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_profiles
    ADD CONSTRAINT disk_profiles_storage_domain_id_fkey FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: fence_agents fence_agent_host_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY fence_agents
    ADD CONSTRAINT fence_agent_host_id_fk FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: async_tasks_entities fk_async_task_entity; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY async_tasks_entities
    ADD CONSTRAINT fk_async_task_entity FOREIGN KEY (async_task_id) REFERENCES async_tasks(task_id) ON DELETE CASCADE;


--
-- Name: cluster_policy_units fk_cluster_policy_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster_policy_units
    ADD CONSTRAINT fk_cluster_policy_id FOREIGN KEY (cluster_policy_id) REFERENCES cluster_policies(id) ON DELETE CASCADE;


--
-- Name: cluster fk_cluster_storage_pool_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster
    ADD CONSTRAINT fk_cluster_storage_pool_id FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE SET NULL;


--
-- Name: vds_static fk_cluster_vds_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT fk_cluster_vds_static FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id);


--
-- Name: vm_pools fk_cluster_vm_pools; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pools
    ADD CONSTRAINT fk_cluster_vm_pools FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id);


--
-- Name: vm_static fk_cluster_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_cluster_vm_static FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id);


--
-- Name: command_assoc_entities fk_coco_command_assoc_entity; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY command_assoc_entities
    ADD CONSTRAINT fk_coco_command_assoc_entity FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE;


--
-- Name: disk_vm_element fk_disk_vm_element_base_disks; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_vm_element
    ADD CONSTRAINT fk_disk_vm_element_base_disks FOREIGN KEY (disk_id) REFERENCES base_disks(disk_id) ON DELETE CASCADE;


--
-- Name: disk_vm_element fk_disk_vm_element_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_vm_element
    ADD CONSTRAINT fk_disk_vm_element_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: event_notification_hist fk_event_notification_hist_audit_log; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_notification_hist
    ADD CONSTRAINT fk_event_notification_hist_audit_log FOREIGN KEY (audit_log_id) REFERENCES audit_log(audit_log_id) ON DELETE CASCADE;


--
-- Name: event_subscriber fk_event_subscriber_users; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_subscriber
    ADD CONSTRAINT fk_event_subscriber_users FOREIGN KEY (subscriber_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: gluster_georep_config fk_gluster_georep_config_session_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_config
    ADD CONSTRAINT fk_gluster_georep_config_session_id FOREIGN KEY (session_id) REFERENCES gluster_georep_session(session_id) ON DELETE CASCADE;


--
-- Name: gluster_georep_session_details fk_gluster_georep_details_brick_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_session_details
    ADD CONSTRAINT fk_gluster_georep_details_brick_id FOREIGN KEY (master_brick_id) REFERENCES gluster_volume_bricks(id) ON DELETE CASCADE;


--
-- Name: gluster_georep_session_details fk_gluster_georep_details_session_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_session_details
    ADD CONSTRAINT fk_gluster_georep_details_session_id FOREIGN KEY (session_id) REFERENCES gluster_georep_session(session_id) ON DELETE CASCADE;


--
-- Name: gluster_georep_session fk_gluster_georep_session_vol_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_georep_session
    ADD CONSTRAINT fk_gluster_georep_session_vol_id FOREIGN KEY (master_volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_bricks fk_gluster_volume_bricks_network_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT fk_gluster_volume_bricks_network_id FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE RESTRICT;


--
-- Name: gluster_volume_snapshot_config fk_gluster_volume_snapshot_config_cluster_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshot_config
    ADD CONSTRAINT fk_gluster_volume_snapshot_config_cluster_id FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: gluster_volume_snapshot_config fk_gluster_volume_snapshot_config_volume_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshot_config
    ADD CONSTRAINT fk_gluster_volume_snapshot_config_volume_id FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_snapshot_schedules fk_gluster_volume_snapshot_schedules_volume_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshot_schedules
    ADD CONSTRAINT fk_gluster_volume_snapshot_schedules_volume_id FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_snapshots fk_gluster_volume_snapshots_volume_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_snapshots
    ADD CONSTRAINT fk_gluster_volume_snapshots_volume_id FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: host_device fk_host_device_host_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_device
    ADD CONSTRAINT fk_host_device_host_id FOREIGN KEY (host_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: host_device fk_host_device_parent_name; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_device
    ADD CONSTRAINT fk_host_device_parent_name FOREIGN KEY (host_id, parent_device_name) REFERENCES host_device(host_id, device_name) DEFERRABLE;


--
-- Name: host_device fk_host_device_physfn; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_device
    ADD CONSTRAINT fk_host_device_physfn FOREIGN KEY (host_id, physfn) REFERENCES host_device(host_id, device_name) DEFERRABLE;


--
-- Name: host_device fk_host_device_vm_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_device
    ADD CONSTRAINT fk_host_device_vm_id FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE SET NULL;


--
-- Name: image_storage_domain_map fk_image_storage_domain_map_disk_profile_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_disk_profile_id FOREIGN KEY (disk_profile_id) REFERENCES disk_profiles(id) ON DELETE SET NULL;


--
-- Name: image_storage_domain_map fk_image_storage_domain_map_images; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_images FOREIGN KEY (image_id) REFERENCES images(image_guid) ON DELETE CASCADE;


--
-- Name: image_storage_domain_map fk_image_storage_domain_map_quota; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;


--
-- Name: image_storage_domain_map fk_image_storage_domain_map_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_storage_domain_static FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: iscsi_bonds_networks_map fk_iscsi_bonds_networks_map_iscsi_bond_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT fk_iscsi_bonds_networks_map_iscsi_bond_id FOREIGN KEY (iscsi_bond_id) REFERENCES iscsi_bonds(id) ON DELETE CASCADE;


--
-- Name: iscsi_bonds_networks_map fk_iscsi_bonds_networks_map_network_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT fk_iscsi_bonds_networks_map_network_id FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: iscsi_bonds_storage_connections_map fk_iscsi_bonds_storage_connections_map_connection_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT fk_iscsi_bonds_storage_connections_map_connection_id FOREIGN KEY (connection_id) REFERENCES storage_server_connections(id) ON DELETE CASCADE;


--
-- Name: iscsi_bonds_storage_connections_map fk_iscsi_bonds_storage_connections_map_iscsi_bond_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT fk_iscsi_bonds_storage_connections_map_iscsi_bond_id FOREIGN KEY (iscsi_bond_id) REFERENCES iscsi_bonds(id) ON DELETE CASCADE;


--
-- Name: iscsi_bonds fk_iscsi_bonds_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds
    ADD CONSTRAINT fk_iscsi_bonds_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: job_subject_entity fk_job_subject_entity_job; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY job_subject_entity
    ADD CONSTRAINT fk_job_subject_entity_job FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE CASCADE;


--
-- Name: lun_storage_server_connection_map fk_lun_storage_server_connection_map_luns; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT fk_lun_storage_server_connection_map_luns FOREIGN KEY (lun_id) REFERENCES luns(lun_id) ON DELETE CASCADE;


--
-- Name: lun_storage_server_connection_map fk_lun_storage_server_connection_map_storage_server_connections; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT fk_lun_storage_server_connection_map_storage_server_connections FOREIGN KEY (storage_server_connection) REFERENCES storage_server_connections(id) ON DELETE CASCADE;


--
-- Name: network_cluster fk_network_cluster_cluster; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT fk_network_cluster_cluster FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: network_cluster fk_network_cluster_network; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT fk_network_cluster_network FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: network fk_network_provided_by; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_provided_by FOREIGN KEY (provider_network_provider_id) REFERENCES providers(id) ON DELETE CASCADE;


--
-- Name: network fk_network_qos_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_qos_id FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL;


--
-- Name: network fk_network_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE SET NULL;


--
-- Name: numa_node_cpu_map fk_numa_node_cpu_map_numa_node; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY numa_node_cpu_map
    ADD CONSTRAINT fk_numa_node_cpu_map_numa_node FOREIGN KEY (numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE CASCADE;


--
-- Name: numa_node fk_numa_node_vds; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY numa_node
    ADD CONSTRAINT fk_numa_node_vds FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: numa_node fk_numa_node_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY numa_node
    ADD CONSTRAINT fk_numa_node_vm FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: permissions fk_permissions_roles; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT fk_permissions_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;


--
-- Name: qos fk_qos_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qos
    ADD CONSTRAINT fk_qos_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: qrtz_blob_triggers fk_qrtz_blob_triggers_sched_name; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT fk_qrtz_blob_triggers_sched_name FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group) ON DELETE CASCADE;


--
-- Name: qrtz_cron_triggers fk_qrtz_cron_triggers_sched_name; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT fk_qrtz_cron_triggers_sched_name FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group) ON DELETE CASCADE;


--
-- Name: qrtz_simple_triggers fk_qrtz_simple_triggers_sched_name; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT fk_qrtz_simple_triggers_sched_name FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group) ON DELETE CASCADE;


--
-- Name: qrtz_simprop_triggers fk_qrtz_simprop_triggers_sched_name; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT fk_qrtz_simprop_triggers_sched_name FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group) ON DELETE CASCADE;


--
-- Name: quota_limitation fk_quota_limitation_cluster_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT fk_quota_limitation_cluster_id FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: repo_file_meta_data fk_repo_file_meta_data_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY repo_file_meta_data
    ADD CONSTRAINT fk_repo_file_meta_data_storage_domain_static FOREIGN KEY (repo_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: roles_groups fk_roles_groups_action_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY roles_groups
    ADD CONSTRAINT fk_roles_groups_action_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;


--
-- Name: snapshots fk_snapshot_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT fk_snapshot_vm FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid);


--
-- Name: snapshots fk_snapshots_dump_disk_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT fk_snapshots_dump_disk_id FOREIGN KEY (memory_dump_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL;


--
-- Name: snapshots fk_snapshots_metadata_disk_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT fk_snapshots_metadata_disk_id FOREIGN KEY (memory_metadata_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL;


--
-- Name: step fk_step_job; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY step
    ADD CONSTRAINT fk_step_job FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE CASCADE;


--
-- Name: storage_domain_dynamic fk_storage_domain_dynamic_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domain_dynamic
    ADD CONSTRAINT fk_storage_domain_dynamic_storage_domain_static FOREIGN KEY (id) REFERENCES storage_domain_static(id);


--
-- Name: storage_pool_iso_map fk_storage_domain_pool_map_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT fk_storage_domain_pool_map_storage_domain_static FOREIGN KEY (storage_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: storage_pool_iso_map fk_storage_domain_pool_map_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT fk_storage_domain_pool_map_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: tags_vm_pool_map fk_tags_vm_pool_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT fk_tags_vm_pool_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE;


--
-- Name: tags_vm_pool_map fk_tags_vm_pool_map_vm_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT fk_tags_vm_pool_map_vm_pool FOREIGN KEY (vm_pool_id) REFERENCES vm_pools(vm_pool_id) ON DELETE CASCADE;


--
-- Name: unregistered_disks fk_unregistered_disks_storage_domain; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_disks
    ADD CONSTRAINT fk_unregistered_disks_storage_domain FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: unregistered_disks_to_vms fk_unregistered_disks_to_vms; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_disks_to_vms
    ADD CONSTRAINT fk_unregistered_disks_to_vms FOREIGN KEY (disk_id) REFERENCES unregistered_disks(disk_id) ON DELETE CASCADE;


--
-- Name: unregistered_ovf_of_entities fk_unregistered_ovf_of_entities_storage_domain; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY unregistered_ovf_of_entities
    ADD CONSTRAINT fk_unregistered_ovf_of_entities_storage_domain FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: user_profiles fk_user_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY user_profiles
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: vds_cpu_statistics fk_vds_cpu_statistics_vds; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_cpu_statistics
    ADD CONSTRAINT fk_vds_cpu_statistics_vds FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vds_interface_statistics fk_vds_interface_statistics_vds_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface_statistics
    ADD CONSTRAINT fk_vds_interface_statistics_vds_static FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vds_interface fk_vds_interface_vds_interface; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT fk_vds_interface_vds_interface FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vds_kdump_status fk_vds_kdump_status_vds_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_kdump_status
    ADD CONSTRAINT fk_vds_kdump_status_vds_static FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vds_spm_id_map fk_vds_spm_id_map_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT fk_vds_spm_id_map_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: vds_spm_id_map fk_vds_spm_id_map_vds_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT fk_vds_spm_id_map_vds_id FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vds_static fk_vds_static_host_provider_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT fk_vds_static_host_provider_id FOREIGN KEY (host_provider_id) REFERENCES providers(id) ON DELETE SET NULL;


--
-- Name: vds_static fk_vds_static_openstack_network_provider_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT fk_vds_static_openstack_network_provider_id FOREIGN KEY (openstack_network_provider_id) REFERENCES providers(id) ON DELETE SET NULL;


--
-- Name: vm_device fk_vm_device_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT fk_vm_device_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_guest_agent_interfaces fk_vm_guest_agent_interfaces; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_guest_agent_interfaces
    ADD CONSTRAINT fk_vm_guest_agent_interfaces FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_icon_defaults fk_vm_icon_defaults_large_icon_id_vm_icons_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_icon_defaults
    ADD CONSTRAINT fk_vm_icon_defaults_large_icon_id_vm_icons_id FOREIGN KEY (large_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: vm_icon_defaults fk_vm_icon_defaults_small_icon_id_vm_icons_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_icon_defaults
    ADD CONSTRAINT fk_vm_icon_defaults_small_icon_id_vm_icons_id FOREIGN KEY (small_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: vm_interface_statistics fk_vm_interface_statistics_vm_interface; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface_statistics
    ADD CONSTRAINT fk_vm_interface_statistics_vm_interface FOREIGN KEY (id) REFERENCES vm_interface(id) ON DELETE CASCADE;


--
-- Name: vm_interface_statistics fk_vm_interface_statistics_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface_statistics
    ADD CONSTRAINT fk_vm_interface_statistics_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_interface fk_vm_interface_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vm_static FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_interface fk_vm_interface_vm_static_template; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vm_static_template FOREIGN KEY (vmt_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_interface fk_vm_interface_vnic_profile_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vnic_profile_id FOREIGN KEY (vnic_profile_id) REFERENCES vnic_profiles(id) ON DELETE SET NULL;


--
-- Name: vm_jobs fk_vm_jobs_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_jobs
    ADD CONSTRAINT fk_vm_jobs_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_static fk_vm_static_cpu_profile_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_cpu_profile_id FOREIGN KEY (cpu_profile_id) REFERENCES cpu_profiles(id) ON DELETE SET NULL;


--
-- Name: vm_static fk_vm_static_large_icon_id_vm_icons_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_large_icon_id_vm_icons_id FOREIGN KEY (large_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: vm_static fk_vm_static_provider_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_provider_id FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE SET NULL;


--
-- Name: vm_static fk_vm_static_quota; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;


--
-- Name: vm_static fk_vm_static_small_icon_id_vm_icons_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_small_icon_id_vm_icons_id FOREIGN KEY (small_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: vm_vds_numa_node_map fk_vm_vds_numa_node_map_vds_numa_node; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_vds_numa_node_map
    ADD CONSTRAINT fk_vm_vds_numa_node_map_vds_numa_node FOREIGN KEY (vds_numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE SET NULL;


--
-- Name: vm_vds_numa_node_map fk_vm_vds_numa_node_map_vm_numa_node; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_vds_numa_node_map
    ADD CONSTRAINT fk_vm_vds_numa_node_map_vm_numa_node FOREIGN KEY (vm_numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE CASCADE;


--
-- Name: vnic_profiles fk_vnic_profiles_network_qos_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT fk_vnic_profiles_network_qos_id FOREIGN KEY (network_qos_id) REFERENCES qos(id) ON DELETE SET NULL;


--
-- Name: gluster_cluster_services gluster_cluster_services_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT gluster_cluster_services_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: gluster_cluster_services gluster_cluster_services_service_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT gluster_cluster_services_service_type_fkey FOREIGN KEY (service_type) REFERENCES gluster_service_types(service_type) ON DELETE CASCADE;


--
-- Name: gluster_hooks gluster_hooks_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_hooks
    ADD CONSTRAINT gluster_hooks_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: gluster_server_hooks gluster_server_hooks_hook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_hooks
    ADD CONSTRAINT gluster_server_hooks_hook_id_fkey FOREIGN KEY (hook_id) REFERENCES gluster_hooks(id) ON DELETE CASCADE;


--
-- Name: gluster_server_hooks gluster_server_hooks_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_hooks
    ADD CONSTRAINT gluster_server_hooks_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server gluster_server_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server
    ADD CONSTRAINT gluster_server_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server_services gluster_server_services_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT gluster_server_services_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server_services gluster_server_services_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT gluster_server_services_service_id_fkey FOREIGN KEY (service_id) REFERENCES gluster_services(id) ON DELETE CASCADE;


--
-- Name: gluster_services gluster_services_service_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT gluster_services_service_type_fkey FOREIGN KEY (service_type) REFERENCES gluster_service_types(service_type) ON DELETE CASCADE;


--
-- Name: gluster_volume_access_protocols gluster_volume_access_protocols_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_access_protocols
    ADD CONSTRAINT gluster_volume_access_protocols_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_brick_details gluster_volume_brick_details_brick_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_brick_details
    ADD CONSTRAINT gluster_volume_brick_details_brick_id_fkey FOREIGN KEY (brick_id) REFERENCES gluster_volume_bricks(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_bricks gluster_volume_bricks_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT gluster_volume_bricks_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_volume_bricks gluster_volume_bricks_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT gluster_volume_bricks_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_details gluster_volume_details_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_details
    ADD CONSTRAINT gluster_volume_details_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_options gluster_volume_options_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT gluster_volume_options_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_transport_types gluster_volume_transport_types_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_transport_types
    ADD CONSTRAINT gluster_volume_transport_types_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volumes gluster_volumes_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT gluster_volumes_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: labels_map labels_map_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY labels_map
    ADD CONSTRAINT labels_map_label_id_fkey FOREIGN KEY (label_id) REFERENCES labels(label_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: labels_map labels_map_vds_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY labels_map
    ADD CONSTRAINT labels_map_vds_id_fkey FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: labels_map labels_map_vm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY labels_map
    ADD CONSTRAINT labels_map_vm_id_fkey FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: libvirt_secrets libvirt_secrets_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY libvirt_secrets
    ADD CONSTRAINT libvirt_secrets_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE;


--
-- Name: mac_pool_ranges mac_pool_ranges_mac_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY mac_pool_ranges
    ADD CONSTRAINT mac_pool_ranges_mac_pool_id_fkey FOREIGN KEY (mac_pool_id) REFERENCES mac_pools(id) ON DELETE CASCADE;


--
-- Name: network_attachments network_attachments_network_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_attachments
    ADD CONSTRAINT network_attachments_network_id_fkey FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: network_attachments network_attachments_nic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_attachments
    ADD CONSTRAINT network_attachments_nic_id_fkey FOREIGN KEY (nic_id) REFERENCES vds_interface(id) ON DELETE CASCADE;


--
-- Name: vnic_profiles network_filter_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT network_filter_id_fk FOREIGN KEY (network_filter_id) REFERENCES network_filter(filter_id) ON DELETE SET NULL;


--
-- Name: qrtz_triggers qrtz_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_sched_name_fkey FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details(sched_name, job_name, job_group);


--
-- Name: quota_limitation quota_limitation_quota_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT quota_limitation_quota_id_fkey FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE CASCADE;


--
-- Name: quota_limitation quota_limitation_storage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT quota_limitation_storage_id_fkey FOREIGN KEY (storage_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: quota quota_storage_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT quota_storage_pool_id_fkey FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: storage_device storage_device_vds_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_device
    ADD CONSTRAINT storage_device_vds_id_fkey FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: storage_domains_ovf_info storage_domains_ovf_info_ovf_disk_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domains_ovf_info
    ADD CONSTRAINT storage_domains_ovf_info_ovf_disk_id_fkey FOREIGN KEY (ovf_disk_id) REFERENCES base_disks(disk_id) ON DELETE CASCADE;


--
-- Name: storage_domains_ovf_info storage_domains_ovf_info_storage_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domains_ovf_info
    ADD CONSTRAINT storage_domains_ovf_info_storage_domain_id_fkey FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: storage_pool storage_pool_mac_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool
    ADD CONSTRAINT storage_pool_mac_pool_id_fkey FOREIGN KEY (mac_pool_id) REFERENCES mac_pools(id);


--
-- Name: supported_cluster_features supported_cluster_features_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY supported_cluster_features
    ADD CONSTRAINT supported_cluster_features_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES cluster(cluster_id) ON DELETE CASCADE;


--
-- Name: supported_cluster_features supported_cluster_features_feature_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY supported_cluster_features
    ADD CONSTRAINT supported_cluster_features_feature_id_fkey FOREIGN KEY (feature_id) REFERENCES cluster_features(feature_id) ON DELETE CASCADE;


--
-- Name: supported_host_features supported_host_features_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY supported_host_features
    ADD CONSTRAINT supported_host_features_host_id_fkey FOREIGN KEY (host_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: tags_user_group_map tags_user_group_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT tags_user_group_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_user_map tags_user_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT tags_user_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_user_map tags_user_map_user; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT tags_user_map_user FOREIGN KEY (user_id) REFERENCES users(user_id);


--
-- Name: tags_user_group_map tags_user_map_user_group; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT tags_user_map_user_group FOREIGN KEY (group_id) REFERENCES ad_groups(id);


--
-- Name: tags_vds_map tags_vds_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT tags_vds_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_vds_map tags_vds_map_vds; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT tags_vds_map_vds FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: tags_vm_map tags_vm_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT tags_vm_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_vm_map tags_vm_map_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT tags_vm_map_vm FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vds_dynamic vds_static_vds_dynamic; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_dynamic
    ADD CONSTRAINT vds_static_vds_dynamic FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: vds_statistics vds_static_vds_statistics; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_statistics
    ADD CONSTRAINT vds_static_vds_statistics FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: vm_dynamic vds_static_vm_dynamic_m; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vds_static_vm_dynamic_m FOREIGN KEY (migrating_to_vds) REFERENCES vds_static(vds_id);


--
-- Name: vm_dynamic vds_static_vm_dynamic_r; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vds_static_vm_dynamic_r FOREIGN KEY (run_on_vds) REFERENCES vds_static(vds_id);


--
-- Name: vfs_config_labels vfs_config_labels_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vfs_config_labels
    ADD CONSTRAINT vfs_config_labels_id_fk FOREIGN KEY (vfs_config_id) REFERENCES host_nic_vfs_config(id) ON DELETE CASCADE;


--
-- Name: vfs_config_networks vfs_config_networks_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vfs_config_networks
    ADD CONSTRAINT vfs_config_networks_id_fk FOREIGN KEY (vfs_config_id) REFERENCES host_nic_vfs_config(id) ON DELETE CASCADE;


--
-- Name: vfs_config_networks vfs_config_networks_network_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vfs_config_networks
    ADD CONSTRAINT vfs_config_networks_network_fk FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: host_nic_vfs_config vfs_config_nic_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY host_nic_vfs_config
    ADD CONSTRAINT vfs_config_nic_id_fk FOREIGN KEY (nic_id) REFERENCES vds_interface(id) ON DELETE CASCADE;


--
-- Name: vm_device vm_device_snapshot_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT vm_device_snapshot_id_fkey FOREIGN KEY (snapshot_id) REFERENCES snapshots(snapshot_id) ON DELETE CASCADE;


--
-- Name: vm_pool_map vm_guid_pools; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT vm_guid_pools FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_host_pinning_map vm_host_pinning_map_vds_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_host_pinning_map
    ADD CONSTRAINT vm_host_pinning_map_vds_id_fkey FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: vm_host_pinning_map vm_host_pinning_map_vm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_host_pinning_map
    ADD CONSTRAINT vm_host_pinning_map_vm_id_fkey FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_ovf_generations vm_ovf_generations_storage_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_ovf_generations
    ADD CONSTRAINT vm_ovf_generations_storage_pool_id_fkey FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: vm_pool_map vm_pools_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT vm_pools_vm FOREIGN KEY (vm_pool_id) REFERENCES vm_pools(vm_pool_id);


--
-- Name: vm_dynamic vm_static_vm_dynamic; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vm_static_vm_dynamic FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_init vm_static_vm_init; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_init
    ADD CONSTRAINT vm_static_vm_init FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_statistics vm_static_vm_statistics; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_statistics
    ADD CONSTRAINT vm_static_vm_statistics FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_static vm_templates_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT vm_templates_vm_static FOREIGN KEY (vmt_guid) REFERENCES vm_static(vm_guid);


--
-- Name: vnic_profiles vnic_profiles_network_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT vnic_profiles_network_id_fkey FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

