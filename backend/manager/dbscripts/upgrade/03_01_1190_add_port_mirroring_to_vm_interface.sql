--v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
--v_super_user_id_0001 := '00000000-0000-0000-0000-000000000001';

--CONFIGURE_STORAGE_POOL_VM_INTERFACE
INSERT INTO roles_groups(role_id,action_group_id) VALUES( 'DEF00002-0000-0000-0000-DEF000000002',1200);
INSERT INTO roles_groups(role_id,action_group_id) VALUES( '00000000-0000-0000-0000-000000000001',1200);


select fn_db_add_column('vm_interface', 'port_mirroring', 'boolean NOT NULL DEFAULT false');
