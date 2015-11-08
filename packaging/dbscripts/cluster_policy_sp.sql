

-- Cluster Policy SP
-- General Queries
-- All cluster policies
-- 'Policys' typo is intentional, naming convention for DefaultGenericDao is: "GetAllFrom{0}s",
-- where {0} is the business entity (ClusterPolicy).
CREATE OR REPLACE FUNCTION GetAllFromClusterPolicys ()
RETURNS SETOF cluster_policies STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cluster_policies;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- get cluster policy by id
CREATE OR REPLACE FUNCTION GetClusterPolicyByClusterPolicyId (v_id UUID)
RETURNS SETOF cluster_policies STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cluster_policies
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Clsuter Policy Commands CRUD procs
-- Insert
CREATE OR REPLACE FUNCTION InsertClusterPolicy (
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_is_locked BOOLEAN,
    v_is_default BOOLEAN,
    v_custom_properties TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO cluster_policies (
        id,
        name,
        description,
        is_locked,
        is_default,
        custom_properties
        )
    VALUES (
        v_id,
        v_name,
        v_description,
        v_is_locked,
        v_is_default,
        v_custom_properties
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Update
CREATE OR REPLACE FUNCTION UpdateClusterPolicy (
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_is_locked BOOLEAN,
    v_is_default BOOLEAN,
    v_custom_properties TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE cluster_policies
    SET name = v_name,
        description = v_description,
        is_locked = v_is_locked,
        is_default = v_is_default,
        custom_properties = v_custom_properties
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete
CREATE OR REPLACE FUNCTION DeleteClusterPolicy (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM cluster_policies
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Cluster Policy Units
-- Get all units per cluster policy id
CREATE OR REPLACE FUNCTION GetAllFromClusterPolicyUnits ()
RETURNS SETOF cluster_policy_units STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cluster_policy_units;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Get all units per cluster policy id
CREATE OR REPLACE FUNCTION GetClusterPolicyUnitsByClusterPolicyId (v_id UUID)
RETURNS SETOF cluster_policy_units STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cluster_policy_units
    WHERE cluster_policy_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete all cluster policy units by cluster policy id
CREATE OR REPLACE FUNCTION DeleteClusterPolicyUnitsByClusterPolicyId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM cluster_policy_units
    WHERE cluster_policy_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertClusterPolicyUnit (
    v_cluster_policy_id UUID,
    v_policy_unit_id UUID,
    v_filter_sequence INT,
    v_factor INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO cluster_policy_units (
        cluster_policy_id,
        policy_unit_id,
        filter_sequence,
        factor
        )
    VALUES (
        v_cluster_policy_id,
        v_policy_unit_id,
        v_filter_sequence,
        v_factor
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


