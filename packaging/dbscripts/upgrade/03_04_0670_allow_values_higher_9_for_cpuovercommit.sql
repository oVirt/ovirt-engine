-- 736999d0-1023-46a4-9a75-1316ed50e151 - PowerSaving
UPDATE policy_units set custom_properties_regex = '{
  "CpuOverCommitDurationMinutes" : "^([1-9][0-9]*)$",
  "HighUtilization" : "^([5-9][0-9])$",
  "LowUtilization" : "^([0-9]|[1-4][0-9])$"
}'
WHERE id = '736999d0-1023-46a4-9a75-1316ed50e151';

-- 7db4ab05-81ab-42e8-868a-aee2df483ed2 - EvenDistribution
UPDATE policy_units set custom_properties_regex = '{
  "CpuOverCommitDurationMinutes" : "^([1-9][0-9]*)$",
  "HighUtilization" : "^([5-9][0-9])$"
}'
WHERE id = '7db4ab05-81ab-42e8-868a-aee2df483ed2';
