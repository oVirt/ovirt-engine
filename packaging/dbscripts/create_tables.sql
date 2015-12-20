--
-- PostgreSQL database dump
--


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
-- Name: all_vds_group_usage_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE all_vds_group_usage_rs AS (
	quota_vds_group_id uuid,
	quota_id uuid,
	vds_group_id uuid,
	vds_group_name character varying(40),
	virtual_cpu integer,
	virtual_cpu_usage integer,
	mem_size_mb bigint,
	mem_size_mb_usage bigint
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
-- Name: user_permissions; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE user_permissions AS (
	permission_id uuid,
	role_id uuid,
	user_id uuid
);



--
-- Name: vds_group_usage_rs; Type: TYPE; Schema: public; Owner: engine
--

CREATE TYPE vds_group_usage_rs AS (
	virtual_cpu_usage integer,
	mem_size_mb_usage bigint
);





--
-- Name: action_version_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE action_version_map (
    action_type integer NOT NULL,
    cluster_minimal_version character varying(40) NOT NULL,
    storage_pool_minimal_version character varying(40) NOT NULL
);



--
-- Name: ad_groups; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE ad_groups (
    id uuid NOT NULL,
    name character varying(256) NOT NULL,
    domain character varying(100),
    distinguishedname character varying(4000) DEFAULT NULL::character varying,
    active boolean DEFAULT false NOT NULL,
    external_id bytea DEFAULT '\x'::bytea NOT NULL
);



--
-- Name: affinity_group_members; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE affinity_group_members (
    affinity_group_id uuid NOT NULL,
    vm_id uuid NOT NULL
);




--
-- Name: affinity_groups; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: async_tasks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE async_tasks (
    task_id uuid NOT NULL,
    action_type integer NOT NULL,
    status integer NOT NULL,
    result integer NOT NULL,
    action_parameters text,
    action_params_class character varying(256),
    step_id uuid,
    command_id uuid NOT NULL,
    started_at timestamp with time zone,
    storage_pool_id uuid,
    task_type integer DEFAULT 0 NOT NULL,
    task_parameters text,
    task_params_class character varying(256),
    vdsm_task_id uuid,
    root_command_id uuid
);



--
-- Name: async_tasks_entities; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: audit_log; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    vds_group_id uuid,
    vds_group_name character varying(255),
    correlation_id character varying(50),
    job_id uuid,
    quota_id uuid,
    quota_name character varying(60),
    gluster_volume_id uuid,
    gluster_volume_name character varying(1000),
    origin character varying(255) DEFAULT 'oVirt'::character varying,
    custom_event_id integer DEFAULT (-1),
    event_flood_in_sec integer DEFAULT 30,
    custom_data text DEFAULT ''::text,
    deleted boolean DEFAULT false,
    call_stack text DEFAULT ''::text
);



--
-- Name: base_disks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE base_disks (
    disk_id uuid NOT NULL,
    disk_interface character varying(32) NOT NULL,
    wipe_after_delete boolean DEFAULT false NOT NULL,
    propagate_errors character varying(32) DEFAULT 'Off'::character varying NOT NULL,
    disk_alias character varying(255),
    disk_description character varying(500),
    shareable boolean DEFAULT false,
    boot boolean DEFAULT false NOT NULL,
    sgio smallint,
    alignment smallint DEFAULT 0 NOT NULL,
    last_alignment_scan timestamp with time zone
);



--
-- Name: bookmarks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE bookmarks (
    bookmark_id uuid NOT NULL,
    bookmark_name character varying(40),
    bookmark_value character varying(300) NOT NULL
);



--
-- Name: business_entity_snapshot; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: cluster_policies; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: cluster_policy_units; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE cluster_policy_units (
    cluster_policy_id uuid,
    policy_unit_id uuid,
    filter_sequence integer DEFAULT 0,
    factor integer DEFAULT 1
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
-- Name: custom_actions; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE custom_actions (
    action_id integer DEFAULT nextval('custom_actions_seq'::regclass) NOT NULL,
    action_name character varying(50) NOT NULL,
    path character varying(300) NOT NULL,
    tab integer NOT NULL,
    description character varying(4000)
);



--
-- Name: disk_image_dynamic; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: disk_lun_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE disk_lun_map (
    disk_id uuid NOT NULL,
    lun_id character varying NOT NULL
);



--
-- Name: dwh_history_timekeeping; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE dwh_history_timekeeping (
    var_name character varying(50) NOT NULL,
    var_value character varying(255),
    var_datetime timestamp with time zone
);




--
-- Name: dwh_osinfo; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE dwh_osinfo (
    os_id integer NOT NULL,
    os_name character varying(255)
);




--
-- Name: event_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE event_map (
    event_up_name character varying(100) NOT NULL,
    event_down_name character varying(100) NOT NULL
);



--
-- Name: event_notification_hist; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE event_notification_hist (
    subscriber_id uuid NOT NULL,
    event_name character varying(100) NOT NULL,
    audit_log_id bigint NOT NULL,
    method_type character(10) NOT NULL,
    sent_at timestamp with time zone NOT NULL,
    status boolean NOT NULL,
    reason character(255)
);



--
-- Name: event_notification_methods; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE event_notification_methods (
    method_id integer NOT NULL,
    method_type character(10) NOT NULL
);



--
-- Name: event_subscriber; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE event_subscriber (
    subscriber_id uuid NOT NULL,
    event_up_name character varying(100) NOT NULL,
    method_id integer NOT NULL,
    method_address character varying(255),
    tag_name character varying(50) DEFAULT ''::character varying NOT NULL
);




--
-- Name: gluster_cluster_services; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_cluster_services (
    cluster_id uuid NOT NULL,
    service_type character varying(100) NOT NULL,
    status character varying(32) NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone NOT NULL,
    _update_date timestamp with time zone
);




--
-- Name: gluster_hooks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: gluster_server; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_server (
    server_id uuid NOT NULL,
    gluster_server_uuid uuid NOT NULL
);




--
-- Name: gluster_server_hooks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: gluster_server_services; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: gluster_service_types; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_service_types (
    service_type character varying(100) NOT NULL
);



--
-- Name: gluster_services; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_services (
    id uuid NOT NULL,
    service_type character varying(100) NOT NULL,
    service_name character varying(100) NOT NULL
);




--
-- Name: gluster_volume_access_protocols; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_volume_access_protocols (
    volume_id uuid NOT NULL,
    access_protocol character varying(32) NOT NULL
);




--
-- Name: gluster_volume_bricks; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    task_id uuid
);




--
-- Name: gluster_volume_options; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_volume_options (
    volume_id uuid NOT NULL,
    option_key character varying(8192) NOT NULL,
    option_val character varying(8192) NOT NULL,
    id uuid NOT NULL
);



--
-- Name: gluster_volume_transport_types; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE gluster_volume_transport_types (
    volume_id uuid NOT NULL,
    transport_type character varying(32) NOT NULL
);



--
-- Name: gluster_volumes; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    task_id uuid
);



--
-- Name: image_storage_domain_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE image_storage_domain_map (
    image_id uuid NOT NULL,
    storage_domain_id uuid NOT NULL,
    quota_id uuid
);



--
-- Name: images; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    active boolean DEFAULT false NOT NULL
);




--
-- Name: iscsi_bonds; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE iscsi_bonds (
    id uuid NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(4000),
    storage_pool_id uuid NOT NULL
);



--
-- Name: iscsi_bonds_networks_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE iscsi_bonds_networks_map (
    iscsi_bond_id uuid NOT NULL,
    network_id uuid NOT NULL
);



--
-- Name: iscsi_bonds_storage_connections_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE iscsi_bonds_storage_connections_map (
    iscsi_bond_id uuid NOT NULL,
    connection_id character varying(50) NOT NULL
);




--
-- Name: job; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    is_auto_cleared boolean DEFAULT true
);



--
-- Name: job_subject_entity; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE job_subject_entity (
    job_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(32) NOT NULL
);



--
-- Name: lun_storage_server_connection_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE lun_storage_server_connection_map (
    lun_id character varying(255) NOT NULL,
    storage_server_connection character varying(50) NOT NULL
);



--
-- Name: luns; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: materialized_views; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: network; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: network_cluster; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE network_cluster (
    network_id uuid NOT NULL,
    cluster_id uuid NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    is_display boolean DEFAULT false NOT NULL,
    required boolean DEFAULT true NOT NULL,
    migration boolean DEFAULT false NOT NULL
);




--
-- Name: network_qos; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE network_qos (
    id uuid NOT NULL,
    name character varying(50),
    storage_pool_id uuid,
    inbound_average integer,
    inbound_peak integer,
    inbound_burst integer,
    outbound_average integer,
    outbound_peak integer,
    outbound_burst integer,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone
);



--
-- Name: object_column_white_list; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE object_column_white_list (
    object_name character varying(128) NOT NULL,
    column_name character varying(128) NOT NULL
);



--
-- Name: object_column_white_list_sql; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE object_column_white_list_sql (
    object_name character varying(128) NOT NULL,
    sql text NOT NULL
);




--
-- Name: permissions; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE permissions (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    ad_element_id uuid NOT NULL,
    object_id uuid NOT NULL,
    object_type_id integer NOT NULL
);



--
-- Name: policy_units; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: providers; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    agent_configuration text
);



--
-- Name: quota; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE quota (
    id uuid NOT NULL,
    storage_pool_id uuid NOT NULL,
    quota_name character varying(65) NOT NULL,
    description character varying(250),
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    threshold_vds_group_percentage integer DEFAULT 80,
    threshold_storage_percentage integer DEFAULT 80,
    grace_vds_group_percentage integer DEFAULT 20,
    grace_storage_percentage integer DEFAULT 20
);



--
-- Name: quota_limitation; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE quota_limitation (
    id uuid NOT NULL,
    quota_id uuid NOT NULL,
    storage_id uuid,
    vds_group_id uuid,
    virtual_cpu integer,
    mem_size_mb bigint,
    storage_size_gb bigint
);



--
-- Name: repo_file_meta_data; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE repo_file_meta_data (
    repo_domain_id uuid NOT NULL,
    repo_image_id character varying(256) NOT NULL,
    size bigint DEFAULT 0,
    date_created timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    last_refreshed bigint DEFAULT 0,
    file_type integer DEFAULT 0,
    repo_image_name character varying(256)
);



--
-- Name: roles; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: roles_groups; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: schema_version; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: snapshots; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    memory_volume character varying(255)
);



--
-- Name: step; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: storage_domain_dynamic; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE storage_domain_dynamic (
    id uuid NOT NULL,
    available_disk_size integer,
    used_disk_size integer,
    _update_date timestamp with time zone
);



--
-- Name: storage_domain_static; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    storage_comment text
);



--
-- Name: storage_pool; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    is_local boolean
);



--
-- Name: storage_pool_iso_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE storage_pool_iso_map (
    storage_id uuid NOT NULL,
    storage_pool_id uuid NOT NULL,
    status integer
);



--
-- Name: storage_server_connections; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE storage_server_connections (
    id character varying(50) NOT NULL,
    connection character varying(250) NOT NULL,
    user_name character varying(50),
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
-- Name: tags; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: tags_user_group_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE tags_user_group_map (
    tag_id uuid NOT NULL,
    group_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_user_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE tags_user_map (
    tag_id uuid NOT NULL,
    user_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_vds_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE tags_vds_map (
    tag_id uuid NOT NULL,
    vds_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: tags_vm_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE tags_vm_map (
    tag_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    defaultdisplaytype integer DEFAULT 0
);



--
-- Name: tags_vm_pool_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE tags_vm_pool_map (
    tag_id uuid NOT NULL,
    vm_pool_id uuid NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone
);



--
-- Name: users; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE users (
    user_id uuid NOT NULL,
    name character varying(255),
    surname character varying(255),
    domain character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    groups character varying NOT NULL,
    department character varying(255),
    role character varying(255),
    email character varying(255),
    note character varying(255),
    last_admin_check_status boolean DEFAULT false NOT NULL,
    group_ids text,
    external_id bytea DEFAULT '\x'::bytea NOT NULL,
    active boolean DEFAULT false NOT NULL
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
-- Name: vdc_db_log; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: vdc_options; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vdc_options (
    option_id integer DEFAULT nextval('vdc_options_seq'::regclass) NOT NULL,
    option_name character varying(100) NOT NULL,
    option_value character varying(4000) NOT NULL,
    version character varying(40) DEFAULT 'general'::character varying NOT NULL
);



--
-- Name: vds_dynamic; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    cpu_over_commit_time_stamp timestamp with time zone,
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
    hooks character varying(4000) DEFAULT ''::character varying,
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
    supported_emulated_machines character varying(255),
    gluster_version character varying(4000),
    controlled_by_pm_policy boolean DEFAULT false
);



--
-- Name: vds_groups; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vds_groups (
    vds_group_id uuid NOT NULL,
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
    enable_ksm boolean DEFAULT true NOT NULL
);



--
-- Name: vds_interface; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    qos_overridden boolean DEFAULT false NOT NULL
);



--
-- Name: vds_interface_statistics; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vds_interface_statistics (
    id uuid NOT NULL,
    vds_id uuid,
    rx_rate numeric(18,0),
    tx_rate numeric(18,0),
    rx_drop numeric(18,0),
    tx_drop numeric(18,0),
    iface_status integer,
    _update_date timestamp with time zone
);



--
-- Name: vds_spm_id_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vds_spm_id_map (
    storage_pool_id uuid NOT NULL,
    vds_spm_id integer NOT NULL,
    vds_id uuid NOT NULL
);



--
-- Name: vds_static; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vds_static (
    vds_id uuid NOT NULL,
    vds_name character varying(255) NOT NULL,
    ip character varying(255),
    vds_unique_id character varying(128),
    host_name character varying(255) NOT NULL,
    port integer NOT NULL,
    vds_group_id uuid NOT NULL,
    server_ssl_enabled boolean,
    vds_type integer DEFAULT 0 NOT NULL,
    vds_strength integer DEFAULT 100 NOT NULL,
    pm_type character varying(20),
    pm_user character varying(50),
    pm_password text,
    pm_port integer,
    pm_options character varying(4000) DEFAULT ''::character varying NOT NULL,
    pm_enabled boolean DEFAULT false NOT NULL,
    _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
    _update_date timestamp with time zone,
    otp_validity bigint,
    vds_spm_priority smallint DEFAULT 5,
    recoverable boolean DEFAULT true NOT NULL,
    sshkeyfingerprint character varying(128),
    pm_proxy_preferences character varying(255) DEFAULT ''::character varying,
    pm_secondary_ip character varying(255),
    pm_secondary_type character varying(20),
    pm_secondary_user character varying(50),
    pm_secondary_password text,
    pm_secondary_port integer,
    pm_secondary_options character varying(4000),
    pm_secondary_concurrent boolean DEFAULT false,
    console_address character varying(255) DEFAULT NULL::character varying,
    ssh_username character varying(255),
    ssh_port integer,
    free_text_comment text,
    disable_auto_pm boolean DEFAULT false,
    CONSTRAINT vds_static_vds_spm_priority_check CHECK (((vds_spm_priority >= (-1)) AND (vds_spm_priority <= 10)))
);



--
-- Name: vds_statistics; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    anonymous_hugepages integer
);



--
-- Name: vm_device; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_device (
    device_id uuid NOT NULL,
    vm_id uuid NOT NULL,
    type character varying(30) NOT NULL,
    device character varying(30) NOT NULL,
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
    snapshot_id uuid
);



--
-- Name: vm_dynamic; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_dynamic (
    vm_guid uuid NOT NULL,
    status integer NOT NULL,
    vm_ip text,
    vm_host character varying(255),
    vm_pid integer,
    last_start_time timestamp with time zone,
    guest_cur_user_name character varying(255),
    guest_last_login_time timestamp with time zone,
    guest_last_logout_time timestamp with time zone,
    guest_os character varying(255),
    run_on_vds uuid,
    migrating_to_vds uuid,
    app_list text,
    display integer,
    acpi_enable boolean,
    session integer,
    display_ip character varying(255),
    display_type integer,
    kvm_enable boolean,
    display_secure_port integer,
    utc_diff integer,
    last_vds_run_on uuid,
    client_ip character varying(255),
    guest_requested_memory integer,
    hibernation_vol_handle character varying(255),
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
    current_cd character varying(4000) DEFAULT NULL::character varying
);



--
-- Name: vm_guest_agent_interfaces; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_guest_agent_interfaces (
    vm_id uuid NOT NULL,
    interface_name text,
    mac_address character varying(59),
    ipv4_addresses text,
    ipv6_addresses text
);




--
-- Name: vm_init; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    custom_script text
);




--
-- Name: vm_interface; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
-- Name: vm_interface_statistics; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_interface_statistics (
    id uuid NOT NULL,
    vm_id uuid,
    rx_rate numeric(18,0),
    tx_rate numeric(18,0),
    rx_drop numeric(18,0),
    tx_drop numeric(18,0),
    iface_status integer,
    _update_date timestamp with time zone
);



--
-- Name: vm_ovf_generations; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_ovf_generations (
    vm_guid uuid NOT NULL,
    storage_pool_id uuid,
    ovf_generation bigint DEFAULT 0
);



--
-- Name: vm_pool_map; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_pool_map (
    vm_pool_id uuid,
    vm_guid uuid NOT NULL
);



--
-- Name: vm_pools; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_pools (
    vm_pool_id uuid NOT NULL,
    vm_pool_name character varying(255) NOT NULL,
    vm_pool_description character varying(4000) NOT NULL,
    vm_pool_type integer,
    parameters character varying(200),
    vds_group_id uuid,
    prestarted_vms smallint DEFAULT 0,
    max_assigned_vms_per_user smallint DEFAULT 1,
    vm_pool_comment text,
    spice_proxy character varying(255)
);



--
-- Name: vm_static; Type: TABLE; Schema: public; Owner: engine; Tablespace:
--

CREATE TABLE vm_static (
    vm_guid uuid NOT NULL,
    vm_name character varying(255) NOT NULL,
    mem_size_mb integer NOT NULL,
    vmt_guid uuid NOT NULL,
    os integer DEFAULT 0 NOT NULL,
    description character varying(4000),
    vds_group_id uuid NOT NULL,
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
    dedicated_vm_for_vds uuid,
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
    template_version_name character varying(40) DEFAULT NULL::character varying
);



--
-- Name: vm_statistics; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    _update_date timestamp with time zone
);




--
-- Name: vnic_profiles; Type: TABLE; Schema: public; Owner: engine; Tablespace:
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
    network_qos_id uuid
);



--
-- Name: affinity_group_pk; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY affinity_groups
    ADD CONSTRAINT affinity_group_pk PRIMARY KEY (id);


--
-- Name: cluster_policy_pk; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY cluster_policies
    ADD CONSTRAINT cluster_policy_pk PRIMARY KEY (id);


--
-- Name: disk_lun_map_pk; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_map_pk PRIMARY KEY (disk_id, lun_id);


--
-- Name: gluster_volumes_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT gluster_volumes_name_unique UNIQUE (cluster_id, vol_name);


--
-- Name: groups_domain_external_id_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY ad_groups
    ADD CONSTRAINT groups_domain_external_id_unique UNIQUE (domain, external_id);


--
-- Name: idx_gluster_volume_bricks_volume_server_brickdir; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT idx_gluster_volume_bricks_volume_server_brickdir UNIQUE (volume_id, server_id, brick_dir);


--
-- Name: idx_gluster_volume_options_volume_id_option_key; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT idx_gluster_volume_options_volume_id_option_key UNIQUE (volume_id, option_key);


--
-- Name: materialized_views_pkey; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY materialized_views
    ADD CONSTRAINT materialized_views_pkey PRIMARY KEY (mv_name);


--
-- Name: pk_action_version_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY action_version_map
    ADD CONSTRAINT pk_action_version_map PRIMARY KEY (action_type);


--
-- Name: pk_ad_group_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY ad_groups
    ADD CONSTRAINT pk_ad_group_id PRIMARY KEY (id);


--
-- Name: pk_async_tasks; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY async_tasks
    ADD CONSTRAINT pk_async_tasks PRIMARY KEY (task_id);


--
-- Name: pk_audit_log; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY audit_log
    ADD CONSTRAINT pk_audit_log PRIMARY KEY (audit_log_id);


--
-- Name: pk_bookmarks; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY bookmarks
    ADD CONSTRAINT pk_bookmarks PRIMARY KEY (bookmark_id);


--
-- Name: pk_custom_actions; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY custom_actions
    ADD CONSTRAINT pk_custom_actions PRIMARY KEY (action_name, tab);


--
-- Name: pk_disk_image_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY disk_image_dynamic
    ADD CONSTRAINT pk_disk_image_dynamic PRIMARY KEY (image_id);


--
-- Name: pk_disks; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY base_disks
    ADD CONSTRAINT pk_disks PRIMARY KEY (disk_id);


--
-- Name: pk_event_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY event_map
    ADD CONSTRAINT pk_event_map PRIMARY KEY (event_up_name);


--
-- Name: pk_event_notification_methods; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY event_notification_methods
    ADD CONSTRAINT pk_event_notification_methods PRIMARY KEY (method_id);


--
-- Name: pk_event_subscriber; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY event_subscriber
    ADD CONSTRAINT pk_event_subscriber PRIMARY KEY (subscriber_id, event_up_name, method_id, tag_name);


--
-- Name: pk_gluster_cluster_services; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT pk_gluster_cluster_services PRIMARY KEY (cluster_id, service_type);


--
-- Name: pk_gluster_hooks; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_hooks
    ADD CONSTRAINT pk_gluster_hooks PRIMARY KEY (id);


--
-- Name: pk_gluster_server; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_server
    ADD CONSTRAINT pk_gluster_server PRIMARY KEY (server_id);


--
-- Name: pk_gluster_server_services; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT pk_gluster_server_services PRIMARY KEY (id);


--
-- Name: pk_gluster_service_types; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_service_types
    ADD CONSTRAINT pk_gluster_service_types PRIMARY KEY (service_type);


--
-- Name: pk_gluster_services; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT pk_gluster_services PRIMARY KEY (id);


--
-- Name: pk_gluster_volume_access_protocols; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_access_protocols
    ADD CONSTRAINT pk_gluster_volume_access_protocols PRIMARY KEY (volume_id, access_protocol);


--
-- Name: pk_gluster_volume_bricks; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT pk_gluster_volume_bricks PRIMARY KEY (id);


--
-- Name: pk_gluster_volume_options; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT pk_gluster_volume_options PRIMARY KEY (id);


--
-- Name: pk_gluster_volume_transport_types; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volume_transport_types
    ADD CONSTRAINT pk_gluster_volume_transport_types PRIMARY KEY (volume_id, transport_type);


--
-- Name: pk_gluster_volumes; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT pk_gluster_volumes PRIMARY KEY (id);


--
-- Name: pk_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY business_entity_snapshot
    ADD CONSTRAINT pk_id PRIMARY KEY (id);


--
-- Name: pk_image_storage_domain_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT pk_image_storage_domain_map PRIMARY KEY (image_id, storage_domain_id);


--
-- Name: pk_images; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY images
    ADD CONSTRAINT pk_images PRIMARY KEY (image_guid);


--
-- Name: pk_iscsi_bonds; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY iscsi_bonds
    ADD CONSTRAINT pk_iscsi_bonds PRIMARY KEY (id);


--
-- Name: pk_iscsi_bonds_networks_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT pk_iscsi_bonds_networks_map PRIMARY KEY (iscsi_bond_id, network_id);


--
-- Name: pk_iscsi_bonds_storage_connections_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT pk_iscsi_bonds_storage_connections_map PRIMARY KEY (iscsi_bond_id, connection_id);


--
-- Name: pk_jobs; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY job
    ADD CONSTRAINT pk_jobs PRIMARY KEY (job_id);


--
-- Name: pk_jobs_subject_entity; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY job_subject_entity
    ADD CONSTRAINT pk_jobs_subject_entity PRIMARY KEY (job_id, entity_id);


--
-- Name: pk_lun_storage_server_connection_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT pk_lun_storage_server_connection_map PRIMARY KEY (lun_id, storage_server_connection);


--
-- Name: pk_luns; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY luns
    ADD CONSTRAINT pk_luns PRIMARY KEY (lun_id);


--
-- Name: pk_network; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY network
    ADD CONSTRAINT pk_network PRIMARY KEY (id);


--
-- Name: pk_network_cluster; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT pk_network_cluster PRIMARY KEY (network_id, cluster_id);


--
-- Name: pk_network_qos_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY network_qos
    ADD CONSTRAINT pk_network_qos_id PRIMARY KEY (id);


--
-- Name: pk_object_column_white_list; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY object_column_white_list
    ADD CONSTRAINT pk_object_column_white_list PRIMARY KEY (object_name, column_name);


--
-- Name: pk_object_column_white_list_sql; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY object_column_white_list_sql
    ADD CONSTRAINT pk_object_column_white_list_sql PRIMARY KEY (object_name);


--
-- Name: pk_os_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY dwh_osinfo
    ADD CONSTRAINT pk_os_id PRIMARY KEY (os_id);


--
-- Name: pk_permissions_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT pk_permissions_id PRIMARY KEY (id);


--
-- Name: pk_quota; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT pk_quota PRIMARY KEY (id);


--
-- Name: pk_quota_limitation; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT pk_quota_limitation PRIMARY KEY (id);


--
-- Name: pk_repo_file_meta_data; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY repo_file_meta_data
    ADD CONSTRAINT pk_repo_file_meta_data PRIMARY KEY (repo_domain_id, repo_image_id);


--
-- Name: pk_roles_groups; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY roles_groups
    ADD CONSTRAINT pk_roles_groups PRIMARY KEY (role_id, action_group_id);


--
-- Name: pk_roles_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT pk_roles_id PRIMARY KEY (id);


--
-- Name: pk_snapshots; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT pk_snapshots PRIMARY KEY (snapshot_id);


--
-- Name: pk_steps; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY step
    ADD CONSTRAINT pk_steps PRIMARY KEY (step_id);


--
-- Name: pk_storage; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY storage_domain_static
    ADD CONSTRAINT pk_storage PRIMARY KEY (id);


--
-- Name: pk_storage_domain_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY storage_domain_dynamic
    ADD CONSTRAINT pk_storage_domain_dynamic PRIMARY KEY (id);


--
-- Name: pk_storage_domain_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT pk_storage_domain_pool_map PRIMARY KEY (storage_id, storage_pool_id);


--
-- Name: pk_storage_pool; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY storage_pool
    ADD CONSTRAINT pk_storage_pool PRIMARY KEY (id);


--
-- Name: pk_storage_server; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY storage_server_connections
    ADD CONSTRAINT pk_storage_server PRIMARY KEY (id);


--
-- Name: pk_tags_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags
    ADD CONSTRAINT pk_tags_id PRIMARY KEY (tag_id);


--
-- Name: pk_tags_user_group_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT pk_tags_user_group_map PRIMARY KEY (tag_id, group_id);


--
-- Name: pk_tags_user_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT pk_tags_user_map PRIMARY KEY (tag_id, user_id);


--
-- Name: pk_tags_vds_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT pk_tags_vds_map PRIMARY KEY (tag_id, vds_id);


--
-- Name: pk_tags_vm_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT pk_tags_vm_map PRIMARY KEY (tag_id, vm_id);


--
-- Name: pk_tags_vm_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT pk_tags_vm_pool_map PRIMARY KEY (tag_id, vm_pool_id);


--
-- Name: pk_users; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY users
    ADD CONSTRAINT pk_users PRIMARY KEY (user_id);


--
-- Name: pk_vdc_db_log; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vdc_db_log
    ADD CONSTRAINT pk_vdc_db_log PRIMARY KEY (error_id);


--
-- Name: pk_vdc_options; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vdc_options
    ADD CONSTRAINT pk_vdc_options PRIMARY KEY (option_id);


--
-- Name: pk_vds_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_dynamic
    ADD CONSTRAINT pk_vds_dynamic PRIMARY KEY (vds_id);


--
-- Name: pk_vds_groups; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_groups
    ADD CONSTRAINT pk_vds_groups PRIMARY KEY (vds_group_id);


--
-- Name: pk_vds_interface; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT pk_vds_interface PRIMARY KEY (id);


--
-- Name: pk_vds_interface_statistics; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_interface_statistics
    ADD CONSTRAINT pk_vds_interface_statistics PRIMARY KEY (id);


--
-- Name: pk_vds_spm_id_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT pk_vds_spm_id_map PRIMARY KEY (storage_pool_id, vds_spm_id);


--
-- Name: pk_vds_static; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT pk_vds_static PRIMARY KEY (vds_id);


--
-- Name: pk_vds_statistics; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_statistics
    ADD CONSTRAINT pk_vds_statistics PRIMARY KEY (vds_id);


--
-- Name: pk_vm_device; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT pk_vm_device PRIMARY KEY (device_id, vm_id);


--
-- Name: pk_vm_dynamic; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT pk_vm_dynamic PRIMARY KEY (vm_guid);


--
-- Name: pk_vm_init; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_init
    ADD CONSTRAINT pk_vm_init PRIMARY KEY (vm_id);


--
-- Name: pk_vm_interface; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT pk_vm_interface PRIMARY KEY (id);


--
-- Name: pk_vm_interface_statistics; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_interface_statistics
    ADD CONSTRAINT pk_vm_interface_statistics PRIMARY KEY (id);


--
-- Name: pk_vm_pool_map; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT pk_vm_pool_map PRIMARY KEY (vm_guid);


--
-- Name: pk_vm_pools; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_pools
    ADD CONSTRAINT pk_vm_pools PRIMARY KEY (vm_pool_id);


--
-- Name: pk_vm_static; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT pk_vm_static PRIMARY KEY (vm_guid);


--
-- Name: pk_vm_statistics; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_statistics
    ADD CONSTRAINT pk_vm_statistics PRIMARY KEY (vm_guid);


--
-- Name: pk_vnic_profiles_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT pk_vnic_profiles_id PRIMARY KEY (id);


--
-- Name: policy_unit_pk; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY policy_units
    ADD CONSTRAINT policy_unit_pk PRIMARY KEY (id);


--
-- Name: providers_pk; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY providers
    ADD CONSTRAINT providers_pk PRIMARY KEY (id);


--
-- Name: quota_quota_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT quota_quota_name_unique UNIQUE (quota_name);


--
-- Name: schema_version_primary_key; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY schema_version
    ADD CONSTRAINT schema_version_primary_key PRIMARY KEY (id);


--
-- Name: unique_gluster_server_services_server_service; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT unique_gluster_server_services_server_service UNIQUE (server_id, service_id);


--
-- Name: unique_gluster_services_type_name; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT unique_gluster_services_type_name UNIQUE (service_type, service_name);


--
-- Name: uq_command_id_entity_id; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY business_entity_snapshot
    ADD CONSTRAINT uq_command_id_entity_id UNIQUE (command_id, entity_id, entity_type, snapshot_type);


--
-- Name: users_domain_external_id_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_domain_external_id_unique UNIQUE (domain, external_id);


--
-- Name: vds_static_host_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT vds_static_host_name_unique UNIQUE (host_name);


--
-- Name: vds_static_vds_name_unique; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT vds_static_vds_name_unique UNIQUE (vds_name);


--
-- Name: vm_ovf_generations_pkey; Type: CONSTRAINT; Schema: public; Owner: engine; Tablespace:
--

ALTER TABLE ONLY vm_ovf_generations
    ADD CONSTRAINT vm_ovf_generations_pkey PRIMARY KEY (vm_guid);


--
-- Name: audit_log_origin_custom_event_id_idx; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE UNIQUE INDEX audit_log_origin_custom_event_id_idx ON audit_log USING btree (origin, custom_event_id) WHERE ((origin)::text !~~* 'ovirt'::text);


--
-- Name: idx_affinity_group_cluster_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_affinity_group_cluster_id ON affinity_groups USING btree (cluster_id);


--
-- Name: idx_audit_correlation_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_correlation_id ON audit_log USING btree (correlation_id);


--
-- Name: idx_audit_log_job_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_job_id ON audit_log USING btree (job_id);


--
-- Name: idx_audit_log_log_time; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_log_time ON audit_log USING btree (log_time);


--
-- Name: idx_audit_log_storage_domain_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_storage_domain_name ON audit_log USING btree (storage_domain_name);


--
-- Name: idx_audit_log_storage_pool_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_storage_pool_name ON audit_log USING btree (storage_pool_name);


--
-- Name: idx_audit_log_user_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_user_name ON audit_log USING btree (user_name);


--
-- Name: idx_audit_log_vds_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_vds_name ON audit_log USING btree (vds_name);


--
-- Name: idx_audit_log_vm_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_vm_name ON audit_log USING btree (vm_name);


--
-- Name: idx_audit_log_vm_template_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_audit_log_vm_template_name ON audit_log USING btree (vm_template_name);


--
-- Name: idx_business_entity_snapshot_command_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_business_entity_snapshot_command_id ON business_entity_snapshot USING btree (command_id);


--
-- Name: idx_combined_ad_role_object; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE UNIQUE INDEX idx_combined_ad_role_object ON permissions USING btree (ad_element_id, role_id, object_id);


--
-- Name: idx_gluster_bricks_task_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_bricks_task_id ON gluster_volume_bricks USING btree (task_id);


--
-- Name: idx_gluster_cluster_services_cluster_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_cluster_services_cluster_id ON gluster_cluster_services USING btree (cluster_id);


--
-- Name: idx_gluster_hooks_cluster_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_hooks_cluster_id ON gluster_hooks USING btree (cluster_id);


--
-- Name: idx_gluster_hooks_unique; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE UNIQUE INDEX idx_gluster_hooks_unique ON gluster_hooks USING btree (cluster_id, gluster_command, stage, name);


--
-- Name: idx_gluster_server_hooks_unique; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE UNIQUE INDEX idx_gluster_server_hooks_unique ON gluster_server_hooks USING btree (hook_id, server_id);


--
-- Name: idx_gluster_server_services_server_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_server_services_server_id ON gluster_server_services USING btree (server_id);


--
-- Name: idx_gluster_server_unique; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE UNIQUE INDEX idx_gluster_server_unique ON gluster_server USING btree (server_id, gluster_server_uuid);


--
-- Name: idx_gluster_volume_access_protocols_volume_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_volume_access_protocols_volume_id ON gluster_volume_access_protocols USING btree (volume_id);


--
-- Name: idx_gluster_volume_bricks_volume_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_volume_bricks_volume_id ON gluster_volume_bricks USING btree (volume_id);


--
-- Name: idx_gluster_volume_options_volume_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_volume_options_volume_id ON gluster_volume_options USING btree (volume_id);


--
-- Name: idx_gluster_volume_transport_types_volume_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_volume_transport_types_volume_id ON gluster_volume_transport_types USING btree (volume_id);


--
-- Name: idx_gluster_volumes_cluster_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_gluster_volumes_cluster_id ON gluster_volumes USING btree (cluster_id);


--
-- Name: idx_job_start_time; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_job_start_time ON job USING btree (start_time);


--
-- Name: idx_job_subject_entity_entity_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_job_subject_entity_entity_id ON job_subject_entity USING btree (entity_id);


--
-- Name: idx_network_external_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_network_external_id ON network USING btree (provider_network_external_id) WHERE (provider_network_external_id IS NOT NULL);


--
-- Name: idx_network_qos_storage_pool_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_network_qos_storage_pool_id ON network_qos USING btree (storage_pool_id);


--
-- Name: idx_network_storage_pool_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_network_storage_pool_id ON network USING btree (storage_pool_id);


--
-- Name: idx_permissions_ad_element_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_permissions_ad_element_id ON permissions USING btree (ad_element_id);


--
-- Name: idx_permissions_object_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_permissions_object_id ON permissions USING btree (object_id);


--
-- Name: idx_permissions_role_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_permissions_role_id ON permissions USING btree (role_id);


--
-- Name: idx_quota_limitation_quota_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_quota_limitation_quota_id ON quota_limitation USING btree (quota_id);


--
-- Name: idx_quota_limitation_storage_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_quota_limitation_storage_id ON quota_limitation USING btree (storage_id) WHERE (storage_id IS NOT NULL);


--
-- Name: idx_quota_limitation_vds_group_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_quota_limitation_vds_group_id ON quota_limitation USING btree (vds_group_id) WHERE (vds_group_id IS NOT NULL);


--
-- Name: idx_repo_file_file_type; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_repo_file_file_type ON repo_file_meta_data USING btree (file_type);


--
-- Name: idx_roles__app_mode; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_roles__app_mode ON roles USING btree (app_mode);


--
-- Name: idx_roles_groups_action_group_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_roles_groups_action_group_id ON roles_groups USING btree (action_group_id);


--
-- Name: idx_step_external_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_step_external_id ON step USING btree (external_id);


--
-- Name: idx_step_job_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_step_job_id ON step USING btree (job_id);


--
-- Name: idx_step_parent_step_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_step_parent_step_id ON step USING btree (parent_step_id);


--
-- Name: idx_storage_pool_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_storage_pool_id ON quota USING btree (storage_pool_id) WHERE (storage_pool_id IS NOT NULL);


--
-- Name: idx_vds_interface_vds_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vds_interface_vds_id ON vds_interface USING btree (vds_id);


--
-- Name: idx_vdsm_task_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vdsm_task_id ON async_tasks USING btree (vdsm_task_id);


--
-- Name: idx_vm_dynamic_run_on_vds; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_dynamic_run_on_vds ON vm_dynamic USING btree (run_on_vds);


--
-- Name: idx_vm_guest_agent_interfaces_vm_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_guest_agent_interfaces_vm_id ON vm_guest_agent_interfaces USING btree (vm_id);


--
-- Name: idx_vm_interface_vm_vmt_guid; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_interface_vm_vmt_guid ON vm_interface USING btree (vm_guid, vmt_guid);


--
-- Name: idx_vm_interface_vnic_profile_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_interface_vnic_profile_id ON vm_interface USING btree (vnic_profile_id);


--
-- Name: idx_vm_ovf_generations_storage_pool_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_ovf_generations_storage_pool_id ON vm_ovf_generations USING btree (storage_pool_id);


--
-- Name: idx_vm_ovf_generations_vm_guid; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_ovf_generations_vm_guid ON vm_ovf_generations USING btree (vm_guid);


--
-- Name: idx_vm_static_vm_name; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vm_static_vm_name ON vm_static USING btree (vm_name);


--
-- Name: idx_vnic_profiles_network_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vnic_profiles_network_id ON vnic_profiles USING btree (network_id);


--
-- Name: idx_vnic_profiles_network_qos_id; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX idx_vnic_profiles_network_qos_id ON vnic_profiles USING btree (network_qos_id);


--
-- Name: ix_vdc_options; Type: INDEX; Schema: public; Owner: engine; Tablespace:
--

CREATE INDEX ix_vdc_options ON vdc_options USING btree (option_name);


--
-- Name: affinity_group_cluster_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_groups
    ADD CONSTRAINT affinity_group_cluster_id_fk FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: affinity_group_member_affinity_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_group_members
    ADD CONSTRAINT affinity_group_member_affinity_id_fk FOREIGN KEY (affinity_group_id) REFERENCES affinity_groups(id) ON DELETE CASCADE;


--
-- Name: affinity_group_member_vm_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY affinity_group_members
    ADD CONSTRAINT affinity_group_member_vm_id_fk FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: disk_lun_to_disk_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_to_disk_fk FOREIGN KEY (disk_id) REFERENCES base_disks(disk_id);


--
-- Name: disk_lun_to_lun_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_lun_map
    ADD CONSTRAINT disk_lun_to_lun_fk FOREIGN KEY (lun_id) REFERENCES luns(lun_id);


--
-- Name: fk_async_task_entity; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY async_tasks_entities
    ADD CONSTRAINT fk_async_task_entity FOREIGN KEY (async_task_id) REFERENCES async_tasks(task_id) ON DELETE CASCADE;


--
-- Name: fk_cluster_policy_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster_policy_units
    ADD CONSTRAINT fk_cluster_policy_id FOREIGN KEY (cluster_policy_id) REFERENCES cluster_policies(id) ON DELETE CASCADE;


--
-- Name: fk_disk_image_dynamic_images; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY disk_image_dynamic
    ADD CONSTRAINT fk_disk_image_dynamic_images FOREIGN KEY (image_id) REFERENCES images(image_guid) ON DELETE CASCADE;


--
-- Name: fk_event_notification_hist_audit_log; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_notification_hist
    ADD CONSTRAINT fk_event_notification_hist_audit_log FOREIGN KEY (audit_log_id) REFERENCES audit_log(audit_log_id) ON DELETE CASCADE;


--
-- Name: fk_event_notification_users; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_notification_hist
    ADD CONSTRAINT fk_event_notification_users FOREIGN KEY (subscriber_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: fk_event_subscriber_event_map; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_subscriber
    ADD CONSTRAINT fk_event_subscriber_event_map FOREIGN KEY (event_up_name) REFERENCES event_map(event_up_name) ON DELETE CASCADE;


--
-- Name: fk_event_subscriber_event_notification_methods; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_subscriber
    ADD CONSTRAINT fk_event_subscriber_event_notification_methods FOREIGN KEY (method_id) REFERENCES event_notification_methods(method_id) ON DELETE CASCADE;


--
-- Name: fk_event_subscriber_users; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY event_subscriber
    ADD CONSTRAINT fk_event_subscriber_users FOREIGN KEY (subscriber_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: fk_image_storage_domain_map_images; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_images FOREIGN KEY (image_id) REFERENCES images(image_guid) ON DELETE CASCADE;


--
-- Name: fk_image_storage_domain_map_quota; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;


--
-- Name: fk_image_storage_domain_map_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY image_storage_domain_map
    ADD CONSTRAINT fk_image_storage_domain_map_storage_domain_static FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: fk_iscsi_bonds_networks_map_iscsi_bond_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT fk_iscsi_bonds_networks_map_iscsi_bond_id FOREIGN KEY (iscsi_bond_id) REFERENCES iscsi_bonds(id) ON DELETE CASCADE;


--
-- Name: fk_iscsi_bonds_networks_map_network_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_networks_map
    ADD CONSTRAINT fk_iscsi_bonds_networks_map_network_id FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: fk_iscsi_bonds_storage_connections_map_connection_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT fk_iscsi_bonds_storage_connections_map_connection_id FOREIGN KEY (connection_id) REFERENCES storage_server_connections(id) ON DELETE CASCADE;


--
-- Name: fk_iscsi_bonds_storage_connections_map_iscsi_bond_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds_storage_connections_map
    ADD CONSTRAINT fk_iscsi_bonds_storage_connections_map_iscsi_bond_id FOREIGN KEY (iscsi_bond_id) REFERENCES iscsi_bonds(id) ON DELETE CASCADE;


--
-- Name: fk_iscsi_bonds_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY iscsi_bonds
    ADD CONSTRAINT fk_iscsi_bonds_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: fk_job_subject_entity_job; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY job_subject_entity
    ADD CONSTRAINT fk_job_subject_entity_job FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE CASCADE;


--
-- Name: fk_lun_storage_server_connection_map_luns; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT fk_lun_storage_server_connection_map_luns FOREIGN KEY (lun_id) REFERENCES luns(lun_id) ON DELETE CASCADE;


--
-- Name: fk_lun_storage_server_connection_map_storage_server_connections; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY lun_storage_server_connection_map
    ADD CONSTRAINT fk_lun_storage_server_connection_map_storage_server_connections FOREIGN KEY (storage_server_connection) REFERENCES storage_server_connections(id) ON DELETE CASCADE;


--
-- Name: fk_network_cluster_network; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT fk_network_cluster_network FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: fk_network_cluster_vds_groups; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_cluster
    ADD CONSTRAINT fk_network_cluster_vds_groups FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: fk_network_provided_by; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_provided_by FOREIGN KEY (provider_network_provider_id) REFERENCES providers(id) ON DELETE CASCADE;


--
-- Name: fk_network_qos_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_qos_id FOREIGN KEY (qos_id) REFERENCES network_qos(id) ON DELETE SET NULL;


--
-- Name: fk_network_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fk_network_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE SET NULL;


--
-- Name: fk_permissions_roles; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT fk_permissions_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;


--
-- Name: fk_policy_unit_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY cluster_policy_units
    ADD CONSTRAINT fk_policy_unit_id FOREIGN KEY (policy_unit_id) REFERENCES policy_units(id) ON DELETE CASCADE;


--
-- Name: fk_repo_file_meta_data_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY repo_file_meta_data
    ADD CONSTRAINT fk_repo_file_meta_data_storage_domain_static FOREIGN KEY (repo_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: fk_roles_groups_action_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY roles_groups
    ADD CONSTRAINT fk_roles_groups_action_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;


--
-- Name: fk_snapshot_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY snapshots
    ADD CONSTRAINT fk_snapshot_vm FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid);


--
-- Name: fk_step_job; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY step
    ADD CONSTRAINT fk_step_job FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE CASCADE;


--
-- Name: fk_storage_domain_dynamic_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_domain_dynamic
    ADD CONSTRAINT fk_storage_domain_dynamic_storage_domain_static FOREIGN KEY (id) REFERENCES storage_domain_static(id);


--
-- Name: fk_storage_domain_pool_map_storage_domain_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT fk_storage_domain_pool_map_storage_domain_static FOREIGN KEY (storage_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: fk_storage_domain_pool_map_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY storage_pool_iso_map
    ADD CONSTRAINT fk_storage_domain_pool_map_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: fk_tags_vm_pool_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT fk_tags_vm_pool_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE;


--
-- Name: fk_tags_vm_pool_map_vm_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_pool_map
    ADD CONSTRAINT fk_tags_vm_pool_map_vm_pool FOREIGN KEY (vm_pool_id) REFERENCES vm_pools(vm_pool_id) ON DELETE CASCADE;


--
-- Name: fk_vds_groups_storage_pool_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_groups
    ADD CONSTRAINT fk_vds_groups_storage_pool_id FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE SET NULL;


--
-- Name: fk_vds_groups_vm_pools; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pools
    ADD CONSTRAINT fk_vds_groups_vm_pools FOREIGN KEY (vds_group_id) REFERENCES vds_groups(vds_group_id);


--
-- Name: fk_vds_interface_statistics_vds_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface_statistics
    ADD CONSTRAINT fk_vds_interface_statistics_vds_static FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: fk_vds_interface_vds_interface; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_interface
    ADD CONSTRAINT fk_vds_interface_vds_interface FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: fk_vds_spm_id_map_storage_pool; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT fk_vds_spm_id_map_storage_pool FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: fk_vds_spm_id_map_vds_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_spm_id_map
    ADD CONSTRAINT fk_vds_spm_id_map_vds_id FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: fk_vds_static_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vds_static_vm_static FOREIGN KEY (dedicated_vm_for_vds) REFERENCES vds_static(vds_id);


--
-- Name: fk_vm_device_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT fk_vm_device_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: fk_vm_guest_agent_interfaces; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_guest_agent_interfaces
    ADD CONSTRAINT fk_vm_guest_agent_interfaces FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: fk_vm_interface_statistics_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface_statistics
    ADD CONSTRAINT fk_vm_interface_statistics_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: fk_vm_interface_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vm_static FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: fk_vm_interface_vm_static_template; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vm_static_template FOREIGN KEY (vmt_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: fk_vm_interface_vnic_profile_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_interface
    ADD CONSTRAINT fk_vm_interface_vnic_profile_id FOREIGN KEY (vnic_profile_id) REFERENCES vnic_profiles(id) ON DELETE SET NULL;


--
-- Name: fk_vm_static_quota; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT fk_vm_static_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;


--
-- Name: fk_vnic_profiles_network_qos_id; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT fk_vnic_profiles_network_qos_id FOREIGN KEY (network_qos_id) REFERENCES network_qos(id) ON DELETE SET NULL;


--
-- Name: gluster_cluster_services_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT gluster_cluster_services_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: gluster_cluster_services_service_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_cluster_services
    ADD CONSTRAINT gluster_cluster_services_service_type_fkey FOREIGN KEY (service_type) REFERENCES gluster_service_types(service_type) ON DELETE CASCADE;


--
-- Name: gluster_hooks_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_hooks
    ADD CONSTRAINT gluster_hooks_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: gluster_server_hooks_hook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_hooks
    ADD CONSTRAINT gluster_server_hooks_hook_id_fkey FOREIGN KEY (hook_id) REFERENCES gluster_hooks(id) ON DELETE CASCADE;


--
-- Name: gluster_server_hooks_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_hooks
    ADD CONSTRAINT gluster_server_hooks_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server
    ADD CONSTRAINT gluster_server_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server_services_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT gluster_server_services_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_server_services_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_server_services
    ADD CONSTRAINT gluster_server_services_service_id_fkey FOREIGN KEY (service_id) REFERENCES gluster_services(id) ON DELETE CASCADE;


--
-- Name: gluster_services_service_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_services
    ADD CONSTRAINT gluster_services_service_type_fkey FOREIGN KEY (service_type) REFERENCES gluster_service_types(service_type) ON DELETE CASCADE;


--
-- Name: gluster_volume_access_protocols_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_access_protocols
    ADD CONSTRAINT gluster_volume_access_protocols_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_bricks_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT gluster_volume_bricks_server_id_fkey FOREIGN KEY (server_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE;


--
-- Name: gluster_volume_bricks_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_bricks
    ADD CONSTRAINT gluster_volume_bricks_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_options_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_options
    ADD CONSTRAINT gluster_volume_options_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volume_transport_types_volume_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volume_transport_types
    ADD CONSTRAINT gluster_volume_transport_types_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE;


--
-- Name: gluster_volumes_cluster_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY gluster_volumes
    ADD CONSTRAINT gluster_volumes_cluster_id_fkey FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: image_templates_images; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY images
    ADD CONSTRAINT image_templates_images FOREIGN KEY (it_guid) REFERENCES images(image_guid);


--
-- Name: network_qos_storage_pool_fk; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY network_qos
    ADD CONSTRAINT network_qos_storage_pool_fk FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: quota_limitation_quota_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT quota_limitation_quota_id_fkey FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE CASCADE;


--
-- Name: quota_limitation_storage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT quota_limitation_storage_id_fkey FOREIGN KEY (storage_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE;


--
-- Name: quota_limitation_vds_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota_limitation
    ADD CONSTRAINT quota_limitation_vds_group_id_fkey FOREIGN KEY (vds_group_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE;


--
-- Name: quota_storage_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY quota
    ADD CONSTRAINT quota_storage_pool_id_fkey FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: tags_user_group_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT tags_user_group_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_user_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT tags_user_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_user_map_user; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_map
    ADD CONSTRAINT tags_user_map_user FOREIGN KEY (user_id) REFERENCES users(user_id);


--
-- Name: tags_user_map_user_group; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_user_group_map
    ADD CONSTRAINT tags_user_map_user_group FOREIGN KEY (group_id) REFERENCES ad_groups(id);


--
-- Name: tags_vds_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT tags_vds_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_vds_map_vds; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vds_map
    ADD CONSTRAINT tags_vds_map_vds FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: tags_vm_map_tag; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT tags_vm_map_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id);


--
-- Name: tags_vm_map_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY tags_vm_map
    ADD CONSTRAINT tags_vm_map_vm FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vds_groups_cluster_policy; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_groups
    ADD CONSTRAINT vds_groups_cluster_policy FOREIGN KEY (cluster_policy_id) REFERENCES cluster_policies(id);


--
-- Name: vds_groups_vds_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_static
    ADD CONSTRAINT vds_groups_vds_static FOREIGN KEY (vds_group_id) REFERENCES vds_groups(vds_group_id);


--
-- Name: vds_groups_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT vds_groups_vm_static FOREIGN KEY (vds_group_id) REFERENCES vds_groups(vds_group_id);


--
-- Name: vds_static_vds_dynamic; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_dynamic
    ADD CONSTRAINT vds_static_vds_dynamic FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: vds_static_vds_statistics; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vds_statistics
    ADD CONSTRAINT vds_static_vds_statistics FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id);


--
-- Name: vds_static_vm_dynamic_m; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vds_static_vm_dynamic_m FOREIGN KEY (migrating_to_vds) REFERENCES vds_static(vds_id);


--
-- Name: vds_static_vm_dynamic_r; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vds_static_vm_dynamic_r FOREIGN KEY (run_on_vds) REFERENCES vds_static(vds_id);


--
-- Name: vm_device_snapshot_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_device
    ADD CONSTRAINT vm_device_snapshot_id_fkey FOREIGN KEY (snapshot_id) REFERENCES snapshots(snapshot_id) ON DELETE CASCADE;


--
-- Name: vm_guid_pools; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT vm_guid_pools FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_ovf_generations_storage_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_ovf_generations
    ADD CONSTRAINT vm_ovf_generations_storage_pool_id_fkey FOREIGN KEY (storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;


--
-- Name: vm_pools_vm; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_pool_map
    ADD CONSTRAINT vm_pools_vm FOREIGN KEY (vm_pool_id) REFERENCES vm_pools(vm_pool_id);


--
-- Name: vm_static_vm_dynamic; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_dynamic
    ADD CONSTRAINT vm_static_vm_dynamic FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_static_vm_init; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_init
    ADD CONSTRAINT vm_static_vm_init FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_static_vm_statistics; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_statistics
    ADD CONSTRAINT vm_static_vm_statistics FOREIGN KEY (vm_guid) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;


--
-- Name: vm_templates_vm_static; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vm_static
    ADD CONSTRAINT vm_templates_vm_static FOREIGN KEY (vmt_guid) REFERENCES vm_static(vm_guid);


--
-- Name: vnic_profiles_network_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: engine
--

ALTER TABLE ONLY vnic_profiles
    ADD CONSTRAINT vnic_profiles_network_id_fkey FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

