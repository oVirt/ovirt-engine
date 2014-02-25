UPDATE policy_units set custom_properties_regex =
'{
  "HighVmCount" : "^([0-9]|[1-9][0-9]+)$",
  "MigrationThreshold" : "^([2-9]|[1-9][0-9]+)$",
  "SpmVmGrace":"^([0-9]|[1-9][0-9]+)$"
}'
where id = 'd58c8e32-44e1-418f-9222-52cd887bf9e0';
