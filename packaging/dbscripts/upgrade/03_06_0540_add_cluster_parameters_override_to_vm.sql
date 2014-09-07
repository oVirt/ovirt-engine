-- add basic fields to db (cpu_name already exists)
select fn_db_add_column('vm_static', 'custom_emulated_machine', 'character varying(40)');
select fn_db_add_column('vm_static', 'custom_cpu_name', 'character varying(40)');
select fn_db_add_column('vm_dynamic', 'emulated_machine', 'varchar(255)');

-- cpu_name now represents the vdsVerb, convert all old values
UPDATE vm_dynamic SET cpu_name=converted_query.new_cpu_name FROM (SELECT vm_guid, CASE cpu_name
  WHEN 'Intel Conroe Family' THEN 'Conroe'
  WHEN 'Intel Penryn Family' THEN 'Penryn'
  WHEN 'Intel Nehalem Family' THEN 'Nehalem'
  WHEN 'Intel Westmere Family' THEN 'Westmere'
  WHEN 'Intel SandyBridge Family' THEN 'Conroe'
  WHEN 'Intel Haswell Family' THEN 'Haswell'

  WHEN 'AMD Opteron G1' THEN 'Opteron_G1'
  WHEN 'AMD Opteron G2' THEN 'Opteron_G2'
  WHEN 'AMD Opteron G3' THEN 'Opteron_G3'
  WHEN 'AMD Opteron G4' THEN 'Opteron_G4'
  WHEN 'AMD Opteron G5' THEN 'Opteron_G5'

  WHEN 'IBM POWER 7 v2.0' THEN 'POWER7_v2.0'
  WHEN 'IBM POWER 7 v2.1' THEN 'POWER7_v2.1'
  WHEN 'IBM POWER 7 v2.3' THEN 'POWER7_v2.3'
  WHEN 'IBM POWER 7+ v2.1' THEN 'POWER7+_v2.1'
  WHEN 'IBM POWER 8 v1.0' THEN 'POWER8_v1.0'

  ELSE inner_dynamic.cpu_name

  END AS new_cpu_name
FROM vm_dynamic AS inner_dynamic) AS converted_query WHERE vm_dynamic.vm_guid=converted_query.vm_guid;
