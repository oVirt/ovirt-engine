package org.ovirt.engine.core.bll.network;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

@Singleton
public class FindActiveVmsUsingNetwork {

    @Inject
    private VmDao vmDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    private List<VM> findActiveVmsUsingNetworks(Guid vdsId, Collection<String> networks) {
        if (networks.isEmpty()) {
            return Collections.emptyList();
        }

        List<VM> runningVms = vmDao.getAllRunningForVds(vdsId);
        List<VM> vms = new ArrayList<>();
        for (VM vm : runningVms) {
            List<VmNetworkInterface> vmInterfaces = vmNetworkInterfaceDao.getAllForVm(vm.getId());
            for (VmNetworkInterface vmNic : vmInterfaces) {
                boolean vmHasNetworkAttachedToPluggedNic = vmNic.isPlugged()
                        && vmNic.getNetworkName() != null
                        && networks.contains(vmNic.getNetworkName());

                if (vmHasNetworkAttachedToPluggedNic) {
                    vms.add(vm);
                    break;
                }
            }
        }
        return vms;
    }

    /**
     * Finds active VMs which actively uses a network from a given networks list
     *
     * @param vdsId
     *            The host id on which VMs are running
     * @param networkNames
     *            the networks to check if used
     * @return A list of VM names which uses the networks
     */
    public List<String> findNamesOfActiveVmsUsingNetworks(Guid vdsId, Collection<String> networkNames) {
        return findActiveVmsUsingNetworks(vdsId, networkNames).stream().map(VM::getName).collect(toList());
    }

    public List<String> findNamesOfActiveVmsUsingNetworks(Guid vdsId, String ... networkNames) {
        return findNamesOfActiveVmsUsingNetworks(vdsId, Arrays.asList(networkNames));
    }
}
