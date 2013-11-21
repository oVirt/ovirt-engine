-- Add architecture type in vds_groups table
select fn_db_add_column('vds_groups', 'architecture', 'INTEGER  NOT NULL DEFAULT 0');

-- Existent clusters with cpu_name are x86_64, since alternative architectures are introduced after this upgrade
UPDATE vds_groups SET architecture = 1 where cpu_name is not NULL and architecture = 0;

-- Existent clusters without cpu_name and containing VMs are x86_64, because these VMs were created with x86_64 specific OSes and devices
UPDATE vds_groups AS c SET architecture = 1 WHERE c.cpu_name is null and exists (select 1 from vm_static where vm_static.vds_group_id = c.vds_group_id and vm_static.vm_guid != '00000000-0000-0000-0000-000000000000');

