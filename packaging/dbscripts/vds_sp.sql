
----------------------------------------------------------------
-- [vds_static] Table
--
Create or replace FUNCTION GetVdsStaticByIp(v_ip VARCHAR(40)) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_static.*
      FROM vds_static vds_static, fence_agents fence_agents
      WHERE fence_agents.ip = v_ip AND fence_agents.vds_id = vds_static.vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsByUniqueID(v_vds_unique_id VARCHAR(128)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_unique_id = v_vds_unique_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;

---------------------------------------------------------------------------------------------------
--    [vds] - view
---------------------------------------------------------------------------------------------------



CREATE OR REPLACE FUNCTION GetUpAndPrioritizedVds(v_storage_pool_id UUID) RETURNS SETOF vds STABLE
AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds.*
      FROM vds vds, vds_groups vdsgroup
      WHERE (vds.status = 3) AND (vds.storage_pool_id = v_storage_pool_id) AND (vds_spm_priority IS NULL OR vds_spm_priority > -1)
      AND vds.vds_group_id = vdsgroup.vds_group_id AND vdsgroup.virt_service = true
      ORDER BY vds_spm_priority DESC, RANDOM();
   END;
   RETURN;
END; $procedure$
  LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromVds(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                          FROM user_vds_permissions_view
                                          WHERE user_id = v_user_id AND entity_id = vds_id));
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVdsByVdsId(v_vds_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
DECLARE
v_columns text[];
BEGIN
    BEGIN
      if (v_is_filtered) then
          RETURN QUERY SELECT DISTINCT (rec).*
          FROM fn_db_mask_object('vds') as q (rec vds)
          WHERE (rec).vds_id = v_vds_id
          AND EXISTS (SELECT 1
              FROM   user_vds_permissions_view
              WHERE  user_id = v_user_id AND entity_id = v_vds_id);
      else
          RETURN QUERY SELECT DISTINCT vds.*
          FROM vds
          WHERE vds_id = v_vds_id;
      end if;
    END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVdsWithoutMigratingVmsByVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN


	-- this sp returns all vds in given cluster that have no pending vms and no vms in migration states
	BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_group_id = v_vds_group_id and
      pending_vcpus_count = 0
      and	vds.status = 3
      and	vds_id not in(select distinct RUN_ON_VDS from vm_dynamic
         where status in(5,6,11,12));
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteVds(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      UPDATE vm_static
      SET dedicated_vm_for_vds = null
      WHERE dedicated_vm_for_vds = v_vds_id;
      DELETE FROM tags_vds_map
      WHERE vds_id = v_vds_id;
   -- Delete all Vds Alerts from the database
      PERFORM DeleteAuditLogAlertsByVdsID(v_vds_id);
      DELETE FROM vds_statistics WHERE vds_id = v_vds_id;
      DELETE FROM vds_dynamic WHERE vds_id = v_vds_id;
      DELETE FROM vds_static WHERE vds_id = v_vds_id;
      DELETE FROM permissions where object_id = v_vds_id;
   END;
   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByType(v_vds_type INTEGER) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_type = v_vds_type;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByName(v_vds_name VARCHAR(255)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_name = v_vds_name;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByHostName(v_host_name VARCHAR(255)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE host_name = v_host_name;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByVdsGroupId(v_vds_group_id UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
	-- this sp returns all vds for a given cluster
   BEGIN
      if (v_is_filtered) then
          RETURN QUERY SELECT DISTINCT (rec).*
          FROM fn_db_mask_object('vds') as q (rec vds)
          WHERE (rec).vds_group_id = v_vds_group_id
          AND EXISTS (SELECT 1
              FROM   user_vds_permissions_view
              WHERE  user_id = v_user_id AND entity_id = (rec).vds_id);
      else
          RETURN QUERY SELECT DISTINCT vds.*
          FROM vds
          WHERE vds_group_id = v_vds_group_id;
      end if;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsByStoragePoolId(v_storage_pool_id UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM   vds
      WHERE  storage_pool_id = v_storage_pool_id
      AND    (NOT v_is_filtered OR EXISTS (SELECT 1
                                           FROM   user_vds_permissions_view
                                           WHERE  user_id = v_user_id AND entity_id = vds_id));
   END;
   RETURN;
END; $procedure$
LANGUAGE plpgsql;


-- Returns all VDS for a given cluster and having given status
CREATE OR REPLACE FUNCTION getVdsForVdsGroupWithStatus(v_vds_group_id UUID, v_status integer) RETURNS SETOF vds STABLE
    AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        WHERE (status = v_status) AND (vds_group_id = v_vds_group_id)
        ORDER BY vds.vds_id ASC;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

-- Returns all VDS for a given pool and having given status
CREATE OR REPLACE FUNCTION getVdsByStoragePoolIdWithStatus(v_storage_pool_id UUID, v_status integer) RETURNS SETOF vds STABLE
    AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        INNER JOIN vds_groups vdsgroup ON vds.vds_group_id = vdsgroup.vds_group_id
        WHERE (v_status IS NULL OR vds.status = v_status) AND (vds.storage_pool_id = v_storage_pool_id)
        AND vdsgroup.virt_service = true;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getHostsForStorageOperation(v_storage_pool_id UUID, v_local_fs_only BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        LEFT JOIN vds_groups vg ON vds.vds_group_id = vg.vds_group_id
        LEFT JOIN storage_pool sp ON vds.storage_pool_id = sp.id
        WHERE (v_storage_pool_id IS NULL OR vds.storage_pool_id = v_storage_pool_id)
        AND (vg.virt_service = true)
        AND (NOT v_local_fs_only OR sp.is_local = true)
        AND (v_storage_pool_id IS NOT NULL OR vds.status = 3); -- if DC is unspecified return only hosts with status = UP
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVdsByNetworkId(v_network_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vds
   WHERE EXISTS (
      SELECT 1
      FROM vds_interface
      INNER JOIN network
      ON network.name = vds_interface.network_name
      INNER JOIN network_cluster
      ON network.id = network_cluster.network_id
      WHERE network_id = v_network_id
      AND vds.vds_group_id = network_cluster.cluster_id
      AND vds_interface.vds_id = vds.vds_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsWithoutNetwork(v_network_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds.*
   FROM vds
   INNER JOIN network_cluster
   ON vds.vds_group_id = network_cluster.cluster_id
   WHERE network_cluster.network_id = v_network_id
   AND NOT EXISTS (
      SELECT 1
      FROM vds_interface
      INNER JOIN network
      ON network.name = vds_interface.network_name
      INNER JOIN network_cluster
      ON network.id = network_cluster.network_id
      WHERE network_cluster.network_id = v_network_id
      AND vds.vds_group_id = network_cluster.cluster_id
      AND vds_interface.vds_id = vds.vds_id);
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [vds_cpu_statistics] Table
--


Create or replace FUNCTION InsertVdsCpuStatistics(v_vds_cpu_id UUID,
 v_vds_id UUID,
 v_cpu_core_id INTEGER,
 v_cpu_sys DECIMAL(18,0),
 v_cpu_user DECIMAL(18,0),
 v_cpu_idle DECIMAL(18,0),
 v_usage_cpu_percent INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      INSERT INTO vds_cpu_statistics(vds_cpu_id, vds_id, cpu_core_id, cpu_sys, cpu_user, cpu_idle, usage_cpu_percent)
      VALUES(v_vds_cpu_id, v_vds_id, v_cpu_core_id, v_cpu_sys, v_cpu_user, v_cpu_idle, v_usage_cpu_percent);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVdsCpuStatistics(v_vds_id UUID,
 v_cpu_core_id INTEGER,
 v_cpu_sys DECIMAL(18,0),
 v_cpu_user DECIMAL(18,0),
 v_cpu_idle DECIMAL(18,0),
 v_usage_cpu_percent INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      UPDATE vds_cpu_statistics
      SET cpu_sys = v_cpu_sys, cpu_user = v_cpu_user, cpu_idle = v_cpu_idle,
      usage_cpu_percent = v_usage_cpu_percent
      WHERE vds_id = v_vds_id and cpu_core_id = v_cpu_core_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVdsCpuStatisticsByVdsId(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vds_cpu_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsCpuStatisticsByVdsId(v_vds_id UUID) RETURNS SETOF vds_cpu_statistics STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT vds_cpu_statistics.*
      FROM vds_cpu_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromVdsCpuStatistics() RETURNS SETOF vds_cpu_statistics STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT vds_cpu_statistics.*
      FROM vds_cpu_statistics;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION getFirstUpRhelForVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   BEGIN
   -- both centos and RHEL return RHEL as host_os
      RETURN QUERY select * from vds where (host_os like 'RHEL%' or host_os like 'oVirt Node%' or host_os like 'RHEV Hypervisor%') and status = 3 and vds_group_id = v_vds_group_id LIMIT 1;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;
