-- This replaces /dev/random with /dev/urandom RNG source for non-cluster vm-like entities (i.e. "Blank" template
-- and instance types. Cluster-aware entities (VMs and other templates) are updated dynamically in UpdateClusterCommand.
-- The replace should not affect entities with custom compatibility version.
UPDATE vm_device
SET spec_params = replace(spec_params, '"random"', '"urandom"')
FROM vm_static
WHERE vm_device.vm_id = vm_static.vm_guid
    AND vm_static.cluster_id IS NULL
    AND vm_device.type = 'rng'
    AND vm_device.device = 'virtio'
    AND vm_static.custom_compatibility_version IS NULL;
