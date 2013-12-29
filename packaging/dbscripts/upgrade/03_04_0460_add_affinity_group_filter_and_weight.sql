-- Add Affinity Group filter for VMs
INSERT INTO policy_units(id, name, is_internal, custom_properties_regex, type, enabled, description)
VALUES ('84e6ddee-ab0d-42dd-82f0-c297779db566', 'VmAffinityGroups', TRUE, NULL, 0, true, 'Enables Affinity Groups hard enforcement for VMs; VMs in group are required to run either on the same hypervisor host (positive) or on independent hypervisor hosts (negative)');

-- Add vm Affinity Group filter to all existing cluster policies.
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
SELECT cluster_policies.id, '84e6ddee-ab0d-42dd-82f0-c297779db566', 0, 0
FROM cluster_policies;

-- Add Affinity Group weight module for VMs
INSERT INTO policy_units(id, name, is_internal, custom_properties_regex, type, enabled, description)
VALUES ('84e6ddee-ab0d-42dd-82f0-c297779db567', 'VmAffinityGroups', TRUE, NULL, 1, true, 'Enables Affinity Groups soft enforcement for VMs; VMs in group are most likely to run either on the same hypervisor host (positive) or on independent hypervisor hosts (negative)');

-- Add vm Affinity Group weight module to all existing cluster policies.
INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor)
SELECT cluster_policies.id, '84e6ddee-ab0d-42dd-82f0-c297779db567', 0, 1
FROM cluster_policies;
