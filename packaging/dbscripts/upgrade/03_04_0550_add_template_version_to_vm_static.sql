select fn_db_add_column('vm_static', 'template_version_number', 'INTEGER DEFAULT NULL');
select fn_db_add_column('vm_static', 'template_version_name', 'varchar(40) DEFAULT NULL');
-- make all existing vms and templates have version 1
update vm_static set template_version_number=1;
-- update base template for all templates
update vm_static set vmt_guid = vm_guid where entity_type='TEMPLATE';
-- set default 'base version' as version name
update vm_static set template_version_name = 'base version' where entity_type='TEMPLATE';
