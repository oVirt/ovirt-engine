package org.ovirt.engine.core.common.utils;

import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Version;

public class CompatibilityVersionUtils {

    public static Version getEffective(Version vmCustomCompatibilityVersion,
            Version clusterCompatibilityVersion, Version defaultVersion) {
        if (vmCustomCompatibilityVersion != null) {
            return vmCustomCompatibilityVersion;
        }
        if (clusterCompatibilityVersion != null) {
            return clusterCompatibilityVersion;
        }
        return defaultVersion;
    }

    public static Version getEffective(Version vmCustomCompatibilityVersion,
            Supplier<Version> clusterCompatibilityVersionSupplier) {
        if (vmCustomCompatibilityVersion != null) {
            return vmCustomCompatibilityVersion;
        }
        if (clusterCompatibilityVersionSupplier != null) {
            Version clusterCompatibilityVersion = clusterCompatibilityVersionSupplier.get();
            if (clusterCompatibilityVersion != null) {
                return clusterCompatibilityVersion;
            }
        }
        return Version.getLast();
    }

    public static Version getEffective(VmBase vmBase, Cluster cluster) {
        Version vmCustomCompatibilityVersion = vmBase != null ? vmBase.getCustomCompatibilityVersion() : null;
        Version clusterCompatibilityVersion = cluster != null ? cluster.getCompatibilityVersion() : null;
        return getEffective(vmCustomCompatibilityVersion, clusterCompatibilityVersion, Version.getLast());
    }

    public static Version getEffective(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        if (vmBase != null && vmBase.getCustomCompatibilityVersion() != null) {
            return vmBase.getCustomCompatibilityVersion();
        } else {
            Cluster cluster = clusterSupplier.get();
            return cluster != null ? cluster.getCompatibilityVersion() : null;
        }
    }

    public static Version getEffective(VM vm, Supplier<Cluster> clusterSupplier) {
        return getEffective(vm != null ? vm.getStaticData() : null, clusterSupplier);
    }

}
