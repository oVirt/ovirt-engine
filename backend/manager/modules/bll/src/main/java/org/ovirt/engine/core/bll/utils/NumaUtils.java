package org.ovirt.engine.core.bll.utils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;

@Singleton
public class NumaUtils {

    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    /**
     * Get the number of numa nodes, to which the whole VM fits.
     *
     * If a VM without NUMA nodes is run on a host with NUMA,
     * it is good if the VM can fit in a single NUMA node,
     * otherwise it will impact its performance.
     */
    public long countNumaNodesWhereVmFits(VmStatic vm, Guid hostId) {
        List<VdsNumaNode> hostNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(hostId);

        return hostNodes.stream()
                .filter(node -> vm.getMemSizeMb() <= node.getMemTotal())
                .filter(node -> vm.getNumOfCpus() <= node.getCpuIds().size())
                .count();
    }
}
