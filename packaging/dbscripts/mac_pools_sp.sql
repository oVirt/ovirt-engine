

CREATE OR REPLACE FUNCTION InsertMacPool (
    v_id UUID,
    v_name VARCHAR(40),
    v_allow_duplicate_mac_addresses BOOLEAN,
    v_description VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO mac_pools (
        id,
        name,
        allow_duplicate_mac_addresses,
        description
        )
    VALUES (
        v_id,
        v_name,
        v_allow_duplicate_mac_addresses,
        v_description
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateMacPool (
    v_id UUID,
    v_name VARCHAR(40),
    v_allow_duplicate_mac_addresses BOOLEAN,
    v_description VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE mac_pools
    SET id = v_id,
        name = v_name,
        allow_duplicate_mac_addresses = v_allow_duplicate_mac_addresses,
        description = v_description
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteMacPool (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM mac_pools
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacPoolByMacPoolId (v_id UUID)
RETURNS SETOF mac_pools STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM mac_pools
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDefaultMacPool ()
RETURNS SETOF mac_pools STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM mac_pools
    WHERE default_pool IS true;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacPoolByClusterId (v_cluster_id UUID)
RETURNS SETOF mac_pools STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT mp.*
    FROM mac_pools mp
    INNER JOIN cluster c
        ON c.mac_pool_id = mp.id
    WHERE c.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromMacPools ()
RETURNS SETOF mac_pools STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM mac_pools;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllMacsByMacPoolId (v_id UUID)
RETURNS SETOF VARCHAR STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT mac_addr
    FROM vm_interface
    WHERE EXISTS (
            SELECT 1
            FROM vm_static
            INNER JOIN cluster c
                ON vm_static.cluster_id = c.cluster_id
            WHERE c.mac_pool_id = v_id
                AND vm_static.vm_guid = vm_interface.vm_guid
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Procedures for MAC ranges
CREATE OR REPLACE FUNCTION InsertMacPoolRange (
    v_mac_pool_id UUID,
    v_from_mac VARCHAR(17),
    v_to_mac VARCHAR(17)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO mac_pool_ranges (
        mac_pool_id,
        from_mac,
        to_mac
        )
    VALUES (
        v_mac_pool_id,
        v_from_mac,
        v_to_mac
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteMacPoolRangesByMacPoolId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM mac_pool_ranges
    WHERE mac_pool_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllMacPoolRangesByMacPoolId (v_id UUID)
RETURNS SETOF mac_pool_ranges STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM mac_pool_ranges
    WHERE mac_pool_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


