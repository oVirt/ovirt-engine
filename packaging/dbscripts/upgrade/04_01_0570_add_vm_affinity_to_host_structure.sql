SELECT fn_db_rename_column('affinity_groups', 'positive', 'vm_positive');
SELECT fn_db_rename_column('affinity_groups', 'enforcing', 'vm_enforcing');
SELECT fn_db_add_column('affinity_groups', 'vds_positive', 'BOOLEAN NOT NULL DEFAULT true');
SELECT fn_db_add_column('affinity_groups', 'vds_enforcing', 'BOOLEAN NOT NULL DEFAULT false');
SELECT fn_db_add_column('affinity_groups', 'vms_affinity_enabled', 'BOOLEAN NOT NULL DEFAULT true');
ALTER TABLE affinity_groups ALTER COLUMN vm_positive DROP NOT NULL;

ALTER TABLE affinity_group_members ALTER COLUMN vm_id DROP NOT NULL;
SELECT fn_db_add_column('affinity_group_members', 'vds_id', 'UUID NULL');
SELECT fn_db_create_index('idx_affinity_group_members_vds_id', 'affinity_group_members', 'vds_id', '', false);
SELECT fn_db_create_constraint('affinity_group_members', 'affinity_group_member_vds_id_fk', 'FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('affinity_group_members','affinity_group_member_vm_or_vds_id_not_null','CHECK ((vm_id IS NOT NULL) OR (vds_id IS NOT NULL))');

