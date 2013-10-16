-- set the PinToHost filter as the first one in PowerSaving cluster policy
UPDATE cluster_policy_units SET filter_sequence=-1 WHERE cluster_policy_id='5a2b0939-7d46-4b73-a469-e9c2c7fc6a53' and policy_unit_id='12262ab6-9690-4bc3-a2b3-35573b172d54';
