-- create policy unit table
CREATE TABLE policy_units
(
    id UUID CONSTRAINT policy_unit_pk PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    is_internal BOOLEAN NOT NULL,
    has_filter BOOLEAN NOT NULL,
    has_function BOOLEAN NOT NULL,
    has_balance BOOLEAN NOT NULL,
    custom_properties_regex text
);
-- fill policy unit table with pre-defined policy units
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('84e6ddee-ab0d-42dd-82f0-c297779db5e5', 'Migration', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('a267eddb-768d-45fd-9dbb-6ebcee343508', 'MigrationDomain', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('12262ab6-9690-4bc3-a2b3-35573b172d54', 'PinToHost', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('6d636bf6-a35c-4f9d-b68d-0731f720cddc', 'CPU', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('c9ddbb34-0e1d-4061-a8d7-b0893fa80932', 'Memory', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('72163d1c-9468-4480-99d9-0888664eb143', 'Network', true, true, false, false, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('38440000-8cf0-14bd-c43e-10b96e4ef00a', 'None', true, false, true, true, NULL);
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('736999d0-1023-46a4-9a75-1316ed50e151', 'PowerSaving', true, false, true, true, '{
  "CpuOverCommitDurationMinutes" : "^([1-9])$",
  "HighUtilization" : "^([5-9][0-9])$",
  "LowUtilization" : "^([1-4][0-9])$"
}');
INSERT INTO policy_units (id, name, is_internal, has_filter, has_function, has_balance, custom_properties_regex) VALUES ('7db4ab05-81ab-42e8-868a-aee2df483ed2', 'EvenDistribution', true, false, true, true, '{
  "CpuOverCommitDurationMinutes" : "^([1-9])$",
  "HighUtilization" : "^([5-9][0-9])$"
}');
-- create cluster policy table
CREATE TABLE cluster_policies
(
    id UUID CONSTRAINT cluster_policy_pk PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(4000),
    is_locked BOOLEAN NOT NULL,
    is_default BOOLEAN NOT NULL,
    custom_properties text
);
-- fill cluster policy table with pre-defined policies
INSERT INTO cluster_policies (id, name, description, is_locked, is_default, custom_properties) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', 'Evenly_Distributed', '', true, false, '{
  "CpuOverCommitDurationMinutes" : "2",
  "HighUtilization" : "80"
}');
INSERT INTO cluster_policies (id, name, description, is_locked, is_default, custom_properties) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', 'Power_Saving', '', true, false, '{
  "CpuOverCommitDurationMinutes" : "2",
  "HighUtilization" : "80",
  "LowUtilization" : "20"
}');
INSERT INTO cluster_policies (id, name, description, is_locked, is_default, custom_properties) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', 'None', '', true, true, NULL);
-- create cluster policy units table
CREATE TABLE cluster_policy_units
(
    cluster_policy_id UUID,
    policy_unit_id UUID,
    is_filter_selected BOOLEAN NOT NULL,
    filter_sequence INTEGER DEFAULT 0,
    is_function_selected BOOLEAN NOT NULL,
    factor INTEGER DEFAULT 1,
    is_balance_selected BOOLEAN,
    CONSTRAINT FK_cluster_policy_id FOREIGN KEY(cluster_policy_id) REFERENCES cluster_policies(id) ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT FK_policy_unit_id FOREIGN KEY(policy_unit_id) REFERENCES policy_units(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
-- fill cluster policy units table
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '84e6ddee-ab0d-42dd-82f0-c297779db5e5', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', 'c9ddbb34-0e1d-4061-a8d7-b0893fa80932', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '6d636bf6-a35c-4f9d-b68d-0731f720cddc', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '12262ab6-9690-4bc3-a2b3-35573b172d54', true, -1, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '7db4ab05-81ab-42e8-868a-aee2df483ed2', false, 0, true, 1, true);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '72163d1c-9468-4480-99d9-0888664eb143', true, 1, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', 'a267eddb-768d-45fd-9dbb-6ebcee343508', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '84e6ddee-ab0d-42dd-82f0-c297779db5e5', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', 'c9ddbb34-0e1d-4061-a8d7-b0893fa80932', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '736999d0-1023-46a4-9a75-1316ed50e151', false, 0, true, 1, true);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '6d636bf6-a35c-4f9d-b68d-0731f720cddc', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '12262ab6-9690-4bc3-a2b3-35573b172d54', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '72163d1c-9468-4480-99d9-0888664eb143', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', 'a267eddb-768d-45fd-9dbb-6ebcee343508', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '38440000-8cf0-14bd-c43e-10b96e4ef00a', false, 0, true, 1, true);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '84e6ddee-ab0d-42dd-82f0-c297779db5e5', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', 'c9ddbb34-0e1d-4061-a8d7-b0893fa80932', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '6d636bf6-a35c-4f9d-b68d-0731f720cddc', true, 0, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '12262ab6-9690-4bc3-a2b3-35573b172d54', true, -1, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '72163d1c-9468-4480-99d9-0888664eb143', true, 1, false, 0, false);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', 'a267eddb-768d-45fd-9dbb-6ebcee343508', true, 0, false, 0, false);
-- update vds_group table with new fields
select fn_db_add_column('vds_groups', 'cluster_policy_id', 'UUID');
select fn_db_add_column('vds_groups', 'cluster_policy_custom_properties', 'text');
-- upgrade current clusters to point new arch
UPDATE vds_groups
SET    cluster_policy_id = '20d25257-b4bd-4589-92a6-c4c5c5d3fd1a',
	       cluster_policy_custom_properties =
'{
  "CpuOverCommitDurationMinutes" : "'|| cpu_over_commit_duration_minutes ||'",
  "HighUtilization" : "' || high_utilization ||'"
}'
WHERE  selection_algorithm = 1;
UPDATE vds_groups
SET    cluster_policy_id = '5a2b0939-7d46-4b73-a469-e9c2c7fc6a53',
cluster_policy_custom_properties =
'{
  "CpuOverCommitDurationMinutes" : "'|| cpu_over_commit_duration_minutes ||'",
  "HighUtilization" : "' || high_utilization ||'",
  "LowUtilization" : "' || low_utilization ||'"
}'
WHERE  selection_algorithm = 2;
UPDATE vds_groups
SET    cluster_policy_id = 'b4ed2332-a7ac-4d5f-9596-99a439cb2812'
WHERE  selection_algorithm = 0
-- set none policy for default (shouldn't get here but who knows...)
OR cluster_policy_id IS NULL;
-- create constrait to cluster policy table
select fn_db_create_constraint('vds_groups', 'vds_groups_cluster_policy', 'FOREIGN KEY(cluster_policy_id) REFERENCES cluster_policies(id)');






