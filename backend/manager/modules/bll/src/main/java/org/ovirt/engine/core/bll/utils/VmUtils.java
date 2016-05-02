package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.VmCommonUtils;

public class VmUtils {
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
    public static int getTotalRequiredMemoryInMb(VM vm) {
        ArchitectureType architecture = vm.getClusterArch().getFamily();
        boolean onPpc = architecture == ArchitectureType.ppc;
        int vmRam = vm.getVmMemSizeMb();
        int pageTable;
        if (onPpc) {
            int maxRam = vmRam;
            if (FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())) {
                maxRam = VmCommonUtils.maxMemorySizeWithHotplugInMb(vm);
            }
            int powerOf2 = Integer.highestOneBit(maxRam);
            pageTable = (maxRam > powerOf2 ? powerOf2 * 2 : powerOf2) / 64;
        } else {
            pageTable = vmRam / 512;
        }
        int videoRam = VideoDeviceSettings.totalVideoRAMSizeMb(vm);
        int fixedOverhead = onPpc ? 100 : 64;
        int cpuOverhead = 8 * vm.getNumOfCpus(true);
        int iothreadsOverhead = 8 * vm.getNumOfIoThreads();
        return vmRam + pageTable + videoRam + fixedOverhead + cpuOverhead + iothreadsOverhead;
    }
}
