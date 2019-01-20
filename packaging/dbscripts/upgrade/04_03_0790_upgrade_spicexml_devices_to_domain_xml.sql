DO $$
-- Copy the device address of spicevmc devices from the redirdev device types
-- and remove the redirdev devices when applicable.
  DECLARE
    device_cursor CURSOR FOR
        SELECT * FROM vm_device
        WHERE device='spicevmc'
            AND type='redir'
            AND NOT is_plugged;
    device_cursor_row RECORD;
    device_dev_cursor CURSOR FOR
        SELECT * FROM vm_device
        WHERE device='spicevmc'
            AND type='redirdev'
            AND device_cursor_row.vm_id = vm_id;
    device_dev_cursor_row RECORD;
  BEGIN
    OPEN device_cursor;
    LOOP
    FETCH device_cursor INTO device_cursor_row;
        EXIT WHEN NOT FOUND OR device_cursor_row IS NULL;
        BEGIN
            OPEN device_dev_cursor;
            FETCH device_dev_cursor INTO device_dev_cursor_row;
                IF FOUND THEN
                    BEGIN
                        UPDATE vm_device
                        SET is_plugged = true,
                            address = device_dev_cursor_row.address,
                            alias = device_dev_cursor_row.alias
                        WHERE device_cursor_row.vm_id = vm_id
                            AND device_cursor_row.device_id = device_id;
                        DELETE FROM vm_device
                        WHERE vm_id=device_dev_cursor_row.vm_id
                            AND device_id=device_dev_cursor_row.device_id;
                    END;
                ELSE
                    BEGIN
                        UPDATE vm_device
                        SET is_plugged = true
                        WHERE device_cursor_row.vm_id = vm_id
                            AND device_cursor_row.device_id = device_id;
                    END;
                END IF;
            CLOSE device_dev_cursor;
        END;
    END LOOP;
    CLOSE device_cursor;
  END;
$$;
