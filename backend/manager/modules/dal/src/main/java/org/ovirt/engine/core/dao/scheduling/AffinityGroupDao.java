package org.ovirt.engine.core.dao.scheduling;

import java.util.List;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface AffinityGroupDao extends GenericDao<AffinityGroup, Guid> {
    /**
     * get all Affinity Groups by cluster id
     */
    List<AffinityGroup> getAllAffinityGroupsByClusterId(Guid clusterId);

    /**
     * get all Affinity Groups by vm id
     */
    List<AffinityGroup> getAllAffinityGroupsByVmId(Guid vmId);

    /**
     * get Affinity Group by name
     */
    AffinityGroup getByName(String str);

    /**
     * get all positive enforcing affinity groups containing VMs running on given host
     */
    List<AffinityGroup> getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(Guid vdsId);

    /**
     * Adds the vm to specified affinity groups and removes it from all other affinity groups
     */
    void setAffinityGroupsForVm(Guid vmId, List<Guid> groupIds);

    /**
     * Adds the host to specified affinity groups and removes it from all other affinity groups
     */
    void setAffinityGroupsForHost(Guid hostId, List<Guid> groupIds);
}
