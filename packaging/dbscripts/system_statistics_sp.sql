




DROP TYPE IF EXISTS Getsystem_statistics_rs CASCADE;
CREATE TYPE Getsystem_statistics_rs AS (val INTEGER);
Create or replace FUNCTION Getsystem_statistics(v_entity VARCHAR(10), -- /*VM,HOST,USER,SD*/
v_status VARCHAR(20)) -- comma seperated list of status values
RETURNS Getsystem_statistics_rs STABLE
   AS $procedure$
   DECLARE
   v_i Getsystem_statistics_rs;
   v_sql  VARCHAR(4000);
   v_sys_entity VARCHAR(10);
BEGIN
   v_sql := NULL;

   v_sys_entity := upper(v_entity);
   if v_sys_entity = 'VM' then
      v_sql := 'select count(vm_guid) from vm_dynamic';
   elsif v_sys_entity = 'HOST' then
      v_sql := 'select count(vds_id) from vds_dynamic';
   elsif v_sys_entity = 'USER' then
      v_sql := 'select count(user_id) from users';
   elsif v_sys_entity = 'TSD' then
      v_sql := 'select count(id) from storage_domain_static';
   elsif v_sys_entity = 'ASD' then
      v_sql := 'select count(storage_id) from storage_pool_iso_map';
   else
      raise 'Unknown entity type "%"', v_entity;
   end if;

   if v_status != '' then
      if v_sys_entity != 'USER' and v_sys_entity != 'TSD' then
          v_sql := coalesce(v_sql, '') || ' where status in (' || coalesce(v_status, '') || ')';
      end if;
   end if;
   EXECUTE v_sql INTO v_i;
   RETURN v_i;
END; $procedure$
LANGUAGE plpgsql;



