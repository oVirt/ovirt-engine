INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('e659c871-0bf1-4ccc-b748-f28f5d08ddda', 'Migration', true, NULL, 0, true, 'Prevent migration to the same host.');

INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', 'e659c871-0bf1-4ccc-b748-f28f5d08ddda', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', 'e659c871-0bf1-4ccc-b748-f28f5d08ddda', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', 'e659c871-0bf1-4ccc-b748-f28f5d08ddda', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', 'e659c871-0bf1-4ccc-b748-f28f5d08ddda', 0, 0);