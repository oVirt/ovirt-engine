UPDATE luns
SET discard_zeroes_data = FALSE
WHERE discard_zeroes_data IS NOT NULL;

UPDATE disk_vm_element SET pass_discard = FALSE
FROM base_disks
WHERE disk_vm_element.pass_discard = TRUE
      AND base_disks.wipe_after_delete = TRUE
      AND disk_vm_element.disk_id = base_disks.disk_id;
