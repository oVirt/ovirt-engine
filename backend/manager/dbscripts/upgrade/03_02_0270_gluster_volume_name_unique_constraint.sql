-- Remove the existing index which is not unique
DROP INDEX IDX_gluster_volumes_name_unique;

-- Create a unique constraint
ALTER TABLE gluster_volumes ADD CONSTRAINT gluster_volumes_name_unique UNIQUE(cluster_id, vol_name);
