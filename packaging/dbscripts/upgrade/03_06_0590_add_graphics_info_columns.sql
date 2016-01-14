-- Graphics Device migration script
--  * creating new graphics devices for all vm_static rows
--  * updating vm_dynamic:
--    - adding and updating new columns for dynamic information of graphics

-- create new graphics devices
--  spice
INSERT INTO vm_device(device_id,
    vm_id,
    type,
    device,
    address,
    boot_order,
    spec_params,
    is_managed,
    is_plugged,
    is_readonly,
    _create_date,
    _update_date,
    alias)
SELECT
    uuid_generate_v1(),
    vm_guid,
    'graphics',
    'spice',
    '',
    NULL,
    '',
    true,
    true,
    false,
    'now',
    NULL,
    NULL
FROM vm_static
WHERE default_display_type = 1 and not exists
    (select 1 from vm_device
        where
            vm_id = vm_guid
            and
            device = 'spice');

--  vnc
INSERT INTO vm_device(device_id,
    vm_id,
    type,
    device,
    address,
    boot_order,
    spec_params,
    is_managed,
    is_plugged,
    is_readonly,
    _create_date,
    _update_date,
    alias)
SELECT
    uuid_generate_v1(),
    vm_guid,
    'graphics',
    'vnc',
    '',
    NULL,
    '',
    true,
    true,
    false,
    'now',
    NULL,
    NULL
FROM vm_static
WHERE default_display_type = 0 and not exists
    (select 1 from vm_device
        where
            vm_id = vm_guid
            and
            device = 'vnc');

-- add graphics info columns
select fn_db_add_column('vm_dynamic', 'spice_port', 'integer default NULL');
select fn_db_add_column('vm_dynamic', 'spice_tls_port', 'integer default NULL');
select fn_db_add_column('vm_dynamic', 'spice_ip', 'varchar(255) default NULL');
select fn_db_add_column('vm_dynamic', 'vnc_port', 'integer default NULL');
select fn_db_add_column('vm_dynamic', 'vnc_ip', 'varchar(255) default NULL');
