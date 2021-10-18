


----------------------------------------------------------------
-- [network] Table
--
CREATE OR REPLACE FUNCTION Insertnetwork (
    v_addr VARCHAR(50),
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(256),
    v_vdsm_name VARCHAR(15),
    v_subnet VARCHAR(20),
    v_gateway VARCHAR(20),
    v_type INT,
    v_vlan_id INT,
    v_stp BOOLEAN,
    v_storage_pool_id UUID,
    v_mtu INT,
    v_vm_network BOOLEAN,
    v_provider_network_provider_id UUID,
    v_provider_network_external_id TEXT,
    v_provider_physical_network_id UUID,
    v_qos_id UUID,
    v_label TEXT,
    v_dns_resolver_configuration_id UUID,
    v_port_isolation BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network (
        addr,
        description,
        free_text_comment,
        id,
        name,
        vdsm_name,
        subnet,
        gateway,
        type,
        vlan_id,
        stp,
        storage_pool_id,
        mtu,
        vm_network,
        provider_network_provider_id,
        provider_network_external_id,
        provider_physical_network_id,
        qos_id,
        label,
        dns_resolver_configuration_id,
        port_isolation
        )
    VALUES (
        v_addr,
        v_description,
        v_free_text_comment,
        v_id,
        v_name,
        v_vdsm_name,
        v_subnet,
        v_gateway,
        v_type,
        v_vlan_id,
        v_stp,
        v_storage_pool_id,
        v_mtu,
        v_vm_network,
        v_provider_network_provider_id,
        v_provider_network_external_id,
        v_provider_physical_network_id,
        v_qos_id,
        v_label,
        v_dns_resolver_configuration_id,
        v_port_isolation
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork (
    v_addr VARCHAR(50),
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(256),
    v_vdsm_name VARCHAR(15),
    v_subnet VARCHAR(20),
    v_gateway VARCHAR(20),
    v_type INT,
    v_vlan_id INT,
    v_stp BOOLEAN,
    v_storage_pool_id UUID,
    v_mtu INT,
    v_vm_network BOOLEAN,
    v_provider_network_provider_id UUID,
    v_provider_network_external_id TEXT,
    v_provider_physical_network_id UUID,
    v_qos_id UUID,
    v_label TEXT,
    v_dns_resolver_configuration_id UUID,
    v_port_isolation BOOLEAN
    )
RETURNS VOID
    --The [network] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE network
    SET addr = v_addr,
        description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        vdsm_name = v_vdsm_name,
        subnet = v_subnet,
        gateway = v_gateway,
        type = v_type,
        vlan_id = v_vlan_id,
        stp = v_stp,
        storage_pool_id = v_storage_pool_id,
        mtu = v_mtu,
        vm_network = v_vm_network,
        provider_network_provider_id = v_provider_network_provider_id,
        provider_network_external_id = v_provider_network_external_id,
        provider_physical_network_id = v_provider_physical_network_id,
        qos_id = v_qos_id,
        label = v_label,
        dns_resolver_configuration_id = v_dns_resolver_configuration_id,
        port_isolation = v_port_isolation
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletenetwork (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM network
    WHERE id = v_id
    FOR UPDATE;

    DELETE
    FROM network
    WHERE id = v_id;

    -- Delete the network's permissions
    PERFORM DeletePermissionsByEntityId(v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork (
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE NOT v_is_filtered
        OR EXISTS (
            SELECT 1
            FROM user_network_permissions_view
            WHERE user_id = v_user_id
                AND entity_id = network.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetnetworkByid (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkByNameAndDataCenter (
    v_name VARCHAR(256),
    v_storage_pool_id UUID
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE network.name = v_name
        AND network.storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkByNameAndCluster (
    v_name VARCHAR(256),
    v_cluster_id UUID
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE network.name = v_name
        AND EXISTS (
            SELECT 1
            FROM network_cluster
            WHERE network.id = network_cluster.network_id
                AND network_cluster.cluster_id = v_cluster_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworksByProviderPhysicalNetworkId (v_network_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE provider_physical_network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkByVdsmNameAndDataCenterId (
    v_vdsm_name      VARCHAR(15),
    v_data_center_id UUID
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE vdsm_name = v_vdsm_name
          AND storage_pool_id = v_data_center_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetManagementNetworkByCluster (v_cluster_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE id = (
            SELECT network_id
            FROM network_cluster
            WHERE network_cluster.cluster_id = v_cluster_id
                AND network_cluster.management
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkByStoragePoolId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE storage_pool_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = network.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS networkViewClusterType CASCADE;
CREATE TYPE networkViewClusterType AS (
        id uuid,
        name VARCHAR(256),
        vdsm_name VARCHAR(15),
        description VARCHAR(4000),
        free_text_comment TEXT,
        type INT,
        addr VARCHAR(50),
        subnet VARCHAR(20),
        gateway VARCHAR(20),
        vlan_id INT,
        stp BOOLEAN,
        storage_pool_id UUID,
        mtu INT,
        vm_network BOOLEAN,
        label TEXT,
        provider_network_provider_id UUID,
        provider_network_external_id TEXT,
        provider_physical_network_id UUID,
        qos_id UUID,
        dns_resolver_configuration_id UUID,
        port_isolation BOOLEAN,
        network_id UUID,
        cluster_id UUID,
        status INT,
        is_display BOOLEAN,
        required BOOLEAN,
        migration BOOLEAN,
        management BOOLEAN,
        is_gluster BOOLEAN,
        default_route BOOLEAN
        );

CREATE OR REPLACE FUNCTION GetAllNetworkByClusterId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF networkViewClusterType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT network.id,
        network.name,
        network.vdsm_name,
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
        network.provider_physical_network_id,
        network.qos_id,
        network.dns_resolver_configuration_id,
        network.port_isolation,
        network_cluster.network_id,
        network_cluster.cluster_id,
        network_cluster.status,
        network_cluster.is_display,
        network_cluster.required,
        network_cluster.migration,
        network_cluster.management,
        network_cluster.is_gluster,
        network_cluster.default_route
    FROM network
    INNER JOIN network_cluster
        ON network.id = network_cluster.network_id
    WHERE network_cluster.cluster_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = network.id
                )
            )
    ORDER BY network.name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworksByQosId (v_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE qos_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworksByNetworkProviderId (v_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE provider_network_provider_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkViewsByNetworkProviderId (v_id UUID)
RETURNS SETOF network_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_view
    WHERE provider_network_provider_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkLabelsByDataCenterId (v_id UUID)
RETURNS SETOF TEXT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT label
    FROM network
    WHERE network.storage_pool_id = v_id
        AND label IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetRequiredNetworksByDataCenterId (v_data_center_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    INNER JOIN network_cluster
        ON network.id = network_cluster.network_id
    WHERE network_cluster.required
        AND network.storage_pool_id = v_data_center_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [network] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----------------------------------------------------------------
-- [vds_interface] Table
--
CREATE OR REPLACE FUNCTION Insertvds_interface (
    v_addr VARCHAR(20),
    v_bond_name VARCHAR(50),
    v_bond_type INT,
    v_gateway VARCHAR(20),
    v_ipv4_default_route BOOLEAN,
    v_id UUID,
    v_is_bond BOOLEAN,
    v_reported_switch_type VARCHAR(6),
    v_bond_opts VARCHAR(4000),
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_network_name VARCHAR(256),
    v_speed INT,
    v_subnet VARCHAR(20),
    v_boot_protocol INT,
    v_type INT,
    v_vds_id UUID,
    v_base_interface VARCHAR(50),
    v_vlan_id INT,
    v_mtu INT,
    v_bridged BOOLEAN,
    v_labels TEXT,
    v_ipv6_boot_protocol INT,
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_ad_partner_mac VARCHAR(50),
    v_ad_aggregator_id INT,
    v_bond_active_slave VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_interface (
        addr,
        bond_name,
        bond_type,
        gateway,
        ipv4_default_route,
        id,
        is_bond,
        reported_switch_type,
        bond_opts,
        mac_addr,
        name,
        network_name,
        speed,
        subnet,
        boot_protocol,
        type,
        VDS_ID,
        base_interface,
        vlan_id,
        mtu,
        bridged,
        labels,
        ipv6_address,
        ipv6_gateway,
        ipv6_prefix,
        ipv6_boot_protocol,
        ad_partner_mac,
        ad_aggregator_id,
        bond_active_slave
        )
    VALUES (
        v_addr,
        v_bond_name,
        v_bond_type,
        v_gateway,
        v_ipv4_default_route,
        v_id,
        v_is_bond,
        v_reported_switch_type,
        v_bond_opts,
        v_mac_addr,
        v_name,
        v_network_name,
        v_speed,
        v_subnet,
        v_boot_protocol,
        v_type,
        v_vds_id,
        v_base_interface,
        v_vlan_id,
        v_mtu,
        v_bridged,
        v_labels,
        v_ipv6_address,
        v_ipv6_gateway,
        v_ipv6_prefix,
        v_ipv6_boot_protocol,
        v_ad_partner_mac,
        v_ad_aggregator_id,
        v_bond_active_slave
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevds_interface (
    v_addr VARCHAR(20),
    v_bond_name VARCHAR(50),
    v_bond_type INT,
    v_gateway VARCHAR(20),
    v_ipv4_default_route BOOLEAN,
    v_id UUID,
    v_is_bond BOOLEAN,
    v_reported_switch_type VARCHAR(6),
    v_bond_opts VARCHAR(4000),
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_network_name VARCHAR(256),
    v_speed INT,
    v_subnet VARCHAR(20),
    v_boot_protocol INT,
    v_type INT,
    v_vds_id UUID,
    v_base_interface VARCHAR(50),
    v_vlan_id INT,
    v_mtu INT,
    v_bridged BOOLEAN,
    v_labels TEXT,
    v_ipv6_address VARCHAR(50),
    v_ipv6_gateway VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_boot_protocol INT,
    v_ad_partner_mac VARCHAR(50),
    v_ad_aggregator_id INT,
    v_bond_active_slave VARCHAR(50)
    )
RETURNS VOID
    --The [vds_interface] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vds_interface
    SET addr = v_addr,
        bond_name = v_bond_name,
        bond_type = v_bond_type,
        gateway = v_gateway,
        ipv4_default_route = v_ipv4_default_route,
        is_bond = v_is_bond,
        reported_switch_type = v_reported_switch_type,
        bond_opts = v_bond_opts,
        mac_addr = v_mac_addr,
        name = v_name,
        network_name = v_network_name,
        speed = v_speed,
        subnet = v_subnet,
        boot_protocol = v_boot_protocol,
        type = v_type,
        VDS_ID = v_vds_id,
        base_interface = v_base_interface,
        vlan_id = v_vlan_id,
        _update_date = LOCALTIMESTAMP,
        mtu = v_mtu,
        bridged = v_bridged,
        labels = v_labels,
        ipv6_address = v_ipv6_address,
        ipv6_gateway = v_ipv6_gateway,
        ipv6_prefix = v_ipv6_prefix,
        ipv6_boot_protocol = v_ipv6_boot_protocol,
        ad_partner_mac = v_ad_partner_mac,
        ad_aggregator_id = v_ad_aggregator_id,
        bond_active_slave = v_bond_active_slave
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevds_interface (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vds_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Clear_network_from_nics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_interface
    SET network_name = NULL
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS vds_interface_view_qos_rs CASCADE;
CREATE TYPE vds_interface_view_qos_rs AS (
    rx_rate NUMERIC(24,4),
    tx_rate NUMERIC(24,4),
    rx_drop NUMERIC(20, 0),
    tx_drop NUMERIC(20, 0),
    rx_total NUMERIC(20, 0),
    tx_total NUMERIC(20, 0),
    rx_offset NUMERIC(20, 0),
    tx_offset NUMERIC(20, 0),
    iface_status INTEGER,
    sample_time DOUBLE PRECISION,
    type INTEGER,
    gateway CHARACTER VARYING(20),
    ipv4_default_route BOOLEAN,
    ipv6_gateway CHARACTER VARYING(50),
    subnet CHARACTER VARYING(20),
    ipv6_prefix INTEGER,
    addr CHARACTER VARYING(20),
    ipv6_address CHARACTER VARYING(50),
    speed INTEGER,
    base_interface CHARACTER VARYING(50),
    vlan_id INTEGER,
    bond_type INTEGER,
    bond_name CHARACTER VARYING(50),
    is_bond BOOLEAN,
    bond_opts CHARACTER VARYING(4000),
    mac_addr CHARACTER VARYING(59),
    network_name CHARACTER VARYING(256),
    name CHARACTER VARYING(50),
    vds_id UUID,
    vds_name CHARACTER VARYING(255),
    id UUID,
    boot_protocol INTEGER,
    ipv6_boot_protocol INTEGER,
    mtu INTEGER,
    bridged BOOLEAN,
    reported_switch_type CHARACTER VARYING(6),
    is_vds INTEGER,
    qos_overridden BOOLEAN,
    labels TEXT,
    cluster_id UUID,
    ad_partner_mac CHARACTER VARYING(59),
    ad_aggregator_id INTEGER,
    bond_active_slave CHARACTER VARYING(50),
    qos_id UUID,
    qos_name CHARACTER VARYING(50),
    qos_type SMALLINT,
    out_average_linkshare INTEGER,
    out_average_upperlimit INTEGER,
    out_average_realtime INTEGER
);

CREATE OR REPLACE FUNCTION GetInterfaceViewWithQosByVdsId (
    v_vds_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds_interface_view_qos_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT
        s1.rx_rate,
        s1.tx_rate,
        s1.rx_drop,
        s1.tx_drop,
        s1.rx_total,
        s1.tx_total,
        s1.rx_offset,
        s1.tx_offset,
        s1.iface_status,
        s1.sample_time,
        s1.type,
        s1.gateway,
        s1.ipv4_default_route,
        s1.ipv6_gateway,
        s1.subnet,
        s1.ipv6_prefix,
        s1.addr,
        s1.ipv6_address,
        s1.speed,
        s1.base_interface,
        s1.vlan_id,
        s1.bond_type,
        s1.bond_name,
        s1.is_bond,
        s1.bond_opts,
        s1.mac_addr,
        s1.network_name,
        s1.name,
        s1.vds_id,
        s1.vds_name,
        s1.id,
        s1.boot_protocol,
        s1.ipv6_boot_protocol,
        s1.mtu,
        s1.bridged,
        s1.reported_switch_type,
        s1.is_vds,
        s1.qos_overridden,
        s1.labels,
        s1.cluster_id,
        s1.ad_partner_mac,
        s1.ad_aggregator_id,
        s1.bond_active_slave,
        s2.id AS qos_id,
        s2.name AS qos_name,
        s2.qos_type,
        s2.out_average_linkshare,
        s2.out_average_upperlimit,
        s2.out_average_realtime
    FROM (
        SELECT *
        FROM vds_interface_view
        WHERE vds_id = v_vds_id
            AND (
                NOT v_is_filtered
                OR EXISTS (
                    SELECT 1
                    FROM user_vds_permissions_view
                    WHERE user_id = v_user_id
                        AND entity_id = v_vds_id
                    )
                )
        ) s1
    LEFT JOIN (
            SELECT *
            FROM qos
            WHERE qos.qos_type = 4
        ) s2
        ON s1.id = s2.id
    ;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS host_networks_by_cluster_rs CASCADE;
CREATE TYPE host_networks_by_cluster_rs AS (
        vds_id UUID,
        network_name VARCHAR
        );

CREATE OR REPLACE FUNCTION GetHostNetworksByCluster (v_cluster_id UUID)
RETURNS SETOF host_networks_by_cluster_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.vds_id,
        vds_interface.network_name
    FROM vds_static
    INNER JOIN vds_interface
        ON vds_interface.vds_id = vds_static.vds_id
            AND vds_static.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getinterface_viewByAddr (
    v_cluster_id UUID,
    v_addr VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds_static
        ON vds_interface_view.vds_id = vds_static.vds_id
    WHERE (vds_interface_view.addr = v_addr
        OR vds_interface_view.ipv6_address = v_addr)
        AND vds_static.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsManagedInterfaceByVdsId (
    v_vds_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    -- Checking if the 2nd bit in the type column is set, meaning that the interface is managed
    WHERE vds_id = v_vds_id
        AND (type & 2) = 2
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vds_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds
        ON vds.vds_id = vds_interface_view.vds_id
    INNER JOIN network_cluster
        ON network_cluster.cluster_id = vds.cluster_id
    INNER JOIN network
        ON network.id = network_cluster.network_id
            AND network.name = vds_interface_view.network_name
    WHERE network.id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfaceById (v_vds_interface_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    WHERE id = v_vds_interface_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfaceByName (
    v_host_id UUID,
    v_name VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    WHERE name = v_name
        AND vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetInterfacesWithQosByClusterId (
    v_cluster_id UUID
    )
RETURNS SETOF vds_interface_view_qos_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT
        s1.rx_rate,
        s1.tx_rate,
        s1.rx_drop,
        s1.tx_drop,
        s1.rx_total,
        s1.tx_total,
        s1.rx_offset,
        s1.tx_offset,
        s1.iface_status,
        s1.sample_time,
        s1.type,
        s1.gateway,
        s1.ipv4_default_route,
        s1.ipv6_gateway,
        s1.subnet,
        s1.ipv6_prefix,
        s1.addr,
        s1.ipv6_address,
        s1.speed,
        s1.base_interface,
        s1.vlan_id,
        s1.bond_type,
        s1.bond_name,
        s1.is_bond,
        s1.bond_opts,
        s1.mac_addr,
        s1.network_name,
        s1.name,
        s1.vds_id,
        s1.vds_name,
        s1.id,
        s1.boot_protocol,
        s1.ipv6_boot_protocol,
        s1.mtu,
        s1.bridged,
        s1.reported_switch_type,
        s1.is_vds,
        s1.qos_overridden,
        s1.labels,
        s1.cluster_id,
        s1.ad_partner_mac,
        s1.ad_aggregator_id,
        s1.bond_active_slave,
        s2.id AS qos_id,
        s2.name AS qos_name,
        s2.qos_type,
        s2.out_average_linkshare,
        s2.out_average_upperlimit,
        s2.out_average_realtime
    FROM (
        SELECT vds_interface_view.*
        FROM vds_interface_view
        INNER JOIN vds_static
            ON vds_interface_view.vds_id = vds_static.vds_id
        WHERE vds_static.cluster_id = v_cluster_id
        ) s1
    LEFT JOIN (
            SELECT *
            FROM qos
            WHERE qos.qos_type = 4
        ) s2
        ON s1.id = s2.id
    ;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetInterfacesByDataCenterId (v_data_center_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds_static
        ON vds_interface_view.vds_id = vds_static.vds_id
    INNER JOIN cluster
        ON vds_static.cluster_id = cluster.cluster_id
    WHERE cluster.storage_pool_id = v_data_center_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface] Table
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVmInterface (
    v_id UUID,
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_speed INT,
    v_vnic_profile_id UUID,
    v_vm_guid UUID,
    v_type INT,
    v_linked BOOLEAN,
    v_synced BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_interface (
        id,
        mac_addr,
        name,
        speed,
        vnic_profile_id,
        vm_guid,
        type,
        linked,
        synced
        )
    VALUES (
        v_id,
        v_mac_addr,
        v_name,
        v_speed,
        v_vnic_profile_id,
        v_vm_guid,
        v_type,
        v_linked,
        v_synced
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmInterface (
    v_id UUID,
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_speed INT,
    v_vnic_profile_id UUID,
    v_vm_guid UUID,
    v_type INT,
    v_linked BOOLEAN,
    v_synced BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_interface
    SET mac_addr = v_mac_addr,
        name = v_name,
        speed = v_speed,
        vnic_profile_id = v_vnic_profile_id,
        vm_guid = v_vm_guid,
        type = v_type,
        _update_date = LOCALTIMESTAMP,
        linked = v_linked,
        synced = v_synced
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmInterface (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vm_interface
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vm_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceByVmInterfaceId (v_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmInterfaces ()
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetActiveVmInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interfaces_plugged_on_vm_not_down_view as vm_interface
    INNER JOIN vnic_profiles
        ON vm_interface.vnic_profile_id = vnic_profiles.id
    WHERE vnic_profiles.network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetActiveVmInterfacesByProfileId (v_profile_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interfaces_plugged_on_vm_not_down_view as vm_interface
    WHERE vm_interface.vnic_profile_id = v_profile_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfacesByVmId (v_vm_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE vm_guid = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceByTemplateId (v_template_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE vm_guid = v_template_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interface
    INNER JOIN vnic_profiles
        ON vm_interface.vnic_profile_id = vnic_profiles.id
    INNER JOIN vm_static
        ON vm_interface.vm_guid = vm_static.vm_guid
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_static.entity_type = 'VM';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTemplateInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interface
    INNER JOIN vm_static
        ON vm_interface.vm_guid = vm_static.vm_guid
    INNER JOIN vnic_profiles
        ON vm_interface.vnic_profile_id = vnic_profiles.id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_static.entity_type = 'TEMPLATE';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacsByDataCenterId (v_data_center_id UUID)
RETURNS SETOF VARCHAR STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT mac_addr
    FROM vm_interface
    WHERE EXISTS (
            SELECT 1
            FROM vm_static
            INNER JOIN cluster
                ON vm_static.cluster_id = cluster.cluster_id
            WHERE cluster.storage_pool_id = v_data_center_id
                AND vm_static.vm_guid = vm_interface.vm_guid
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacsByClusterId (v_cluster_id UUID)
RETURNS SETOF VARCHAR STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT mac_addr
    FROM vm_interface
    WHERE EXISTS (
            SELECT 1
            FROM vm_static
            WHERE vm_static.cluster_id = v_cluster_id
              AND vm_static.vm_guid = vm_interface.vm_guid
              AND vm_interface.mac_addr IS NOT NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- VM Interface View
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetAllFromVmNetworkInterfaceViews ()
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByVmNetworkInterfaceViewId (v_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPluggedVmInterfacesByMac (v_mac_address VARCHAR(20))
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE mac_addr = v_mac_address
        AND is_plugged = true;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE vm_guid = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceToMonitorByVmId (v_vm_id UUID)
RETURNS SETOF vm_interface_monitoring_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_monitoring_view
    WHERE vm_guid = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByTemplateId (
    v_template_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE vm_guid = v_template_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_template_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_template_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceViewsByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_view.*
    FROM vm_interface_view
    INNER JOIN vnic_profiles
        ON vnic_profiles.id = vm_interface_view.vnic_profile_id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_interface_view.vm_entity_type = 'VM';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTemplateInterfaceViewsByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_view.*
    FROM vm_interface_view
    INNER JOIN vnic_profiles
        ON vnic_profiles.id = vm_interface_view.vnic_profile_id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_interface_view.vm_entity_type = 'TEMPLATE';
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface_statistics] Table
--
CREATE OR REPLACE FUNCTION Getvm_interface_statisticsById (v_id UUID)
RETURNS SETOF vm_interface_statistics STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Insertvm_interface_statistics (
    v_id UUID,
    v_rx_drop NUMERIC(20, 0),
    v_rx_rate NUMERIC(24, 4),
    v_rx_total NUMERIC(20, 0),
    v_rx_offset NUMERIC(20, 0),
    v_tx_drop NUMERIC(20, 0),
    v_tx_rate NUMERIC(24, 4),
    v_tx_total NUMERIC(20, 0),
    v_tx_offset NUMERIC(20, 0),
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vm_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_interface_statistics (
        id,
        rx_drop,
        rx_rate,
        rx_total,
        rx_offset,
        tx_drop,
        tx_rate,
        tx_total,
        tx_offset,
        vm_id,
        iface_status,
        sample_time
        )
    VALUES (
        v_id,
        v_rx_drop,
        v_rx_rate,
        v_rx_total,
        v_rx_offset,
        v_tx_drop,
        v_tx_rate,
        v_tx_total,
        v_tx_offset,
        v_vm_id,
        v_iface_status,
        v_sample_time
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevm_interface_statistics (
    v_id UUID,
    v_rx_drop NUMERIC(20, 0),
    v_rx_rate NUMERIC(24, 4),
    v_rx_total NUMERIC(20, 0),
    v_rx_offset NUMERIC(20, 0),
    v_tx_drop NUMERIC(20, 0),
    v_tx_rate NUMERIC(24, 4),
    v_tx_total NUMERIC(20, 0),
    v_tx_offset NUMERIC(20, 0),
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vm_id UUID
    )
RETURNS VOID
    --The [vm_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vm_interface_statistics
    SET rx_drop = v_rx_drop,
        rx_rate = v_rx_rate,
        rx_total = v_rx_total,
        rx_offset = v_rx_offset,
        tx_drop = v_tx_drop,
        tx_rate = v_tx_rate,
        tx_total = v_tx_total,
        tx_offset = v_tx_offset,
        vm_id = v_vm_id,
        iface_status = v_iface_status,
        sample_time = v_sample_time,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevm_interface_statistics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vm_interface_statistics
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vm_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [network_cluster] Table
--
CREATE OR REPLACE FUNCTION GetVmGuestAgentInterfacesByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_filtered BOOLEAN
    )
RETURNS SETOF vm_guest_agent_interfaces STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_guest_agent_interfaces
    WHERE vm_id = v_vm_id
        AND (
            NOT v_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmGuestAgentInterfacesByVmIds (v_vm_ids UUID[])
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_guest_agent_interfaces
    WHERE vm_id = ANY(v_vm_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmGuestAgentInterface (
    v_vm_id UUID,
    v_interface_name VARCHAR(50),
    v_mac_address VARCHAR(59),
    v_ipv4_addresses TEXT,
    v_ipv6_addresses TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_guest_agent_interfaces (
        vm_id,
        interface_name,
        mac_address,
        ipv4_addresses,
        ipv6_addresses
        )
    VALUES (
        v_vm_id,
        v_interface_name,
        v_mac_address,
        v_ipv4_addresses,
        v_ipv6_addresses
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vds_interface_statistics] Table
--
CREATE OR REPLACE FUNCTION Insertvds_interface_statistics (
    v_id UUID,
    v_rx_drop NUMERIC(20, 0),
    v_rx_rate NUMERIC(24, 4),
    v_rx_total NUMERIC(20, 0),
    v_rx_offset NUMERIC(20, 0),
    v_tx_drop NUMERIC(20, 0),
    v_tx_rate NUMERIC(24, 4),
    v_tx_total NUMERIC(20, 0),
    v_tx_offset NUMERIC(20, 0),
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vds_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_interface_statistics (
        id,
        rx_drop,
        rx_rate,
        rx_total,
        rx_offset,
        tx_drop,
        tx_rate,
        tx_total,
        tx_offset,
        vds_id,
        iface_status,
        sample_time
        )
    VALUES (
        v_id,
        v_rx_drop,
        v_rx_rate,
        v_rx_total,
        v_rx_offset,
        v_tx_drop,
        v_tx_rate,
        v_tx_total,
        v_tx_offset,
        v_vds_id,
        v_iface_status,
        v_sample_time
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevds_interface_statistics (
    v_id UUID,
    v_rx_drop NUMERIC(20, 0),
    v_rx_rate NUMERIC(24, 4),
    v_rx_total NUMERIC(20, 0),
    v_rx_offset NUMERIC(20, 0),
    v_tx_drop NUMERIC(20, 0),
    v_tx_rate NUMERIC(24, 4),
    v_tx_total NUMERIC(20, 0),
    v_tx_offset NUMERIC(20, 0),
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vds_id UUID
    )
RETURNS VOID
    --The [vds_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vds_interface_statistics
    SET rx_drop = v_rx_drop,
        rx_rate = v_rx_rate,
        rx_total = v_rx_total,
        rx_offset = v_rx_offset,
        tx_drop = v_tx_drop,
        tx_rate = v_tx_rate,
        tx_total = v_tx_total,
        tx_offset = v_tx_offset,
        vds_id = v_vds_id,
        iface_status = v_iface_status,
        sample_time = v_sample_time,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevds_interface_statistics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vds_interface_statistics
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vds_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [network_cluster] Table
--
CREATE OR REPLACE FUNCTION Insertnetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT,
    v_is_display BOOLEAN,
    v_required BOOLEAN,
    v_migration BOOLEAN,
    v_management BOOLEAN,
    v_is_gluster BOOLEAN,
    v_default_route BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network_cluster (
        cluster_id,
        network_id,
        status,
        is_display,
        required,
        migration,
        management,
        is_gluster,
        default_route
        )
    VALUES (
        v_cluster_id,
        v_network_id,
        v_status,
        v_is_display,
        v_required,
        v_migration,
        v_management,
        v_is_gluster,
        v_default_route
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT,
    v_is_display BOOLEAN,
    v_required BOOLEAN,
    v_migration BOOLEAN,
    v_management BOOLEAN,
    v_is_gluster BOOLEAN,
    v_default_route BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET status = v_status,
        is_display = v_is_display,
        required = v_required,
        migration = v_migration,
        management = v_management,
        is_gluster = v_is_gluster,
        default_route = v_default_route
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork_cluster_status (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET status = v_status
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletenetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_cluster
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_cluster ()
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_clusterByClusterId (v_cluster_id UUID)
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllManagementNetworksByDataCenterId (v_data_center_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    INNER JOIN network_cluster
        ON network.id = network_cluster.network_id
    INNER JOIN cluster
        ON network_cluster.cluster_id = cluster.cluster_id
    WHERE cluster.storage_pool_id = v_data_center_id
        AND network_cluster.management;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_clusterByNetworkId (v_network_id UUID)
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getnetwork_clusterBycluster_idAndBynetwork_id (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetvmStaticByGroupIdAndNetwork (
    v_groupId UUID,
    v_networkName VARCHAR(50)
    )
RETURNS SETOF vm_static_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_static_view.*
    FROM vm_static_view
    INNER JOIN vm_interface_view
        ON vm_static_view.vm_guid = vm_interface_view.vm_guid
            AND network_name = v_networkName
            AND vm_static_view.cluster_id = v_groupId;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_display (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET is_display = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET is_display = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_migration (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET migration = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET migration = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_default_role_network (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET default_route = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET default_route = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_gluster (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET is_gluster = COALESCE(network_id = v_network_id, false)
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_management (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET management = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET management = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  Vnic Profile
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVnicProfileByVnicProfileId (v_id UUID)
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVnicProfile (
    v_id UUID,
    v_name VARCHAR(256),
    v_network_id UUID,
    v_network_qos_id UUID,
    v_port_mirroring BOOLEAN,
    v_passthrough BOOLEAN,
    v_migratable BOOLEAN,
    v_custom_properties TEXT,
    v_description TEXT,
    v_network_filter_id UUID,
    v_failover_vnic_profile_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vnic_profiles (
        id,
        name,
        network_id,
        network_qos_id,
        port_mirroring,
        passthrough,
        migratable,
        custom_properties,
        description,
        network_filter_id,
        failover_vnic_profile_id
        )
    VALUES (
        v_id,
        v_name,
        v_network_id,
        v_network_qos_id,
        v_port_mirroring,
        v_passthrough,
        v_migratable,
        v_custom_properties,
        v_description,
        v_network_filter_id,
        v_failover_vnic_profile_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVnicProfile (
    v_id UUID,
    v_name VARCHAR(256),
    v_network_id UUID,
    v_network_qos_id UUID,
    v_port_mirroring BOOLEAN,
    v_passthrough BOOLEAN,
    v_migratable BOOLEAN,
    v_custom_properties TEXT,
    v_description TEXT,
    v_network_filter_id UUID,
    v_failover_vnic_profile_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vnic_profiles
    SET id = v_id,
        name = v_name,
        network_id = v_network_id,
        network_qos_id = v_network_qos_id,
        port_mirroring = v_port_mirroring,
        passthrough = v_passthrough,
        migratable = v_migratable,
        custom_properties = v_custom_properties,
        description = v_description,
        _update_date = LOCALTIMESTAMP,
        network_filter_id = v_network_filter_id,
        failover_vnic_profile_id = v_failover_vnic_profile_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVnicProfile (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM vnic_profiles
    WHERE id = v_id;

    -- Delete the vnic profiles permissions
    PERFORM DeletePermissionsByEntityId(v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVnicProfiles ()
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfilesByNetworkId (v_network_id UUID)
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfilesByFailoverVnicProfileId (v_failover_vnic_profile_id UUID)
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
RETURN QUERY

SELECT *
FROM vnic_profiles
WHERE failover_vnic_profile_id = v_failover_vnic_profile_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  Vnic Profile View
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVnicProfileViewByVnicProfileViewId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVnicProfileViews (
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE NOT v_is_filtered
        OR EXISTS (
            SELECT 1
            FROM user_vnic_profile_permissions_view
            WHERE user_id = v_user_id
                AND entity_id = vnic_profiles_view.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByNetworkId (
    v_network_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE network_id = v_network_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByDataCenterId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE data_center_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByClusterId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE EXISTS (
        SELECT 1
        FROM network_cluster
        WHERE cluster_id = v_id
        AND network_cluster.network_id = vnic_profiles_view.network_id
    )
    AND (
      NOT v_is_filtered
      OR EXISTS(
          SELECT 1
          FROM user_vnic_profile_permissions_view
          WHERE user_id = v_user_id
                AND entity_id = vnic_profiles_view.id
      )
    );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByNetworkQosId (v_network_qos_id UUID)
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE network_qos_id = v_network_qos_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIscsiIfacesByHostIdAndStorageTargetId (
    v_host_id UUID,
    v_target_id VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view,
        network_cluster,
        network,
        iscsi_bonds_networks_map,
        iscsi_bonds_storage_connections_map
    WHERE iscsi_bonds_storage_connections_map.connection_id = v_target_id
        AND iscsi_bonds_storage_connections_map.iscsi_bond_id = iscsi_bonds_networks_map.iscsi_bond_id
        AND iscsi_bonds_networks_map.network_id = network.id
        AND network.id = network_cluster.network_id
        AND network.name = vds_interface_view.network_name
        AND network_cluster.cluster_id = vds_interface_view.cluster_id
        AND vds_interface_view.vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getActiveMigrationNetworkInterfaceForHost (v_host_id UUID)
RETURNS SETOF active_migration_network_interfaces STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM active_migration_network_interfaces
    WHERE vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  hostNicVfsConfig
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertHostNicVfsConfig (
    v_id UUID,
    v_nic_id UUID,
    v_is_all_networks_allowed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO host_nic_vfs_config (
        id,
        nic_id,
        is_all_networks_allowed
        )
    VALUES (
        v_id,
        v_nic_id,
        v_is_all_networks_allowed
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostNicVfsConfig (
    v_id UUID,
    v_nic_id UUID,
    v_is_all_networks_allowed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_nic_vfs_config
    SET id = v_id,
        nic_id = v_nic_id,
        is_all_networks_allowed = v_is_all_networks_allowed,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteHostNicVfsConfig (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM host_nic_vfs_config
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostNicVfsConfigById (v_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVfsConfigByNicId (v_nic_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config
    WHERE nic_id = v_nic_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromHostNicVfsConfigs ()
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllVfsConfigByHostId (v_host_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT host_nic_vfs_config.*
    FROM host_nic_vfs_config
    INNER JOIN vds_interface
        ON host_nic_vfs_config.nic_id = vds_interface.id
    WHERE vds_interface.vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------------------------------------------------------------
-- DnsResolverConfiguration
-------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetDnsResolverConfigurationByDnsResolverConfigurationId (v_id UUID)
RETURNS SETOF dns_resolver_configuration STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM dns_resolver_configuration
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromDnsResolverConfigurations ()
RETURNS SETOF dns_resolver_configuration STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM dns_resolver_configuration;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertDnsResolverConfiguration (
    v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO dns_resolver_configuration (id)
    VALUES (v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateDnsResolverConfiguration (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE dns_resolver_configuration
    SET id = v_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteDnsResolverConfiguration (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM dns_resolver_configuration
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNameServersByDnsResolverConfigurationId (v_dns_resolver_configuration_id UUID)
RETURNS SETOF name_server STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM name_server
    WHERE dns_resolver_configuration_id = v_dns_resolver_configuration_id
    ORDER BY position ASC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertNameServer (
    v_dns_resolver_configuration_id UUID,
    v_address VARCHAR(45),
    v_position SMALLINT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO
    name_server(
      address,
      position,
      dns_resolver_configuration_id)
    VALUES (
      v_address,
      v_position,
      v_dns_resolver_configuration_id);

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNameServersByDnsResolverConfigurationId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM name_server
    WHERE dns_resolver_configuration_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeleteDnsResolverConfigurationByNetworkAttachmentId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM dns_resolver_configuration
    WHERE id = (
      SELECT
        dns_resolver_configuration_id
      FROM
        network_attachments
      WHERE
        id = v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteDnsResolverConfigurationByNetworkId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM dns_resolver_configuration
    WHERE id = (
      SELECT
        dns_resolver_configuration_id
      FROM
        network
      WHERE
        id = v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteDnsResolverConfigurationByVdsDynamicId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM dns_resolver_configuration
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------------------------------------------------------------
-- Network attachments
-------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetNetworkAttachmentByNetworkAttachmentId (v_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertNetworkAttachment (
    v_id UUID,
    v_network_id UUID,
    v_nic_id UUID,
    v_boot_protocol VARCHAR(20),
    v_address VARCHAR(20),
    v_netmask VARCHAR(20),
    v_gateway VARCHAR(20),
    v_ipv6_boot_protocol VARCHAR(20),
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_custom_properties TEXT,
    v_dns_resolver_configuration_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network_attachments (
        id,
        network_id,
        nic_id,
        boot_protocol,
        address,
        netmask,
        gateway,
        ipv6_boot_protocol,
        ipv6_address,
        ipv6_prefix,
        ipv6_gateway,
        custom_properties,
        dns_resolver_configuration_id
        )
    VALUES (
        v_id,
        v_network_id,
        v_nic_id,
        v_boot_protocol,
        v_address,
        v_netmask,
        v_gateway,
        v_ipv6_boot_protocol,
        v_ipv6_address,
        v_ipv6_prefix,
        v_ipv6_gateway,
        v_custom_properties,
        v_dns_resolver_configuration_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateNetworkAttachment (
    v_id UUID,
    v_network_id UUID,
    v_nic_id UUID,
    v_boot_protocol VARCHAR(20),
    v_address VARCHAR(20),
    v_netmask VARCHAR(20),
    v_gateway VARCHAR(20),
    v_ipv6_boot_protocol VARCHAR(20),
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_custom_properties TEXT,
    v_dns_resolver_configuration_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_attachments
    SET network_id = v_network_id,
        nic_id = v_nic_id,
        boot_protocol = v_boot_protocol,
        address = v_address,
        netmask = v_netmask,
        gateway = v_gateway,
        custom_properties = v_custom_properties,
        ipv6_boot_protocol = v_ipv6_boot_protocol,
        ipv6_address = v_ipv6_address,
        ipv6_prefix = v_ipv6_prefix,
        ipv6_gateway = v_ipv6_gateway,
        _update_date = LOCALTIMESTAMP,
        dns_resolver_configuration_id = v_dns_resolver_configuration_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNetworkAttachment (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_attachments
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromNetworkAttachments ()
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsByNicId (v_nic_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE nic_id = v_nic_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsByNetworkId (v_network_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS network_attachments_qos_rs CASCADE;
CREATE TYPE network_attachments_qos_rs AS (
    id UUID,
    network_id UUID,
    nic_id UUID,
    boot_protocol CHARACTER VARYING(20),
    address CHARACTER VARYING(20),
    netmask CHARACTER VARYING(20),
    gateway CHARACTER VARYING(20),
    custom_properties TEXT,
    _create_date TIMESTAMP WITH TIME ZONE,
    _update_date TIMESTAMP WITH TIME ZONE,
    ipv6_boot_protocol CHARACTER VARYING(20),
    ipv6_address CHARACTER VARYING(50),
    ipv6_prefix INTEGER,
    ipv6_gateway CHARACTER VARYING(50),
    dns_resolver_configuration_id UUID,
    qos_id UUID,
    qos_name CHARACTER VARYING(50),
    qos_type SMALLINT,
    out_average_linkshare INTEGER,
    out_average_upperlimit INTEGER,
    out_average_realtime INTEGER
);

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsWithQosByHostId (v_host_id UUID)
RETURNS SETOF network_attachments_qos_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT
        s1.id,
        s1.network_id,
        s1.nic_id,
        s1.boot_protocol,
        s1.address,
        s1.netmask,
        s1.gateway,
        s1.custom_properties,
        s1._create_date,
        s1._update_date,
        s1.ipv6_boot_protocol,
        s1.ipv6_address,
        s1.ipv6_prefix,
        s1.ipv6_gateway,
        s1.dns_resolver_configuration_id,
        s2.id AS qos_id,
        s2.name AS qos_name,
        s2.qos_type,
        s2.out_average_linkshare,
        s2.out_average_upperlimit,
        s2.out_average_realtime
    FROM (
        SELECT *
        FROM network_attachments na
        WHERE EXISTS (
            SELECT 1
            FROM vds_interface
            WHERE na.nic_id = vds_interface.id
                AND vds_interface.vds_id = v_host_id
            )
        ) s1
    LEFT JOIN (
            SELECT *
            FROM qos
            WHERE qos.qos_type = 4
        ) s2
        ON s1.id = s2.id
    ;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentWithQosByNicIdAndNetworkId (
    v_nic_id UUID,
    v_network_id UUID
    )
RETURNS SETOF network_attachments_qos_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT
        s1.id,
        s1.network_id,
        s1.nic_id,
        s1.boot_protocol,
        s1.address,
        s1.netmask,
        s1.gateway,
        s1.custom_properties,
        s1._create_date,
        s1._update_date,
        s1.ipv6_boot_protocol,
        s1.ipv6_address,
        s1.ipv6_prefix,
        s1.ipv6_gateway,
        s1.dns_resolver_configuration_id,
        s2.id AS qos_id,
        s2.name AS qos_name,
        s2.qos_type,
        s2.out_average_linkshare,
        s2.out_average_upperlimit,
        s2.out_average_realtime
    FROM (
        SELECT na.*
        FROM network_attachments na
        WHERE na.network_id = v_network_id
            AND na.nic_id = v_nic_id
        ) s1
    LEFT JOIN (
            SELECT *
            FROM qos
            WHERE qos.qos_type = 4
        ) s2
        ON s1.id = s2.id
    ;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveNetworkAttachmentByNetworkId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_attachments na
    WHERE na.network_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  vfsConfigNetworks
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVfsConfigNetwork (
    v_vfs_config_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vfs_config_networks (
        vfs_config_id,
        network_id
        )
    VALUES (
        v_vfs_config_id,
        v_network_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVfsConfigNetwork (
    v_vfs_config_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllVfsConfigNetworks (v_vfs_config_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworksByVfsConfigId (v_vfs_config_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network_id
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  vfsConfigLabels
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVfsConfigLabel (
    v_vfs_config_id UUID,
    v_label TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vfs_config_labels (
        vfs_config_id,
        label
        )
    VALUES (
        v_vfs_config_id,
        v_label
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVfsConfigLabel (
    v_vfs_config_id UUID,
    v_label TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id
        AND label = v_label;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllVfsConfigLabels (v_vfs_config_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLabelsByVfsConfigId (v_vfs_config_id UUID)
RETURNS SETOF TEXT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT label
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllNetworkFilters ()
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllSupportedNetworkFiltersByVersion (v_version VARCHAR(40))
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE v_version >= version;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkFilterById (v_filter_id UUID)
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE filter_id = v_filter_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkFilterByName (v_filter_name VARCHAR(50))
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE filter_name like v_filter_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostProviderBinding (
    v_vds_id UUID,
    v_plugin_type character varying(64)
    )
RETURNS SETOF character varying(64) STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT binding_host_id
    FROM provider_binding_host_id
    WHERE vds_id = v_vds_id
        AND plugin_type = v_plugin_type;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateHostProviderBinding (
    v_vds_id UUID,
    v_plugin_types character varying(64)[],
    v_provider_binding_host_ids character varying(64)[]
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM 1 FROM provider_binding_host_id WHERE vds_id = v_vds_id FOR UPDATE;
    DELETE FROM provider_binding_host_id WHERE vds_id = v_vds_id;
    INSERT INTO provider_binding_host_id (
        vds_id,
        plugin_type,
        binding_host_id
        )
    SELECT v_vds_id, unnest(v_plugin_types), unnest(v_provider_binding_host_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmIdsForVnicsOutOfSync (v_ids UUID[])
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT vm_guid
    FROM vm_interface
    WHERE NOT synced
    AND vm_guid = ANY(v_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetVmInterfacesSyncedForVm (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_interface
    SET synced = true
    WHERE vm_interface.vm_guid = v_vm_id;

END;$PROCEDURE$
LANGUAGE plpgsql;