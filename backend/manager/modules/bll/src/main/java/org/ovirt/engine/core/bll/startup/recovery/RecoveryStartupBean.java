package org.ovirt.engine.core.bll.startup.recovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Startup
@Singleton
@DependsOn("InitBackendServicesOnStartupBean")
final class RecoveryStartupBean {

    private final ResourceManager resourceManager;
    private final VdsDao hostDao;
    private final VdsDynamicDao hostDynamicDao;
    private final VmDao vmDao;

    @Inject
    RecoveryStartupBean(
            ResourceManager resourceManager,
            VdsDao hostDao,
            VdsDynamicDao hostDynamicDao,
            VmDao vmDao) {

        Objects.requireNonNull(resourceManager, "resourceManager cannot be null");
        Objects.requireNonNull(hostDao, "hostDao cannot be null");
        Objects.requireNonNull(hostDynamicDao, "hostDynamicDao cannot be null");
        Objects.requireNonNull(vmDao, "vmDao cannot be null");

        this.resourceManager = resourceManager;
        this.hostDynamicDao = hostDynamicDao;
        this.hostDao = hostDao;
        this.vmDao = vmDao;
    }

    @PostConstruct
    void recovery() {
        final List<VDS> allHosts = hostDao.getAll();
        final Set<Guid> nonResponsiveHosts = findNonResponsiveHosts(allHosts);

        final boolean vmsInTransition = setVmsInTransitionAsUnknown(nonResponsiveHosts);

        if (!vmsInTransition) {
            updateHostsResources(allHosts);
        }
    }

    /**
     * Clean pending memory and CPUs
     * (meaning we tried to start a VM and the engine crashed before telling VDSM about it).
     */
    private void updateHostsResources(List<VDS> hosts) {
        final List<VdsDynamic> updatedEntities = new ArrayList<>();
        for (VDS host : hosts) {
            boolean hostDynamicDataTobeSaved = false;

            if (host.getPendingVcpusCount() != 0) {
                host.setPendingVcpusCount(0);
                hostDynamicDataTobeSaved = true;
            }

            if (host.getPendingVmemSize() != 0) {
                host.setPendingVmemSize(0);
                hostDynamicDataTobeSaved = true;
            }

            if (hostDynamicDataTobeSaved) {
                updatedEntities.add(host.getDynamicData());
            }
        }

        hostDynamicDao.updateAllInBatch(updatedEntities);
    }

    /**
     * Sets "unknown" status on VMs that are in transition state on the given hosts.
     * Cleanup all vms dynamic data. This is defensive code on power crash.
     *
     * @param hostIds host ids to be inspected
     * @return <code>true</code> if VMs in transition state were found
     */
    private boolean setVmsInTransitionAsUnknown(Set<Guid> hostIds) {
        // Is there any VM that is not fully Up or fully Down?
        boolean runningVmsInTransition = false;

        final List<VM> vms = vmDao.getAll();
        for (VM vm : vms) {
            if (!vm.isNotRunning()) {
                if (vm.getRunOnVds() != null && hostIds.contains(vm.getRunOnVds())) {
                    resourceManager.setVmUnknown(vm);
                }
            }

            if (isVmInTransition(vm)) {
                runningVmsInTransition = true;
            }
        }

        return runningVmsInTransition;
    }

    private boolean isVmInTransition(VM vm) {
        return vm.isRunning() && vm.getStatus() != VMStatus.Up;
    }

    private Set<Guid> findNonResponsiveHosts(List<VDS> hosts) {
        final Set<Guid> nonResponsiveHosts = new HashSet<>();
        for (VDS host : hosts) {
            if (host.getStatus() == VDSStatus.NonResponsive) {
                nonResponsiveHosts.add(host.getId());
            }

            // Check if engine was restarted in the middle of a fencing flow
            // this might happen when engine runs in a VM hosted by the host (a.k.a Hosted Engine)
            // In this case we are marking again the host as non-responsive in order to complete
            // the fencing flow

            if (host.isInFenceFlow()
                    && (host.getStatus() == VDSStatus.Down
                    || host.getStatus() == VDSStatus.Reboot)) {
                host.setStatus(VDSStatus.NonResponsive);
                nonResponsiveHosts.add(host.getId());
            }
        }
        return nonResponsiveHosts;
    }
}
