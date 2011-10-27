

print 'Dropping Foreign Keys...'
print 'Dropping Primary Keys...'
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vds_history__0CBAE877' AND TABLE_NAME='vds_history')
BEGIN
ALTER TABLE [dbo].[vds_history] DROP CONSTRAINT PK__vds_history__0CBAE877
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vds_interface_co__15502E78' AND TABLE_NAME='vds_interface_configuration')
BEGIN
ALTER TABLE [dbo].[vds_interface_configuration] DROP CONSTRAINT PK__vds_interface_co__15502E78
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vds_interface_hi__1920BF5C' AND TABLE_NAME='vds_interface_history')
BEGIN
ALTER TABLE [dbo].[vds_interface_history] DROP CONSTRAINT PK__vds_interface_hi__1920BF5C
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vm_disk_history__1CF15040' AND TABLE_NAME='vm_disk_history')
BEGIN
ALTER TABLE [dbo].[vm_disk_history] DROP CONSTRAINT PK__vm_disk_history__1CF15040
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vm_history__014935CB' AND TABLE_NAME='vm_history')
BEGIN
ALTER TABLE [dbo].[vm_history] DROP CONSTRAINT PK__vm_history__014935CB
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vm_interface_con__1367E606' AND TABLE_NAME='vm_interface_configuration')
BEGIN
ALTER TABLE [dbo].[vm_interface_configuration] DROP CONSTRAINT PK__vm_interface_con__1367E606
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK__vm_interface_his__173876EA' AND TABLE_NAME='vm_interface_history')
BEGIN
ALTER TABLE [dbo].[vm_interface_history] DROP CONSTRAINT PK__vm_interface_his__173876EA
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_vds_configuration' AND TABLE_NAME='vds_configuration')
BEGIN
ALTER TABLE [dbo].[vds_configuration] DROP CONSTRAINT PK_vds_configuration
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_vds_groups' AND TABLE_NAME='vds_group_configuration')
BEGIN
ALTER TABLE [dbo].[vds_group_configuration] DROP CONSTRAINT PK_vds_groups
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_vm_configuration' AND TABLE_NAME='vm_configuration')
BEGIN
ALTER TABLE [dbo].[vm_configuration] DROP CONSTRAINT PK_vm_configuration
END
go
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_vm_disk_configuration' AND TABLE_NAME='vm_disk_configuration')
BEGIN
ALTER TABLE [dbo].[vm_disk_configuration] DROP CONSTRAINT PK_vm_disk_configuration
END
go
print 'Dropping Indexes...'
IF INDEXPROPERTY ( OBJECT_ID('vm_history') , 'IDX_vm_history_history_datetime' , 'IndexID' ) IS NOT NULL
BEGIN
DROP INDEX IDX_vm_history_history_datetime ON vm_history
END
go
IF INDEXPROPERTY ( OBJECT_ID('vds_history') , 'IDX_vds_history_history_datetime' , 'IndexID' ) IS NOT NULL
BEGIN
DROP INDEX IDX_vds_history_history_datetime ON vds_history
END
go
IF INDEXPROPERTY ( OBJECT_ID('vm_interface_history') , 'IDX_vm_interface_history_history_datetime' , 'IndexID' ) IS NOT NULL
BEGIN
DROP INDEX IDX_vm_interface_history_history_datetime ON vm_interface_history
END
go
IF INDEXPROPERTY ( OBJECT_ID('vds_interface_history') , 'IDX_vds_interface_history_history_datetime' , 'IndexID' ) IS NOT NULL
BEGIN
DROP INDEX IDX_vds_interface_history_history_datetime ON vds_interface_history
END
go
IF INDEXPROPERTY ( OBJECT_ID('vm_disk_history') , 'IDX_vm_disk_history_history_datetime' , 'IndexID' ) IS NOT NULL
BEGIN
DROP INDEX IDX_vm_disk_history_history_datetime ON vm_disk_history
END
go
