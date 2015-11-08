

-- Affinity Groups Stored Procedures script file
-- get All Affinity Groups with members by vm id
CREATE OR REPLACE FUNCTION getAllAffinityGroupsByVmId (v_vm_id UUID)
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT affinity_groups_view.*
    FROM affinity_groups_view
    INNER JOIN affinity_group_members
        ON v_vm_id = affinity_group_members.vm_id
            AND affinity_group_members.affinity_group_id = affinity_groups_view.id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- get All Affinity Groups with members by cluster id
CREATE OR REPLACE FUNCTION getAllAffinityGroupsByClusterId (v_cluster_id UUID)
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM affinity_groups_view
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- get Affinity Group with members by id
CREATE OR REPLACE FUNCTION GetAffinityGroupByAffinityGroupId (v_id UUID)
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM affinity_groups_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- get Affinity Group by name, used for name validation
CREATE OR REPLACE FUNCTION GetAffinityGroupByName (v_name VARCHAR(255))
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM affinity_groups_view
    WHERE name = v_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Insert Affinity Group with members
CREATE OR REPLACE FUNCTION InsertAffinityGroupWithMembers (
    v_id UUID,
    v_name VARCHAR(255),
    v_description VARCHAR(4000),
    v_cluster_id UUID,
    v_positive BOOLEAN,
    v_enforcing BOOLEAN,
    v_vm_ids VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
DECLARE iter_id UUID;

BEGIN
    INSERT INTO affinity_groups (
        id,
        name,
        description,
        cluster_id,
        positive,
        enforcing
        )
    VALUES (
        v_id,
        v_name,
        v_description,
        v_cluster_id,
        v_positive,
        v_enforcing
        );
    FOR
        iter_id IN (
            SELECT *
            FROM fnsplitteruuid(v_vm_ids)
            )
    LOOP
        INSERT INTO affinity_group_members (
            affinity_group_id,
            vm_id
            )
        VALUES (
            v_id,
            iter_id
            );
    END LOOP;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete Affinity Group (uses cascade to remove members)
CREATE OR REPLACE FUNCTION DeleteAffinityGroup (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM affinity_groups
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Update Affinity Group (implemeted using Delete and Insert SPs)
CREATE OR REPLACE FUNCTION UpdateAffinityGroupWithMembers (
    v_id UUID,
    v_name VARCHAR(255),
    v_description VARCHAR(4000),
    v_cluster_id UUID,
    v_positive BOOLEAN,
    v_enforcing BOOLEAN,
    v_vm_ids VARCHAR(4000)
    )
RETURNS VOID AS $PROCEDURE$
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
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Remove vm from all Affinity Group
CREATE OR REPLACE FUNCTION RemoveVmFromAffinityGroups (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM affinity_group_members
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Get All positive enforcing Affinity Groups which contain VMs running on given host id
CREATE OR REPLACE FUNCTION getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId (v_vds_id UUID)
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT affinity_groups_view.*
    FROM affinity_groups_view
    INNER JOIN affinity_group_members
        ON id = affinity_group_members.affinity_group_id
    INNER JOIN vm_dynamic
        ON affinity_group_members.vm_id = vm_dynamic.vm_guid
            AND vm_dynamic.run_on_vds = v_vds_id
    WHERE positive
        AND enforcing;
END;$PROCEDURE$
LANGUAGE plpgsql;


