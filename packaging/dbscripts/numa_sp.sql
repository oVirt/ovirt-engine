

----------------------------------------------------------------
-- [numa_node] Table
--


Create or replace FUNCTION InsertNumaNode(v_numa_node_id UUID,
 v_vds_id UUID,
 v_vm_id UUID,
 v_numa_node_index SMALLINT,
 v_mem_total BIGINT,
 v_cpu_count SMALLINT,
 v_mem_free BIGINT,
 v_usage_mem_percent INTEGER,
 v_cpu_sys DECIMAL(5,2),
 v_cpu_user DECIMAL(5,2),
 v_cpu_idle DECIMAL(5,2),
 v_usage_cpu_percent INTEGER,
 v_distance text)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      INSERT INTO numa_node(numa_node_id, vds_id, vm_id, numa_node_index, mem_total, cpu_count, mem_free, usage_mem_percent, cpu_sys, cpu_user, cpu_idle, usage_cpu_percent, distance)
      VALUES(v_numa_node_id, v_vds_id, v_vm_id, v_numa_node_index, v_mem_total, v_cpu_count, v_mem_free, v_usage_mem_percent, v_cpu_sys, v_cpu_user, v_cpu_idle, v_usage_cpu_percent, v_distance);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateNumaNode(v_numa_node_id UUID,
 v_numa_node_index SMALLINT,
 v_mem_total BIGINT,
 v_cpu_count SMALLINT,
 v_distance text)
RETURNS VOID

   AS $procedure$
BEGIN

   BEGIN
      UPDATE numa_node
      SET numa_node_index = v_numa_node_index,
      mem_total = v_mem_total, cpu_count = v_cpu_count,
      distance = v_distance
      WHERE numa_node_id = v_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateNumaNodeStatistics(v_numa_node_id UUID,
 v_mem_free BIGINT,
 v_usage_mem_percent INTEGER,
 v_cpu_sys DECIMAL(5,2),
 v_cpu_user DECIMAL(5,2),
 v_cpu_idle DECIMAL(5,2),
 v_usage_cpu_percent INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN

   BEGIN
      UPDATE numa_node
      SET mem_free = v_mem_free, usage_mem_percent = v_usage_mem_percent, cpu_sys = v_cpu_sys,
      cpu_user = v_cpu_user, cpu_idle = v_cpu_idle, usage_cpu_percent = v_usage_cpu_percent
      WHERE numa_node_id = v_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteNumaNode(v_numa_node_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM numa_node
      WHERE numa_node_id = v_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromNumaNode() RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node.*
      FROM numa_node;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNumaNodeByNumaNodeId(v_numa_node_id UUID) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node.*
      FROM numa_node
      WHERE numa_node_id = v_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNumaNodeByVdsId(v_vds_id UUID) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node.*
      FROM numa_node
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNumaNodeByVmId(v_vm_id UUID) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node.*
      FROM numa_node
      WHERE vm_id = v_vm_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [numa_node_cpu_map] Table
--


Create or replace FUNCTION InsertNumaNodeCpu(v_id UUID,
 v_numa_node_id UUID,
 v_cpu_core_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      INSERT INTO numa_node_cpu_map(id, numa_node_id, cpu_core_id)
      VALUES(v_id, v_numa_node_id, v_cpu_core_id);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteNumaNodeCpuByNumaNodeId(v_numa_node_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM numa_node_cpu_map
      WHERE numa_node_id = v_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromNumaNodeCpuMap() RETURNS SETOF numa_node_cpu_map STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_cpu_map.*
      FROM numa_node_cpu_map;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNumaNodeCpuByNumaNodeId(v_numa_node_id UUID) RETURNS SETOF integer STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_cpu_map.cpu_core_id
      FROM numa_node_cpu_map
      WHERE numa_node_id = v_numa_node_id
      ORDER BY numa_node_cpu_map.cpu_core_id ASC;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [vm_vds_numa_node_map] Table
--


Create or replace FUNCTION InsertNumaNodeMap(v_id UUID,
 v_vm_numa_node_id UUID,
 v_vds_numa_node_id UUID,
 v_vds_numa_node_index SMALLINT,
 v_is_pinned BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      INSERT INTO vm_vds_numa_node_map(id, vm_numa_node_id, vds_numa_node_id, vds_numa_node_index, is_pinned)
      VALUES(v_id, v_vm_numa_node_id, v_vds_numa_node_id, v_vds_numa_node_index, v_is_pinned);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteNumaNodeMapByVmNumaNodeId(v_vm_numa_node_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vm_vds_numa_node_map
      WHERE vm_numa_node_id = v_vm_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteNumaNodeMapByVdsNumaNodeId(v_vds_numa_node_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vm_vds_numa_node_map
      WHERE vds_numa_node_id = v_vds_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteUnpinnedNumaNodeMapByVmNumaNodeId(v_vm_numa_node_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vm_vds_numa_node_map
      WHERE vm_numa_node_id = v_vm_numa_node_id AND is_pinned = FALSE;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [numa_node_cpus_view] View
--


Create or replace FUNCTION GetNumaNodeCpuByVdsId(v_vds_id UUID) RETURNS SETOF numa_node_cpus_view STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_cpus_view.*
      FROM numa_node_cpus_view
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNumaNodeCpuByVmId(v_vm_id UUID) RETURNS SETOF numa_node_cpus_view STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_cpus_view.*
      FROM numa_node_cpus_view
      WHERE vm_id = v_vm_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [numa_node_assignment_view] View
--


Create or replace FUNCTION GetVmNumaNodeByVdsNumaNodeIdWithPinnedInfo(v_vds_numa_node_id UUID, v_is_pinned BOOLEAN) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_assignment_view.assigned_vm_numa_node_id,
      numa_node_assignment_view.run_in_vds_id,
      numa_node_assignment_view.vm_numa_node_vm_id,
      numa_node_assignment_view.vm_numa_node_index,
      numa_node_assignment_view.vm_numa_node_mem_total,
      numa_node_assignment_view.vm_numa_node_cpu_count,
      numa_node_assignment_view.vm_numa_node_mem_free,
      numa_node_assignment_view.vm_numa_node_usage_mem_percent,
      numa_node_assignment_view.vm_numa_node_cpu_sys,
      numa_node_assignment_view.vm_numa_node_cpu_user,
      numa_node_assignment_view.vm_numa_node_cpu_idle,
      numa_node_assignment_view.vm_numa_node_usage_cpu_percent,
      numa_node_assignment_view.vm_numa_node_distance
      FROM numa_node_assignment_view
      WHERE run_in_vds_numa_node_id = v_vds_numa_node_id AND is_pinned = v_is_pinned;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmNumaNodeByVdsNumaNodeId(v_vds_numa_node_id UUID) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_assignment_view.assigned_vm_numa_node_id,
      numa_node_assignment_view.run_in_vds_id,
      numa_node_assignment_view.vm_numa_node_vm_id,
      numa_node_assignment_view.vm_numa_node_index,
      numa_node_assignment_view.vm_numa_node_mem_total,
      numa_node_assignment_view.vm_numa_node_cpu_count,
      numa_node_assignment_view.vm_numa_node_mem_free,
      numa_node_assignment_view.vm_numa_node_usage_mem_percent,
      numa_node_assignment_view.vm_numa_node_cpu_sys,
      numa_node_assignment_view.vm_numa_node_cpu_user,
      numa_node_assignment_view.vm_numa_node_cpu_idle,
      numa_node_assignment_view.vm_numa_node_usage_cpu_percent,
      numa_node_assignment_view.vm_numa_node_distance
      FROM numa_node_assignment_view
      WHERE run_in_vds_numa_node_id = v_vds_numa_node_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllAssignedNumaNodeInfomation() RETURNS SETOF numa_node_assignment_view STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_assignment_view.*
      FROM numa_node_assignment_view;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetLastRunInPnodeInfoByVmId(v_vm_id UUID) RETURNS SETOF numa_node_assignment_view STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_assignment_view.*
      FROM numa_node_assignment_view
      WHERE vm_numa_node_vm_id = v_vm_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [numa_node_with_vds_group_view] View
--


Create or replace FUNCTION GetVmNumaNodeByVdsGroup(v_vds_group_id UUID) RETURNS SETOF numa_node_with_vds_group_view STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_with_vds_group_view.*
      FROM numa_node_with_vds_group_view
      WHERE vds_group_id = v_vds_group_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;
