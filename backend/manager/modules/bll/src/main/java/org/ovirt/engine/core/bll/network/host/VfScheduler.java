package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public interface VfScheduler {
    /**
     * Validates whether the VM interfaces have suitable virtual functions on the specified host. If the host has
     * suitable VFs, the <code>vmToHostToVnicToVfMap</vmToHostToVnicToVfMap> is updated
     * with mapping between the VM interfaces to the virtual functions.
     * If not, the problematic VM interface's names are returned.
     *
     * @return the names of the problematic vm interfaces
     */
    public List<String> validatePassthroughVnics(Guid vmId, Guid hostId, List<VmNetworkInterface> vnics);

    /**
     * Return the name of a free suitable vf. If there is no one, return null.
     *
     * @return the name of a free suitable vf. If there is no one, return null.
     */
    public String findFreeVfForVnic(Guid hostId, Network vnicNetwork, Guid vmId);

    /**
     * Returns the mapping between the VM interfaces to the virtual functions.
     *
     * @return the mapping between the VM interfaces to the virtual functions.
     */
    public Map<Guid, String> getVnicToVfMap(Guid vmId, Guid hostId);

    /**
     * Cleans the all the <code>vnicToVf</code> maps of the specified vm.
     */
    public void cleanVmData(Guid vmId);
}
