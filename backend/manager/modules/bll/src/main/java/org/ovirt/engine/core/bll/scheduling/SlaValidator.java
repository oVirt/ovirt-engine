package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlaValidator {
    private static final Logger log = LoggerFactory.getLogger(SlaValidator.class);

    private static final SlaValidator instance = new SlaValidator();

    public static SlaValidator getInstance() {
        return instance;
    }

    /**
     * Check whether a host has enough physical memory to start or receive the VM.
     * We have to take swap into account here as it will increase the theoretical
     * limit QEMU/kernel uses to determine whether the required memory space can
     * be allocated (the actual memory is then allocated only when needed, but the
     * full check is done in advance).
     *
     * The overcommit rules do not apply here as we can reclaim some memory back
     * only after the VM was successfully started.
     *
     * @param curVds The host in question
     * @param vm The currently scheduled VM
     * @return true when there is enough memory, false otherwise
     */
    public boolean hasPhysMemoryToRunVM(VDS curVds, VM vm, int pendingMemory) {
        if (curVds.getMemFree() != null && curVds.getGuestOverhead() != null) {
            double vmMemRequired = vm.getMemSizeMb() + curVds.getGuestOverhead();
            double vdsMemLimit = curVds.getMemFree() - pendingMemory;

            log.debug("hasPhysMemoryToRunVM: host '{}'; free memory is : {} MB (+ {} MB pending); free swap is: {} MB, required memory is {} MB; Guest overhead {} MB",
                    curVds.getName(),
                    vdsMemLimit,
                    pendingMemory,
                    curVds.getSwapFree(),
                    vmMemRequired,
                    curVds.getGuestOverhead());

            if (curVds.getSwapFree() != null) {
                vdsMemLimit += curVds.getSwapFree();
            }

            log.debug("{} <= ???  {}", vmMemRequired, vdsMemLimit);
            return vmMemRequired <= vdsMemLimit;
        } else {
            return false;
        }
    }

    /**
     * Check whether a host has enough memory to host the new VM while
     * taking the engine overcommit limits into account.
     *
     * Swap space and real available memory is not important here, only
     * the theoretical sum of all VM assigned memory against the host
     * memory multiplied by overcommit.
     *
     * @param curVds The host in question
     * @param vm The currently scheduled VM
     * @return true when there is enough memory, false otherwise
     */
    public boolean hasOvercommitMemoryToRunVM(VDS curVds, VM vm) {
        double vmMemRequired = vm.getMemSizeMb() + curVds.getGuestOverhead();
        double vdsMemLimit = curVds.getMaxSchedulingMemory();

        log.debug("hasOvercommitMemoryToRunVM: host '{}'; max scheduling memory : {} MB; required memory is {} MB; Guest overhead {} MB",
                curVds.getName(),
                vdsMemLimit,
                vmMemRequired,
                curVds.getGuestOverhead());

        log.debug("{} <= ???  {}", vmMemRequired, vdsMemLimit);
        return vmMemRequired <= vdsMemLimit;
    }

    public static Integer getEffectiveCpuCores(VDS vds, boolean countThreadsAsCores) {
        if (vds.getCpuThreads() != null
                && countThreadsAsCores) {
            return vds.getCpuThreads();
        } else {
            return vds.getCpuCores();
        }
    }

    /**
     * Find out which cpus are currently online at the provided host.
     * <p>
     * When no cpus are reported to be online or no information is provided (a running host without a CPU does not make
     * sense and is therefore equivalent to no information available), an empty collection is returned.
     *
     * @param host to check for online cpus
     * @return online cpus or empty collection if no information is available
     */
    public static Collection<Integer> getOnlineCpus(final VDS host) {
        final Collection<Integer> cpus = new ArrayList<>();
        if (StringUtils.isEmpty(host.getOnlineCpus())) {
            return cpus;
        }

        for (String cpu : StringUtils.split(host.getOnlineCpus(), ",")) {
            cpu = StringUtils.trim(cpu);
            if (!StringUtils.isEmpty(cpu)) {
                cpus.add(Integer.parseInt(cpu));
            }
        }
        return cpus;
    }
}
