
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('438b052c-90ab-40e8-9be0-a22560202ea6', 'CPU-Level', true, NULL, 0, true, 'Runs VMs only on hosts with a proper CPU level');
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '438b052c-90ab-40e8-9be0-a22560202ea6', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '438b052c-90ab-40e8-9be0-a22560202ea6', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '438b052c-90ab-40e8-9be0-a22560202ea6', 0, 0);
