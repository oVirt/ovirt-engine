----------------------------------------------------------------
-- [vm_device] Table
--
Create or replace FUNCTION InsertVmDevice(
    v_device_id UUID,
    v_vm_id UUID,
    v_device varchar(30),
    v_type varchar(30),
    v_address varchar(255),
    v_boot_order int,
    v_spec_params text,
    v_is_managed boolean,
    v_is_plugged boolean,
    v_is_shared boolean,
    v_is_readonly boolean)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO vm_device(
        device_id,
        vm_id ,
        device ,
        type,
        address ,
        boot_order ,
        spec_params,
        is_managed,
        is_plugged,
        is_shared,
        is_readonly)
    VALUES(
        v_device_id ,
        v_vm_id ,
        v_device ,
        v_type ,
        v_address ,
        v_boot_order ,
        v_spec_params,
        v_is_managed,
        v_is_plugged,
        v_is_shared,
        v_is_readonly);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateVmDevice(
    v_device_id UUID,
    v_vm_id UUID,
    v_device varchar(30),
    v_type varchar(30),
    v_address varchar(255),
    v_boot_order int,
    v_spec_params text,
    v_is_managed boolean,
    v_is_plugged boolean,
    v_is_shared boolean,
    v_is_readonly boolean)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_device
    SET
           device = v_device,
           type = v_type,
           address = v_address,
           boot_order = v_boot_order,
           spec_params = v_spec_params,
           is_managed = v_is_managed,
           is_plugged = v_is_plugged,
           is_shared = v_is_shared,
           is_readonly = v_is_readonly
    WHERE  device_id = v_device_id and vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteVmDevice(v_device_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   vm_device
    WHERE  device_id = v_device_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllFromVmDevice() RETURNS SETOF vm_device
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByDeviceId(v_device_id UUID)
RETURNS SETOF vm_device
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device
    WHERE  device_id = v_device_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmId(v_vm_id UUID)
RETURNS SETOF vm_device
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device
    WHERE  vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmIdAndType(v_vm_id UUID, v_type varchar(30))
RETURNS SETOF vm_device
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device
    WHERE  vm_id = v_vm_id and type = v_type ;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmIdTypeAndDevice(v_vm_id UUID, v_type varchar(30), v_device varchar(30))
RETURNS SETOF vm_device
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device
    WHERE  vm_id = v_vm_id and type = v_type and device = v_device;
END; $procedure$
LANGUAGE plpgsql;

