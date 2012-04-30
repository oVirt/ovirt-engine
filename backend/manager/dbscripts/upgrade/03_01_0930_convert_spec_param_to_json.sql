create or replace function __temp_convert_record_to_json(v_spec_param text)
returns text
AS $procedure$
declare
    params record;
    params_as_json text;
    key text;
    value text;
begin

params_as_json := '{ ';

for params in select * from regexp_split_to_table(v_spec_param, ',') as param
loop
    key := trim(split_part(params.param, '=', 1));
    -- the deviceId property is being sent as an explicit property to VDSM
    if (key != 'deviceId') then
        key := '"' || key || '"';
        value := '"' ||(trim(split_part(params.param, '=', 2))) || '"';
        params_as_json := params_as_json || key || ' : ' || value || ', ';
    end if;
end loop;

params_as_json := rtrim(rtrim(params_as_json), ',');

params_as_json := params_as_json || ' }';
return params_as_json;

END; $procedure$
LANGUAGE plpgsql;

update vm_device
set spec_params = __temp_convert_record_to_json(rtrim(ltrim(trim(both spec_params), '{'),'}'));

DROP FUNCTION __temp_convert_record_to_json(text);

