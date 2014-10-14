-- this method make sure there isn't any duplicates that may fail the
-- unique constraint, if so it adds a uuid to the name.
CREATE OR REPLACE FUNCTION __temp_qos_name_uniqueness()
  RETURNS void AS
$BODY$
DECLARE
   -- oredered cursor
   v_cur cursor for select * from qos order by qos_type, name, storage_pool_id;
   v_entry qos%ROWTYPE;
   v_qos_type smallint;
   v_name varchar(50);
   v_storage_pool_id uuid;
BEGIN
    open v_cur;
    loop
        fetch v_cur into v_entry;
        -- since the cursor is ordered, check that 2 consecutive rows not match
        if (v_entry.qos_type = v_qos_type and v_entry.name = v_name and v_entry.storage_pool_id = v_storage_pool_id) then
            update qos set name = v_name || '_' || cast(uuid_generate_v1() as varchar) where id = v_entry.id;
         end if;
        exit when not found;
        v_qos_type := v_entry.qos_type;
        v_name := v_entry.name;
        v_storage_pool_id :=  v_entry.storage_pool_id;
    end loop;
    close v_cur;
    -- adding constaint for qos name uniqueness of same context
    alter table qos add unique (qos_type, name, storage_pool_id);
END; $BODY$
LANGUAGE plpgsql;

select __temp_qos_name_uniqueness();
drop function __temp_qos_name_uniqueness();
