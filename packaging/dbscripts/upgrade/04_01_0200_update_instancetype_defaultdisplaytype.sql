-- Change InstanceType's Default Display Type from Cirrus (0) to QXL (1)
UPDATE vm_static
SET default_display_type = 1
WHERE entity_type = 'INSTANCE_TYPE'
    AND default_display_type = 0;
