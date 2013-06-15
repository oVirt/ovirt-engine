CREATE OR REPLACE FUNCTION __temp_add_vm_device()
RETURNS void
AS $function$
BEGIN
   IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'vm_device') THEN
       -- Add the vm_device table.
       CREATE TABLE vm_device
       (
          device_id UUID NOT NULL,
          vm_id UUID NOT NULL CONSTRAINT fk_vm_device_vm_static REFERENCES vm_static(vm_guid) ON DELETE CASCADE,
          type varchar(30) NOT NULL,
          device varchar(30) NOT NULL,
          address varchar(255) NOT NULL,
          boot_order int default 0,
          spec_params text,
          is_managed boolean NOT NULL default false,
          is_plugged boolean NULL ,
          is_shared boolean NOT NULL default false,
          is_readonly boolean NOT NULL default false,
          CONSTRAINT pk_vm_device  PRIMARY KEY(device_id,vm_id)
       )
       WITH OIDS;
   END IF;

END; $function$
LANGUAGE plpgsql;

SELECT * FROM __temp_add_vm_device();
DROP FUNCTION __temp_add_vm_device();

