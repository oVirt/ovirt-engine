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
    v_is_readonly boolean,
    v_alias varchar(255),
    v_custom_properties text,
    v_snapshot_id uuid,
    v_logical_name varchar(255),
    v_is_using_scsi_reservation boolean)
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
        is_readonly,
        alias,
        custom_properties,
        snapshot_id,
        logical_name,
        is_using_scsi_reservation)
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
        v_is_readonly,
        v_alias,
        v_custom_properties,
        v_snapshot_id,
        v_logical_name,
        v_is_using_scsi_reservation);
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
    v_is_readonly boolean,
    v_alias varchar(255),
    v_custom_properties text,
    v_snapshot_id uuid,
    v_logical_name varchar(255),
    v_is_using_scsi_reservation boolean)
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
           is_readonly = v_is_readonly,
           alias = v_alias,
           custom_properties = v_custom_properties,
           snapshot_id = v_snapshot_id,
           logical_name = v_logical_name,
           is_using_scsi_reservation = v_is_using_scsi_reservation,
           _update_date = current_timestamp
    WHERE  device_id = v_device_id and vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateVmDeviceRuntimeInfo(
    v_device_id UUID,
    v_vm_id UUID,
    v_address varchar(255),
    v_alias varchar(255))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_device
    SET
           address = v_address,
           alias = v_alias,
           _update_date = current_timestamp
    WHERE  device_id = v_device_id and vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmDeviceForHotPlugDisk(
    v_device_id UUID,
    v_vm_id UUID,
    v_is_plugged boolean)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_device
    SET
           is_plugged = v_is_plugged,
           _update_date = current_timestamp
    WHERE  device_id = v_device_id and vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmDeviceBootOrder(
    v_device_id UUID,
    v_vm_id UUID,
    v_boot_order int)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE vm_device
    SET
           boot_order = v_boot_order,
           _update_date = current_timestamp
    WHERE  device_id = v_device_id and vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVmDevice(v_device_id UUID, v_vm_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   vm_device
    WHERE  device_id = v_device_id
    AND (v_vm_id IS NULL or vm_id = v_vm_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmDevicesByVmIdAndType(v_vm_id UUID, v_type VARCHAR(30))
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   vm_device
    WHERE  vm_id = v_vm_id AND type = v_type;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllFromVmDevice() RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByDeviceId(v_device_id UUID, v_vm_id UUID)
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view
    WHERE  device_id = v_device_id
    AND (v_vm_id IS NULL OR vm_id = v_vm_id);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmId(v_vm_id UUID)
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view
    WHERE  vm_id = v_vm_id order by device_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmIdAndType(v_vm_id UUID, v_type varchar(30))
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view
    WHERE  vm_id = v_vm_id and type = v_type
    ORDER BY NULLIF(alias,'') NULLS LAST;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmDeviceByVmIdTypeAndDevice(v_vm_id UUID, v_type varchar(30), v_device varchar(30), v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view
    WHERE  vm_id = v_vm_id and type = v_type and device = v_device
    AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                      FROM   user_vm_permissions_view
                                      WHERE  user_id = v_user_id AND entity_id = v_vm_id))
    ORDER BY NULLIF(alias,'') NULLS LAST;

END; $procedure$
LANGUAGE plpgsql;

create or replace FUNCTION GetVmUnmanagedDevicesByVmId(v_vm_id UUID)
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    select vm_device_view.* from vm_device_view
    where vm_id = v_vm_id and
          is_managed = false;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION isMemBalloonEnabled(v_vm_id UUID)
  RETURNS boolean STABLE AS
$BODY$
declare
    result boolean := false;
begin
    if exists (select 1 from vm_device where vm_id = v_vm_id and type = 'balloon' and device = 'memballoon') then
        result := true;
    end if;
    return result;
end;
$BODY$
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION clearVmDeviceAddress(v_device_id UUID)
  RETURNS VOID AS
$BODY$
begin
    update vm_device set address = '' where device_id = v_device_id;
end;
$BODY$
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION ExistsVmDeviceByVmIdAndType(v_vm_id UUID, v_type VARCHAR(30))
RETURNS BOOLEAN STABLE
AS $procedure$
BEGIN
  RETURN EXISTS(
    SELECT 1
    FROM vm_device
    WHERE vm_id = v_vm_id AND type = v_type);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDeviceByType(v_type VARCHAR(30))
RETURNS SETOF vm_device_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vm_device_view
    WHERE  type = v_type;
END; $procedure$
LANGUAGE plpgsql;
