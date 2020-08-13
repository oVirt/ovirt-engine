package org.ovirt.engine.core.common.utils;

import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class BiosTypeUtils {

    public static BiosType getEffective(BiosType vmBiosType, BiosType clusterBiosType) {
        return vmBiosType != BiosType.CLUSTER_DEFAULT ? vmBiosType : clusterBiosType;
    }

    public static BiosType getEffective(VmBase vmBase, Cluster cluster) {
        if (vmBase.getCustomBiosType() != BiosType.CLUSTER_DEFAULT) {
            return vmBase.getCustomBiosType();
        }
        return cluster != null && cluster.getBiosType() != BiosType.CLUSTER_DEFAULT
                ? cluster.getBiosType() : BiosType.I440FX_SEA_BIOS;
    }

    public static BiosType getEffective(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        if (vmBase.getCustomBiosType() != BiosType.CLUSTER_DEFAULT) {
            return vmBase.getCustomBiosType();
        }
        Cluster cluster = clusterSupplier != null ? clusterSupplier.get() : null;
        return cluster != null && cluster.getBiosType() != BiosType.CLUSTER_DEFAULT
                ? cluster.getBiosType() : BiosType.I440FX_SEA_BIOS;
    }

}
