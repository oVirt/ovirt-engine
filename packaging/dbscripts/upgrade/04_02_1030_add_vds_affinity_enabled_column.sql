SELECT fn_db_add_column('affinity_groups', 'vds_affinity_enabled', 'BOOLEAN NOT NULL DEFAULT false');

-- Enable vm to host affinity, if the group has hosts assigned
UPDATE affinity_groups
SET vds_affinity_enabled = true
FROM affinity_group_members
WHERE affinity_group_members.affinity_group_id = affinity_groups.id
  AND affinity_group_members.vds_id IS NOT NULL

