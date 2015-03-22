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
