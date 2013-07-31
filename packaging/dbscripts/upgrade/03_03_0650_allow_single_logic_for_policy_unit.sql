-- add type column (default is type = filter)
SELECT fn_db_add_column('policy_units', 'type', 'smallint default 0');
-- update balance p.u to type = 2
UPDATE policy_units SET type = 2 WHERE has_balance = true;
-- remove has_XXX columns (transformed into type column)
SELECT fn_db_drop_column('policy_units', 'has_filter');
SELECT fn_db_drop_column('policy_units', 'has_function');
SELECT fn_db_drop_column('policy_units', 'has_balance');
-- becuase policy unit cannot contain more than one type, adding new entries for weight functions.
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type) VALUES ('38440000-8cf0-14bd-c43e-10b96e4ef00b', 'None', true, NULL, 1);
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type) VALUES ('736999d0-1023-46a4-9a75-1316ed50e15b', 'PowerSaving', true, NULL, 1);
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type) VALUES ('7db4ab05-81ab-42e8-868a-aee2df483edb', 'EvenDistribution', true, NULL, 1);
-- cluster_policy_units: adding entries for new weight functions
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected)
SELECT cluster_policy_id, '38440000-8cf0-14bd-c43e-10b96e4ef00b', is_filter_selected, filter_sequence, false, factor, is_balance_selected
FROM cluster_policy_units
WHERE policy_unit_id = '38440000-8cf0-14bd-c43e-10b96e4ef00a' AND is_function_selected = true;
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected)
SELECT cluster_policy_id, '736999d0-1023-46a4-9a75-1316ed50e15b', is_filter_selected, filter_sequence, false, factor, is_balance_selected
FROM cluster_policy_units
WHERE policy_unit_id = '736999d0-1023-46a4-9a75-1316ed50e151' AND is_function_selected = true;
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, is_filter_selected, filter_sequence, is_function_selected, factor, is_balance_selected)
SELECT cluster_policy_id, '7db4ab05-81ab-42e8-868a-aee2df483edb', is_filter_selected, filter_sequence, false, factor, is_balance_selected
FROM cluster_policy_units
WHERE policy_unit_id = '7db4ab05-81ab-42e8-868a-aee2df483ed2' AND is_function_selected = true;
-- Removing only is_function_selected link (note that in insert into select is_function_selected is set to false).
DELETE FROM cluster_policy_units WHERE is_function_selected = true AND is_filter_selected = false AND is_balance_selected = false;
-- cluster_policy_units: removing is_XXX_selected (since policy unit contain only a single entry)
SELECT fn_db_drop_column('cluster_policy_units', 'is_filter_selected');
SELECT fn_db_drop_column('cluster_policy_units', 'is_function_selected');
SELECT fn_db_drop_column('cluster_policy_units', 'is_balance_selected');
