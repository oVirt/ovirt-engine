UPDATE base_disks bd
SET disk_description = format('Memory snapshot disk for snapshot ''%s'' of VM ''%s'' (VM ID: ''%s'')', sn.description, vm.vm_name, vm.vm_guid)
FROM snapshots sn, vm_static vm
WHERE bd.disk_content_type IN (2, 3)
    AND bd.disk_id IN (sn.memory_metadata_disk_id, sn.memory_dump_disk_id)
    AND vm.vm_guid = sn.vm_id;
