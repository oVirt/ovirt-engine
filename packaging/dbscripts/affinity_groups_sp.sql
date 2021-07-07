

-- Affinity Groups Stored Procedures script file

-- get All Affinity Groups
CREATE OR REPLACE FUNCTION getAllFromAffinityGroups ()
RETURNS SETOF affinity_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT affinity_groups_view.*
    FROM affinity_groups_view;
END;$PROCEDURE$
LANGUAGE plpgsql;


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


-- get All Affinity Groups with members from labels by vm id
CREATE OR REPLACE FUNCTION getAllAffinityGroupsWithFlatLabelsByVmId (v_vm_id UUID)
RETURNS SETOF affinity_groups_with_members_from_labels_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT groups_view.*
    FROM affinity_groups_with_members_from_labels_view as groups_view
    INNER JOIN affinity_groups_members_flat_labels_view as members
        ON v_vm_id = members.vm_id
            AND members.affinity_group_id = groups_view.id;
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


-- get All Affinity Groups with members from labels by cluster id
CREATE OR REPLACE FUNCTION getAllAffinityGroupsWithFlatLabelsByClusterId (v_cluster_id UUID)
RETURNS SETOF affinity_groups_with_members_from_labels_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM affinity_groups_with_members_from_labels_view
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
    v_vm_positive BOOLEAN,
    v_vm_enforcing BOOLEAN,
    v_vds_positive BOOLEAN,
    v_vds_enforcing BOOLEAN,
    v_vms_affinity_enabled BOOLEAN,
    v_vds_affinity_enabled BOOLEAN,
    v_priority BIGINT,
    v_vm_ids UUID[],
    v_vds_ids UUID[],
    v_vm_label_ids UUID[],
    v_host_label_ids UUID[]
    )
RETURNS VOID AS $PROCEDURE$
DECLARE
    o uuid;
BEGIN
    INSERT INTO affinity_groups (
        id,
        name,
        description,
        cluster_id,
        vm_positive,
        vm_enforcing,
        vds_positive,
        vds_enforcing,
        vms_affinity_enabled,
        vds_affinity_enabled,
        priority
        )
    VALUES (
        v_id,
        v_name,
        v_description,
        v_cluster_id,
        v_vm_positive,
        v_vm_enforcing,
        v_vds_positive,
        v_vds_enforcing,
        v_vms_affinity_enabled,
        v_vds_affinity_enabled,
        v_priority
        );

    FOREACH o IN ARRAY v_vm_ids
    LOOP
        PERFORM InsertAffinityVm(v_id, o);
    END LOOP;

    FOREACH o IN ARRAY v_vds_ids
    LOOP
        PERFORM InsertAffinityHost(v_id, o);
    END LOOP;

    FOREACH o IN ARRAY v_vm_label_ids
    LOOP
        PERFORM InsertAffinityVmLabel(v_id, o);
    END LOOP;

    FOREACH o IN ARRAY v_host_label_ids
    LOOP
        PERFORM InsertAffinityHostLabel(v_id, o);
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
    v_vm_positive BOOLEAN,
    v_vm_enforcing BOOLEAN,
    v_vds_positive BOOLEAN,
    v_vds_enforcing BOOLEAN,
    v_vms_affinity_enabled BOOLEAN,
    v_vds_affinity_enabled BOOLEAN,
    v_priority BIGINT,
    v_vm_ids UUID[],
    v_vds_ids UUID[],
    v_vm_label_ids UUID[],
    v_host_label_ids UUID[]
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM DeleteAffinityGroup(v_id);

    PERFORM InsertAffinityGroupWithMembers(
        v_id,
        v_name,
        v_description,
        v_cluster_id,
        v_vm_positive,
        v_vm_enforcing,
        v_vds_positive,
        v_vds_enforcing,
        v_vms_affinity_enabled,
        v_vds_affinity_enabled,
        v_priority,
        v_vm_ids,
        v_vds_ids,
        v_vm_label_ids,
        v_host_label_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Get All positive enforcing Affinity Groups which contain VMs running on given host id
CREATE OR REPLACE FUNCTION getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId (v_vds_id UUID)
RETURNS SETOF affinity_groups_with_members_from_labels_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT affinity_groups_with_members_from_labels_view.*
    FROM affinity_groups_with_members_from_labels_view
    INNER JOIN affinity_group_members
        ON id = affinity_group_members.affinity_group_id
    INNER JOIN vm_dynamic
        ON affinity_group_members.vm_id = vm_dynamic.vm_guid
            AND vm_dynamic.run_on_vds = v_vds_id
    WHERE (vm_enforcing AND vm_positive AND vms_affinity_enabled)
        OR (vds_enforcing AND vds_positive);
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set affinity groups for a VM
CREATE OR REPLACE FUNCTION SetAffinityGroupsForVm (
    v_vm_id UUID,
    v_groups uuid[]
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    -- Remove existing entries for the VM
    DELETE FROM affinity_group_members
    WHERE vm_id IS NOT NULL
        AND vm_id = v_vm_id;

    -- Add the current entries for the VM
    INSERT INTO affinity_group_members (
        affinity_group_id,
        vm_id
    )
    SELECT unnest(v_groups), v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set affinity groups for a host
CREATE OR REPLACE FUNCTION SetAffinityGroupsForHost (
    v_host_id UUID,
    v_groups uuid[]
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    -- Remove existing entries for the host
    DELETE FROM affinity_group_members
    WHERE vds_id IS NOT NULL
        AND vds_id = v_host_id;

    -- Add the current entries for the host
    INSERT INTO affinity_group_members (
        affinity_group_id,
        vds_id
    )
    SELECT unnest(v_groups), v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set a VM for affinity group
CREATE OR REPLACE FUNCTION InsertAffinityVm (
    v_affinity_group_id UUID,
    v_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO affinity_group_members(
        affinity_group_id,
        vm_id
    )
    VALUES (
        v_affinity_group_id,
        v_vm_id
    );
END;$PROCEDURE$
LANGUAGE plpgsql;


-- Delete a VM for affinity group
CREATE OR REPLACE FUNCTION DeleteAffinityVm (
    v_affinity_group_id UUID,
    v_vm_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM affinity_group_members
    WHERE affinity_group_id = v_affinity_group_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set a host for affinity group
CREATE OR REPLACE FUNCTION InsertAffinityHost (
    v_affinity_group_id UUID,
    v_vds_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO affinity_group_members(
        affinity_group_id,
        vds_id
    )
    VALUES (
        v_affinity_group_id,
        v_vds_id
    );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete a host for affinity group
CREATE OR REPLACE FUNCTION DeleteAffinityHost (
    v_affinity_group_id UUID,
    v_vds_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM affinity_group_members
    WHERE affinity_group_id = v_affinity_group_id
        AND vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set a VM label for affinity group
CREATE OR REPLACE FUNCTION InsertAffinityVmLabel (
    v_affinity_group_id UUID,
    v_vm_label_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO affinity_group_members(
        affinity_group_id,
        vm_label_id
    )
    VALUES (
        v_affinity_group_id,
        v_vm_label_id
    );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete a VM label for affinity group
CREATE OR REPLACE FUNCTION DeleteAffinityVmLabel (
    v_affinity_group_id UUID,
    v_vm_label_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM affinity_group_members
    WHERE affinity_group_id = v_affinity_group_id
        AND vm_label_id = v_vm_label_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Set a host label for affinity group
CREATE OR REPLACE FUNCTION InsertAffinityHostLabel (
    v_affinity_group_id UUID,
    v_host_label_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO affinity_group_members(
        affinity_group_id,
        host_label_id
    )
    VALUES (
        v_affinity_group_id,
        v_host_label_id
    );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete a host label for affinity group
CREATE OR REPLACE FUNCTION DeleteAffinityHostLabel (
    v_affinity_group_id UUID,
    v_host_label_id UUID
)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM affinity_group_members
    WHERE affinity_group_id = v_affinity_group_id
        AND host_label_id = v_host_label_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
