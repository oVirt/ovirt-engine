-- Add the even guest weight policy unit
INSERT INTO policy_units (id, name, is_internal, type, enabled, custom_properties_regex, description) VALUES
('3ba8c988-f779-42c0-90ce-caa8243edee7', 'OptimalForEvenGuestDistribution', true, 1, true, NULL,
 'Weights host according the number of running VMs');

-- Add the even guest weight policy to the even guest distribution
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', '3ba8c988-f779-42c0-90ce-caa8243edee7', 0, 1);

-- Adding Pin2Host filter - should be executed first
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', '12262ab6-9690-4bc3-a2b3-35573b172d54', -1, 0);

-- Adding Network filter - should be executed last
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', '72163d1c-9468-4480-99d9-0888664eb143', 1, 0);

-- Add all other basic filters to even guest distribution
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
SELECT '8d5d7bec-68de-4a67-b53e-0ac54686d579', policy_units.id, 0, 1 FROM policy_units
WHERE not exists (SELECT id FROM cluster_policy_units
                        WHERE cluster_policy_id = '8d5d7bec-68de-4a67-b53e-0ac54686d579'
                        AND policy_unit_id = policy_units.id)
AND policy_units.type = 0
AND policy_units.is_internal = true;

-- Add the OptimalForHaReservation weight policy to even guest distribution
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', '7f262d70-6cac-11e3-981f-0800200c9a66', 0, 1);

-- Add the HA weight policy to even guest distribution
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
VALUES ('8d5d7bec-68de-4a67-b53e-0ac54686d579', '98e92667-6161-41fb-b3fa-34f820ccbc4b', 0, 1);
