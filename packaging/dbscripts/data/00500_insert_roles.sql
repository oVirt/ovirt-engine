--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('00000000-0000-0000-0000-000000000001', 'SuperUser', 'Roles management administrator', true, 1, true, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('00000000-0000-0000-0001-000000000001', 'UserRole', 'Standard User Role', true, 2, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('00000000-0000-0000-0001-000000000002', 'PowerUserRole', 'User Role, allowed to create VMs, Templates and Disks', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00001-0000-0000-0000-def000000001', 'ClusterAdmin', 'Administrator Role, permission for all the objects underneath a specific Cluster', true, 1, true, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00002-0000-0000-0000-def000000002', 'DataCenterAdmin', 'Administrator Role, permission for all the objects underneath a specific Data Center, except Storage', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00003-0000-0000-0000-def000000003', 'StorageAdmin', 'Administrator Role, permission for all operations on a specific Storage Domain', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00004-0000-0000-0000-def000000004', 'HostAdmin', 'Administrator Role, permission for all operations on a specific Host', true, 1, true, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00005-0000-0000-0000-def000000005', 'NetworkAdmin', 'Administrator Role, permission for all operations on a specific Logical Network', true, 1, true, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00006-0000-0000-0000-def000000006', 'UserVmManager', 'User Role, with permission for any operation on Vms', true, 2, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00007-0000-0000-0000-def000000007', 'VmPoolAdmin', 'Administrator Role, permission for all operations on a specific VM Pool', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00008-0000-0000-0000-def000000008', 'TemplateAdmin', 'Administrator Role, permission for all operations on a specific Template', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000a', 'QuotaConsumer', 'User Role, permissions to consume the Quota resources', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000b', 'DiskOperator', 'User Role, permissions for all operations on a specific disk', true, 2, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000c', 'DiskCreator', 'User Role, permission to create Disks', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000b-0000-0000-0000-def00000000b', 'GlusterAdmin', 'Administrator Role, permissions for operations on Gluster objects', true, 1, true, 2);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000d', 'VmCreator', 'User Role, permission to create VMs', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000e', 'TemplateCreator', 'User Role, permission to create Templates', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def00000000f', 'TemplateOwner', 'User Role, permissions for all operations on Templates', true, 2, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000c-0000-0000-0000-def000000000', 'ExternalEventsCreator', 'External Events Creator', true, 2, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000d-0000-0000-0000-def000000000', 'ExternalTasksCreator', 'External Tasks Creator', true, 2, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000a-0000-0000-0000-def000000010', 'VnicProfileUser', 'VM Network Interface Profile User', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00009-0000-0000-0000-def000000009', 'UserTemplateBasedVm', 'Template User', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00011-0000-0000-0000-def000000011', 'InstanceCreator', 'User Role, permission to create Instances', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00012-0000-0000-0000-def000000012', 'UserInstanceManager', 'User Role, with permission for any operation on Instances', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def0000c-0000-0000-0000-def00000000c', 'ReadOnlyAdmin', 'Read Only Administrator Role', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00011-0000-0000-0000-def000000013', 'TagManager', 'Tag Manager', true, 1, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00011-0000-0000-0000-def000000014', 'BookmarkManager', 'Bookmark Manager', true, 1, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00011-0000-0000-0000-def000000015', 'EventNotificationManager', 'Event Notification Manager', true, 1, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00011-0000-0000-0000-def000000016', 'AuditLogManager', 'Audit Log Manager', true, 1, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00013-0000-0000-0000-def000000013', 'MacPoolAdmin', 'MAC Pool Administrator Role, permission for manipulation with MAC pools', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00014-0000-0000-0000-def000000014', 'MacPoolUser', 'MAC Pool User Role, permission allowing using MAC pools', true, 2, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00020-0000-0000-0000-abc000000010', 'DiskProfileUser', 'Disk Profile User', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00006-0000-0000-0000-def000000011', 'UserVmRunTimeManager', 'User Role, with permissions for any operations on VMs except snapshot manipulation', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00021-0000-0000-0000-def000000015', 'UserProfileEditor', 'Role that allow users to edit the UserProfile', true, 2, false, 255);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00030-0000-0000-0000-def000000011', 'VmImporterExporter', 'Administrator Role, with permission to import or export Vms', true, 1, true, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00017-0000-0000-0000-def000000017', 'CpuProfileOperator', 'Cpu Profile Operation', true, 2, false, 1);
INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES ('def00016-0000-0000-0000-def000000016', 'CpuProfileCreator', 'Cpu Profile Creation/Deletion/Updating and Operation', true, 1, false, 1);


--
-- PostgreSQL database dump complete
--

