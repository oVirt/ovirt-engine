CREATE OR REPLACE FUNCTION __temp_add_balloon_to_blank_if_not_present ()
RETURNS void AS $FUNCTION$
BEGIN
    IF NOT EXISTS (
            SELECT 1
            FROM vm_device
            WHERE vm_id = '00000000-0000-0000-0000-000000000000'
                AND type = 'balloon'
                AND device = 'memballoon'
            LIMIT 1
            ) THEN
        INSERT INTO vm_device (
            device_id,
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
            alias
            )
        SELECT uuid_generate_v1(),
            '00000000-0000-0000-0000-000000000000',
            'balloon',
            'memballoon',
            '',
            0,
            '{"model" : "virtio"}',
            true,
            true,
            true,
            'now',
            NULL,
            NULL;
    END IF;
END;$FUNCTION$
LANGUAGE plpgsql;

SELECT __temp_add_balloon_to_blank_if_not_present();
DROP FUNCTION __temp_add_balloon_to_blank_if_not_present();
