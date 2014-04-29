UPDATE policy_units SET custom_properties_regex='{
  "CpuOverCommitDurationMinutes" : "^([1-9][0-9]*)$",
  "HighUtilization" : "^([5-9][0-9])$",
  "LowUtilization" : "^([0-9]|[1-4][0-9])$",
  "HostsInReserve": "^[0-9][0-9]*$",
  "EnableAutomaticHostPowerManagement": "^(true|false)$"
}' WHERE id='736999d0-1023-46a4-9a75-1316ed50e151';
