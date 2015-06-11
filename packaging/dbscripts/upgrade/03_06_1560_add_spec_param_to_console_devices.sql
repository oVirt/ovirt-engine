-- add new specParam attribute to console devices
Create or replace FUNCTION __temp_add_spec_param_to_console_devices()
RETURNS VOID
   AS $procedure$
BEGIN
    update vm_device set spec_params =
'{
  "enableSocket" : "true"
}'
   where device='console' and spec_params='{ }';
RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_add_spec_param_to_console_devices();
DROP function __temp_add_spec_param_to_console_devices();


