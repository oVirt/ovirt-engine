package org.ovirt.engine.core.common.utils;

import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Version;

public class CompatibilityVersionUtils {

    public static Version getEffective(Version vmCustomCompatibilityVersion,
            Version vdsGroupCompatibilityVersion,
            Version defaultVersion) {
        if (vmCustomCompatibilityVersion != null) {
            return vmCustomCompatibilityVersion;
        }
        if (vdsGroupCompatibilityVersion != null) {
            return vdsGroupCompatibilityVersion;
        }
        return defaultVersion;
    }

    public static Version getEffective(Version vmCustomCompatibilityVersion,
            Supplier<Version> vdsGroupCompatibilityVersionSupplier) {
        if (vmCustomCompatibilityVersion != null) {
            return vmCustomCompatibilityVersion;
        }
        if (vdsGroupCompatibilityVersionSupplier != null) {
            Version vdsGroupCompatibilityVersion = vdsGroupCompatibilityVersionSupplier.get();
            if (vdsGroupCompatibilityVersion != null) {
                return vdsGroupCompatibilityVersion;
            }
        }
        return Version.getLast();
    }

    public static Version getEffective(VmBase vmBase, VDSGroup cluster) {
        Version vmCustomCompatibilityVersion = vmBase != null ? vmBase.getCustomCompatibilityVersion() : null;
        Version clusterCompatibilityVersion = cluster != null ? cluster.getCompatibilityVersion() : null;
        return CompatibilityVersionUtils.getEffective(
                vmCustomCompatibilityVersion,
                clusterCompatibilityVersion,
                Version.getLast());
    }

    public static Version getEffective(VmBase vmBase, Supplier<VDSGroup> vdsGroupSupplier) {
        if (vmBase != null && vmBase.getCustomCompatibilityVersion() != null) {
            return vmBase.getCustomCompatibilityVersion();
        } else {
            VDSGroup vdsGroup = vdsGroupSupplier.get();
            return vdsGroup != null ? vdsGroup.getCompatibilityVersion() : null;
        }
    }

    public static Version getEffective(VM vm, Supplier<VDSGroup> vdsGroupSupplier) {
        return getEffective(vm != null ? vm.getStaticData() : null, vdsGroupSupplier);
    }

}
