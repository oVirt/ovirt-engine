-- Adding app_mode column to roles table to store the application mode.
-- The value of application modes for a role decides if that role is available in that application mode.
-- The value of application mode is represented by a unique binary number. Value of newer modes should be a power of 2.
-- Current valid values of application modes which can be set for a role are -
-- 1. VirtOnly		0000 0001
-- 2. GlusterOnly	0000 0010
-- 3. AllModes		1111 1111

select fn_db_add_column('roles', 'app_mode', 'INTEGER');

UPDATE roles
SET app_mode = (CASE WHEN name in ('UserRole', 'PowerUserRole', 'DataCenterAdmin', 'StorageAdmin', 'UserVmManager', 'VmPoolAdmin', 'TemplateAdmin', 'TemplateUser', 'QuotaConsumer', 'TemplateOwner', 'DiskOperator', 'DiskCreator', 'VmCreator', 'TemplateCreator', 'VnicProfileUser')
				THEN 1
				WHEN name='GlusterAdmin'
				THEN 2
				WHEN name in ('ClusterAdmin', 'SuperUser', 'HostAdmin', 'NetworkAdmin', 'ExternalEventsCreator', 'ExternalTasksCreator')
				THEN 255
				END);

-- Create the constraint
ALTER TABLE roles ALTER COLUMN app_mode SET NOT NULL;

-- Create the index
CREATE INDEX IDX_roles__app_mode ON roles(app_mode);
