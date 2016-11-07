package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public interface VfScheduler {
    /**
     * Validates whether the VM interfaces have suitable virtual functions on the specified host. If the host has
     * suitable VFs, the mappings belonging to given <code>vmId</code> (obtainable from {@link #getVnicToVfMap}) are
     * updated with mapping between the VM interfaces to the virtual functions.
     * If not, the problematic VM interface's names are returned.
     *
     * @param vmId id of VM
     * @param hostId id of host
     * @param allVmNics All {@link VmNetworkInterface} for given <code>vmId</code>
     *
     * @return the names of the problematic vm interfaces
     */
    public List<String> validatePassthroughVnics(Guid vmId, Guid hostId, List<VmNetworkInterface> allVmNics);

    /**
     * @return the name of a free suitable vf. If there is no one, return null.
     */
    public String findFreeVfForVnic(Guid hostId, Network vnicNetwork, Guid vmId);

    /**
     * @return the mapping between the VM interfaces to the virtual functions. Null is returned if there are no data for
     * given <code>vmId</code> and <code>hostId</code>
     */
    public Map<Guid, String> getVnicToVfMap(Guid vmId, Guid hostId);

    /**
     * Cleans all mappings of the specified vm (obtainable using {@link #getVnicToVfMap}).
     */
    public void cleanVmData(Guid vmId);
}
