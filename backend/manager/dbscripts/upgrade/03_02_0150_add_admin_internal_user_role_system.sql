-- grant admin poweruser role on system
CREATE OR REPLACE FUNCTION __temp_grant_admin_internal_poweruser_role_on_system()
  RETURNS void AS
$BODY$
   DECLARE
   v_user_id uuid ;
   v_power_user_role_id uuid;
BEGIN
    v_user_id := user_id from users where username = 'admin@internal';
    v_power_user_role_id := '00000000-0000-0000-0001-000000000002';

    insert into permissions(id,role_id,ad_element_id,object_id,object_type_id)
        select uuid_generate_v1(), v_power_user_role_id, v_user_id, getGlobalIds('system'), 1
        where not exists
            (select 1 from permissions
             where role_id = v_power_user_role_id and
                               ad_element_id = v_user_id and
                               object_id= getGlobalIds('system') and
                               object_type_id = 1);
END; $BODY$

LANGUAGE plpgsql;

select __temp_grant_admin_internal_poweruser_role_on_system();
drop function __temp_grant_admin_internal_poweruser_role_on_system();

