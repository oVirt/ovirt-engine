SELECT fn_db_add_column('vm_static', 'db_generation', 'BIGINT default 1');

-- not added as foreign key so that when vm is removed, its record in vm_ovf_generations record will stay
CREATE TABLE vm_ovf_generations
(
   vm_guid UUID PRIMARY KEY,
   storage_pool_id UUID REFERENCES storage_pool(id) ON DELETE CASCADE,
   ovf_generation BIGINT DEFAULT 0
);

-- Only pre-existing vms should have ovf_generation set to 1
INSERT INTO vm_ovf_generations
      (SELECT vm.vm_guid, sp.id, 1
       FROM vm_static vm ,storage_pool sp, vds_groups vg
       WHERE vg.storage_pool_id = sp.id AND vm.vds_group_id = vg.vds_group_id);

CREATE INDEX IDX_vm_ovf_generations_vm_guid ON vm_ovf_generations(vm_guid);
CREATE INDEX IDX_vm_ovf_generations_storage_pool_id ON vm_ovf_generations(storage_pool_id);