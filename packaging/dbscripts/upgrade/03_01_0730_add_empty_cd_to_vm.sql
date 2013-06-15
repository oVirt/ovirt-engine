-- This upgrade script adds an empty CD for each VM that has an empty string in its iso_path
-- we currently have either a real CD (with valid iso_path) or an empty CD for a VM

CREATE OR REPLACE FUNCTION __temp_add_empty_cd_to_vm()
RETURNS void
AS $function$
DECLARE
    v_cur CURSOR FOR SELECT vm_guid FROM vm_static where entity_type='VM';
    v_vm_id UUID;
    v_device_id UUID;
    v_spec_params text;
BEGIN
    OPEN v_cur;
    LOOP
        FETCH v_cur INTO v_vm_id;
        EXIT WHEN NOT FOUND;
        v_device_id:=uuid_generate_v1();
        v_spec_params:= 'path=' || ',deviceId=' || v_device_id;
        if not exists (select vm_id from vm_device where vm_id = v_vm_id and type = 'disk' and device = 'cdrom') then
            insert into vm_device(
                   device_id,vm_id,type,device,address,boot_order,spec_params,is_managed,is_plugged,is_readonly)
            values(v_device_id,v_vm_id, 'disk','cdrom','',0,v_spec_params,true,true,true);
        end if;
    END LOOP;
    CLOSE v_cur;
END; $function$
LANGUAGE plpgsql;

select __temp_add_empty_cd_to_vm();
drop function __temp_add_empty_cd_to_vm();

