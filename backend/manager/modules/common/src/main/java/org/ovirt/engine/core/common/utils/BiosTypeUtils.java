package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class BiosTypeUtils {

    public static void setEffective(VmBase vmBase, Cluster cluster) {
        if (vmBase.getCustomBiosType() != BiosType.CLUSTER_DEFAULT) {
            vmBase.setEffectiveBiosType(vmBase.getCustomBiosType());
        } else {
            vmBase.setEffectiveBiosType(cluster.getBiosType());
        }
    }
}
