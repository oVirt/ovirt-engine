-- origin = 6 => MANAGED_HOSTED_ENGINE
-- custom_bios_type = 0 => CLUSTER_DEFAULT
UPDATE vm_static
SET custom_bios_type =
        (CASE
            WHEN EXISTS (SELECT 1 from vm_device where vm_id = vm_guid AND device = 'ide')
            THEN 1 -- if the hosted engine VM has i440fx devices, set to i440fx
            ELSE 2 -- otherwise, set to q35+seabios
         END),
    db_generation = db_generation + 1
WHERE origin = 6 AND custom_bios_type = 0;
