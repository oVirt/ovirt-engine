package org.ovirt.engine.core.bll.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;

@Singleton
public class VmOverheadCalculatorImpl implements VmOverheadCalculator {

    @Inject
    private VideoDeviceSettings videoDeviceSettings;

    /**
     * Return total required RAM for running the given VM.
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Please note the return value is just estimation of the memory
     * requirements and the real required RAM may differ in both directions.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getTotalRequiredMemMb(VM vm) {
        return vm.getMemSizeMb() + getOverheadInMb(vm);
    }

    /**
     * Return total required RAM for running the given VM.
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Hugepages are taken into account here, a VM backed with hugepages
     * only consumes the overhead from the free RAM.
     *
     * Please note the return value is just estimation of the memory
     * requirements and the real required RAM may differ in both directions.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getTotalRequiredMemWithoutHugePagesMb(VM vm) {
        int vmRam = HugePageUtils.getRequiredMemoryWithoutHugePages(vm.getStaticData());
        return vmRam + getOverheadInMb(vm);
    }

    /**
     * Return total required RAM for running the given VM.
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Hugepages are taken into account here, a VM backed with hugepages
     * only consumes the overhead from the free RAM.
     *
     * Please note the return value is just estimation of the memory
     * requirements and the real required RAM may differ in both directions.
     *
     * @param vm the relevant VM
     * @param numOfCpus number of CPUs
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getTotalRequiredMemWithoutHugePagesMb(VM vm, int numOfCpus) {
        int vmRam = HugePageUtils.getRequiredMemoryWithoutHugePages(vm.getStaticData());
        return vmRam + getOverheadInMb(vm, numOfCpus);
    }

    /**
     * Get total expected memory overhead
     *
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Please note the return value is just estimation of the memory
     * requirements and the real required RAM may differ in both directions.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getOverheadInMb(VM vm) {
        return getStaticOverheadInMb(vm) + getPossibleOverheadInMb(vm);
    }

    /**
     * Get total expected memory overhead
     *
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Please note the return value is just estimation of the memory
     * requirements and the real required RAM may differ in both directions.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getOverheadInMb(VM vm, int numOfCpus) {
        return getStaticOverheadInMb(vm) + getPossibleOverheadInMb(vm, numOfCpus);
    }

    /**
     * Get the size of possible memory overhead. This represents memory
     * that might be allocated by QEMU, but the memory is then not used
     * immediately.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     */
    @Override
    public int getPossibleOverheadInMb(VM vm) {
        return getPossibleOverheadInMb(vm, VmCpuCountHelper.getDynamicNumOfCpu(vm));
    }

    /**
     * Get the size of possible memory overhead. This represents memory
     * that might be allocated by QEMU, but the memory is then not used
     * immediately.
     *
     * @param vm the relevant VM
     * @param numOfCpus number of CPUs
     * @return required amount of memory in MiB
     */
    @Override
    public int getPossibleOverheadInMb(VM vm, int numOfCpus) {
        int videoRam = videoDeviceSettings.totalVideoRAMSizeMb(vm, VmDeviceCommonUtils.isSingleQxlPci(vm.getStaticData()));
        int cpuOverhead = 8 * numOfCpus;
        int iothreadsOverhead = 8 * vm.getNumOfIoThreads();

        return videoRam + cpuOverhead + iothreadsOverhead;
    }

    /**
     * Get the memory overhead QEMU imposes on the VM immediately.
     * The value contains the needed memory size for page tables and
     * memory hotplug structures + expected fixed overhead (shared libraries
     * and internal QEMU structures).
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    @Override
    public int getStaticOverheadInMb(VM vm) {
        int vmRam = vm.getVmMemSizeMb();
        ArchitectureType architecture = vm.getClusterArch().getFamily();
        boolean onPpc = architecture == ArchitectureType.ppc;
        int fixedOverhead = onPpc ? 100 : 64;
        int pageTable;
        if (onPpc) {
            int maxRam = vmRam;
            if (FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())) {
                maxRam = vm.getMaxMemorySizeMb();
            }
            int powerOf2 = Integer.highestOneBit(maxRam);
            pageTable = (maxRam > powerOf2 ? powerOf2 * 2 : powerOf2) / 64;
        } else {
            pageTable = vmRam / 512;
        }
        return pageTable + fixedOverhead;
    }

    /**
     * Returns the required size for saving all the memory used by this VM.
     * It is useful for determining the size to be allocated in the storage when hibernating
     * VM or taking a snapshot with memory.
     *
     * @param vm The VM to compute the memory size for.
     * @return - Memory size for allocation in bytes.
     */
    @Override
    public long getSnapshotMemorySizeInBytes(VM vm) {
        long videoRam = (long)videoDeviceSettings.totalVideoRAMSizeMb(vm,
                VmDeviceCommonUtils.isSingleQxlPci(vm.getStaticData()));
        return (vm.getVmMemSizeMb() + 200 + videoRam) * 1024 * 1024;
    }
}
