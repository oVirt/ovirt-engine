

----------------------------------------------------------------
-- [numa_node] Table
--

---------------------------------------------------------------
-- [numa_node_assignment_view] View
--


Create or replace FUNCTION GetVmNumaNodeByVdsNumaNodeIdWithPinnedInfo(v_vds_numa_node_id UUID, v_is_pinned BOOLEAN) RETURNS SETOF numa_node STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT numa_node_assignment_view.assigned_vm_numa_node_id as numa_node_id,
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
