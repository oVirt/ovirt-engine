

----------------------------------------------------------------
-- [iscsi_bonds] Table
--
CREATE OR REPLACE FUNCTION GetIscsiBondByIscsiBondId (v_id UUID)
RETURNS SETOF iscsi_bonds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds.*
    FROM iscsi_bonds
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromIscsiBonds ()
RETURNS SETOF iscsi_bonds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds.*
    FROM iscsi_bonds;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIscsiBondsByStoragePoolId (v_storage_pool_id UUID)
RETURNS SETOF iscsi_bonds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds.*
    FROM iscsi_bonds
    WHERE storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworksByIscsiBondId (v_iscsi_bond_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds_networks_map.network_id
    FROM iscsi_bonds_networks_map
    WHERE iscsi_bond_id = v_iscsi_bond_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIscsiBondsByNetworkId (v_network_id UUID)
RETURNS SETOF iscsi_bonds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds.*
    FROM iscsi_bonds_networks_map,
        iscsi_bonds
    WHERE iscsi_bonds.id = iscsi_bonds_networks_map.iscsi_bond_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertIscsiBond (
    v_id UUID,
    v_name VARCHAR(50),
    v_description VARCHAR(4000),
    v_storage_pool_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO iscsi_bonds (
        id,
        name,
        description,
        storage_pool_id
        )
    VALUES (
        v_id,
        v_name,
        v_description,
        v_storage_pool_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateIscsiBond (
    v_id UUID,
    v_name VARCHAR(50),
    v_description VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE iscsi_bonds
    SET name = v_name,
        description = v_description
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteIscsiBond (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM iscsi_bonds
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION AddNetworkToIscsiBond (
    v_iscsi_bond_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO iscsi_bonds_networks_map (
        iscsi_bond_id,
        network_id
        )
    VALUES (
        v_iscsi_bond_id,
        v_network_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveNetworkFromIscsiBond (
    v_iscsi_bond_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM iscsi_bonds_networks_map
    WHERE iscsi_bond_id = v_iscsi_bond_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION AddConnectionToIscsiBond (
    v_iscsi_bond_id UUID,
    v_connection_id VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO iscsi_bonds_storage_connections_map (
        iscsi_bond_id,
        connection_id
        )
    VALUES (
        v_iscsi_bond_id,
        v_connection_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveConnectionFromIscsiBond (
    v_iscsi_bond_id UUID,
    v_connection_id VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM iscsi_bonds_storage_connections_map
    WHERE iscsi_bond_id = v_iscsi_bond_id
        AND connection_id = v_connection_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetConnectionsByIscsiBondId (v_iscsi_bond_id UUID)
RETURNS SETOF VARCHAR(50) STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT iscsi_bonds_storage_connections_map.connection_id
    FROM iscsi_bonds_storage_connections_map
    WHERE iscsi_bond_id = v_iscsi_bond_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


