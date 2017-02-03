-- commands adding and updating vm_interface table are not transactive and does not use locking.
-- They validate condition, that VM does not have 2 VmNics with same MAC address, but we cannot rely on that.


-- There might exist VMs, which has VmNics which has duplicate MAC addresses set. In that case, VM won't work, and
-- creation of following constraint won't work either. But as the VM is not working anyways, I believe it's safe to
-- nullify badly assigned MACs.
--
-- We find vm_guid in vm_interface table, where one vm_guid relates to specific mac_addr more than once. Then we nullify
-- mac_addr column in all rows of this vm_guid.
UPDATE
  vm_interface
SET
  mac_addr = NULL
WHERE vm_guid in
  (
    SELECT
      vm_guid
    FROM
      vm_interface
    WHERE
      mac_addr IS NOT NULL
    GROUP BY vm_guid, mac_addr
    HAVING count(mac_addr) > 1
  );

ALTER TABLE vm_interface ADD UNIQUE(vm_guid, mac_addr);
