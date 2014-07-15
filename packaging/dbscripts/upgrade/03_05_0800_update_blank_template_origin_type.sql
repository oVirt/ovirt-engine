-- update blank template origin to the origin in the config table
-- there is also conversion from the enum name that is saved in the config
-- to the value that is saved in vm_static ('RHEV'->0 otherwise use 'OVIRT'->3)
update vm_static set
origin=
    case
        when exists (select * from vdc_options where option_name = 'OriginType' and option_value = 'RHEV')
        then 0
        else 3
    end
where vm_guid='00000000-0000-0000-0000-000000000000';
