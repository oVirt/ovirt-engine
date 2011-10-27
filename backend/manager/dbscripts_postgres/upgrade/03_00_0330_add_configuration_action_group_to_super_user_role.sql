INSERT INTO roles_groups(role_id,action_group_id)
	select '00000000-0000-0000-0000-000000000001', -- super user role id
		800				       -- CONFIGURE_ENGINE ActionGroup
	where not exists (
		select * from roles_groups
		where role_id='00000000-0000-0000-0000-000000000001' and action_group_id=800
	);
