

----------------------------------------------------------------
-- [network] Table
--



Create or replace FUNCTION Insertnetwork(v_addr VARCHAR(50) ,
	v_description VARCHAR(4000) ,
	v_free_text_comment text,
	v_id UUID,
	v_name VARCHAR(50),
	v_subnet VARCHAR(20) ,
	v_gateway VARCHAR(20) ,
	v_type INTEGER ,
	v_vlan_id INTEGER ,
	v_stp BOOLEAN ,
    	v_storage_pool_id UUID,
	v_mtu INTEGER,
	v_vm_network BOOLEAN,
	v_provider_network_provider_id UUID,
	v_provider_network_external_id TEXT,
	v_qos_id UUID,
	v_label TEXT)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO network(addr, description, free_text_comment, id, name, subnet, gateway, type, vlan_id, stp, storage_pool_id, mtu, vm_network, provider_network_provider_id, provider_network_external_id, qos_id, label)
	VALUES(v_addr, v_description, v_free_text_comment, v_id, v_name, v_subnet, v_gateway, v_type, v_vlan_id, v_stp, v_storage_pool_id, v_mtu, v_vm_network, v_provider_network_provider_id, v_provider_network_external_id, v_qos_id, v_label);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Updatenetwork(v_addr VARCHAR(50) ,
	v_description VARCHAR(4000) ,
	v_free_text_comment text,
	v_id UUID,
	v_name VARCHAR(50),
	v_subnet VARCHAR(20) ,
	v_gateway VARCHAR(20) ,
	v_type INTEGER ,
	v_vlan_id INTEGER ,
	v_stp BOOLEAN ,
	v_storage_pool_id UUID,
	v_mtu INTEGER,
	v_vm_network BOOLEAN,
	v_provider_network_provider_id UUID,
	v_provider_network_external_id TEXT,
	v_qos_id UUID,
	v_label TEXT)
RETURNS VOID

	--The [network] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE network
      SET addr = v_addr,description = v_description, free_text_comment = v_free_text_comment, name = v_name,subnet = v_subnet,
      gateway = v_gateway,type = v_type,vlan_id = v_vlan_id,
      stp = v_stp,storage_pool_id = v_storage_pool_id, mtu = v_mtu,
      vm_network = v_vm_network,
      provider_network_provider_id = v_provider_network_provider_id,
      provider_network_external_id = v_provider_network_external_id,
      qos_id = v_qos_id,
      label = v_label
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletenetwork(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM network  WHERE id = v_id     FOR UPDATE;

   DELETE FROM network
   WHERE id = v_id;

   -- Delete the network's permissions
   DELETE FROM permissions WHERE object_id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromnetwork(v_user_id uuid, v_is_filtered boolean) RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network
   WHERE NOT v_is_filtered OR EXISTS (SELECT 1
                                      FROM   user_network_permissions_view
                                      WHERE  user_id = v_user_id AND entity_id = network.id);

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetnetworkByid(v_id UUID, v_user_id uuid, v_is_filtered boolean) RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM network
   WHERE id = v_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                    FROM   user_network_permissions_view
                                    WHERE  user_id = v_user_id AND entity_id = v_id));

END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetnetworkByName(v_networkName VARCHAR(50))
RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network
   WHERE name = v_networkName;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetNetworkByNameAndDataCenter(v_name VARCHAR(50), v_storage_pool_id UUID)
RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT network.*
   FROM network
   WHERE network.name = v_name
   AND   network.storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetNetworkByNameAndCluster(v_name VARCHAR(50), v_cluster_id UUID)
RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT network.*
   FROM network
   WHERE network.name = v_name
   AND EXISTS (SELECT 1
               FROM network_cluster
               WHERE network.id = network_cluster.network_id
               AND   network_cluster.cluster_id = v_cluster_id);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetManagementNetworkByCluster(v_cluster_id UUID)
RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
   RETURN QUERY
   SELECT network.*
   FROM network
   WHERE id = (SELECT network_id
               FROM network_cluster
               WHERE network_cluster.cluster_id = v_cluster_id
               AND   network_cluster.management);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllNetworkByStoragePoolId(v_id UUID, v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF network STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM network
   WHERE storage_pool_id = v_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_network_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = network.id));

END; $procedure$
LANGUAGE plpgsql;


DROP TYPE IF EXISTS networkViewClusterType CASCADE;
CREATE TYPE networkViewClusterType AS(id uuid,name VARCHAR(50),description VARCHAR(4000), free_text_comment text, type INTEGER,
            addr VARCHAR(50),subnet VARCHAR(20),gateway VARCHAR(20),vlan_id INTEGER,stp BOOLEAN,storage_pool_id UUID,
	    mtu INTEGER, vm_network BOOLEAN, label TEXT,
	    provider_network_provider_id UUID, provider_network_external_id TEXT, qos_id UUID,
	    network_id UUID,cluster_id UUID, status INTEGER, is_display BOOLEAN,
	    required BOOLEAN, migration BOOLEAN);
Create or replace FUNCTION GetAllNetworkByClusterId(v_id UUID, v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF networkViewClusterType STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT
    DISTINCT
    network.id,
    network.name,
    network.description,
    network.free_text_comment,
    network.type,
    network.addr,
    network.subnet,
    network.gateway,
    network.vlan_id,
    network.stp,
    network.storage_pool_id,
    network.mtu,
    network.vm_network,
    network.label,
    network.provider_network_provider_id,
    network.provider_network_external_id,
    network.qos_id,
    network_cluster.network_id,
    network_cluster.cluster_id,
    network_cluster.status,
    network_cluster.is_display,
    network_cluster.required,
    network_cluster.migration
   FROM network
   INNER JOIN network_cluster
   ON network.id = network_cluster.network_id
   WHERE network_cluster.cluster_id = v_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_network_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = network.id))
   ORDER BY network.name;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllNetworksByQosId(v_id UUID)
RETURNS SETOF network STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   network
    WHERE  qos_id = v_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllNetworksByNetworkProviderId(v_id UUID)
RETURNS SETOF network STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   network
    WHERE  provider_network_provider_id = v_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllNetworkViewsByNetworkProviderId(v_id UUID)
RETURNS SETOF network_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   network_view
    WHERE  provider_network_provider_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllNetworkLabelsByDataCenterId(v_id UUID)
RETURNS SETOF TEXT STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT DISTINCT label
    FROM network
    WHERE network.storage_pool_id = v_id
    AND label IS NOT NULL;
END; $procedure$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [network] table doesn't have at least one foreign key column or the foreign keys are also primary keys.

----------------------------------------------------------------
-- [vds_interface] Table
--


Create or replace FUNCTION Insertvds_interface(v_addr VARCHAR(20) ,
 v_bond_name VARCHAR(50) ,
 v_bond_type INTEGER ,
 v_gateway VARCHAR(20) ,
 v_id UUID,
 v_is_bond BOOLEAN ,
 v_bond_opts VARCHAR(4000) ,
 v_mac_addr VARCHAR(20) ,
 v_name VARCHAR(50),
 v_network_name VARCHAR(50) ,
 v_speed INTEGER ,
 v_subnet VARCHAR(20) ,
 v_boot_protocol INTEGER ,
 v_type INTEGER ,
 v_vds_id UUID,
 v_base_interface VARCHAR(50) ,
 v_vlan_id INTEGER,
 v_mtu INTEGER,
 v_bridged BOOLEAN,
 v_qos_overridden BOOLEAN,
 v_labels TEXT,
 v_custom_properties TEXT)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vds_interface(addr, bond_name, bond_type, gateway, id, is_bond, bond_opts, mac_addr, name, network_name, speed, subnet, boot_protocol, type, VDS_ID, base_interface, vlan_id, mtu, bridged, qos_overridden, labels, custom_properties)
	VALUES(v_addr, v_bond_name, v_bond_type, v_gateway, v_id, v_is_bond, v_bond_opts, v_mac_addr, v_name, v_network_name, v_speed, v_subnet, v_boot_protocol, v_type, v_vds_id, v_base_interface, v_vlan_id, v_mtu, v_bridged, v_qos_overridden, v_labels, v_custom_properties);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatevds_interface(v_addr VARCHAR(20) ,
 v_bond_name VARCHAR(50) ,
 v_bond_type INTEGER ,
 v_gateway VARCHAR(20) ,
 v_id UUID,
 v_is_bond BOOLEAN ,
 v_bond_opts VARCHAR(4000) ,
 v_mac_addr VARCHAR(20) ,
 v_name VARCHAR(50),
 v_network_name VARCHAR(50) ,
 v_speed INTEGER ,
 v_subnet VARCHAR(20) ,
 v_boot_protocol INTEGER ,
 v_type INTEGER ,
 v_vds_id UUID,
 v_base_interface VARCHAR(50),
 v_vlan_id INTEGER,
 v_mtu INTEGER,
 v_bridged BOOLEAN,
 v_qos_overridden BOOLEAN,
 v_labels TEXT,
 v_custom_properties TEXT)
RETURNS VOID

	--The [vds_interface] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vds_interface
      SET addr = v_addr,bond_name = v_bond_name,bond_type = v_bond_type,gateway = v_gateway,
      is_bond = v_is_bond,bond_opts = v_bond_opts,mac_addr = v_mac_addr,
      name = v_name,network_name = v_network_name,speed = v_speed,
      subnet = v_subnet,boot_protocol = v_boot_protocol,
      type = v_type,VDS_ID = v_vds_id,base_interface = v_base_interface,vlan_id = v_vlan_id,_update_date = LOCALTIMESTAMP, mtu = v_mtu,
      bridged = v_bridged, qos_overridden = v_qos_overridden, labels = v_labels,
      custom_properties = v_custom_properties
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletevds_interface(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM vds_interface
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getinterface_viewByvds_id(v_vds_id UUID, v_user_id UUID, v_is_filtered boolean)
RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vds_interface_view
   WHERE vds_id = v_vds_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_vds_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = v_vds_id));

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetHostNetworksByCluster(v_cluster_id UUID)
RETURNS TABLE(vds_id UUID, network_name VARCHAR) STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_static.vds_id, vds_interface.network_name
   FROM vds_static
   JOIN vds_interface ON vds_interface.vds_id = vds_static.vds_id
   AND vds_static.vds_group_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getinterface_viewByAddr(v_cluster_id UUID, v_addr VARCHAR(50))
RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_interface_view.*
   FROM vds_interface_view
   INNER JOIN vds_static
   ON vds_interface_view.vds_id = vds_static.vds_id
   WHERE vds_interface_view.addr = v_addr
   AND vds_static.vds_group_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsManagedInterfaceByVdsId(v_vds_id UUID, v_user_id UUID, v_is_filtered boolean)
RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vds_interface_view
   -- Checking if the 2nd bit in the type column is set, meaning that the interface is managed
   WHERE vds_id = v_vds_id AND (type & 2) = 2
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_vds_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = v_vds_id));

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVdsInterfacesByNetworkId(v_network_id UUID) RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_interface_view.*
   FROM vds_interface_view
   INNER JOIN vds
   ON vds.vds_id = vds_interface_view.vds_id
   INNER JOIN network_cluster
   ON network_cluster.cluster_id = vds.vds_group_id
   INNER JOIN network
   ON network.id = network_cluster.network_id
   AND network.name = vds_interface_view.network_name
   WHERE network.id = v_network_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVdsInterfaceById(v_vds_interface_id UUID) RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vds_interface_view
   WHERE id = v_vds_interface_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetInterfacesByClusterId(v_cluster_id UUID)
RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_interface_view.*
   FROM vds_interface_view
   INNER JOIN vds_static
   ON vds_interface_view.vds_id = vds_static.vds_id
   WHERE vds_static.vds_group_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetInterfacesByDataCenterId(v_data_center_id UUID)
RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_interface_view.*
   FROM vds_interface_view
   INNER JOIN vds_static
   ON vds_interface_view.vds_id = vds_static.vds_id
   INNER JOIN vds_groups
   ON vds_static.vds_group_id = vds_groups.vds_group_id
   WHERE vds_groups.storage_pool_id = v_data_center_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface] Table
----------------------------------------------------------------
Create or replace FUNCTION InsertVmInterface(v_id UUID,
    v_mac_addr VARCHAR(20) ,
    v_name VARCHAR(50),
    v_speed INTEGER ,
    v_vnic_profile_id UUID ,
    v_vm_guid UUID ,
    v_vmt_guid UUID ,
    v_type INTEGER,
    v_linked BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_interface(id, mac_addr, name, speed, vnic_profile_id, vm_guid, vmt_guid, type, linked)
       VALUES(v_id, v_mac_addr, v_name, v_speed, v_vnic_profile_id, v_vm_guid, v_vmt_guid, v_type, v_linked);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmInterface(v_id UUID,
    v_mac_addr VARCHAR(20) ,
    v_name VARCHAR(50),
    v_speed INTEGER ,
    v_vnic_profile_id UUID ,
    v_vm_guid UUID ,
    v_vmt_guid UUID ,
    v_type INTEGER,
    v_linked BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_interface
      SET mac_addr = v_mac_addr,name = v_name, speed = v_speed, vnic_profile_id = v_vnic_profile_id, vm_guid = v_vm_guid,
      vmt_guid = v_vmt_guid,type = v_type, _update_date = LOCALTIMESTAMP, linked = v_linked
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVmInterface(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

   -- Get (and keep) a shared lock with "right to upgrade to exclusive"
   -- in order to force locking parent before children
   select   id INTO v_val FROM vm_interface  WHERE id = v_id     FOR UPDATE;

   DELETE FROM vm_interface
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmInterfaceByVmInterfaceId(v_id UUID) RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface
   WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromVmInterfaces() RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmInterfacesByVmId(v_vm_id UUID)
RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface
   WHERE vm_guid = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmInterfaceByTemplateId(v_template_id UUID)
RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface
   WHERE vmt_guid = v_template_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmInterfacesByNetworkId(v_network_id UUID) RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT vm_interface.*
   FROM vm_interface
   INNER JOIN vnic_profiles ON vm_interface.vnic_profile_id = vnic_profiles.id
   INNER JOIN vm_static on vm_interface.vm_guid = vm_static.vm_guid
   WHERE vnic_profiles.network_id = v_network_id
   AND vm_static.entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmTemplateInterfacesByNetworkId(v_network_id UUID) RETURNS SETOF vm_interface STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT vm_interface.*
   FROM vm_interface
   INNER JOIN vm_static on vm_interface.vmt_guid = vm_static.vm_guid
   INNER JOIN vnic_profiles ON vm_interface.vnic_profile_id = vnic_profiles.id
   WHERE vnic_profiles.network_id = v_network_id
   AND vm_static.entity_type  = 'TEMPLATE';
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetMacsByDataCenterId(v_data_center_id UUID) RETURNS SETOF varchar STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT mac_addr
   FROM vm_interface
   WHERE EXISTS (SELECT 1
                 FROM vm_static
                 JOIN vds_groups ON vm_static.vds_group_id = vds_groups.vds_group_id
                 WHERE vds_groups.storage_pool_id = v_data_center_id
                 AND vm_static.vm_guid = vm_interface.vm_guid);
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- VM Interface View
----------------------------------------------------------------

Create or replace FUNCTION GetAllFromVmNetworkInterfaceViews() RETURNS SETOF vm_interface_view STABLE
AS $procedure$
BEGIN
RETURN QUERY SELECT *
FROM vm_interface_view;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmNetworkInterfaceViewByVmNetworkInterfaceViewId(v_id UUID)
RETURNS SETOF vm_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface_view
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetPluggedVmInterfacesByMac(v_mac_address VARCHAR(20))
RETURNS SETOF vm_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface_view
   WHERE mac_addr = v_mac_address
   AND is_plugged = true;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmNetworkInterfaceViewByVmId(v_vm_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF vm_interface_view STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface_view
   WHERE vm_guid = v_vm_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
   FROM   user_vm_permissions_view
   WHERE  user_id = v_user_id AND entity_id = v_vm_id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmNetworkInterfaceViewByTemplateId(v_template_id UUID, v_user_id UUID, v_is_filtered boolean)
RETURNS SETOF vm_interface_view STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vm_interface_view
   WHERE vmt_guid = v_template_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
   FROM   user_vm_template_permissions_view
   WHERE  user_id = v_user_id AND entity_id = v_template_id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmInterfaceViewsByNetworkId(v_network_id UUID) RETURNS SETOF vm_interface_view STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT vm_interface_view.*
   FROM vm_interface_view
   INNER JOIN vnic_profiles ON vnic_profiles.id = vm_interface_view.vnic_profile_id
   WHERE vnic_profiles.network_id = v_network_id
   AND vm_interface_view.vm_entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmTemplateInterfaceViewsByNetworkId(v_network_id UUID) RETURNS SETOF vm_interface_view STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT vm_interface_view.*
   FROM vm_interface_view
   INNER JOIN vnic_profiles ON vnic_profiles.id = vm_interface_view.vnic_profile_id
   WHERE vnic_profiles.network_id = v_network_id
   AND vm_interface_view.vm_entity_type = 'TEMPLATE';
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface_statistics] Table
--


Create or replace FUNCTION Getvm_interface_statisticsById(v_id UUID) RETURNS SETOF vm_interface_statistics STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM vm_interface_statistics
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Insertvm_interface_statistics(v_id UUID,
	v_rx_drop DECIMAL(18,0) ,
	v_rx_rate DECIMAL(18,0) ,
	v_tx_drop DECIMAL(18,0) ,
	v_tx_rate DECIMAL(18,0) ,
	v_iface_status INTEGER ,
	v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_interface_statistics(id, rx_drop, rx_rate, tx_drop, tx_rate, vm_id, iface_status)
	VALUES(v_id, v_rx_drop, v_rx_rate, v_tx_drop, v_tx_rate, v_vm_id,v_iface_status);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatevm_interface_statistics(v_id UUID,
 v_rx_drop DECIMAL(18,0) ,
 v_rx_rate DECIMAL(18,0) ,
 v_tx_drop DECIMAL(18,0) ,
 v_tx_rate DECIMAL(18,0) ,
 v_iface_status INTEGER ,
 v_vm_id UUID)
RETURNS VOID

	--The [vm_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_interface_statistics
      SET rx_drop = v_rx_drop,rx_rate = v_rx_rate,tx_drop = v_tx_drop,tx_rate = v_tx_rate,
      vm_id = v_vm_id,iface_status = v_iface_status, _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletevm_interface_statistics(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM vm_interface_statistics  WHERE id = v_id     FOR UPDATE;

   DELETE FROM vm_interface_statistics
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




----------------------------------------------------------------
-- [network_cluster] Table
--
Create or replace FUNCTION GetVmGuestAgentInterfacesByVmId(v_vm_id UUID, v_user_id UUID, v_filtered BOOLEAN)
RETURNS SETOF vm_guest_agent_interfaces STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM vm_guest_agent_interfaces
   WHERE vm_id = v_vm_id
   AND (NOT v_filtered OR EXISTS (SELECT 1
                                  FROM   user_vm_permissions_view
                                  WHERE  user_id = v_user_id AND entity_id = v_vm_id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVmGuestAgentInterfacesByVmId(v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   DELETE FROM vm_guest_agent_interfaces
   WHERE vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertVmGuestAgentInterface(v_vm_id UUID,
   v_interface_name VARCHAR(50),
   v_mac_address VARCHAR(59),
   v_ipv4_addresses text,
   v_ipv6_addresses text)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_guest_agent_interfaces(vm_id, interface_name, mac_address, ipv4_addresses, ipv6_addresses)
       VALUES(v_vm_id, v_interface_name, v_mac_address, v_ipv4_addresses, v_ipv6_addresses);
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [vds_interface_statistics] Table
--


Create or replace FUNCTION Insertvds_interface_statistics(v_id UUID,
	v_rx_drop DECIMAL(18,0) ,
	v_rx_rate DECIMAL(18,0) ,
	v_tx_drop DECIMAL(18,0) ,
	v_tx_rate DECIMAL(18,0) ,
	v_iface_status INTEGER ,
	v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vds_interface_statistics(id, rx_drop, rx_rate, tx_drop, tx_rate, vds_id, iface_status)
	VALUES(v_id, v_rx_drop, v_rx_rate, v_tx_drop, v_tx_rate, v_vds_id,v_iface_status);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatevds_interface_statistics(v_id UUID,
 v_rx_drop DECIMAL(18,0) ,
 v_rx_rate DECIMAL(18,0) ,
 v_tx_drop DECIMAL(18,0) ,
 v_tx_rate DECIMAL(18,0) ,
 v_iface_status INTEGER ,
 v_vds_id UUID)
RETURNS VOID

	--The [vds_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vds_interface_statistics
      SET rx_drop = v_rx_drop,rx_rate = v_rx_rate,tx_drop = v_tx_drop,tx_rate = v_tx_rate,
      vds_id = v_vds_id,iface_status = v_iface_status, _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletevds_interface_statistics(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM vds_interface_statistics  WHERE id = v_id     FOR UPDATE;

   DELETE FROM vds_interface_statistics
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;






----------------------------------------------------------------
-- [network_cluster] Table
--


Create or replace FUNCTION Insertnetwork_cluster(v_cluster_id UUID,
   v_network_id UUID,
   v_status INTEGER,
   v_is_display BOOLEAN,
   v_required BOOLEAN,
   v_migration BOOLEAN,
   v_management BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO network_cluster(cluster_id, network_id, status, is_display, required, migration, management)
	VALUES(v_cluster_id, v_network_id, v_status, v_is_display, v_required, v_migration, v_management);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Updatenetwork_cluster(v_cluster_id UUID,
    v_network_id UUID,
    v_status INTEGER,
    v_is_display BOOLEAN,
    v_required BOOLEAN,
    v_migration BOOLEAN,
    v_management BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
   UPDATE network_cluster
   SET status = v_status,
       is_display = v_is_display,
       required = v_required,
       migration = v_migration,
       management = v_management
   WHERE cluster_id = v_cluster_id
   AND network_id = v_network_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Updatenetwork_cluster_status(v_cluster_id UUID,
        v_network_id UUID,
        v_status INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
   UPDATE network_cluster
   SET status = v_status
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION Deletenetwork_cluster(v_cluster_id UUID,
	v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM network_cluster
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromnetwork_cluster() RETURNS SETOF network_cluster STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network_cluster;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromnetwork_clusterByClusterId(v_cluster_id UUID)
RETURNS SETOF network_cluster STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network_cluster
   WHERE cluster_id = v_cluster_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromnetwork_clusterByNetworkId(v_network_id UUID)
RETURNS SETOF network_cluster STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network_cluster
   WHERE network_id = v_network_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getnetwork_clusterBycluster_idAndBynetwork_id(v_cluster_id UUID,
 v_network_id UUID) RETURNS SETOF network_cluster STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network_cluster
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetvmStaticByGroupIdAndNetwork(v_groupId UUID,
     v_networkName VARCHAR(50)) RETURNS SETOF vm_static STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT
   vm_static.* from vm_static
   inner join vm_interface_view
   on vm_static.vm_guid = vm_interface_view.vm_guid
   and network_name = v_networkName
   and vm_static.vds_group_id = v_groupId;


END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION set_network_exclusively_as_display(v_cluster_id UUID, v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE network_cluster
   SET is_display = true
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;

   IF FOUND THEN
       UPDATE network_cluster
       SET is_display = false
       WHERE cluster_id = v_cluster_id AND network_id != v_network_id;
   END IF;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION set_network_exclusively_as_migration(v_cluster_id UUID, v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE network_cluster
   SET migration = true
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;

   IF FOUND THEN
       UPDATE network_cluster
       SET migration = false
       WHERE cluster_id = v_cluster_id AND network_id != v_network_id;
   END IF;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION set_network_exclusively_as_management(v_cluster_id UUID, v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE network_cluster
   SET management = true
   WHERE cluster_id = v_cluster_id AND network_id = v_network_id;

   IF FOUND THEN
       UPDATE network_cluster
       SET management = false
       WHERE cluster_id = v_cluster_id AND network_id != v_network_id;
   END IF;

END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------------
--  Vnic Profile
----------------------------------------------------------------------

Create or replace FUNCTION GetVnicProfileByVnicProfileId(v_id UUID)
RETURNS SETOF vnic_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM vnic_profiles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertVnicProfile(v_id UUID,
  v_name VARCHAR(50),
  v_network_id UUID,
  v_network_qos_id UUID,
  v_port_mirroring BOOLEAN,
  v_custom_properties TEXT,
  v_description TEXT)
RETURNS VOID
   AS $procedure$
BEGIN

   INSERT INTO vnic_profiles(id, name, network_id, network_qos_id, port_mirroring, custom_properties, description)
       VALUES(v_id, v_name, v_network_id, v_network_qos_id, v_port_mirroring, v_custom_properties, v_description);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVnicProfile(v_id UUID,
  v_name VARCHAR(50),
  v_network_id UUID,
  v_network_qos_id UUID,
  v_port_mirroring BOOLEAN,
  v_custom_properties TEXT,
  v_description TEXT)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE vnic_profiles
   SET id = v_id, name = v_name, network_id = v_network_id, network_qos_id = v_network_qos_id,
   port_mirroring = v_port_mirroring, custom_properties = v_custom_properties,
   description = v_description,_update_date = LOCALTIMESTAMP
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVnicProfile(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val UUID;
BEGIN

    DELETE FROM vnic_profiles
    WHERE id = v_id;

    -- Delete the vnic profiles permissions
    DELETE FROM permissions WHERE object_id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromVnicProfiles()
RETURNS SETOF vnic_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM vnic_profiles;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVnicProfilesByNetworkId(v_network_id UUID)
RETURNS SETOF vnic_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM vnic_profiles
   WHERE network_id = v_network_id;

END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------------
--  Vnic Profile View
----------------------------------------------------------------------
Create or replace FUNCTION GetVnicProfileViewByVnicProfileViewId(v_id UUID, v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF vnic_profiles_view STABLE
AS $procedure$
BEGIN

RETURN QUERY SELECT *
FROM vnic_profiles_view
WHERE id = v_id
AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                  FROM   user_vnic_profile_permissions_view
                                  WHERE  user_id = v_user_id AND entity_id = vnic_profiles_view.id));

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromVnicProfileViews(v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF vnic_profiles_view STABLE
AS $procedure$
BEGIN

RETURN QUERY SELECT *
FROM vnic_profiles_view
WHERE NOT v_is_filtered OR EXISTS (SELECT 1
                                   FROM   user_vnic_profile_permissions_view
                                   WHERE  user_id = v_user_id AND entity_id = vnic_profiles_view.id);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVnicProfileViewsByNetworkId(v_network_id UUID, v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF vnic_profiles_view STABLE
AS $procedure$
BEGIN

RETURN QUERY SELECT *
FROM vnic_profiles_view
WHERE network_id = v_network_id
AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                  FROM   user_vnic_profile_permissions_view
                                  WHERE  user_id = v_user_id AND entity_id = vnic_profiles_view.id));

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVnicProfileViewsByDataCenterId(v_id UUID, v_user_id uuid, v_is_filtered boolean)
RETURNS SETOF vnic_profiles_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   vnic_profiles_view
    WHERE  data_center_id = v_id
    AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                      FROM   user_vnic_profile_permissions_view
                                      WHERE  user_id = v_user_id AND entity_id = vnic_profiles_view.id));
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVnicProfileViewsByNetworkQosId(v_network_qos_id UUID) RETURNS SETOF vnic_profiles_view STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM vnic_profiles_view
   WHERE network_qos_id = v_network_qos_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetIscsiIfacesByHostIdAndStorageTargetId(v_host_id UUID, v_target_id varchar(50)) RETURNS SETOF vds_interface_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds_interface_view.*
   FROM vds_interface_view,
        network_cluster,
        network,
        iscsi_bonds_networks_map,
        iscsi_bonds_storage_connections_map
   WHERE
       iscsi_bonds_storage_connections_map.connection_id = v_target_id AND
       iscsi_bonds_storage_connections_map.iscsi_bond_id = iscsi_bonds_networks_map.iscsi_bond_id AND
       iscsi_bonds_networks_map.network_id = network.id AND
       network.id = network_cluster.network_id AND
       network.name = vds_interface_view.network_name AND
       network_cluster.cluster_id = vds_interface_view.vds_group_id AND
       vds_interface_view.vds_id = v_host_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION RenameManagementNetwork(v_name varchar(50)) RETURNS VOID
   AS $procedure$
DECLARE
    v_old_name  varchar(4000);
BEGIN
    select option_value into v_old_name from vdc_options where option_name = 'DefaultManagementNetwork' and version = 'general';
    perform fn_db_update_config_value('DefaultManagementNetwork', v_name, 'general');
    update network set name = v_name where name = v_old_name;
    update vnic_profiles set name = v_name where name = v_old_name;
END; $procedure$
LANGUAGE plpgsql;

