-- Remove the existing index which is not unique
DROP INDEX IDX_gluster_volumes_name_unique;

-- Create a unique constraint
select fn_db_create_constraint('gluster_volumes', 'gluster_volumes_name_unique', 'UNIQUE(cluster_id, vol_name)');
