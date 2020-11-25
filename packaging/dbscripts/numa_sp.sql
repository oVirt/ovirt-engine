

----------------------------------------------------------------
-- [numa_node] Table
--
CREATE OR REPLACE FUNCTION InsertNumaNode (
    v_numa_node_id UUID,
    v_vds_id UUID,
    v_vm_id UUID,
    v_numa_node_index SMALLINT,
    v_mem_total BIGINT,
    v_cpu_count SMALLINT,
    v_mem_free BIGINT,
    v_usage_mem_percent INT,
    v_cpu_sys DECIMAL(5, 2),
    v_cpu_user DECIMAL(5, 2),
    v_cpu_idle DECIMAL(5, 2),
    v_usage_cpu_percent INT,
    v_distance TEXT,
    v_hugepages TEXT,
    v_numa_tune_mode VARCHAR(20)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO numa_node (
            numa_node_id,
            vds_id,
            vm_id,
            numa_node_index,
            mem_total,
            cpu_count,
            mem_free,
            usage_mem_percent,
            cpu_sys,
            cpu_user,
            cpu_idle,
            usage_cpu_percent,
            distance,
            hugepages,
            numa_tune_mode
            )
        VALUES (
            v_numa_node_id,
            v_vds_id,
            v_vm_id,
            v_numa_node_index,
            v_mem_total,
            v_cpu_count,
            v_mem_free,
            v_usage_mem_percent,
            v_cpu_sys,
            v_cpu_user,
            v_cpu_idle,
            v_usage_cpu_percent,
            v_distance,
            v_hugepages,
            v_numa_tune_mode
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateNumaNode (
    v_numa_node_id UUID,
    v_numa_node_index SMALLINT,
    v_mem_total BIGINT,
    v_cpu_count SMALLINT,
    v_distance TEXT,
    v_hugepages TEXT,
    v_numa_tune_mode VARCHAR(20)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE numa_node
        SET numa_node_index = v_numa_node_index,
            mem_total = v_mem_total,
            cpu_count = v_cpu_count,
            distance = v_distance,
            hugepages = v_hugepages,
            numa_tune_mode = v_numa_tune_mode
        WHERE numa_node_id = v_numa_node_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateNumaNodeStatistics (
    v_numa_node_id UUID,
    v_mem_free BIGINT,
    v_usage_mem_percent INT,
    v_cpu_sys DECIMAL(5, 2),
    v_cpu_user DECIMAL(5, 2),
    v_cpu_idle DECIMAL(5, 2),
    v_usage_cpu_percent INT,
    v_hugepages TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        UPDATE numa_node
        SET mem_free = v_mem_free,
            usage_mem_percent = v_usage_mem_percent,
            cpu_sys = v_cpu_sys,
            cpu_user = v_cpu_user,
            cpu_idle = v_cpu_idle,
            usage_cpu_percent = v_usage_cpu_percent,
            hugepages = v_hugepages
        WHERE numa_node_id = v_numa_node_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNumaNode (v_numa_node_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM numa_node
        WHERE numa_node_id = v_numa_node_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNumaNodeByVdsId (v_vds_id UUID)
RETURNS SETOF numa_node_cpus_view STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT numa_node_cpus_view.*
        FROM numa_node_cpus_view
        WHERE vds_id = v_vds_id
        ORDER BY numa_node_index ASC;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNumaNodeByVmId (v_vm_id UUID)
RETURNS SETOF numa_node_cpus_view STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT numa_node_cpus_view.*
        FROM numa_node_cpus_view
        WHERE vm_id = v_vm_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [numa_node_cpu_map] Table
--
CREATE OR REPLACE FUNCTION InsertNumaNodeCpu (
    v_id UUID,
    v_numa_node_id UUID,
    v_cpu_core_id INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO numa_node_cpu_map (
            id,
            numa_node_id,
            cpu_core_id
            )
        VALUES (
            v_id,
            v_numa_node_id,
            v_cpu_core_id
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNumaNodeCpuByNumaNodeId (v_numa_node_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM numa_node_cpu_map
        WHERE numa_node_id = v_numa_node_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromNumaNodeCpuMap ()
RETURNS SETOF numa_node_cpu_map STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT numa_node_cpu_map.*
        FROM numa_node_cpu_map;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_vds_numa_node_map] Table
--
CREATE OR REPLACE FUNCTION InsertNumaNodeMap (
    v_id UUID,
    v_vm_numa_node_id UUID,
    v_vds_numa_node_index SMALLINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO vm_vds_numa_node_map (
            id,
            vm_numa_node_id,
            vds_numa_node_index
            )
        VALUES (
            v_id,
            v_vm_numa_node_id,
            v_vds_numa_node_index
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNumaNodeMapByVmNumaNodeId (v_vm_numa_node_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM vm_vds_numa_node_map
        WHERE vm_numa_node_id = v_vm_numa_node_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [numa_node_assignment_view] View
--

CREATE OR REPLACE FUNCTION GetAllAssignedNumaNodeInfomation ()
RETURNS SETOF numa_node_assignment_view STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT numa_node_assignment_view.*
        FROM numa_node_assignment_view;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [numa_node_with_cluster_view] View
--
CREATE OR REPLACE FUNCTION GetVmNumaNodeByCluster (v_cluster_id UUID)
RETURNS SETOF numa_node_with_cluster_view STABLE AS $PROCEDURE$
BEGIN
    BEGIN
        RETURN QUERY

        SELECT numa_node_with_cluster_view.*
        FROM numa_node_with_cluster_view
        WHERE cluster_id = v_cluster_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;


