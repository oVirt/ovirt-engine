CREATE OR REPLACE FUNCTION InsertHostDevice(
  v_host_id UUID,
  v_device_name VARCHAR(255),
  v_parent_device_name VARCHAR(255),
  v_capability VARCHAR(32),
  v_iommu_group INTEGER,
  v_product_name VARCHAR(255),
  v_product_id VARCHAR(255),
  v_vendor_name VARCHAR(255),
  v_vendor_id VARCHAR(255),
  v_physfn VARCHAR(255),
  v_total_vfs INTEGER,
  v_net_iface_name VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
  SET CONSTRAINTS ALL DEFERRED;
  INSERT INTO host_device(
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
    net_iface_name)
  VALUES(
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
    v_net_iface_name
  );
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostDevice(
  v_host_id UUID,
  v_device_name VARCHAR(255),
  v_parent_device_name VARCHAR(255),
  v_capability VARCHAR(32),
  v_iommu_group INTEGER,
  v_product_name VARCHAR(255),
  v_product_id VARCHAR(255),
  v_vendor_name VARCHAR(255),
  v_vendor_id VARCHAR(255),
  v_physfn VARCHAR(255),
  v_total_vfs INTEGER,
  v_net_iface_name VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
  SET CONSTRAINTS ALL DEFERRED;
  UPDATE host_device
  SET
    host_id = v_host_id,
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
    net_iface_name = v_net_iface_name
  WHERE host_id = v_host_id AND device_name = v_device_name;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteHostDevice(v_host_id UUID, v_device_name VARCHAR(255))
RETURNS VOID
AS $procedure$
BEGIN
  SET CONSTRAINTS ALL DEFERRED;
  DELETE
  FROM   host_device
  WHERE  host_id = v_host_id AND device_name = v_device_name;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDevicesByHostId(v_host_id UUID)
RETURNS SETOF host_device STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT  *
  FROM    host_device
  WHERE   host_id = v_host_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDevicesByHostIdAndIommuGroup(v_host_id UUID, v_iommu_group INTEGER)
RETURNS SETOF host_device STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT  *
  FROM    host_device
  WHERE   host_id = v_host_id AND iommu_group = v_iommu_group;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostDeviceByHostIdAndDeviceName(v_host_id UUID, v_device_name VARCHAR(255))
RETURNS SETOF host_device STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT  *
  FROM    host_device
  WHERE   host_id = v_host_id AND device_name = v_device_name;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromHostDevices()
RETURNS SETOF host_device STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT  *
  FROM    host_device;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmExtendedHostDevicesByVmId(v_vm_id UUID)
RETURNS SETOF vm_host_device_view STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT vm_host_device_view.*
  FROM   vm_host_device_view
  WHERE  vm_host_device_view.configured_vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetExtendedHostDevicesByHostId(v_host_id UUID)
RETURNS SETOF host_device_view STABLE
AS $procedure$
BEGIN
  RETURN QUERY
  SELECT host_device_view.*
  FROM   host_device_view
  WHERE  host_device_view.host_id = v_host_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CheckVmHostDeviceAvailability(v_vm_id UUID, v_host_id UUID)
RETURNS BOOLEAN STABLE
AS $procedure$
BEGIN
  RETURN NOT EXISTS(
    SELECT 1
    FROM vm_device
    WHERE vm_id = v_vm_id AND
          device IN (SELECT device_name
                     FROM host_device
                     WHERE host_id = v_host_id AND vm_id IS NOT NULL AND vm_id <> v_vm_id)); -- device free or already belonging to the vm
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION MarkHostDevicesUsedByVmId(v_vm_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
  UPDATE host_device
  SET vm_id = v_vm_id
  WHERE device_name IN (SELECT device
                        FROM vm_device
                        WHERE vm_id = v_vm_id AND type = 'hostdev');
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetVmIdOnHostDevice(v_host_id UUID, v_device_name VARCHAR(255), v_vm_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
  UPDATE host_device
  SET vm_id = v_vm_id
  WHERE host_id = v_host_id AND device_name = v_device_name;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION FreeHostDevicesUsedByVmId(v_vm_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
  UPDATE host_device
  SET vm_id = NULL
  WHERE device_name IN (SELECT device
                        FROM vm_device
                        WHERE vm_id = v_vm_id AND type = 'hostdev');
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION CleanDownVms()
RETURNS VOID
AS $procedure$
BEGIN
  UPDATE host_device
  SET vm_id = NULL
  FROM vm_dynamic
  WHERE host_device.vm_id = vm_dynamic.vm_guid AND vm_dynamic.status = 0;
END; $procedure$
LANGUAGE plpgsql;
