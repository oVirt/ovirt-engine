package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.common.utils.CpuPinningHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class VdsCpuUnitPinningHelper {
    @Inject
    private ResourceManager resourceManager;

    /**
     * This function will tell if the host is capable to run a given dedicated CPU policy VM.
     *
     * The function apply the pending CPU pinning on the host topology, then:
     * 1. If the number of vCPUs is smaller or equal to the number of pCPUs on a single socket:
     *    Pass on each socket and check if there are enough free pCPU to use.
     *    If there are enough pCPUs - return true, otherwise false.
     * 2. If the number of vCPUs is bigger than the pCPU exists on a single socket:
     *    - We must use multiple sockets.
     *    Pass on the host and check if there are enough free pCPU to use (regardless being in multiple sockets).
     *    If there are enough pCPUs - return true, otherwise false.
     *
     * @param vmToPendingPinnings Map of VDS GUID keys to list of VdsCpuUnits pending to be taken.
     * @param vm VM object.
     * @param host VDS object.
     * @return boolean. True if possible to dedicate the VM on the host. Otherwise false.
     */
    public boolean isDedicatedCpuPinningPossibleAtHost(Map<Guid, List<VdsCpuUnit>> vmToPendingPinnings,
            VM vm, VDS host) {
        List<VdsCpuUnit> cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();

        if (vm.getCpuPinningPolicy() != CpuPinningPolicy.DEDICATED) {
            // TODO: Implementation for siblings and isolate threads
            return false;
        }

        previewPinOfPendingExclusiveCpus(cpuTopology, vmToPendingPinnings, vm.getCpuPinningPolicy());

        int vcpus = vm.getNumOfCpus();
        int cpusLeft = vcpus;
        int hostCoresPerSocket = host.getCpuCores() / host.getCpuSockets();
        int hostThreadsPerCore = host.getCpuThreads() / host.getCpuCores();

        for (int socket = 0; socket < host.getCpuSockets(); socket++) {
            if (hostCoresPerSocket * hostThreadsPerCore >= vcpus && getFreeCpusInSocket(cpuTopology, socket).size() < vcpus) {
                // we should fit into one socket
                continue;
            }
            cpusLeft -= getFreeCpusInSocket(cpuTopology, socket).size();
            if (cpusLeft <= 0) {
                return true;
            }
        }
        return cpusLeft == 0;
    }

    private void previewPinOfPendingExclusiveCpus(List<VdsCpuUnit> cpuTopology, Map<Guid, List<VdsCpuUnit>> vmToPendingPinning, CpuPinningPolicy cpuPinningPolicy) {
        for (var vmToPendingPinningEntry : vmToPendingPinning.entrySet()) {
            vmToPendingPinningEntry.getValue().forEach(vdsCpuUnit -> {
                VdsCpuUnit cpuUnit = getCpu(cpuTopology, vdsCpuUnit.getCpu());
                cpuUnit.pinVm(vmToPendingPinningEntry.getKey(), cpuPinningPolicy);
            });
        }
    }

    /**
     * This function will tell if the host is capable to run a given dedicated CPU policy VM.
     *
     * The function apply the pending CPU pinning on the host topology, then:
     * 1. If the number of vCPUs is smaller or equal to the number of pCPUs on a single socket:
     *    Pass on each socket and try to allocate full cores.
     *    If there are more vCPUs to use and there is no fully free core, try to take pCPU from any core.
     *    (Preferring to use cores as a whole).
     *    When there are no more vCPUs to allocate - return the list of VdsCpuUnit we chose.
     * 2. If the number of vCPUs is bigger than the pCPU exists on a single socket:
     *    - We must use multiple sockets.
     *    Pass on each socket and try to allocate full cores (regardless being in multiple sockets).
     *    If there are more vCPUs to use and there is no a fully free core, try to take pCPU from any core.
     *    (Preferring to use cores as a whole).
     *    When there are no more vCPUs to allocate - return the list of VdsCpuUnit we chose.
     *
     * @param vm VM object.
     * @param vmToPendingPinnings Map of VDS GUID keys to list of VdsCpuUnits pending to be taken.
     * @param host VDS object.
     * @return List<{@link VdsCpuUnit}>. The list of VdsCpuUnit we are going to use. If not possible, return an empty List.
     */
    public List<VdsCpuUnit> allocateDedicatedCpus(VM vm, Map<Guid, List<VdsCpuUnit>> vmToPendingPinnings, VDS host) {
        if (vm.getCpuPinningPolicy() == CpuPinningPolicy.NONE) {
            return new ArrayList<>();
        }
        List<VdsCpuUnit> cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();
        if (cpuTopology.isEmpty()) {
            return new ArrayList<>();
        }

        previewPinOfPendingExclusiveCpus(cpuTopology, vmToPendingPinnings, vm.getCpuPinningPolicy());

        List<VdsCpuUnit> cpusToBeAllocated = new ArrayList<>();

        if (vm.getCpuPinningPolicy() != CpuPinningPolicy.DEDICATED) {
            String cpuPinning = vm.getCpuPinningPolicy() == CpuPinningPolicy.MANUAL ? vm.getCpuPinning() : vm.getCurrentCpuPinning();
            Set<Integer> requestedCpus = CpuPinningHelper.getAllPinnedPCpus(cpuPinning);
            for (Integer cpuId : requestedCpus) {
                VdsCpuUnit vdsCpuUnit = getCpu(cpuTopology, cpuId);
                vdsCpuUnit.pinVm(vm.getId(), vm.getCpuPinningPolicy());
                cpusToBeAllocated.add(vdsCpuUnit);
            }
            return cpusToBeAllocated;
        }

        int vcpus = vm.getNumOfCpus();
        int cpusLeft = vcpus;
        int hostCoresPerSocket = host.getCpuCores() / host.getCpuSockets();
        int hostThreadsPerCore = host.getCpuThreads() / host.getCpuCores();

        for (int socket = 0; socket < host.getCpuSockets(); socket++) {
            if (hostCoresPerSocket * hostThreadsPerCore >= vcpus && getFreeCpusInSocket(cpuTopology, socket).size() < vcpus) {
                // we should fit into one socket
                continue;
            }
            cpusLeft = allocateCores(cpuTopology, hostCoresPerSocket, hostThreadsPerCore, socket, cpusLeft,
                    cpusToBeAllocated, true, vm.getId(), vm.getCpuPinningPolicy());
            if (cpusLeft > 0 && getFreeCpusInSocket(cpuTopology, socket).size() >= cpusLeft) {
                // iterate again on the cores, take whatever we can.
                cpusLeft = allocateCores(cpuTopology, hostCoresPerSocket, hostThreadsPerCore, socket, cpusLeft,
                        cpusToBeAllocated, false, vm.getId(), vm.getCpuPinningPolicy());
            }
            if (cpusLeft == 0) {
                return cpusToBeAllocated;
            }
        }
        return cpusLeft == 0 ? cpusToBeAllocated : new ArrayList<>();
    }

    private int allocateCores(List<VdsCpuUnit> cpuTopology, int hostCoresPerSocket, int hostThreadsPerCore, int socket,
                              int cpusLeft, List<VdsCpuUnit> cpusToBeAllocated, boolean wholeCore, Guid vmId,
                              CpuPinningPolicy cpuPinningPolicy) {
        for (int core = 0; core < hostCoresPerSocket; core++) {
            List<VdsCpuUnit> sharedCpus = getFreeCpusInCore(cpuTopology, socket, core);
            int sharedCpusInCore = sharedCpus.size();
            List<Integer> sharedCpuIds = sharedCpus.stream().map(VdsCpuUnit::getCpu).collect(Collectors.toCollection(LinkedList::new));
            Iterator<Integer> sharedCpusIterator = sharedCpuIds.iterator();
            if (cpusLeft == 0) {
                return cpusLeft;
            }
            if (wholeCore) {
                if (cpusLeft >= hostThreadsPerCore && sharedCpusInCore == hostThreadsPerCore) {
                    // take the whole core
                    while (sharedCpusInCore-- > 0) {
                        VdsCpuUnit cpuToTake = getCpu(sharedCpus, socket, core, sharedCpusIterator.next());
                        cpusToBeAllocated.add(cpuToTake);
                        cpuToTake.pinVm(vmId, cpuPinningPolicy);
                        cpusLeft--;
                    }
                }
            } else {
                while (sharedCpusInCore-- > 0 && cpusLeft > 0) {
                    VdsCpuUnit cpuToTake = getCpu(sharedCpus, socket, core, sharedCpusIterator.next());
                    cpusToBeAllocated.add(cpuToTake);
                    cpuToTake.pinVm(vmId, cpuPinningPolicy);
                    cpusLeft--;
                }
            }
        }
        return cpusLeft;
    }

    public int countTakenCores(VDS host) {
        List<VdsCpuUnit> cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();
        if (cpuTopology.isEmpty()) {
            return 0;
        }
        int hostCoresPerSocket = host.getCpuCores() / host.getCpuSockets();
        int numOfTakenCores = 0;
        for (int socket = 0; socket < host.getCpuSockets(); socket++) {
            for (int core = 0; core < hostCoresPerSocket; core++) {
                if (getNonDedicatedCpusInCore(cpuTopology, socket, core).isEmpty()) {
                    numOfTakenCores++;
                }
            }
        }
        return numOfTakenCores;
    }

    private List<VdsCpuUnit> getCoresInSocket(List<VdsCpuUnit> cpuTopology, int socketId) {
        return cpuTopology.stream().filter(vdsCpuUnit -> vdsCpuUnit.getSocket() == socketId).collect(Collectors.toList());
    }

    private List<VdsCpuUnit> getCpusInCore(List<VdsCpuUnit> cpuTopology, int coreId) {
        return cpuTopology.stream().filter(vdsCpuUnit -> vdsCpuUnit.getCore() == coreId).collect(Collectors.toList());
    }

    private List<VdsCpuUnit> getFreeCpusInSocket(List<VdsCpuUnit> cpuTopology, int socketId) {
        return getCoresInSocket(cpuTopology, socketId).stream().filter(cpu -> !cpu.isPinned()).collect(Collectors.toList());
    }

    private List<VdsCpuUnit> getFreeCpusInCore(List<VdsCpuUnit> cpuTopology, int socketId, int coreId) {
        return getCpusInCore(getCoresInSocket(cpuTopology, socketId), coreId).stream().filter(cpu -> !cpu.isPinned()).collect(Collectors.toList());
    }

    private VdsCpuUnit getCpu(List<VdsCpuUnit> cpuTopology, int cpuId) {
        return cpuTopology.stream().filter(vdsCpuUnit -> vdsCpuUnit.getCpu() == cpuId).findFirst().orElse(null);
    }

    private VdsCpuUnit getCpu(List<VdsCpuUnit> cpuTopology, int socketId, int coreId, int cpuId) {
        return getCpu(getCpusInCore(getCoresInSocket(cpuTopology, socketId), coreId), cpuId);
    }

    private List<VdsCpuUnit> getNonDedicatedCpusInCore(List<VdsCpuUnit> cpuTopology, int socketId, int coreId) {
        return getCpusInCore(getCoresInSocket(cpuTopology, socketId), coreId).stream().filter(cpu -> !cpu.isDedicated()).collect(Collectors.toList());
    }

    public int getDedicatedCount(Guid vdsId) {
        return (int) resourceManager.getVdsManager(vdsId).getCpuTopology().stream()
                .filter(VdsCpuUnit::isDedicated).count();
    }
}
