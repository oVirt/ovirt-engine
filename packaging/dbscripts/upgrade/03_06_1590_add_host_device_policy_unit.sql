CREATE FUNCTION _createFilter(uuid, character varying(128), text) RETURNS void AS $$
  INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ($1, $2, true, NULL, 0, true, $3);
  INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('20d25257-b4bd-4589-92a6-c4c5c5d3fd1a', $1, 0, 0);
  INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('5a2b0939-7d46-4b73-a469-e9c2c7fc6a53', $1, 0, 0);
  INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('b4ed2332-a7ac-4d5f-9596-99a439cb2812', $1, 0, 0);
  INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', $1, 0, 0);
$$ LANGUAGE SQL;


select _createFilter('728a21f1-f97e-4d32-bc3e-b3cc49756abb', 'HostDevice', 'Filters out hosts not supporting VM required host devices');

DROP FUNCTION  _createFilter(uuid, character varying(128), text);
