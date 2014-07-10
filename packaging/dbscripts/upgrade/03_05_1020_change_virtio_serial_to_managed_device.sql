-- change existing unmanaged virtio-serial devices to be managed
update vm_device set is_managed=true where device='virtio-serial';

-- add managed virtio-serial device to each vm that does not have it
Create or replace FUNCTION __temp_add_missing_virio_serial_devices()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_vm_guid UUID;
BEGIN
   FOR v_vm_guid in (select vm_guid from vm_static where entity_type!='INSTANCE_TYPE' and vm_guid not in (select vm_id from vm_device where device='virtio-serial')) LOOP
      INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, custom_properties, snapshot_id)
	VALUES ((SELECT uuid_generate_v1()), v_vm_guid, 'controller', 'virtio-serial', '', '', true, true, '', NULL);
   END LOOP;

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_add_missing_virio_serial_devices();
DROP function __temp_add_missing_virio_serial_devices();
