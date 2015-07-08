-----------------------------------------------------------
-- Pinning VM to multiple Hosts
-----------------------------------------------------------

-- create a mapping table
CREATE TABLE vm_host_pinning_map
(
     vm_id  UUID NOT NULL,
     vds_id UUID NOT NULL,
     FOREIGN KEY (vm_id)  REFERENCES vm_static(vm_guid)  ON DELETE CASCADE,
     FOREIGN KEY (vds_id) REFERENCES vds_static (vds_id) ON DELETE CASCADE,
     UNIQUE (vm_id, vds_id)
);
-- populate mapping table with existing relationships
INSERT INTO vm_host_pinning_map(vm_id, vds_id)
SELECT vm_guid, dedicated_vm_for_vds::uuid FROM vm_static
WHERE dedicated_vm_for_vds IS NOT null;