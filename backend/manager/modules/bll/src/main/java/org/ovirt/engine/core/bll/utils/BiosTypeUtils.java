package org.ovirt.engine.core.bll.utils;

import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class BiosTypeUtils {

    public static BiosType getEffective(BiosType vmBiosType, BiosType clusterBiosType) {
        return vmBiosType != BiosType.CLUSTER_DEFAULT ? vmBiosType : clusterBiosType;
    }

    public static BiosType getEffective(VmBase vmBase, Cluster cluster) {
        if (vmBase.getBiosType() != BiosType.CLUSTER_DEFAULT) {
            return vmBase.getBiosType();
        }
        return cluster != null ? cluster.getBiosType() : BiosType.I440FX_SEA_BIOS;
    }

    public static BiosType getEffective(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        if (vmBase.getBiosType() != BiosType.CLUSTER_DEFAULT) {
            return vmBase.getBiosType();
        }
        Cluster cluster = clusterSupplier != null ? clusterSupplier.get() : null;
        return cluster != null ? cluster.getBiosType() : BiosType.I440FX_SEA_BIOS;
    }

}
