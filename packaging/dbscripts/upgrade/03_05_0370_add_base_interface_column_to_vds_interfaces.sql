select fn_db_add_column('vds_interface', 'base_interface', 'varchar(50)');
-- Copying the base interface name from the vlan device name to base_interface column
-- For example if the vlan device name is "eth1.10", "eth1" will be copied to base_interface column
update vds_interface set base_interface = substring(name from '(.*)\.[^\.]*') where vlan_id is not NULL;
