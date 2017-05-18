-- remove all malformed memory devices and reset VM device hash for VMs devices of which were removed

-- malformed memory device is device that has 'size' or 'node' spec_param missing or of type 'int'
-- VM device hash is cleaned to invoke new full VM device monitoring cycle

WITH changed_vm_ids AS (
    DELETE FROM vm_device
    WHERE device = 'memory'
          AND type = 'memory'
          AND (NOT spec_params LIKE '%"size"%'
               OR NOT spec_params LIKE '%"node"%'
               OR EXISTS (SELECT regexp_matches(spec_params, '"size"\s*:\s*\d+\s*,?'))
               OR EXISTS (SELECT regexp_matches(spec_params, '"node"\s*:\s*\d+\s*,?'))
               OR spec_params IS NULL)
    RETURNING vm_id
)
UPDATE vm_dynamic
SET hash = NULL
WHERE vm_guid IN (SELECT vm_id FROM changed_vm_ids);
