CREATE OR REPLACE FUNCTION __temp_he_vm()
  RETURNS uuid AS
$BODY$
BEGIN
    RETURN (SELECT vm_guid FROM vm_static WHERE origin = 6);
END; $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION __temp_add_device(_name VARCHAR(30), _type VARCHAR(30), _managed BOOLEAN)
    RETURNS VOID AS
$BODY$
BEGIN
    INSERT
        INTO vm_device (type, device, device_id, vm_id, is_managed, is_plugged, address, spec_params)
        VALUES (_type, _name, uuid_generate_v1(), (SELECT __temp_he_vm()), _managed, TRUE, '', '{}');
END; $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION __temp_update_he_vm()
  RETURNS void AS
$BODY$
BEGIN
    -- If a managed hosted engine exists
    PERFORM * FROM vm_static WHERE origin = 6;
    IF FOUND THEN
        -- Delete all associated graphics and video devices
        DELETE FROM vm_device WHERE vm_id = (SELECT __temp_he_vm()) AND type IN ('video', 'graphics');
        -- Add correct graphics and video devices. The only way to detect if our partial imported HE VM uses SPICE or
        -- VNC is to check for the SPICEVMC device which could be imported
        PERFORM * FROM vm_device WHERE vm_id = (SELECT __temp_he_vm()) AND device = 'spicevmc';
        IF FOUND THEN
            -- We have A HE VM with SPICE
            PERFORM __temp_add_device('qxl', 'video', FALSE);
            PERFORM __temp_add_device('spice', 'graphics', TRUE);
            UPDATE vm_static SET default_display_type = 1 WHERE vm_guid = __temp_he_vm();
        ELSE
            -- We have A HE VM with VNC
            PERFORM __temp_add_device('cirrus', 'video', FALSE);
            PERFORM __temp_add_device('vnc', 'graphics', TRUE);
            UPDATE vm_static SET default_display_type = 0 WHERE vm_guid = __temp_he_vm();
        END IF;
        -- Hosted engine VM does not use that feature, always disable it
        UPDATE vm_static SET single_qxl_pci = false WHERE vm_guid = (SELECT __temp_he_vm());
        -- FORCE a OVF write on the next write cycle
        UPDATE vm_static SET db_generation = db_generation + 1 WHERE vm_guid = (SELECT __temp_he_vm());
    END IF;
END; $BODY$
LANGUAGE plpgsql;

SELECT __temp_update_he_vm();
drop function __temp_update_he_vm();
drop function __temp_add_device(VARCHAR(30), VARCHAR(30), BOOLEAN);
drop function __temp_he_vm();
