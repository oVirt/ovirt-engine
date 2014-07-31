----------------------------------------------------------------
-- [iscsi_bonds] Table
--

Create or replace FUNCTION GetIscsiBondByIscsiBondId(v_id UUID) RETURNS SETOF iscsi_bonds STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds.*
      FROM iscsi_bonds
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromIscsiBonds() RETURNS SETOF iscsi_bonds STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds.*
      FROM iscsi_bonds;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetIscsiBondsByStoragePoolId(v_storage_pool_id UUID) RETURNS SETOF iscsi_bonds STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds.*
      FROM iscsi_bonds
      WHERE storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNetworksByIscsiBondId(v_iscsi_bond_id UUID) RETURNS SETOF UUID STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds_networks_map.network_id
      FROM iscsi_bonds_networks_map
      WHERE iscsi_bond_id = v_iscsi_bond_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetIscsiBondsByNetworkId(v_network_id UUID) RETURNS SETOF iscsi_bonds  STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds.*
      FROM iscsi_bonds_networks_map, iscsi_bonds
      WHERE iscsi_bonds.id = iscsi_bonds_networks_map.iscsi_bond_id
      AND network_id = v_network_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertIscsiBond(v_id UUID,
  v_name VARCHAR(50),
  v_description VARCHAR(4000),
  v_storage_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO iscsi_bonds(id, name, description, storage_pool_id)
      VALUES(v_id, v_name, v_description, v_storage_pool_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateIscsiBond(v_id UUID,
  v_name VARCHAR(50),
  v_description VARCHAR(4000))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE iscsi_bonds
      SET name = v_name, description = v_description
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteIscsiBond(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val UUID;
BEGIN
      DELETE FROM iscsi_bonds WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION AddNetworkToIscsiBond(v_iscsi_bond_id UUID, v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO iscsi_bonds_networks_map(iscsi_bond_id, network_id)
      VALUES(v_iscsi_bond_id, v_network_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION RemoveNetworkFromIscsiBond(v_iscsi_bond_id UUID, v_network_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM iscsi_bonds_networks_map
      WHERE iscsi_bond_id = v_iscsi_bond_id and network_id = v_network_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION AddConnectionToIscsiBond(v_iscsi_bond_id UUID, v_connection_id VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO iscsi_bonds_storage_connections_map(iscsi_bond_id, connection_id)
      VALUES(v_iscsi_bond_id, v_connection_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION RemoveConnectionFromIscsiBond(v_iscsi_bond_id UUID, v_connection_id VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM iscsi_bonds_storage_connections_map
      WHERE iscsi_bond_id = v_iscsi_bond_id and connection_id = v_connection_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetConnectionsByIscsiBondId(v_iscsi_bond_id UUID) RETURNS SETOF VARCHAR(50) STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT iscsi_bonds_storage_connections_map.connection_id
      FROM iscsi_bonds_storage_connections_map
      WHERE iscsi_bond_id = v_iscsi_bond_id;
END; $procedure$
LANGUAGE plpgsql;

