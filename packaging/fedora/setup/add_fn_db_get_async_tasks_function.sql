/* Displays DC id , DC name, SPM Host id , SPM Host name and number of async tasks awaiting.

1) create a record type with DC name, DC id, SPM host id, SPM host name, count

2) get all distinct DC ids from the parameters Json representation

3) Run a cursor for each result in 2)

   a) get DC name
   b) get SPM Host id & name if available
   c) get count of tasks

   return current record

4) return set of generated records
*/

DROP TYPE IF EXISTS async_tasks_info_rs CASCADE;
CREATE TYPE async_tasks_info_rs AS (
    dc_id UUID, dc_name CHARACTER VARYING, spm_host_id UUID, spm_host_name CHARACTER VARYING, task_count integer);


create or replace FUNCTION fn_db_get_async_tasks()
returns SETOF async_tasks_info_rs
AS $procedure$
DECLARE
    v_record async_tasks_info_rs;

    -- selects all UUID values after the storage_pool_id and uuid found in serialized parameters
    v_tasks_cursor cursor for select distinct
         substring(
                   substring(
                             substring (action_parameters from 'storage_pool_id.*')--storage_pool_id
                   ,'uuid.*')--uuid
         ,'........-....-....-....-............') from async_tasks;--uuid value
begin

    OPEN v_tasks_cursor;
    FETCH v_tasks_cursor into v_record.dc_id;
    WHILE FOUND LOOP
        -- get dc_name and SPM Host id
        v_record.dc_name := name from storage_pool where id = v_record.dc_id;
        v_record.spm_host_id :=
            spm_vds_id from storage_pool where id = v_record.dc_id;
        -- get Host name if we have non NULL SPM Host
        if (v_record.spm_host_id IS NOT NULL) then
            v_record.spm_host_name :=
                vds_name from vds_static where vds_id = v_record.spm_host_id;
        else
            v_record.spm_host_name:='';
        end if;
        -- get tasks count for this DC
        v_record.task_count := count(*) from async_tasks
            where position (cast(v_record.dc_id as varchar) in action_parameters) > 0;
        -- return the record
        RETURN NEXT v_record;
        FETCH v_tasks_cursor into v_record.dc_id;
    END LOOP;
    CLOSE v_tasks_cursor;
    -- return full set of generated records
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

