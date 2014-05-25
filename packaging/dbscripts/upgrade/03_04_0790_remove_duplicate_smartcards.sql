-- only one smartcard is supported, remove any duplicates caused by a bug
delete from vm_device a
where device='smartcard'
    and device_id not in (
        select device_id from vm_device where vm_id=a.vm_id and device='smartcard' limit 1);
