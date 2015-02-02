--
-- PostgreSQL database dump
--


--
-- Data for Name: policy_units; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('12262ab6-9690-4bc3-a2b3-35573b172d54', 'PinToHost', true, NULL, 0, true, 'Filters out all hosts that VM is not pinned to');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('6d636bf6-a35c-4f9d-b68d-0731f720cddc', 'CPU', true, NULL, 0, true, 'Filters out hosts with less CPUs than VM''s CPUs');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('c9ddbb34-0e1d-4061-a8d7-b0893fa80932', 'Memory', true, NULL, 0, true, 'Filters out hosts that have insufficient memory to run the VM');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('72163d1c-9468-4480-99d9-0888664eb143', 'Network', true, NULL, 0, true, 'Filters out hosts that are missing networks required by VM NICs, or missing cluster''s display network');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('38440000-8cf0-14bd-c43e-10b96e4ef00a', 'None', true, NULL, 2, true, 'No load balancing operation');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('38440000-8cf0-14bd-c43e-10b96e4ef00b', 'None', true, NULL, 1, true, 'Follows Even Distribution weight module');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('438b052c-90ab-40e8-9be0-a22560202ea6', 'CPU-Level', true, NULL, 0, true, 'Runs VMs only on hosts with a proper CPU level');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('736999d0-1023-46a4-9a75-1316ed50e151', 'OptimalForPowerSaving', true, '{
  "CpuOverCommitDurationMinutes" : "^([1-9])$",
  "HighUtilization" : "^([5-9][0-9])$",
  "LowUtilization" : "^([1-4][0-9])$"
}', 2, true, 'Load balancing VMs in cluster according to hosts CPU load, striving cluster''s hosts CPU load to be over ''LowUtilization'' and under ''HighUtilization''');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('736999d0-1023-46a4-9a75-1316ed50e15b', 'OptimalForPowerSaving', true, NULL, 1, true, 'Gives hosts with higher CPU usage, higher weight (means that hosts with lower CPU usage are more likely to be selected)');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('7db4ab05-81ab-42e8-868a-aee2df483ed2', 'OptimalForEvenDistribution', true, '{
  "CpuOverCommitDurationMinutes" : "^([1-9])$",
  "HighUtilization" : "^([5-9][0-9])$"
}', 2, true, 'Load balancing VMs in cluster according to hosts CPU load, striving cluster''s hosts CPU load to be under ''HighUtilization''');
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, enabled, description) VALUES ('7db4ab05-81ab-42e8-868a-aee2df483edb', 'OptimalForEvenDistribution', true, NULL, 1, true, 'Gives hosts with lower CPU usage, higher weight (means that hosts with higher CPU usage are more likely to be selected)');


--
-- PostgreSQL database dump complete
--

