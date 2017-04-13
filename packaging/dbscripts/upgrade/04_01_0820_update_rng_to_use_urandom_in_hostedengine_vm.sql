UPDATE vm_device as d
SET spec_params = '{"source" : "urandom"}'
FROM vm_static as v
WHERE d.vm_id = v.vm_guid
  AND v.origin = 6
  AND d.alias = 'rng0'
  AND d.spec_params LIKE '%"source" : "random"%'
