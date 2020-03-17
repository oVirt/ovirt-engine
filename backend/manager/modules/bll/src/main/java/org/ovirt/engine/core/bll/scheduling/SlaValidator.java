package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SlaValidator {
    private static final Logger log = LoggerFactory.getLogger(SlaValidator.class);

    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    /**
     * Check whether a host has enough physical memory to start or receive the VMs.
     * We have to take swap into account here as it will increase the theoretical
     * limit QEMU/kernel uses to determine whether the required memory space can
     * be allocated (the actual memory is then allocated only when needed, but the
     * full check is done in advance).
     *
     * The overcommit rules do not apply here as we can reclaim some memory back
     * only after the VM was successfully started.
     *
     * @param curVds The host in question
     * @param vmGroup The currently scheduled VM group
     * @return true when there is enough memory, false otherwise
     */
    public boolean hasPhysMemoryToRunVmGroup(VDS curVds,
            List<VM> vmGroup,
            int memoryForDynamicHugePages,
            int pendingMemory) {
        if (curVds.getMemFree() != null) {
            long vmMemRequired = vmGroup.stream()
                    .mapToLong(vm -> HugePageUtils.getRequiredMemoryWithoutHugePages(vm.getStaticData())
                            + vmOverheadCalculator.getStaticOverheadInMb(vm))
                    .sum();

            double vdsMemLimit = curVds.getMemFree() - pendingMemory;

            if (log.isDebugEnabled()) {
                long totalStaticOverhead = vmGroup.stream()
                        .mapToLong(vm -> vmOverheadCalculator.getStaticOverheadInMb(vm))
                        .sum();

                log.debug("hasPhysMemoryToRunVM: host '{}'; free memory is : {} MB (+ {} MB pending); free swap is: {} MB; required memory is {} MB; Required dynamic huge-pages memory is {} MB; Guest overhead {} MB",
                        curVds.getName(),
                        vdsMemLimit,
                        pendingMemory,
                        curVds.getSwapFree(),
                        vmMemRequired,
                        memoryForDynamicHugePages,
                        totalStaticOverhead);
            }

            // Test if hugepage memory can fit in the host's physical memory without swap
            vdsMemLimit -= memoryForDynamicHugePages;
            if (vdsMemLimit < 0) {
                return false;
            }

            // Adding swap after hugepages can fit
            long swapFree = curVds.getSwapFree() != null ? curVds.getSwapFree() : 0;
            vdsMemLimit += swapFree;

            // Testing if the rest of VM memory can fit
            return vmMemRequired <= vdsMemLimit;
        } else {
            return false;
        }
    }

    /**
     * Check whether a host has enough memory to host the new VMs while
     * taking the engine overcommit limits into account.
     *
     * Swap space and real available memory is not important here, only
     * the theoretical sum of all VM assigned memory against the host
     * memory multiplied by overcommit.
     *
     * @param curVds The host in question
     * @param vmGroup The currently scheduled VM group
     * @return true when there is enough memory, false otherwise
     */
    public boolean hasOvercommitMemoryToRunVM(VDS curVds,
            List<VM> vmGroup,
            int memoryForDynamicHugePages) {
        long vmMemRequired = vmGroup.stream()
                .mapToLong(vm -> vmOverheadCalculator.getTotalRequiredMemWithoutHugePagesMb(vm))
                .sum();

        double vdsMemLimit = curVds.getMaxSchedulingMemory();

        if (log.isDebugEnabled()) {
            long totalOverhead = vmGroup.stream()
                    .mapToLong(vm -> vmOverheadCalculator.getOverheadInMb(vm))
                    .sum();

            log.debug("hasOvercommitMemoryToRunVM: host '{}'; max scheduling memory : {} MB; required memory is {} MB; Required dynamic huge-pages memory is {} MB; Guest overhead {} MB",
                    curVds.getName(),
                    vdsMemLimit,
                    vmMemRequired,
                    memoryForDynamicHugePages,
                    totalOverhead);
        }
        return vmMemRequired + memoryForDynamicHugePages <= vdsMemLimit;
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

    // Test only
    public void setVmOverheadCalculator(VmOverheadCalculator vmOverheadCalculator) {
        this.vmOverheadCalculator = vmOverheadCalculator;
    }
}
