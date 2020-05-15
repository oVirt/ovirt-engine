

CREATE OR REPLACE FUNCTION InsertHostDevice (
    v_host_id UUID,
    v_device_name VARCHAR(255),
    v_parent_device_name VARCHAR(255),
    v_capability VARCHAR(32),
    v_iommu_group INT,
    v_product_name VARCHAR(255),
    v_product_id VARCHAR(255),
    v_vendor_name VARCHAR(255),
    v_vendor_id VARCHAR(255),
    v_physfn VARCHAR(255),
    v_total_vfs INT,
    v_net_iface_name VARCHAR(50),
    v_driver VARCHAR(255),
    v_is_assignable BOOLEAN,
    v_address VARCHAR(255),
    v_mdev_types TEXT,
    v_block_path TEXT,
    v_hostdev_spec_params TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    SET CONSTRAINTS ALL DEFERRED;

    INSERT INTO host_device (
        host_id,
        device_name,
        parent_device_name,
        capability,
        iommu_group,
        product_name,
        product_id,
        vendor_name,
        vendor_id,
        physfn,
        total_vfs,
        net_iface_name,
        driver,
        is_assignable,
        address,
        mdev_types,
        block_path,
        hostdev_spec_params
        )
    VALUES (
        v_host_id,
        v_device_name,
        v_parent_device_name,
        v_capability,
        v_iommu_group,
        v_product_name,
        v_product_id,
        v_vendor_name,
        v_vendor_id,
        v_physfn,
        v_total_vfs,
        v_net_iface_name,
        v_driver,
        v_is_assignable,
        v_address,
        v_mdev_types,
        v_block_path,
        v_hostdev_spec_params
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostDevice (
    v_host_id UUID,
    v_device_name VARCHAR(255),
    v_parent_device_name VARCHAR(255),
    v_capability VARCHAR(32),
    v_iommu_group INT,
    v_product_name VARCHAR(255),
    v_product_id VARCHAR(255),
    v_vendor_name VARCHAR(255),
    v_vendor_id VARCHAR(255),
    v_physfn VARCHAR(255),
    v_total_vfs INT,
    v_net_iface_name VARCHAR(50),
    v_driver VARCHAR(255),
    v_is_assignable BOOLEAN,
    v_address VARCHAR(255),
    v_mdev_types TEXT,
    v_block_path TEXT,
    v_hostdev_spec_params TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    SET CONSTRAINTS ALL DEFERRED;

    UPDATE host_device
    SET host_id = v_host_id,
        device_name = v_device_name,
        parent_device_name = v_parent_device_name,
        capability = v_capability,
        iommu_group = v_iommu_group,
        product_name = v_product_name,
        product_id = v_product_id,
        vendor_name = v_vendor_name,
        vendor_id = v_vendor_id,
        physfn = v_physfn,
        total_vfs = v_total_vfs,
        net_iface_name = v_net_iface_name,
        driver = v_driver,
        is_assignable = v_is_assignable,
        address = v_address,
        mdev_types = v_mdev_types,
        block_path = v_block_path,
        hostdev_spec_params = v_hostdev_spec_params
    WHERE host_id = v_host_id
        AND device_name = v_device_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteHostDevice (
    v_host_id UUID,
    v_device_name VARCHAR(255)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    SET CONSTRAINTS ALL DEFERRED;

    DELETE
    FROM host_device
    WHERE host_id = v_host_id
        AND device_name = v_device_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDevicesByHostId (v_host_id UUID)
RETURNS SETOF host_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_device
    WHERE host_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDevicesByHostIdAndIommuGroup (
    v_host_id UUID,
    v_iommu_group INT
    )
RETURNS SETOF host_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_device
    WHERE host_id = v_host_id
        AND iommu_group = v_iommu_group;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDeviceByHostIdAndDeviceName (
    v_host_id UUID,
    v_device_name VARCHAR(255)
    )
RETURNS SETOF host_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_device
    WHERE host_id = v_host_id
        AND device_name = v_device_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromHostDevices ()
RETURNS SETOF host_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_device;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmExtendedHostDevicesByVmId (v_vm_id UUID)
RETURNS SETOF vm_host_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_host_device_view.*
    FROM vm_host_device_view
    WHERE vm_host_device_view.configured_vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetExtendedHostDevicesByHostId (v_host_id UUID)
RETURNS SETOF host_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT host_device_view.*
    FROM host_device_view
    WHERE host_device_view.host_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CheckVmHostDeviceAvailability (
    v_vm_id UUID,
    v_host_id UUID
    )
RETURNS BOOLEAN STABLE AS $PROCEDURE$
BEGIN
    RETURN NOT EXISTS (
            SELECT 1
            FROM vm_device
            WHERE vm_id = v_vm_id
                AND device IN (
                    SELECT device_name
                    FROM host_device
                    WHERE host_id = v_host_id
                        AND vm_id IS NOT NULL
                        AND vm_id <> v_vm_id
                    )
            );-- device free or already belonging to the vm
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION MarkHostDevicesUsedByVmId (
    v_vm_id UUID,
    v_host_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_device
    SET vm_id = v_vm_id
    WHERE host_id = v_host_id
        AND device_name IN (
            SELECT device
            FROM vm_device
            WHERE vm_id = v_vm_id
                AND type = 'hostdev'
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetVmIdOnHostDevice (
    v_host_id UUID,
    v_device_name VARCHAR(255),
    v_vm_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_device
    SET vm_id = v_vm_id
    WHERE host_id = v_host_id
        AND device_name = v_device_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION FreeHostDevicesUsedByVmId (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_device
    SET vm_id = NULL
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CleanDownVms ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_device
    SET vm_id = NULL
    FROM vm_dynamic
    WHERE host_device.vm_id = vm_dynamic.vm_guid
        AND vm_dynamic.status = 0;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDevicesAttachedToHost (v_host_id UUID)
RETURNS SETOF vm_device AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_device.*
    FROM vm_device
    INNER JOIN vm_host_pinning_map
        ON vm_device.vm_id = vm_host_pinning_map.vm_id
            AND vm_host_pinning_map.vds_id = v_host_id
    WHERE vm_device.type = 'hostdev';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetUsedScsiDevicesByHostId (v_host_id UUID)
RETURNS SETOF host_device_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT hdv1.* FROM host_device_view hdv
        INNER JOIN luns
            ON hdv.device_name LIKE '%'||luns.lun_id
        INNER JOIN host_device_view hdv1
            ON hdv1.device_name = hdv.parent_device_name
        WHERE hdv1.host_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
