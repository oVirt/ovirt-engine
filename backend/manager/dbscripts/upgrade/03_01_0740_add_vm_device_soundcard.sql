CREATE OR REPLACE FUNCTION __temp_add_vm_device_soundcard()
RETURNS void
AS $function$
DECLARE
    -- get all VM desktops
    v_cur CURSOR FOR SELECT * FROM vm_static where entity_type='VM' and vm_type=0;
    v_record vm_static%ROWTYPE;
    v_soundcard varchar(10);
    v_device_id UUID;
    v_spec_params text;
BEGIN
       OPEN v_cur;
       LOOP
           FETCH v_cur INTO v_record;
           EXIT WHEN NOT FOUND;
           case when v_record.os =1 or v_record.os =5 or v_record.os =6 or v_record.os =8 or
                      v_record.os =9 or v_record.os =10 or v_record.os =14 or v_record.os =15 then
		v_soundcard := 'ac97';
            else
		v_soundcard := 'ich6';
	   end case;
           if not exists (select 1 from vm_device where vm_id = v_record.vm_guid and type = 'sound') then
             v_device_id := uuid_generate_v1();
             v_spec_params := 'deviceId=' || v_device_id;
             insert INTO vm_device(
               device_id, vm_id,type,device,address,boot_order,spec_params,is_managed,is_plugged,is_readonly)
               values ( v_device_id, v_record.vm_guid, 'sound', v_soundcard, '', 0, v_spec_params, true, true, true);
           end if;
       END LOOP;
       CLOSE v_cur;
END; $function$
LANGUAGE plpgsql;
select __temp_add_vm_device_soundcard();
drop function __temp_add_vm_device_soundcard();
