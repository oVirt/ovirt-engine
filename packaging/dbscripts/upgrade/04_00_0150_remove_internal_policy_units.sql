-- Disable foreign key between policy unit and cluster policy unit
-- Some units will be internal and won't have a database row to use in the constraint
ALTER TABLE cluster_policy_units DROP CONSTRAINT IF EXISTS fk_policy_unit_id;

-- Disable foreigh key between cluster and cluster policy
-- Some cluster policies will be internal and won't have a database row
ALTER TABLE vds_groups DROP CONSTRAINT IF EXISTS vds_groups_cluster_policy;

-- Convert all references to Emulated-Machine policy unit to use the new fixed uuid 58894b5b-d55d-4f85-8f82-5bf217e640b0
UPDATE cluster_policy_units SET policy_unit_id = '58894b5b-d55d-4f85-8f82-5bf217e640b0' WHERE policy_unit_id = (SELECT id FROM policy_units WHERE name = 'Emulated-Machine');
UPDATE policy_units set id='58894b5b-d55d-4f85-8f82-5bf217e640b0' where name='Emulated-Machine';

--  Delete all internal units and cluster policies
DELETE FROM cluster_policy_units WHERE cluster_policy_id = 'b4ed2332-a7ac-4d5f-9596-99a439cb2812';
DELETE FROM cluster_policy_units WHERE cluster_policy_id = '20d25257-b4bd-4589-92a6-c4c5c5d3fd1a';
DELETE FROM cluster_policy_units WHERE cluster_policy_id = '5a2b0939-7d46-4b73-a469-e9c2c7fc6a53';
DELETE FROM cluster_policy_units WHERE cluster_policy_id = '8d5d7bec-68de-4a67-b53e-0ac54686d579';

DELETE FROM policy_units WHERE is_internal = true;

DELETE FROM cluster_policies WHERE id = 'b4ed2332-a7ac-4d5f-9596-99a439cb2812';
DELETE FROM cluster_policies WHERE id = '20d25257-b4bd-4589-92a6-c4c5c5d3fd1a';
DELETE FROM cluster_policies WHERE id = '5a2b0939-7d46-4b73-a469-e9c2c7fc6a53';
DELETE FROM cluster_policies WHERE id = '8d5d7bec-68de-4a67-b53e-0ac54686d579';
