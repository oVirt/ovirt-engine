CREATE FUNCTION tmp_custom_properties_to_vm_devices() RETURNS void AS $$
DECLARE
    vm record;
    property record;
    mtype record;
    mdev_types text;
    nodisplay boolean;
    device_id uuid;
    spec_params text;
    new_properties text;
BEGIN
    FOR vm IN SELECT vm_guid, predefined_properties FROM vm_static
                     WHERE predefined_properties LIKE '%mdev_type=%'
    LOOP
        FOR property IN SELECT *
                        FROM regexp_split_to_table(vm.predefined_properties, ';') AS custom_property
                        WHERE custom_property LIKE 'mdev_type=%'
        LOOP
            mdev_types := substr(property.custom_property, 11);
            nodisplay := false;
            IF substr(mdev_types, 1, 10) = 'nodisplay,' THEN
               nodisplay := true;
               mdev_types := substr(mdev_types, 11);
            END IF;
            FOR mtype IN SELECT * FROM regexp_split_to_table(mdev_types, ',') AS split_value
            LOOP
                device_id := uuid_generate_v4();
                spec_params := ('{"mdevType": "' ||
                                mtype.split_value ||
                                '", "nodisplay": ' ||
                                nodisplay ||
                                '}'
                               );
                INSERT INTO vm_device (device_id, vm_id, type, device, address, is_managed, is_plugged, spec_params)
                       VALUES (device_id, vm.vm_guid, 'mdev', 'vgpu', '', 't', 't', spec_params);
            END LOOP;
        END LOOP;
        new_properties := (SELECT string_agg(custom_property, ';') FROM regexp_split_to_table(vm.predefined_properties, ';') AS custom_property WHERE custom_property NOT LIKE 'mdev_type=%');
        UPDATE vm_static SET predefined_properties = new_properties WHERE vm_guid = vm.vm_guid;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT tmp_custom_properties_to_vm_devices();

DROP FUNCTION tmp_custom_properties_to_vm_devices();
