SELECT fn_db_add_column('affinity_group_members', 'vm_label_id', 'UUID REFERENCES labels(label_id) ON DELETE CASCADE');
SELECT fn_db_add_column('affinity_group_members', 'host_label_id', 'UUID REFERENCES labels(label_id) ON DELETE CASCADE');


-- Adds the column and sets default to 'true' and then sets the default back to 'false'.
-- As a result, this script is reentrant, because it only sets values to 'true' if the column does not exist.
SELECT fn_db_add_column('labels', 'has_implicit_affinity_group', 'boolean DEFAULT true NOT NULL');
ALTER TABLE labels ALTER COLUMN has_implicit_affinity_group SET DEFAULT false;


SELECT fn_db_drop_constraint('affinity_group_members','affinity_group_member_vm_or_vds_id_not_null');
SELECT fn_db_create_constraint('affinity_group_members','affinity_group_member_not_all_null','CHECK ((vm_id IS NOT NULL) OR (vds_id IS NOT NULL) OR (vm_label_id IS NOT NULL) OR (host_label_id IS NOT NULL))');

