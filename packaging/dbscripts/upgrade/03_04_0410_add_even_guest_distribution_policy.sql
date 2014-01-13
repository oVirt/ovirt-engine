INSERT INTO policy_units (id, name, is_internal, type, enabled, custom_properties_regex, description) VALUES
('d58c8e32-44e1-418f-9222-52cd887bf9e0', 'OptimalForEvenGuestDistribution', true, 2, true,
'{
  "HighVmCount" : "^([0-9][0-9]*)$",
  "MigrationThreshold" : "^([5-9][0-9]*)$",
  "SpmVmGrace":"^([5-9][0-9]*)$"
}',
 'Even VM count distribution policy'
);
INSERT INTO cluster_policies (id, name, description, is_locked, is_default, custom_properties) VALUES (
'8d5d7bec-68de-4a67-b53e-0ac54686d579','VM_Evenly_Distributed', '', true, false,
'{
  "HighVmCount" : "10",
  "MigrationThreshold" : "5",
  "SpmVmGrace" : "5"
}');

-- add the policy units to the VM_Evenly_Distributed cluster policy
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', 'd58c8e32-44e1-418f-9222-52cd887bf9e0', 0, 1);
