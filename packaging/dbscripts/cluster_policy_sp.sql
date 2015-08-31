-- Cluster Policy SP

-- General Queries

-- All cluster policies
-- 'Policys' typo is intentional, naming convention for DefaultGenericDao is: "GetAllFrom{0}s",
-- where {0} is the business entity (ClusterPolicy).
Create or replace FUNCTION GetAllFromClusterPolicys() RETURNS SETOF cluster_policies STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   cluster_policies;
END; $procedure$
LANGUAGE plpgsql;
-- get cluster policy by id
Create or replace FUNCTION GetClusterPolicyByClusterPolicyId(v_id UUID) RETURNS SETOF cluster_policies STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   cluster_policies
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Clsuter Policy Commands CRUD procs
-- Insert
Create or replace FUNCTION InsertClusterPolicy(
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_is_locked BOOLEAN,
    v_is_default BOOLEAN,
    v_custom_properties text)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO cluster_policies(
        id,
        name,
        description,
        is_locked,
        is_default,
        custom_properties)
    VALUES(
        v_id,
        v_name,
        v_description,
        v_is_locked,
        v_is_default,
        v_custom_properties);
END; $procedure$
LANGUAGE plpgsql;

-- Update
Create or replace FUNCTION UpdateClusterPolicy(
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_is_locked BOOLEAN,
    v_is_default BOOLEAN,
    v_custom_properties text)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE cluster_policies
    SET    name = v_name,
           description = v_description,
           is_locked = v_is_locked,
           is_default = v_is_default,
           custom_properties = v_custom_properties
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Delete
Create or replace FUNCTION DeleteClusterPolicy(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   cluster_policies
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Cluster Policy Units

-- Get all units per cluster policy id
Create or replace FUNCTION GetAllFromClusterPolicyUnits() RETURNS SETOF cluster_policy_units STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   cluster_policy_units;
END; $procedure$
LANGUAGE plpgsql;

-- Get all units per cluster policy id
Create or replace FUNCTION GetClusterPolicyUnitsByClusterPolicyId(v_id UUID) RETURNS SETOF cluster_policy_units STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   cluster_policy_units
    WHERE cluster_policy_id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Delete all cluster policy units by cluster policy id
Create or replace FUNCTION DeleteClusterPolicyUnitsByClusterPolicyId(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   cluster_policy_units
    WHERE  cluster_policy_id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertClusterPolicyUnit(
    v_cluster_policy_id UUID,
    v_policy_unit_id UUID,
    v_filter_sequence int,
    v_factor int)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO cluster_policy_units(
        cluster_policy_id,
        policy_unit_id,
        filter_sequence,
        factor)
    VALUES(
        v_cluster_policy_id,
        v_policy_unit_id,
        v_filter_sequence,
        v_factor);
END; $procedure$
LANGUAGE plpgsql;

