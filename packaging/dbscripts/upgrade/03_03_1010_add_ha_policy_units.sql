
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('e659c871-0bf1-4ccc-b748-f28f5d08dffd', 'HA', true, NULL, 0, true, 'Runs the hosted engine VM only on hosts with a positive score');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('98e92667-6161-41fb-b3fa-34f820ccbc4b', 'HA', true, NULL, 1, true, 'Weights hosts according to their HA score');

INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', 'e659c871-0bf1-4ccc-b748-f28f5d08dffd', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', 'e659c871-0bf1-4ccc-b748-f28f5d08dffd', 0, 0);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', 'e659c871-0bf1-4ccc-b748-f28f5d08dffd', 0, 0);

INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '98e92667-6161-41fb-b3fa-34f820ccbc4b', 0, 1);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '98e92667-6161-41fb-b3fa-34f820ccbc4b', 0, 1);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '98e92667-6161-41fb-b3fa-34f820ccbc4b', 0, 1);
