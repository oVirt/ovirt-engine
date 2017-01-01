package org.ovirt.engine.core.vdsbroker;

import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;

/**
 * Sort VMs that are running on a given host by their priority,
 * from the VM with the highest priority to the one with the lowest priority.
 */
public class VmsOnHostComparator implements Comparator<Guid> {

    private Guid hostId;
    private Map<Guid, Integer> vmIdToVmPriority;

    @Inject
    private VmStaticDao vmStaticDao;

    public VmsOnHostComparator(Guid hostId) {
        this.hostId = hostId;
    }

    @PostConstruct
    private void init() {
        vmIdToVmPriority = vmStaticDao.getAllRunningForVds(hostId)
                .stream()
                .collect(toMap(VmStatic::getId, VmStatic::getPriority));
    }

    @Override
    public int compare(Guid vm1, Guid vm2) {
        return vmIdToVmPriority.get(vm2) - vmIdToVmPriority.get(vm1);
    }
}
