package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.utils.CpuVendor;

public class KernelCmdlineUtil {

    public static String create(
            CpuVendor cpuVendor,
            boolean iommu,
            boolean kvmNested,
            boolean unsafeInterrupts,
            boolean pciRealloc) {
        StringBuilder cmdlineBuilder = new StringBuilder();
        cmdlineBuilder.append(getIommu(cpuVendor, iommu));
        cmdlineBuilder.append(getKvmNested(cpuVendor, kvmNested));
        cmdlineBuilder.append(getUnsafeInterrupts(cpuVendor, unsafeInterrupts));
        cmdlineBuilder.append(getPciRealloc(cpuVendor, pciRealloc));
        return cmdlineBuilder.toString().trim();
    }

    private static String getIommu(CpuVendor cpuVendor, boolean iommu) {
        if (!iommu) {
            return "";
        }
        switch (cpuVendor) {
            case AMD:
                return "amd_iommu=on "; //$NON-NLS-1$
            case INTEL:
                return "intel_iommu=on "; //$NON-NLS-1$
            case IBM:
                return "";
            default:
                throw new RuntimeException("Unknown CpuType: " + cpuVendor); //$NON-NLS-1$
        }
    }

    private static String getKvmNested(CpuVendor cpuVendor, boolean kvmNested) {
        if (!kvmNested) {
            return "";
        }
        switch (cpuVendor) {
            case AMD:
                return "kvm-amd.nested=on "; //$NON-NLS-1$
            case INTEL:
                return "kvm-intel.nested=on "; //$NON-NLS-1$
            case IBM:
                return "";
            default:
                throw new RuntimeException("Unknown CpuType: " + cpuVendor); //$NON-NLS-1$
        }
    }

    private static String getUnsafeInterrupts(CpuVendor cpuVendor, boolean unsafeInterrupts) {
        if (!unsafeInterrupts) {
            return "";
        }
        switch (cpuVendor) {
            case AMD:
            case INTEL:
                return "vfio_iommu_type1.allow_unsafe_interrupts=on "; //$NON-NLS-1$
            case IBM:
                return "";
            default:
                throw new RuntimeException("Unknown CpuType: " + cpuVendor); //$NON-NLS-1$
        }
    }

    private static String getPciRealloc(CpuVendor cpuVendor, boolean pciRealloc) {
        if (!pciRealloc) {
            return "";
        }
        switch (cpuVendor) {
            case AMD:
            case INTEL:
                return "pci=realloc "; //$NON-NLS-1$
            case IBM:
                return "";
            default:
                throw new RuntimeException("Unknown CpuType: " + cpuVendor); //$NON-NLS-1$
        }
    }
}
