if (not exists (select * from history_configuration where var_name = 'default_language'))
begin
	PRINT 'INSERTING DEFAULT LANGUAGE TO HISTORY CONFIGURATION'
INSERT INTO [history_configuration]([var_name],[var_value])
     VALUES ('default_language','us-en')
end

PRINT 'INSERTING LAST AGGREGATION DATES TO HISTORY CONFIGURATION'
if (not exists (select * from history_configuration where var_name = 'dcLastDayAggr'))
begin
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('dcLastDayAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hostLastDayAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hinterfaceLastDayAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vmLastDayAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vminterfaceLastDayAggr',cast('01/01/2000 12:00:00 PM' as datetime))
end

if (exists (select * from history_configuration where var_name = 'lastDayAggr' and var_datetime IS NULL)
	and not exists (select * from history_configuration where var_name = 'dcLastDayAggr'))
begin
UPDATE [history_configuration]
SET [var_datetime] = cast([var_value] as datetime) 
WHERE [var_name] = 'lastDayAggr'

DECLARE @lastDayAggr_date datetime
SET @lastDayAggr_date = (SELECT [var_datetime] FROM [history_configuration] WHERE [var_name] = 'lastDayAggr')

INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('dcLastDayAggr',@lastDayAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hostLastDayAggr',@lastDayAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hinterfaceLastDayAggr',@lastDayAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vmLastDayAggr',@lastDayAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vminterfaceLastDayAggr',@lastDayAggr_date)
end

if (not exists (select * from history_configuration where var_name = 'dcLastHourAggr'))
begin
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('dcLastHourAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hostLastHourAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hinterfaceLastHourAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vmLastHourAggr',cast('01/01/2000 12:00:00 PM' as datetime))
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vminterfaceLastHourAggr',cast('01/01/2000 12:00:00 PM' as datetime))
end

if (exists (select * from history_configuration where var_name = 'lastHourAggr' and var_datetime IS NULL)
	and not exists (select * from history_configuration where var_name = 'dcLastHourAggr'))
begin
UPDATE [history_configuration]
SET [var_datetime] = cast([var_value] as datetime) 
WHERE [var_name] = 'lastHourAggr'

DECLARE @lastHourAggr_date datetime
SET @lastHourAggr_date = (SELECT [var_datetime] FROM [history_configuration] WHERE [var_name] = 'lastHourAggr')

INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('dcLastHourAggr',@lastHourAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hostLastHourAggr',@lastHourAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('hinterfaceLastHourAggr',@lastHourAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vmLastHourAggr',@lastHourAggr_date)
INSERT INTO [history_configuration]([var_name],[var_datetime])
     VALUES ('vminterfaceLastHourAggr',@lastHourAggr_date)
end

if (not exists (select * from enum_translator where enum_type = 'HOST_TYPE'))
begin
	PRINT 'INSERTING HOST_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_TYPE',0,'us-en','RHEL')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_TYPE',1,'us-en','RHEV')
end

if (not exists (select * from enum_translator where enum_type = 'NIC_TYPE'))
begin
	PRINT 'INSERTING NIC_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('NIC_TYPE',0,'us-en','rtl8139-pv')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('NIC_TYPE',1,'us-en','rtl8139')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('NIC_TYPE',2,'us-en','e1000')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('NIC_TYPE',3,'us-en','pv')
end

if (not exists (select * from enum_translator where enum_type = 'OS_TYPE'))
begin
	PRINT 'INSERTING OS_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',0,'us-en','Unknown')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',1,'us-en','Windows XP')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',3,'us-en','Windows 2003')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',4,'us-en','Windows 2008')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',5,'us-en','Other Linux')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',6,'us-en','Other')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',7,'us-en','RHEL 5')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',8,'us-en','RHEL 4')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',9,'us-en','RHEL 3')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',10,'us-en','Windows 2003 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',11,'us-en','Windows 7')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',12,'us-en','Windows 7 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',13,'us-en','RHEL 5 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',14,'us-en','RHEL 4 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',15,'us-en','RHEL 3 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',16,'us-en','Windows 2008 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',17,'us-en','Windows 2008R2 x64')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',18,'us-en','RHEL 6')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('OS_TYPE',19,'us-en','RHEL 6 x64')
end

if (not exists (select * from enum_translator where enum_type = 'HOST_STATUS'))
begin
	PRINT 'INSERTING HOST_STATUS TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',0,'us-en','Unussigned')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',1,'us-en','Down')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',2,'us-en','Maintenance')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',3,'us-en','Up')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',4,'us-en','Non Responsive')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',5,'us-en','Error')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',6,'us-en','Installing')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',7,'us-en','Install Failed')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',8,'us-en','Reboot')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',9,'us-en','Preparing For Maintenance')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',10,'us-en','Non Operational')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',11,'us-en','Pending Approval')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',12,'us-en','Initializing')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('HOST_STATUS',13,'us-en','Non-Responsive')
end


if (not exists (select * from enum_translator where enum_type = 'VM_STATUS'))
begin
	PRINT 'INSERTING VM_STATUS TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',-1,'us-en','Unassigned')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',0,'us-en','Down')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',1,'us-en','Up')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',2,'us-en','Powering Up')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',3,'us-en','Powered Down')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',4,'us-en','Paused')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',5,'us-en','Migrating From')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',6,'us-en','Migrating To')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',7,'us-en','Unknown')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',8,'us-en','Not Responding')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',9,'us-en','Wait For Launch')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',10,'us-en','Reboot In Progress')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',11,'us-en','Saving State')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',12,'us-en','Restoring State')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',13,'us-en','Suspended')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',14,'us-en','Image Illegal')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',15,'us-en','Image Locked')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_STATUS',16,'us-en','Powering Down')
end

if (not exists (select * from enum_translator where enum_type = 'VM_DISK_INTERFACE'))
begin
	PRINT 'INSERTING VM_DISK_INTERFACE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_INTERFACE',0,'us-en','IDE')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_INTERFACE',1,'us-en','SCSI')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_INTERFACE',2,'us-en','Virt IO')
end

if (not exists (select * from enum_translator where enum_type = 'VM_DISK_FORMAT'))
begin
	PRINT 'INSERTING VM_DISK_FORMAT TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_FORMAT',3,'us-en','Unassigned')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_FORMAT',4,'us-en','COW')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_FORMAT',5,'us-en','Raw')
end


if (not exists (select * from enum_translator where enum_type = 'VM_DISK_TYPE'))
begin
	PRINT 'INSERTING VM_DISK_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_TYPE',0,'us-en','Unassigned')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_TYPE',1,'us-en','System')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_TYPE',2,'us-en','Data')
end

if (not exists (select * from enum_translator where enum_type = 'VM_DISK_STATUS'))
begin
	PRINT 'INSERTING VM_DISK_STATUS TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_STATUS',0,'us-en','Unassigned')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_STATUS',1,'us-en','OK')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_STATUS',2,'us-en','Locked')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_STATUS',3,'us-en','Invalid')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_DISK_STATUS',4,'us-en','Illegal')
end

if (not exists (select * from enum_translator where enum_type = 'DATACENTER_STATUS'))
begin
	PRINT 'INSERTING DATACENTER_STATUS TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',0,'us-en','Uninitialized')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',1,'us-en','Up')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',2,'us-en','Maintenance')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',3,'us-en','Not Operational')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',4,'us-en','Non Responsive')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('DATACENTER_STATUS',5,'us-en','Contend')
end

if (not exists (select * from enum_translator where enum_type = 'VM_TYPE'))
begin
	PRINT 'INSERTING VM_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_TYPE',0,'us-en','Desktop')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('VM_TYPE',1,'us-en','Server')
end

if (not exists (select * from enum_translator where enum_type = 'STORAGE_DOMAIN_TYPE'))
begin
	PRINT 'INSERTING STORAGE_DOMAIN_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_DOMAIN_TYPE',0,'us-en','Data (Master)')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_DOMAIN_TYPE',1,'us-en','Data')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_DOMAIN_TYPE',2,'us-en','ISO')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_DOMAIN_TYPE',3,'us-en','Export')
end

if (not exists (select * from enum_translator where enum_type = 'STORAGE_TYPE'))
begin
	PRINT 'INSERTING STORAGE_TYPE TO ENUM TRANSLATOR'
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',0,'us-en','Unknown')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',1,'us-en','NFS')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',2,'us-en','FCP')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',3,'us-en','iSCSI')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',4,'us-en','Local')
--INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
--     VALUES ('STORAGE_TYPE',5,'us-en','CIFS')
INSERT INTO [enum_translator]([enum_type],[enum_key],[language_code],[value])
     VALUES ('STORAGE_TYPE',6,'us-en','All')
end


