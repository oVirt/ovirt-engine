-- rename columns
SELECT fn_db_rename_column('audit_log', 'vds_group_id', 'cluster_id');
SELECT fn_db_rename_column('audit_log', 'vds_group_name', 'cluster_name');
SELECT fn_db_rename_column('quota', 'threshold_vds_group_percentage', 'threshold_cluster_percentage');
SELECT fn_db_rename_column('quota', 'grace_vds_group_percentage', 'grace_cluster_percentage');
SELECT fn_db_rename_column('quota_limitation', 'vds_group_id', 'cluster_id');
SELECT fn_db_rename_column('vds_groups', 'vds_group_id', 'cluster_id');
SELECT fn_db_rename_column('vds_static', 'vds_group_id', 'cluster_id');
SELECT fn_db_rename_column('vm_pools', 'vds_group_id', 'cluster_id');
SELECT fn_db_rename_column('vm_static', 'vds_group_id', 'cluster_id');

-- rename table
SELECT fn_db_rename_table('vds_groups', 'cluster');

-- delete redundant config values

DELETE from vdc_options where option_name ilike '%vdsgroup%';

-- drop redundant types (those are already created with correct names)

DROP TYPE IF EXISTS all_vds_group_usage_rs;
DROP TYPE IF EXISTS vds_group_usage_rs;

-- rename primary keys

ALTER TABLE cluster RENAME CONSTRAINT pk_vds_groups TO pk_cluster;

-- rename FKs

ALTER TABLE network_cluster RENAME CONSTRAINT fk_network_cluster_vds_groups TO fk_network_cluster_cluster;
ALTER TABLE cluster RENAME CONSTRAINT fk_vds_groups_storage_pool_id TO fk_cluster_storage_pool_id;
ALTER TABLE vm_pools RENAME CONSTRAINT fk_vds_groups_vm_pools TO fk_cluster_vm_pools;
ALTER TABLE quota_limitation RENAME CONSTRAINT quota_limitation_vds_group_id_fkey TO fk_quota_limitation_cluster_id;
ALTER TABLE vds_static RENAME CONSTRAINT vds_groups_vds_static TO fk_cluster_vds_static;
ALTER TABLE vm_static RENAME CONSTRAINT vds_groups_vm_static TO fk_cluster_vm_static;

-- update indexes

ALTER INDEX IF EXISTS idx_quota_limitation_vds_group_id RENAME TO idx_quota_limitation_cluster_id;
ALTER INDEX IF EXISTS idx_vds_groups_cluster_policy_id RENAME TO idx_cluster_cluster_policy_id;
ALTER INDEX IF EXISTS idx_vds_groups_storage_pool_id RENAME TO idx_cluster_storage_pool_id;
ALTER INDEX IF EXISTS idx_vds_static_vds_group_id RENAME TO idx_vds_static_cluster_id;
ALTER INDEX IF EXISTS idx_vm_pools_vds_group_id RENAME TO idx_vm_pools_cluster_id;
ALTER INDEX IF EXISTS idx_vm_static_vds_group_id RENAME TO idx_vm_static_cluster_id;

-- update event_map up/down names

UPDATE event_map SET
    event_up_name =
        replace(event_up_name,'VDS_GROUP','CLUSTER'),
    event_down_name =
        replace(event_down_name,'VDS_GROUP','CLUSTER');


