-- Affinity Groups Stored Procedures script file

-- get All Affinity Groups with members by vm id
Create or replace FUNCTION getAllAffinityGroupsByVmId(v_vm_id UUID) RETURNS SETOF affinity_groups_view STABLE
AS $procedure$
BEGIN
   RETURN QUERY
    SELECT affinity_groups_view.*
    FROM affinity_groups_view
    JOIN affinity_group_members ON v_vm_id = affinity_group_members.vm_id
    AND affinity_group_members.affinity_group_id = affinity_groups_view.id;
END; $procedure$
LANGUAGE plpgsql;

-- get All Affinity Groups with members by cluster id
Create or replace FUNCTION getAllAffinityGroupsByClusterId(v_cluster_id UUID) RETURNS SETOF affinity_groups_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM affinity_groups_view
    WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

-- get Affinity Group with members by id
Create or replace FUNCTION GetAffinityGroupByAffinityGroupId(v_id UUID) RETURNS SETOF affinity_groups_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM affinity_groups_view
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- get Affinity Group by name, used for name validation
Create or replace FUNCTION GetAffinityGroupByName(v_name VARCHAR(255)) RETURNS SETOF affinity_groups_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM affinity_groups_view
    WHERE name = v_name;
END; $procedure$
LANGUAGE plpgsql;

-- Insert Affinity Group with members
Create or replace FUNCTION InsertAffinityGroupWithMembers(
    v_id UUID,
    v_name VARCHAR(255),
    v_description VARCHAR(4000),
    v_cluster_id UUID,
    v_positive BOOLEAN,
    v_enforcing BOOLEAN,
    v_vm_ids VARCHAR(4000)
)
RETURNS VOID
AS $procedure$
DECLARE
    iter_id UUID;
BEGIN
    INSERT INTO affinity_groups(
        id,
        name,
        description,
        cluster_id,
        positive,
        enforcing)
    VALUES(
        v_id,
        v_name,
        v_description,
        v_cluster_id,
        v_positive,
        v_enforcing);
    FOR iter_id IN (SELECT * FROM fnsplitteruuid(v_vm_ids))
    LOOP
        INSERT INTO affinity_group_members(
            affinity_group_id,
            vm_id)
        VALUES(
            v_id,
            iter_id);
    END LOOP;
END; $procedure$
LANGUAGE plpgsql;

-- Delete Affinity Group (uses cascade to remove members)
Create or replace FUNCTION DeleteAffinityGroup(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM affinity_groups
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Update Affinity Group (implemeted using Delete and Insert SPs)
Create or replace FUNCTION UpdateAffinityGroupWithMembers(
    v_id UUID,
    v_name VARCHAR(255),
    v_description VARCHAR(4000),
    v_cluster_id UUID,
    v_positive BOOLEAN,
    v_enforcing BOOLEAN,
    v_vm_ids VARCHAR(4000)
)
RETURNS VOID
AS $procedure$
BEGIN
    PERFORM DeleteAffinityGroup(v_id);
    PERFORM InsertAffinityGroupWithMembers(
        v_id,
        v_name,
        v_description,
        v_cluster_id,
        v_positive,
        v_enforcing,
        v_vm_ids);
END; $procedure$
LANGUAGE plpgsql;

-- Remove vm from all Affinity Group
Create or replace FUNCTION RemoveVmFromAffinityGroups(v_vm_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM affinity_group_members
    WHERE vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;

-- Get All positive enforcing Affinity Groups which contain VMs running on given host id
Create or replace FUNCTION getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(v_vds_id UUID) RETURNS SETOF affinity_groups_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT DISTINCT affinity_groups_view.* FROM affinity_groups_view
    INNER JOIN affinity_group_members ON id = affinity_group_members.affinity_group_id
    INNER JOIN vm_dynamic ON affinity_group_members.vm_id = vm_dynamic.vm_guid AND vm_dynamic.run_on_vds = v_vds_id
    WHERE positive AND enforcing;
END; $procedure$
LANGUAGE plpgsql;

