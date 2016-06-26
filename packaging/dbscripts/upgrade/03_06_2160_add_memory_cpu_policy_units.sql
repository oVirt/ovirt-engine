INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES
  ('9dfe6086-646d-43b8-8eef-4d94de8472c8', 'OptimalForPowerSavingMemory', TRUE, NULL, 1, TRUE, 'Gives hosts with lower available memory, lower weight (means that hosts with lower
available memory are more likely to be selected');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES
  ('4134247a-9c58-4b9a-8593-530bb9e37c59', 'OptimalForEvenDistributionMemory', TRUE, NULL, 1, TRUE, 'Gives hosts with higher
available memory, lower weight (means that hosts with more available memory are more likely to be selected)');

UPDATE policy_units
SET name = 'OptimalForPowerSavingCPU', description = 'Gives hosts with higher CPU usage, lower weight
 (means that hosts with higher
 CPU usage are more likely to be selected)'
WHERE id = '736999d0-1023-46a4-9a75-1316ed50e15b';
UPDATE policy_units
SET name = 'OptimalForEvenDistributionCPU', description = 'Gives hosts with lower CPU usage, lower
weight (means that hosts with lower CPU usage are more likely to be selected)'
WHERE
  id = '7db4ab05-81ab-42e8-868a-aee2df483edb';

-- add to cluster evenly distributed scheduling policy OptimalForEvenDistributionMemory
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', '4134247a-9c58-4b9a-8593-530bb9e37c59', 0, 1);
-- add to cluster power saving scheduling policy OptimalForPowerSavingMemory
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', '9dfe6086-646d-43b8-8eef-4d94de8472c8', 0, 1);

-- add to cluster none scheduling policy OptimalForEvenDistributionMemory and OptimalForEvenDistributionCPU
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '4134247a-9c58-4b9a-8593-530bb9e37c59', 0, 1);
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', '7db4ab05-81ab-42e8-868a-aee2df483edb', 0, 1);

--remove none weight policy unit
DELETE FROM cluster_policy_units
WHERE
  cluster_policy_id = 'b4ed2332-a7ac-4d5f-9596-99a439cb2812' AND policy_unit_id =
                                                                 '38440000-8cf0-14bd-c43e-10b96e4ef00b';
DELETE FROM policy_units
WHERE id = '38440000-8cf0-14bd-c43e-10b96e4ef00b' AND name = 'None';








